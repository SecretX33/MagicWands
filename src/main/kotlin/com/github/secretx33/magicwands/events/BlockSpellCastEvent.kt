package com.github.secretx33.magicwands.events

import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.utils.getTargetBlockWithFace
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class BlockSpellCastEvent(player: Player, wand: ItemStack, spellType: SpellType, range: Int) : SpellCastEvent(player, wand, spellType) {

    val target: TargetBlock? = player.getTargetBlockWithFace(range)?.run { TargetBlock(first, second) }

    init { require(range > 0) { "range has to be greater than zero, value passed is $range" } }

    data class TargetBlock(val block: Block, val face: BlockFace)
}
