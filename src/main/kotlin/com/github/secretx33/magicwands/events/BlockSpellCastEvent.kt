package com.github.secretx33.magicwands.events

import com.github.secretx33.magicwands.model.SpellType
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class BlockSpellCastEvent(player: Player, wand: ItemStack, spellType: SpellType, range: Int) : SpellCastEvent(player, wand, spellType) {

    val block: Block = player.getTargetBlock(setOf(Material.AIR, Material.GRASS, Material.TALL_GRASS), range)

    init { require(range > 0) { "range has to be greater than zero, value passed is $range" } }
}