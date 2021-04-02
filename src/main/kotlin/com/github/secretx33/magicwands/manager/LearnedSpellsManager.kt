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
import kotlin.collections.HashMap

class LearnedSpellsManager(plugin: Plugin) {

    private val writeLock = Semaphore(1)
    private val manager = YamlManager(plugin, "messages/messages")
    private val cache = HashMap<UUID, Set<SpellType>>()

    fun knows(player: Player, spell: SpellType): Boolean {
        val uuid = player.uniqueId
        val uuidString = uuid.toString()

        if(cache.containsKey(uuid))
            return cache[uuid]?.contains(spell) == true

        if(manager.contains(uuidString)) {
            val list = manager.getList(uuidString)?.mapTo(HashSet()) { SpellType.of(it as String) } ?: emptySet()
            cache[uuid] = list
            return list.contains(spell)
        }
        cache[uuid] = emptySet()
        return false
    }

    fun addSpell(player: Player, spell: SpellType) = CoroutineScope(Dispatchers.IO).launch {
        writeLock.withPermit {
            val spellSet = cache.getOrPut(player.uniqueId) {
                manager.getList(player.uniqueId.toString())?.mapTo(HashSet()) { SpellType.of(it as String) } ?: emptySet()
            } as MutableSet<SpellType>
            spellSet.add(spell)
            cache[player.uniqueId] = spellSet
            manager.save().join()
        }
    }

    fun removeSpell(player: Player, spell: SpellType) = CoroutineScope(Dispatchers.IO).launch {
        writeLock.withPermit {
            val spellSet = cache.getOrPut(player.uniqueId) {
                manager.getList(player.uniqueId.toString())?.mapTo(HashSet()) { SpellType.of(it as String) } ?: emptySet()
            } as MutableSet<SpellType>
            spellSet.remove(spell)
            cache[player.uniqueId] = spellSet
            manager.save().join()
        }
    }

    fun removePlayerEntries(player: Player) = CoroutineScope(Dispatchers.Default).launch {
        writeLock.withPermit { cache.remove(player.uniqueId) }
    }
}