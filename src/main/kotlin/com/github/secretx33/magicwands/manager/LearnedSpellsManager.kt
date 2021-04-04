package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.utils.YamlManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class LearnedSpellsManager(plugin: Plugin) {

    private val writeLock = Semaphore(1)
    private val manager = YamlManager(plugin, "data/spells_learned")
    private val cache = ConcurrentHashMap<UUID, Set<SpellType>>()

    fun knows(player: Player, spell: SpellType): Boolean {
        val uuid = player.uniqueId
        val uuidString = uuid.toString()

        if(cache.containsKey(uuid))
            return cache[uuid]?.contains(spell) == true

        if(manager.contains(uuidString)) {
            val list = manager.getStringList(uuidString).mapNotNullTo(HashSet()) { SpellType.ofOrNull(it) }
            cache[uuid] = list
            return list.contains(spell)
        }
        cache[uuid] = HashSet()
        return false
    }

    fun getKnownSpells(player: Player): Set<SpellType> {
        val uuid = player.uniqueId
        return cache.getOrPut(uuid) {
            manager.getStringList(uuid.toString()).mapNotNullTo(HashSet()) { SpellType.ofOrNull(it) }
        }
    }

    fun addSpell(player: Player, spell: SpellType) = CoroutineScope(Dispatchers.IO).launch {
        val uuid = player.uniqueId
        writeLock.withPermit {
            val spellSet = getKnownSpells(player) as MutableSet
            spellSet.add(spell)
            cache[uuid] = spellSet
            manager.set(uuid.toString(), spellSet.mapTo(ArrayList())  { it.name })
            manager.save().join()
        }
    }

    fun removeSpell(player: Player, spell: SpellType) = CoroutineScope(Dispatchers.IO).launch {
        val uuid = player.uniqueId
        writeLock.withPermit {
            val spellSet = getKnownSpells(player) as MutableSet
            spellSet.remove(spell)
            cache[uuid] = spellSet
            manager.set(uuid.toString(), spellSet.mapTo(ArrayList()) { it.name })
            manager.save().join()
        }
    }

    fun removePlayerEntries(player: Player) = CoroutineScope(Dispatchers.Default).launch {
        writeLock.withPermit { cache.remove(player.uniqueId) }
    }

    fun reload() = CoroutineScope(Dispatchers.Default).launch {
        writeLock.withPermit {
            cache.clear()
            manager.reload()
        }
    }
}