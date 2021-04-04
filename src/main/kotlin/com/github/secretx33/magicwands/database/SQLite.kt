package com.github.secretx33.magicwands.database

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import java.nio.file.FileSystems
import java.sql.Connection
import java.sql.SQLException

class SQLite(plugin: Plugin) {

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

    private fun AutoCloseable?.safeClose() { runCatching { this?.close() } }

    private companion object {
        val gson: Gson = GsonBuilder()
            .registerTypeAdapter(Location::class.java, LocationAdapter())
            .create()

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
