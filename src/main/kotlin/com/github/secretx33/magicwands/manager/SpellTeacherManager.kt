package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.utils.YamlManager
import com.github.secretx33.magicwands.utils.toUuid
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.plugin.Plugin
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.NoSuchElementException
import kotlin.collections.HashSet

class SpellTeacherManager(private val plugin: Plugin, private val config: Config) {

    private val writeLock = Semaphore(1)
    private val manager = YamlManager(plugin, "data/spellteacher")
    private val cache = ConcurrentHashMap<Location, SpellTeacherInfo>()

    init { setEntriesFromFile() }

    private fun setEntriesFromFile() = CoroutineScope(Dispatchers.Default).launch {
        val list: MutableSet<Pair<Location, SpellTeacherInfo>>
        writeLock.withPermit {
            list = manager.getStringList(configKey).deserialize()
        }

        val nullWorlds = list.filterTo(HashSet()) { it.first.world == null }
        list.removeAll(nullWorlds)
        val blockNotFound = list.filterTo(HashSet()) { !nullWorlds.contains(it) && it.first.world?.getBlockAt(it.first)?.type != it.second.blockMaterial }
        list.removeAll(blockNotFound)

        if(nullWorlds.isNotEmpty() && config.get(ConfigKeys.REMOVE_SPELLTEACHER_WORLD_NOT_FOUND)) {
            nullWorlds.forEach {
                plugin.logger.warning("${ChatColor.RED}World with UUID was not found, removing ALL spellteachers in it")
                removeSpellTeachersFromWorld(it.second.worldUuid)
            }
        }
        if(blockNotFound.isNotEmpty()) {
            blockNotFound.forEach {
                plugin.logger.warning("${ChatColor.RED}Block at ${it.first} was not found, removing it from the spellteachers list")
                removeSpellTeachers(blockNotFound.map { m -> m.second })
            }
        }
        cache.putAll(list)
    }

    fun isSpellTeacher(block: Block): Boolean = cache.containsKey(block.location)

    fun getSpellType(block: Block): SpellType = cache[block.location]?.spellType ?: throw NoSuchElementException("${ChatColor.RED}SpellType at ${block.location} was not found!")

    fun makeSpellTeacher(block: Block, spell: SpellType) = CoroutineScope(Dispatchers.IO).launch {
        if(block.location.world == null) return@launch
        val info = block.asSpellTeacher(spell)

        writeLock.withPermit {
            cache[block.location] = info
            val newList = manager.getStringList(configKey).deserialize().apply {
                removeIf { it.first == block.location }
                add(Pair(block.location, info))
            }.serialize()
            manager.set(configKey, newList)
            manager.save().join()
        }
    }

    fun removeSpellTeacher(block: Block) = CoroutineScope(Dispatchers.IO).launch {
        if(block.location.world == null) return@launch

        writeLock.withPermit {
            cache.remove(block.location)
            val newList = manager.getStringList(configKey).asSequence()
                .map { it.deserialize() }
                .filterNot { it.first == block.location }
                .map { it.serialize() }
                .toList()
            manager.set(configKey, newList)
            manager.save().join()
        }
    }

    private fun removeSpellTeachers(iterable: Iterable<SpellTeacherInfo>) = CoroutineScope(Dispatchers.IO).launch {
        writeLock.withPermit {
            val list = manager.getStringList(configKey).deserialize()
            iterable.forEach { info -> list.removeIf { it.second == info } }
            manager.set(configKey, list.serialize())
            manager.save().join()
        }
    }

    private fun removeSpellTeachersFromWorld(worldUuid: UUID) = CoroutineScope(Dispatchers.IO).launch {
        writeLock.withPermit {
            cache.filterKeys { it.world?.uid == worldUuid }.keys.asIterable().forEach { cache.remove(it) }

            val newList = manager.getStringList(configKey).deserialize()
                .filter { it.second.worldUuid == worldUuid }
                .serialize()

            manager.set(configKey, newList)
            manager.save().join()
        }
    }

    fun reload() = CoroutineScope(Dispatchers.Default).launch {
        writeLock.withPermit {
            manager.reload()
            cache.clear()
            setEntriesFromFile()
        }
    }

    private fun String.deserialize(): Pair<Location, SpellTeacherInfo> = gson.fromJson(this, pairTypeAdapter)

    private fun Collection<String>.deserialize(): MutableSet<Pair<Location, SpellTeacherInfo>> = mapTo(HashSet()) { it.deserialize() }

    private fun Pair<Location, SpellTeacherInfo>.serialize(): String = gson.toJson(this, pairTypeAdapter)

    private fun Collection<Pair<Location, SpellTeacherInfo>>.serialize(): List<String> = map { it.serialize() }

    private fun Block.asSpellTeacher(spellType: SpellType) = SpellTeacherInfo(spellType, location.world!!.uid, type)





    private companion object {
        const val configKey = "spellteacher"
        val gson = GsonBuilder().registerTypeAdapter(Location::class.java, LocationAdapter()).create()
        val pairTypeAdapter = object : TypeToken<Pair<Location, SpellTeacherInfo>>() {}.type
    }
}
