package com.github.secretx33.magicwands.events

import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class WandSpellSwitchEvent(player: Player, val wand: ItemStack) : PlayerEvent(player), Cancellable {

    init { require(wand.isWand()) { "Item passed as Wand is not a wand!" } }

    private var isCancelled = false

    override fun getHandlers(): HandlerList = WandSpellSwitchEvent.handlers

    override fun isCancelled(): Boolean = isCancelled

    override fun setCancelled(isCancelled: Boolean) {
        this.isCancelled = isCancelled
    }

    companion object {
        @JvmStatic
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }
}