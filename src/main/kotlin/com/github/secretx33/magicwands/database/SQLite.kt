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
                conn.prepareStatement(CREATE_QUEST_CHESTS).use { it.execute() }
                conn.prepareStatement(CREATE_CHEST_CONTENT).use { it.execute() }
                conn.prepareStatement(CREATE_PLAYER_PROGRESS).use { it.execute() }
                conn.prepareStatement(CREATE_TRIGGER).use { it.execute() }
                conn.commit()
                log.fine("Initiated DB")
            }
        } catch (e: SQLException) {
            log.fine(("${ChatColor.RED}ERROR: An exception occurred while trying to connect to the database and create the tables\n${e.stackTraceToString()}"))
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
        const val CREATE_QUEST_CHESTS = "CREATE TABLE IF NOT EXISTS questChests(location VARCHAR(150) NOT NULL PRIMARY KEY, chest_order INTEGER NOT NULL);"
        const val CREATE_CHEST_CONTENT = "CREATE TABLE IF NOT EXISTS chestContents(id INTEGER PRIMARY KEY, chest_location INTEGER NOT NULL, player_uuid VARCHAR(60) NOT NULL, inventory VARCHAR(500000) NOT NULL, FOREIGN KEY(chest_location) REFERENCES questChests(location));"
        const val CREATE_PLAYER_PROGRESS = "CREATE TABLE IF NOT EXISTS playerProgress(player_uuid VARCHAR(60) NOT NULL PRIMARY KEY, progress INTEGER NOT NULL);"
        const val CREATE_TRIGGER = "CREATE TRIGGER IF NOT EXISTS removeInventories BEFORE DELETE ON questChests FOR EACH ROW BEGIN DELETE FROM chestContents WHERE chestContents.chest_location = OLD.location; END"
        // selects
        const val SELECT_ALL_FROM_QUEST_CHEST = "SELECT * FROM questChests;"
        const val SELECT_ALL_FROM_PLAYER_PROGRESS = "SELECT * FROM playerProgress;"
        const val SELECT_CHEST_CONTENT = "SELECT inventory FROM chestContents WHERE chest_location = ? AND player_uuid = ? LIMIT 1;"
        const val SELECT_PLAYER_PROCESS = "SELECT progress FROM playerProgress WHERE player_uuid = ? LIMIT 1;"
        // inserts
        const val INSERT_QUEST_CHEST = "INSERT INTO questChests(location, chest_order) VALUES (?, ?);"
        const val INSERT_CHEST_CONTENTS = "INSERT INTO chestContents(chest_location, player_uuid, inventory) VALUES (?, ?, ?);"
        const val INSERT_PLAYER_PROGRESS = "INSERT INTO playerProgress(player_uuid, progress) VALUES (?, ?);"
        // updates
        const val UPDATE_QUEST_CHEST_ORDER = "UPDATE questChests SET chest_order = ? WHERE location = ?;"
        const val UPDATE_CHEST_CONTENTS = "UPDATE chestContents SET inventory = ? WHERE chest_location = ? AND player_uuid = ?;"
        const val UPDATE_PLAYER_PROGRESS = "UPDATE playerProgress SET progress = ? WHERE player_uuid = ?;"
        // removes
        const val REMOVE_QUEST_CHESTS_OF_WORLD = "DELETE FROM questChests WHERE location LIKE ?;"
        const val REMOVE_QUEST_CHEST = "DELETE FROM questChests WHERE location = ?;"
        const val REMOVE_PLAYER_PROGRESS = "DELETE FROM playerProgress WHERE player_uuid = ?;"
    }
}