package com.github.secretx33.magicwands.events

import com.github.secretx33.magicwands.model.SpellType
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class BlockSpellCastEvent(player: Player, wand: ItemStack, spellType: SpellType) : SpellCastEvent(player, wand, spellType) {

    val block: Block = player.getTargetBlock(null, 15)
}