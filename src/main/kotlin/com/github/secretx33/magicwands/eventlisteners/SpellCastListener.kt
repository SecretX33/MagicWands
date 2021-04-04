package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.events.SpellCastEvent
import com.github.secretx33.magicwands.manager.LearnedSpellsManager
import com.github.secretx33.magicwands.manager.SpellFuelManager
import com.github.secretx33.magicwands.manager.SpellManager
import com.github.secretx33.magicwands.model.SpellType.LEAP
import com.github.secretx33.magicwands.model.SpellType.VANISH
import com.github.secretx33.magicwands.utils.ItemUtils
import com.google.common.cache.CacheBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

@KoinApiExtension
class SpellCastListener (
    plugin: Plugin,
    private val fuelManager: SpellFuelManager,
    private val spellManager: SpellManager,
    private val learnedSpells: LearnedSpellsManager,
    private val config: Config,
    private val messages: Messages,
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    private val sentMessages = CacheBuilder.newBuilder()
        .expireAfterWrite(2, TimeUnit.SECONDS)
        .build<Player, Long>()

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private fun SpellCastEvent.trySpellCast() {
        require(learnedSpells.knows(player, spellType)) { "Player is trying to use a spell he doesn't know... HOW?" }

        // not enough fuel
        if(isFuelEnabled && !fuelManager.hasEnoughFuel(player, spellType)) {
            player.sendMessage(messages.get(MessageKeys.NOT_ENOUGH_FUEL))
            isCancelled = true
            return
        }

        // still in cooldown
        val cd = spellManager.getSpellCD(player, spellType)
        if(cd > 0 && player.canSendCDMessage()) {
            player.sendMessage(messages.get(MessageKeys.SPELL_IN_COOLDOWN)
                .replace("<cooldown>", (ceil(cd / 1000.0).toLong()).toString())
                .replace("<spell>", spellType.displayName))
            isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun SpellCastEvent.onSuccess() {
        if(isFuelEnabled) fuelManager.consumeFuel(player, spellType)

        when(spellType){
            LEAP -> spellManager.castLeap(this)
            VANISH -> spellManager.castVanish(this)
            else -> {}
        }
        ItemUtils.increaseCastCount(wand)
        if(isCooldownsEnabled) spellManager.addSpellCD(player, spellType)
    }

    private fun Player.canSendCDMessage() = !config.get<Boolean>(ConfigKeys.DISABLE_ALL_COOLDOWNS) && sentMessages.getIfPresent(player)?.plus(100)?.compareTo(System.currentTimeMillis())?.let { it > 0 } == true

    private val isFuelEnabled
        get() = !config.get<Boolean>(ConfigKeys.DISABLE_FUEL_USAGE)

    private val isCooldownsEnabled
        get() = !config.get<Boolean>(ConfigKeys.DISABLE_FUEL_USAGE)
}