package com.github.secretx33.magicwands.eventlisteners.sideeffectsmitigation

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.utils.isPlayer
import com.google.common.cache.CacheBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit
import kotlin.math.max

class FallDamageListener (plugin: Plugin, config: Config) : Listener {

    private val immunePeriod: Long = (max(0.0, config.get<Double>(ConfigKeys.FALL_DAMAGE_IMMUNE_PERIOD) * 1000)).toLong()

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    private val immuneList = CacheBuilder.newBuilder()
        .expireAfterWrite(immunePeriod, TimeUnit.MILLISECONDS)
        .build<Player, Long>()

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private fun EntityDamageEvent.onPlayerFallDamage() {
        if(!isPlayerFallDamage() || immunePeriod == 0L) return

        val player = entity as Player
        if(player.isImmuneToFallDamage())
            isCancelled = true
    }

    fun addFallDamageImmunity(player: Player) = immuneList.put(player, System.currentTimeMillis() + immunePeriod)

    private fun Player.isImmuneToFallDamage(): Boolean = immuneList.getIfPresent(this).let { it != null && (it + immunePeriod) > System.currentTimeMillis() }.also { if(it) immuneList.invalidate(this) }

    private fun EntityDamageEvent.isPlayerFallDamage() = cause == EntityDamageEvent.DamageCause.FALL && entity.isPlayer()
}
