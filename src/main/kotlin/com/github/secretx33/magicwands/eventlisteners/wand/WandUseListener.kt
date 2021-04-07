package com.github.secretx33.magicwands.eventlisteners.wand

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.events.BlockSpellCastEvent
import com.github.secretx33.magicwands.events.EntitySpellCastEvent
import com.github.secretx33.magicwands.events.SpellCastEvent
import com.github.secretx33.magicwands.events.WandSpellSwitchEvent
import com.github.secretx33.magicwands.manager.WorldGuardHelper
import com.github.secretx33.magicwands.model.SpellType.*
import com.github.secretx33.magicwands.repositories.LearnedSpellsRepo
import com.github.secretx33.magicwands.utils.*
import com.google.common.cache.CacheBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
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
import java.util.concurrent.TimeUnit

@KoinApiExtension
class WandUseListener (
    plugin: Plugin,
    private val config: Config,
    private val messages: Messages,
    private val learnedSpells: LearnedSpellsRepo
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    private var lastClickList = CacheBuilder.newBuilder()
        .expireAfterWrite(clickDelay, TimeUnit.MILLISECONDS)
        .build<Player, Long>()

    @EventHandler(priority = EventPriority.NORMAL)
    private fun PlayerInteractEvent.onWandInteract() {
        if(hand != EquipmentSlot.HAND) return
        val item = item ?: return
        if(!item.isWand()) return
        if(!player.canClick()) {
            isCancelled = true
            return
        }

        // player is using another's wand
        if(!item.isWandOwner(player)) {
            isCancelled = true
            val owner = ItemUtils.getWandOwnerName(item)
            player.sendMessage(messages.get(MessageKeys.CANNOT_USE_ANOTHERS_WAND).replace("<owner>", owner))
            return
        }

        if(isLeftClick()) {
            isCancelled = true
            leftClickHandler(player, item)
            return
        }

        if(isRightClick()) {
            val event = WandSpellSwitchEvent(player, item)
            isCancelled = true
            Bukkit.getServer().pluginManager.callEvent(event)
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private fun EntityDamageByEntityEvent.onWandInteract() {
        if(!damager.isPlayer() || !damageIsMelee()) return

        val item = (damager as Player).inventory.itemInMainHand
        if(item.isWand()) isCancelled = true
    }

    private fun Cancellable.leftClickHandler(player: Player, wand: ItemStack) {
        val selected = ItemUtils.getWandSpellOrNull(wand) ?: run {
            return
        }
        // if player don't know the spell he's trying to cast
        if (!learnedSpells.knows(player.uniqueId, selected)) {
            player.sendMessage(
                messages.get(MessageKeys.CANNOT_CAST_UNKNOWN_SPELL).replace("<spell>", selected.displayName)
            )
            return
        }
        // if player is inside antimagic zone
        if(WorldGuardHelper.isInsideAntimagicZone(player, player.location)) {
            player.sendMessage(messages.get(MessageKeys.CANNOT_CAST_INSIDE_ANTIMAGICZONE).replace("<spell>", selected.displayName))
            return
        }
        val event = getSpellEvent(player, wand)
        // if block is inside antimagic zone
        if(event is BlockSpellCastEvent && event.target?.block?.location?.let { WorldGuardHelper.isInsideAntimagicZone(player, it) } == true) {
            player.sendMessage(messages.get(MessageKeys.CANNOT_CAST_TARGET_BLOCK_INSIDE_ANTIMAGICZONE))
            return
        }
        // if target entity is inside antimagic zone
        if(event is EntitySpellCastEvent && event.target?.let { WorldGuardHelper.isInsideAntimagicZone(player, it.location) } == true) {
            player.sendMessage(messages.get(MessageKeys.CANNOT_CAST_TARGET_ENTITY_INSIDE_ANTIMAGICZONE))
            return
        }
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

    private fun EntityDamageEvent.damageIsMelee() = cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK

    private fun Player.canClick(): Boolean = lastClickList.getIfPresent(this).let { it == null || (it + clickDelay) < System.currentTimeMillis() }.also { if(it) lastClickList.put(this, System.currentTimeMillis()) }

    private companion object {
        const val clickDelay = 75L
    }
}
