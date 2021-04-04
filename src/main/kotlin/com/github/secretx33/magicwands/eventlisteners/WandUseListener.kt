package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.events.BlockSpellCastEvent
import com.github.secretx33.magicwands.events.EntitySpellCastEvent
import com.github.secretx33.magicwands.events.SpellCastEvent
import com.github.secretx33.magicwands.events.WandSpellSwitchEvent
import com.github.secretx33.magicwands.model.SpellType.*
import com.github.secretx33.magicwands.repositories.LearnedSpellsRepo
import com.github.secretx33.magicwands.utils.*
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class WandUseListener (
    plugin: Plugin,
    private val config: Config,
    private val messages: Messages,
    private val learnedSpells: LearnedSpellsRepo
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun PlayerInteractEvent.onWandInteract() {
        if(hand != EquipmentSlot.HAND) return
        val item = item ?: return
        if(!item.isWand()) return

        // player is using another's wand
        if(!item.isWandOwner(player)) {
            isCancelled = true
            val owner = ItemUtils.getWandOwnerName(item)
            player.sendMessage(messages.get(MessageKeys.CANNOT_USE_ANOTHERS_WAND).replace("<owner>", owner))
            return
        }

        if(isLeftClick()) {
            val selected = ItemUtils.getWandSpellOrNull(item) ?: run {
                isCancelled = true
                return
            }
            if(!learnedSpells.knows(player.uniqueId, selected)) {
                isCancelled = true
                player.sendMessage(messages.get(MessageKeys.CANNOT_CAST_UNKNOWN_SPELL).replace("<spell>", selected.displayName))
                return
            }
            val event = getSpellEvent(player, item)
            isCancelled = true
            Bukkit.getServer().pluginManager.callEvent(event)
            return
        }

        if(isRightClick()) {
            val event = WandSpellSwitchEvent(player, item)
            isCancelled = true
            Bukkit.getServer().pluginManager.callEvent(event)
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun EntityDamageByEntityEvent.onWandInteract() {
        if(!damager.isPlayer() || !damageIsMelee()) return

        val player = damager as Player
        val item = player.inventory.itemInMainHand
        if(!item.isWand()) return

        val selected = ItemUtils.getWandSpellOrNull(item) ?: run {
            isCancelled = true
            return
        }
        if(!learnedSpells.knows(player.uniqueId, selected)) {
            isCancelled = true
            player.sendMessage(messages.get(MessageKeys.CANNOT_CAST_UNKNOWN_SPELL).replace("<spell>", selected.displayName))
            return
        }
        val event = getSpellEvent(player, item)
        isCancelled = true
        Bukkit.getServer().pluginManager.callEvent(event)
    }

    private fun getSpellEvent(player: Player, wand: ItemStack): SpellCastEvent {
        return when(val type = ItemUtils.getWandSpell(wand)) {
            BLIND, ENSNARE, POISON, SLOW, THRUST -> EntitySpellCastEvent(player, wand, type, config.get(type.configRange, 5))
            BLINK -> BlockSpellCastEvent(player, wand, type, config.get(type.configRange, 5))
            else -> SpellCastEvent(player, wand, type)
        }
    }

    private fun Entity.isPlayer() = type == EntityType.PLAYER

    private fun EntityDamageEvent.damageIsMelee() = this.cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK
}
