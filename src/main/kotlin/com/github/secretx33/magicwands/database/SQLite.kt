package com.github.secretx33.magicwands.database

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.model.SpellTeacher
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.utils.formattedString
import com.google.common.io.Files
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.plugin.Plugin
import java.io.IOException
import java.lang.reflect.Type
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class SQLite(plugin: Plugin, private val config: Config) {

    private val log = plugin.logger
    private val dbFile = plugin.dataFolder.absoluteFile.resolve("database").resolve("sqlite.db")

    init {
        try {
            Files.createParentDirs(dbFile)
        } catch (e: IOException) {
            log.severe("ERROR: Could not create folder ${dbFile.parent} for database.db file\n${e.stackTraceToString()}")
            Bukkit.getPluginManager().disablePlugin(plugin)
        }
    }

    private val url = "jdbc:sqlite:${dbFile.absoluteFile}"
    private val ds = HikariDataSource(hikariConfig.apply { jdbcUrl = url })

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

    fun addSpellteacher(teacher: SpellTeacher) = CoroutineScope(Dispatchers.IO).launch {
        try {
            withStatement(INSERT_SPELLTEACHER){ conn ->
                setString(1, teacher.location.toJson())
                setString(2, teacher.spellType.toJson())
                setString(3, teacher.blockMaterial.toString())
                execute()
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while adding a specific Spellteacher (${teacher.location.formattedString()})\n${e.stackTraceToString()}")
        }
    }

    fun addNewEntryForPlayerLearnedSpell(playerUuid: UUID) = CoroutineScope(Dispatchers.IO).launch {
        try {
            withStatement(INSERT_LEARNED_SPELLS) { conn ->
                setString(1, playerUuid.toString())
                setString(2, emptySet<SpellType>().toJson())
                execute()
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to add new empty entry for player ${Bukkit.getPlayer(playerUuid)?.name ?: "Unknown"} ($playerUuid) to the database\n${e.stackTraceToString()}")
        }
    }

    // REMOVE

    fun removeSpellteacher(blockLoc: Location) = CoroutineScope(Dispatchers.IO).launch {
        try {
            withStatement(REMOVE_SPELLTEACHER) { conn ->
                setString(1, blockLoc.toJson())
                execute()
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to remove a Spellteacher from the database\n${e.stackTraceToString()}")
        }
    }

    private fun removeSpellteachersByWorldUuid(worldUuids: Iterable<String>) = CoroutineScope(Dispatchers.IO).launch {
        try {
            withStatement(REMOVE_SPELLTEACHER_OF_WORLD) { conn ->
                worldUuids.forEach {
                    setString(1, "%$it%")
                    addBatch()
                }
                executeBatch()
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to buck remove all Spellteachers from worlds\n${e.stackTraceToString()}")
        }
    }

    private fun removeSpellteachersByLocation(locations: Iterable<Location>) = CoroutineScope(Dispatchers.IO).launch {
        try {
            withStatement(REMOVE_SPELLTEACHER) { conn ->
                locations.forEach { loc ->
                    setString(1, loc.toJson())
                    addBatch()
                }
                executeBatch()
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to remove a list of Spellteachers\n${e.stackTraceToString()}")
        }
    }

    fun removePlayerLearnedSpells(playerUuid: UUID) = CoroutineScope(Dispatchers.IO).launch {
        try {
            withStatement(REMOVE_LEARNED_SPELLS) { conn ->
                setString(1, playerUuid.toString())
                execute()
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to remove learned spells of player ${Bukkit.getPlayer(playerUuid)?.name} ($playerUuid) from database\n${e.stackTraceToString()}")
        }
    }

    // GET

    fun getAllSpellteachersAsync(): Deferred<MutableMap<Location, SpellTeacher>> = CoroutineScope(Dispatchers.IO).async {
        val spellteachers = HashMap<Location, SpellTeacher>()
        val worldRemoveSet = HashSet<String>()
        val teacherRemoveSet = HashSet<Location>()

        try {
            withQueryStatement(SELECT_ALL_FROM_SPELLTEACHER) { rs ->
                while(rs.next()){
                    val teacherLoc = rs.getString("location").toLocation()
                    val world = teacherLoc.world
                    val spellType = rs.getString("spell_type").toSpellType()
                    val blockMaterial = rs.getString("block_material").toMaterial()

                    if(world == null && removeSpellteacherIfMissingWorld){
                        UUID_WORLD_PATTERN.getOrNull(rs.getString("location"), 1)?.let {
                            worldRemoveSet.add(it)
                        }
                    } else if(world != null) {
                        if (world.getBlockAt(teacherLoc).type != blockMaterial) {
                            log.warning("${ChatColor.RED}WARNING: The Spellteacher located at '${teacherLoc.formattedString()}' was not found, queuing its removal to preserve DB integrity.${ChatColor.WHITE} Usually this happens when a Spellteacher is broken with this plugin being disabled or missing.")
                            teacherRemoveSet.add(teacherLoc)
                        } else {
                            spellteachers[teacherLoc] = SpellTeacher(teacherLoc, spellType, blockMaterial)
                        }
                    }
                }
            }
            if(worldRemoveSet.isNotEmpty()){
                worldRemoveSet.forEach { log.warning("${ChatColor.RED}WARNING: The world with UUID '$it' was not found, removing ALL chests and inventories linked to it") }
                removeSpellteachersByWorldUuid(worldRemoveSet)
            }
            if(teacherRemoveSet.isNotEmpty())
                removeSpellteachersByLocation(teacherRemoveSet)
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to get all Spellteachers async\n${e.stackTraceToString()}")
        }
        spellteachers
    }

    fun getPlayerLearnedSpells(playerUuid: UUID): MutableSet<SpellType>? {
        try {
            withQueryStatement(SELECT_LEARNED_SPELLS, {
                setString(1, playerUuid.toString())
                executeQuery()
            }) { rs ->
                if(rs.next()) return rs.getString("known_spells").toSpellTypeSet()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to get learned spells of player ${Bukkit.getPlayer(playerUuid)?.name} ($playerUuid) from database\n${e.stackTraceToString()}")
        }
        return null
    }

    // UPDATE

    fun updateSpellteacher(newTeacher: SpellTeacher) = CoroutineScope(Dispatchers.IO).launch {
        try {
            withStatement(UPDATE_SPELLTEACHER) { conn ->
                setString(1, newTeacher.spellType.toJson())
                setString(2, newTeacher.location.toJson())
                execute()
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while updating Spellteacher at ${newTeacher.location.formattedString()} to type ${newTeacher.spellType} to the database\n${e.stackTraceToString()}")
        }
    }

    fun updatePlayerLearnedSpells(playerUuid: UUID, knownSpells: Set<SpellType>) = CoroutineScope(Dispatchers.IO).launch {
        try {
            withStatement(UPDATE_LEARNED_SPELLS) { conn ->
                setString(1, knownSpells.toJson())
                setString(2, playerUuid.toString())
                execute()
                conn.commit()
            }
        } catch (e: SQLException) {
            log.severe("${ChatColor.RED}ERROR: An exception occurred while trying to add player ${Bukkit.getPlayer(playerUuid)?.name ?: "Unknown"} ($playerUuid) knownSpells (${knownSpells.joinToString()}) to the database\n${e.stackTraceToString()}")
        }
    }

    private fun String.toMaterial() = Material.valueOf(this)

    private fun String.toLocation() = gson.fromJson(this, Location::class.java)

    private fun Location.toJson() = gson.toJson(this, Location::class.java)

    private fun String.toSpellType() = gson.fromJson(this, SpellType::class.java)

    private fun SpellType.toJson() = gson.toJson(this, SpellType::class.java)

    private fun String.toSpellTypeSet(): MutableSet<SpellType> = gson.fromJson(this, setSpellTypeToken)

    private fun Set<SpellType>.toJson() = gson.toJson(this, setSpellTypeToken)

    private fun Regex.getOrNull(string: String, group: Int) = this.find(string)?.groupValues?.get(group)

    private fun <T> withStatement(statement: String, block: PreparedStatement.(Connection) -> T): T {
        ds.connection.use { conn ->
            conn.prepareStatement(statement).use { prep ->
                return prep.block(conn)
            }
        }
    }

    private inline fun <reified T> withQueryStatement(statement: String, noinline prepareBlock: PreparedStatement.() -> ResultSet = { executeQuery() }, resultBlock: (ResultSet) -> T): T {
        ds.connection.use { conn ->
            conn.prepareStatement(statement).use { prep ->
                prep.prepareBlock().use { rs ->
                    return resultBlock(rs)
                }
            }
        }
    }

    private fun AutoCloseable?.safeClose() { runCatching { this?.close() } }

    private val removeSpellteacherIfMissingWorld: Boolean
        get() = config.get(ConfigKeys.REMOVE_SPELLTEACHER_WORLD_NOT_FOUND)

    private companion object {
        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(Location::class.java, LocationAdapter())
            .create()
        val setSpellTypeToken: Type = object : TypeToken<Set<SpellType>>() {}.type
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

        val UUID_WORLD_PATTERN = """^\{"world":"([0-9a-zA-Z-]+).*""".toRegex()
    }
}
