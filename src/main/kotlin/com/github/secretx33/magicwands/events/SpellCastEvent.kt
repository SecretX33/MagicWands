package com.github.secretx33.magicwands.events

import com.github.secretx33.magicwands.spell.SpellType
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class SpellCastEvent(player: Player, val type: SpellType) : PlayerEvent(player), Cancellable {

    private var isCancelled = false
    private val handlers = HandlerList()

    override fun getHandlers(): HandlerList {
        return handlers
    }

    override fun isCancelled(): Boolean = isCancelled

    override fun setCancelled(isCancelled: Boolean) {
        this.isCancelled = isCancelled
    }
}