package com.github.secretx33.magicwands.utils

import com.github.secretx33.magicwands.config.Const.PLUGIN_CHAT_PREFIX
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.text.WordUtils
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Painting
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.koin.core.component.KoinApiExtension
import java.util.function.Predicate

@KoinApiExtension
object Utils: CustomKoinComponent {
    private val console: ConsoleCommandSender by inject()

    fun consoleMessage(msg: String) = console.sendMessage("$PLUGIN_CHAT_PREFIX $msg")

    fun debugMessage(msg: String) = console.sendMessage("$PLUGIN_CHAT_PREFIX $msg")
}

fun PlayerInteractEvent.isLeftClick() = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK

fun PlayerInteractEvent.isRightClick() = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK

fun Player.getTarget(range: Int): LivingEntity? = world.rayTraceEntities(eyeLocation, eyeLocation.direction, range.toDouble()) { it is LivingEntity && it.uniqueId != uniqueId }?.hitEntity as? LivingEntity

//fun String.upperFirst(): String {
//    if(isEmpty()) return this
//    val array = toCharArray()
//    array[0] = array[0].toUpperCase()
//    return array.toString()
//}

fun String.capitalizeFully(): String = WordUtils.capitalizeFully(this)