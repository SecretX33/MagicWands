package com.github.secretx33.magicwands.eventlisteners.spellcasts

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.eventlisteners.sideeffectsmitigation.FallDamageListener
import com.github.secretx33.magicwands.events.SpellCastEvent
import com.github.secretx33.magicwands.manager.SpellFuelManager
import com.github.secretx33.magicwands.manager.SpellManager
import com.github.secretx33.magicwands.model.SpellType.LEAP
import com.github.secretx33.magicwands.model.SpellType.VANISH
import com.github.secretx33.magicwands.repositories.LearnedSpellsRepo
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
import kotlin.math.max

@KoinApiExtension
class SpellCastListener (
    plugin: Plugin,
    private val fuelManager: SpellFuelManager,
    private val spellManager: SpellManager,
    private val learnedSpells: LearnedSpellsRepo,
    private val fallDamageListener: FallDamageListener,
    private val config: Config,
    private val messages: Messages,
) : Listener {

    private val cooldownMessageDelay: Long = (max(0.0, config.get<Double>(ConfigKeys.COOLDOWN_NOTIFICATION_DELAY) * 1000)).toLong()

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    private var sentMessages = CacheBuilder.newBuilder()
        .expireAfterWrite(cooldownMessageDelay, TimeUnit.MILLISECONDS)
        .build<Player, Long>()

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private fun SpellCastEvent.trySpellCast() {
        require(learnedSpells.knows(player.uniqueId, spellType)) { "Player is trying to use a spell he doesn't know... HOW?" }

        // not enough fuel
        if(isFuelEnabled && !fuelManager.hasEnoughFuel(player, spellType)) {
            player.sendMessage(messages.get(MessageKeys.NOT_ENOUGH_FUEL))
            isCancelled = true
            return
        }

        // still in cooldown
        val cd = spellManager.getPlayerCDSpell(player, spellType)
        if(cd > 0) {
            isCancelled = true
            if(!player.canSendCDMessage()) return
            sentMessages.put(player, System.currentTimeMillis())
            player.sendMessage(messages.get(MessageKeys.SPELL_IN_COOLDOWN)
                .replace("<cooldown>", (ceil(cd / 1000.0).toLong()).toString())
                .replace("<spell>", spellType.displayName))
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun SpellCastEvent.onSuccess() {
        if(isFuelEnabled) fuelManager.consumeFuel(player, spellType)

        when(spellType){
            LEAP -> spellManager.castLeap(this).also { fallDamageListener.addFallDamageImmunity(player) }
            VANISH -> spellManager.castVanish(this)
            else -> {}
        }
        ItemUtils.increaseCastCount(wand)
        val cd = config.get(spellType.configCooldown, 0.0)
        if(isCooldownsEnabled && cd > 0) spellManager.addPlayerCDSpell(player, spellType)
    }

    private fun Player.canSendCDMessage(): Boolean {
        if(!isCooldownsEnabled) return false
        val lastSent = sentMessages.getIfPresent(this)
        return lastSent == null || (lastSent + cooldownMessageDelay) < System.currentTimeMillis()
    }

    private val isFuelEnabled
        get() = !config.get<Boolean>(ConfigKeys.DISABLE_FUEL_USAGE)

    private val isCooldownsEnabled
        get() = !config.get<Boolean>(ConfigKeys.DISABLE_ALL_COOLDOWNS)
}
