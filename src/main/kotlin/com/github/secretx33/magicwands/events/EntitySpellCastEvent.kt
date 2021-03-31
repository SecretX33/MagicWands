package com.github.secretx33.magicwands.events

import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.utils.getTarget
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class EntitySpellCastEvent(player: Player, wand: ItemStack, spellType: SpellType) : SpellCastEvent(player, wand, spellType) {

    val target: LivingEntity? = player.getTarget(15) as? LivingEntity
}