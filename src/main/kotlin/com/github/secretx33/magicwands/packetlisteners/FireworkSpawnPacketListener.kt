package com.github.secretx33.magicwands.packetlisteners

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.secretx33.magicwands.packets.WrapperPlayServerSpawnEntity
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class FireworkSpawnPacketListener(private val plugin: Plugin, private val fireworkId: NamespacedKey) {

    init { setup() }

    private fun setup() {
        val manager = ProtocolLibrary.getProtocolManager()
        manager.addPacketListener(object : PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.SPAWN_ENTITY) {
            override fun onPacketSending(event: PacketEvent) {
                if(event.packetType != PacketType.Play.Server.SPAWN_ENTITY || event.isCancelled) return

                val wrapper = WrapperPlayServerSpawnEntity(event.packet)
                if(wrapper.type != EntityType.FIREWORK) return
                if(wrapper.isCustomFirework(event)) {
                    event.player.sendMessage("Just cancelled a packet containing fireworks")
                    event.isCancelled = true
                }
            }
        })
    }

    private fun WrapperPlayServerSpawnEntity.isCustomFirework(event: PacketEvent) = getEntity(event).persistentDataContainer.has(fireworkId, PersistentDataType.BYTE)
}