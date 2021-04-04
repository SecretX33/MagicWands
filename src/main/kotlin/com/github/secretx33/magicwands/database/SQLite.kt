package com.github.secretx33.magicwands.database

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.utils.prettyString
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.plugin.Plugin
import java.lang.reflect.Type
import java.nio.file.FileSystems
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class SQLite(plugin: Plugin, private val config: Config) {

    private val url = "jdbc:sqlite:${plugin.dataFolder.absolutePath}${folderSeparator}database.db"
    private val ds = HikariDataSource(hikariConfig.apply { jdbcUrl = url })
    private val log = plugin.logger

    init { initialize() }

    fun close() = ds.safeClose()

    private fun initialize() {
        try {
            ds.connection.use { conn: Connection ->
                conn.prepareStatement(CREATE_SPELLTEACHER).use { it.execute() }
                conn.prepareStatement(CREATE_LEARNED_SPELLS).use { it.execute() }
                conn.commit()
                log.fine("Initiated DB")
            }
        } catch (e: SQLException) {
            log.severe(("${ChatColor.RED}ERROR: An exception occurred while trying to connect to the database and create the tables\n${e.stackTraceToString()}"))
        }
    }

    // ADD

    fun addSpellteacher(block: Block, spellType: SpellType) = CoroutineScope(Dispatchers.IO).launch {
        try {
            ds.connection.use { conn: Connection ->
                conn.prepareStatement(INSERT_SPELLTEACHER).use { prep ->
                    prep.setString(1, block.location.toJson())
                    prep.setString(2, spellType.toJson())
                    prep.setString(3, block.type.toString())
                    prep.execute()
                }
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while adding a specific Spellteacher (${block.location.prettyString()})\n${e.stackTraceToString()}")
        }
    }

    fun addLearnedSpell(playerUuid: UUID, knowSpells: Set<SpellType>) = CoroutineScope(Dispatchers.IO).launch {
        try {
            ds.connection.use { conn: Connection ->
                conn.prepareStatement(INSERT_LEARNED_SPELLS).use { prep ->
                    prep.setString(1, playerUuid.toString())
                    prep.setString(2, knowSpells.toJson())
                    prep.execute()
                }
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to add player ${Bukkit.getPlayer(playerUuid)?.name ?: "Unknown"} ($playerUuid) knowSpells (${knowSpells.joinToString()}) to the database\n${e.stackTraceToString()}")
        }
    }

    // REMOVE

    fun removeSpellteacher(blockLoc: Location) = CoroutineScope(Dispatchers.IO).launch {
        try {
            ds.connection.use { conn: Connection ->
                conn.prepareStatement(REMOVE_SPELLTEACHER).use { prep ->
                    prep.setString(1, blockLoc.toJson())
                    prep.execute()
                }
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to remove a Spellteacher from the database\n${e.stackTraceToString()}")
        }
    }

    private fun removeSpellteachersByWorldUuid(worldUuids: Iterable<String>) = CoroutineScope(Dispatchers.IO).launch {
        try {
            ds.connection.use { conn: Connection ->
                conn.prepareStatement(REMOVE_SPELLTEACHER_OF_WORLD).use { prep ->
                    worldUuids.forEach {
                        prep.setString(1, "%$it%")
                        prep.addBatch()
                    }
                    prep.executeBatch()
                }
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to buck remove all quest chests from worlds\n${e.stackTraceToString()}")
        }
    }

    private fun removeSpellteachersByLocation(locations: Iterable<Location>) = CoroutineScope(Dispatchers.IO).launch {
        try {
            ds.connection.use { conn: Connection ->
                conn.prepareStatement(REMOVE_SPELLTEACHER).use { prep ->
                    locations.forEach { loc ->
                        prep.setString(1, loc.toJson())
                        prep.addBatch()
                    }
                    prep.executeBatch()
                }
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to remove a list of quest chests\n${e.stackTraceToString()}")
        }
    }

    fun removePlayerLearnedSpells(playerUuid: UUID) = CoroutineScope(Dispatchers.IO).launch {
        try {
            ds.connection.use { conn: Connection ->
                conn.prepareStatement(REMOVE_LEARNED_SPELLS).use { prep ->
                    prep.setString(1, playerUuid.toString())
                    prep.execute()
                }
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to remove a Quest Chest from the database\n${e.stackTraceToString()}")
        }
    }

    // GET

    fun getAllSpellteachersAsync(): Deferred<Map<Location, Int>> = CoroutineScope(Dispatchers.IO).async {
        var conn: Connection? = null
        var prep: PreparedStatement? = null
        var rs: ResultSet? = null

        val spellteachers = HashMap<Location, Int>()
        val worldRemoveSet = HashSet<String>()
        val spellteacherRemoveSet = HashSet<Location>()

        try {
            conn = ds.connection
            prep = conn.prepareStatement(SELECT_ALL_FROM_SPELLTEACHER)
            rs = prep.executeQuery()
            while(rs.next()){
                val chestLoc = rs.getString("location").toLocation()
                if(chestLoc.world == null && removeSpellteacherIfMissingWorld){
                    UUID_WORLD_PATTERN.matcher(rs.getString("location")).replaceFirst("$1")?.let {
                        worldRemoveSet.add(it)
                    }
                } else if(chestLoc.world != null) {
                    if (chestLoc.world.getBlockAt(chestLoc).state !is Container) {
                        consoleMessage("${ChatColor.RED}WARNING: The chest located at '${chestLoc.prettyString()}' was not found, queuing its removal to preserve DB integrity.${ChatColor.WHITE} Usually this happens when a Quest Chest is broken with this plugin being disabled or missing.")
                        spellteacherRemoveSet.add(chestLoc)
                    } else {
                        spellteachers[chestLoc] = rs.getInt("chest_order")
                    }
                }
            }
            if(worldRemoveSet.isNotEmpty()){
                worldRemoveSet.forEach { consoleMessage("${ChatColor.RED}WARNING: The world with UUID '$it' was not found, removing ALL chests and inventories linked to it") }
                removeQuestChestsByWorldUuid(worldRemoveSet)
            }
            if(spellteacherRemoveSet.isNotEmpty())
                removeQuestChestsByLocation(spellteacherRemoveSet)
        } catch (e: SQLException) {
            consoleMessage("${ChatColor.RED}ERROR: An exception occurred while trying to connect to the database")
            e.printStackTrace()
        } finally {
            rs?.safeClose()
            prep?.safeClose()
            conn?.safeClose()
        }
        spellteachers
    }

    fun getPlayerLearnedSpells(playerUuid: UUID): MutableSet<SpellType> {
        try {
            ds.connection.use { conn: Connection ->
                conn.prepareStatement(SELECT_LEARNED_SPELLS).use { prep ->
                    prep.setString(1, playerUuid.toString())

                    prep.executeQuery().use { rs ->
                        if(rs.next()){
                            return rs.getString("known_spells").toSpellTypeSet()
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to get progress of player ${Bukkit.getPlayer(playerUuid)?.name} ($playerUuid) from database\n${e.stackTraceToString()}")
        }
        return HashSet()
    }

    // UPDATE

    private fun String.toLocation() = gson.fromJson(this, Location::class.java)

    private fun Location.toJson() = gson.toJson(this, Location::class.java)

    private fun String.toSpellType() = gson.fromJson(this, SpellType::class.java)

    private fun SpellType.toJson() = gson.toJson(this, SpellType::class.java)

    private fun String.toSpellTypeSet(): MutableSet<SpellType> = gson.fromJson(this, setSpellTypeToken)

    private fun Set<SpellType>.toJson() = gson.toJson(this, setSpellTypeToken)

    private fun AutoCloseable?.safeClose() { runCatching { this?.close() } }

    private val removeSpellteacherIfMissingWorld: Boolean
        get() = config.get(ConfigKeys.REMOVE_SPELLTEACHER_WORLD_NOT_FOUND)

    private companion object {
        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(Location::class.java, LocationAdapter())
            .create()
        val setSpellTypeToken: Type = object : TypeToken<Set<SpellType>>() {}.type
        val folderSeparator: String = FileSystems.getDefault().separator
        val hikariConfig = HikariConfig().apply { isAutoCommit = false }

        // create tables
        const val CREATE_SPELLTEACHER = "CREATE TABLE IF NOT EXISTS spellTeacher(location VARCHAR(150) NOT NULL PRIMARY KEY, spell_type VARCHAR(80) NOT NULL, block_material VARCHAR(80) NOT NULL);"
        const val CREATE_LEARNED_SPELLS = "CREATE TABLE IF NOT EXISTS learnedSpells(player_uuid VARCHAR(60) NOT NULL PRIMARY KEY, known_spells VARCHAR(800) NOT NULL);"
        // selects
        const val SELECT_ALL_FROM_SPELLTEACHER = "SELECT * FROM spellTeacher;"
        const val SELECT_LEARNED_SPELLS= "SELECT known_spells FROM learnedSpells WHERE player_uuid = ? LIMIT 1;"
        // inserts
        const val INSERT_SPELLTEACHER = "INSERT INTO spellTeacher(location, spell_type, block_material) VALUES (?, ?, ?);"
        const val INSERT_LEARNED_SPELLS = "INSERT INTO learnedSpells(player_uuid, known_spells) VALUES (?, ?);"
        // updates
        const val UPDATE_SPELLTEACHER = "UPDATE spellTeacher SET spell_type = ? WHERE location = ?;"
        const val UPDATE_LEARNED_SPELLS = "UPDATE learnedSpells SET known_spells = ? WHERE player_uuid = ?;"
        // removes
        const val REMOVE_SPELLTEACHER_OF_WORLD = "DELETE FROM spellTeacher WHERE location LIKE ?;"
        const val REMOVE_SPELLTEACHER = "DELETE FROM spellTeacher WHERE location = ?;"
        const val REMOVE_LEARNED_SPELLS = "DELETE FROM learnedSpells WHERE player_uuid = ?;"

        val UUID_WORLD_PATTERN = """^"\{\\"world\\":\\"([0-9a-zA-Z-]+).*""".toPattern()
    }
}
