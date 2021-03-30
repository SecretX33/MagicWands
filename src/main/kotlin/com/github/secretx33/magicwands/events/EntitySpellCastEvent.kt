package com.github.secretx33.magicwands.events

import com.github.secretx33.magicwands.spell.SpellType
import com.github.secretx33.magicwands.utils.getTarget
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class EntitySpellCastEvent(player: Player, wand: ItemStack, spellType: SpellType) : SpellCastEvent(player, wand, spellType) {

    val target: Entity? = player.getTarget(15)
}