package com.github.secretx33.magicwands.utils

import com.github.secretx33.magicwands.config.Const.PLUGIN_CHAT_PREFIX
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.text.WordUtils
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension
import java.util.*
import kotlin.math.abs

@KoinApiExtension
object Utils: CustomKoinComponent {
    private val console: ConsoleCommandSender by inject()

    fun consoleMessage(msg: String) = console.sendMessage("$PLUGIN_CHAT_PREFIX $msg")

    fun debugMessage(msg: String) = console.sendMessage("$PLUGIN_CHAT_PREFIX $msg")
}

fun PlayerInteractEvent.isLeftClick() = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK

fun PlayerInteractEvent.isRightClick() = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK

fun Player.getTarget(range: Int): LivingEntity? = world.rayTraceEntities(eyeLocation, eyeLocation.direction, range.toDouble()) { it is LivingEntity && it.uniqueId != uniqueId }?.hitEntity as? LivingEntity

fun String.capitalizeFully(): String = WordUtils.capitalizeFully(this)

fun String.toUuid(): UUID = UUID.fromString(this)

fun runSync(plugin: Plugin, delay: Long = 0L, block: () -> Unit) {
    if(delay < 0) return
    if(delay == 0L) Bukkit.getScheduler().runTask(plugin, Runnable { block() })
    else Bukkit.getScheduler().runTaskLater(plugin, Runnable { block() }, delay)
}

fun Block.isAir() = type.isAir