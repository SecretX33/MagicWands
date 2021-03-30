package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.spell.SpellType
import com.github.secretx33.magicwands.utils.YamlManager
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension
import java.util.concurrent.ConcurrentHashMap

@KoinApiExtension
class SpellManager(plugin: Plugin) {

    private val manager = YamlManager(plugin, "spells_learned")
    private val cooldown = ConcurrentHashMap<Pair<Player, SpellType>, Long>()

    fun getWandSpell(wand: ItemStack): SpellType {
        require(wand.isWand()) { "Item passed as wand is not a wand" }
        TODO("return the selected spell in the wand")
    }

    fun knows(player: Player, spellType: SpellType) {
        manager.contains("${player.uniqueId}.${spellType.name}")
//        manager.getStringList(player.uniqueId.toString()).contains(spell.name)
    }

    fun makeLearn(player: Player, spellType: SpellType) {
        val path = player.uniqueId.toString()
        val spellList = manager.getStringList(path)
        if(!spellList.contains(spellType.name)) {
            spellList.add(spellType.name)
            manager.set(path, spellList)
            manager.save()
        }
    }

    fun reload() = manager.reload()
}