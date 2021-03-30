package com.github.secretx33.magicwands.utils

import com.github.secretx33.magicwands.config.Const.PLUGIN_CHAT_PREFIX
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
object Utils: CustomKoinComponent {
    private val console: ConsoleCommandSender by inject()

    fun consoleMessage(msg: String) = console.sendMessage("$PLUGIN_CHAT_PREFIX $msg")

    fun debugMessage(msg: String) = console.sendMessage("$PLUGIN_CHAT_PREFIX $msg")
}

fun PlayerInteractEvent.isLeftClick() = action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK

fun PlayerInteractEvent.isRightClick() = action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK