package com.github.secretx33.magicwands.eventlisteners

import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class FireworkDamageWorkaroundListener(plugin: Plugin) : Listener {

    private val fireworkId = NamespacedKey(plugin, "custom_firework")

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private fun EntityDamageByEntityEvent.fireworkDamageWorkaround() {
        if(!damager.isFirework() || !isExplosion()) return

        if(damager.persistentDataContainer.has(fireworkId, PersistentDataType.BYTE)) {
            isCancelled = true
            damage = 0.0
        }
    }

    private fun Entity.isFirework() = type == EntityType.FIREWORK

    private fun EntityDamageEvent.isExplosion() = cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
}