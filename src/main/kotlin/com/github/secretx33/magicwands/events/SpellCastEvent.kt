package com.github.secretx33.magicwands.events

import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
open class SpellCastEvent(player: Player, val wand: ItemStack, val spellType: SpellType) : PlayerEvent(player), Cancellable {

    init { require(wand.isWand()) { "Item passed as Wand is not a wand!" } }

    private var isCancelled = false

    override fun getHandlers(): HandlerList = SpellCastEvent.handlers

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