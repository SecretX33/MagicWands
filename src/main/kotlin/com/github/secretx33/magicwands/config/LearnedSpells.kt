package com.github.secretx33.magicwands.config

import com.github.secretx33.magicwands.spell.Spell
import com.github.secretx33.magicwands.utils.YamlManager
import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class LearnedSpells(val plugin: Plugin){

    private val manager = YamlManager(plugin, "spells_learned")

    fun knows(player: Player, spell: Spell) {
        manager.contains("${player.uniqueId}.${spell.name}")
//        manager.getStringList(player.uniqueId.toString()).contains(spell.name)
    }

    fun makeLearn(player: Player, spell: Spell) {
        val path = player.uniqueId.toString()
        val spellList = manager.getStringList(path)
        if(!spellList.contains(spell.name)) {
            spellList.add(spell.name)
            manager.set(path, spellList)
            manager.save()
        }
    }

    fun reload() = manager.reload()
}