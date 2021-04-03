package com.github.secretx33.magicwands.packetlisteners

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.secretx33.magicwands.packets.WrapperPlayServerSpawnEntity
import com.github.secretx33.magicwands.utils.ItemUtils
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class WandDropPacketListener(private val plugin: Plugin) {

    init { setup() }

    private fun setup() {
        val manager = ProtocolLibrary.getProtocolManager()
        manager.addPacketListener(object : PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Server.SPAWN_ENTITY) {
            override fun onPacketSending(event: PacketEvent) {
                if(event.packetType != PacketType.Play.Server.SPAWN_ENTITY || event.isCancelled) return

                val wrapper = WrapperPlayServerSpawnEntity(event.packet)
                if(wrapper.type != EntityType.DROPPED_ITEM) return

                val item = (wrapper.getEntity(event) as Item).itemStack
                if(!item.isWand()) return

                val ownerUuid = ItemUtils.getWandOwnerUuid(item) ?: run {
                    event.isCancelled = true
                    return
                }
                // if the player is not the original owner of the wand, prevent him from knowing where the dropped wand is
                if(event.player.uniqueId != ownerUuid) event.isCancelled = true
            }
        })
    }
}