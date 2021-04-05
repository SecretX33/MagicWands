package com.github.secretx33.magicwands.repositories

import com.github.secretx33.magicwands.database.SQLite
import com.github.secretx33.magicwands.model.SpellType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class LearnedSpellsRepo(private val db: SQLite) {

    private val learnedSpells = ConcurrentHashMap<UUID, MutableSet<SpellType>>()

    fun knows(playerUuid: UUID, spell: SpellType): Boolean = getKnownSpells(playerUuid).contains(spell)

    fun getKnownSpells(playerUuid: UUID): Set<SpellType> = getKnownSpellsMutable(playerUuid)

    private fun getKnownSpellsMutable(playerUuid: UUID): MutableSet<SpellType> = learnedSpells.getOrPut(playerUuid) { db.getPlayerLearnedSpells(playerUuid) ?: HashSet<SpellType>().also { db.addNewEntryForPlayerLearnedSpell(playerUuid) } }

    fun teachSpell(playerUuid: UUID, spell: SpellType) = CoroutineScope(Dispatchers.Default).launch {
        val knownSpells = getKnownSpellsMutable(playerUuid)
        knownSpells.add(spell)
        db.updatePlayerLearnedSpells(playerUuid, knownSpells)
    }

    fun forgetSpell(playerUuid: UUID, spell: SpellType) = CoroutineScope(Dispatchers.Default).launch {
        val knownSpells = getKnownSpellsMutable(playerUuid)
        knownSpells.remove(spell)
        db.updatePlayerLearnedSpells(playerUuid, knownSpells)
    }

    fun removePlayerEntries(player: Player) = learnedSpells.remove(player.uniqueId)
}
