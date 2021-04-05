package com.github.secretx33.magicwands.utils

import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.text.WordUtils
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin
import java.util.*


fun PlayerInteractEvent.isLeftClick() = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK

fun PlayerInteractEvent.isRightClick() = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK

fun Player.getTarget(range: Int): LivingEntity? = (world.rayTraceEntities(eyeLocation, eyeLocation.direction, range.toDouble()) { it is LivingEntity && type != EntityType.ENDER_DRAGON && it.uniqueId != uniqueId }?.hitEntity as? LivingEntity)?.takeIf { hasLineOfSight(it) }

fun Player.getTargetBlockWithFace(range: Int): Pair<Block, BlockFace>? {
    val result = world.rayTraceBlocks(eyeLocation, eyeLocation.direction, range.toDouble(), FluidCollisionMode.NEVER, true) ?: return null
    val block = result.hitBlock ?: return null
    val face = result.hitBlockFace ?: return null
    return Pair(block, face)
}

fun String.capitalizeFully(): String = WordUtils.capitalizeFully(this)

fun String.toUuid(): UUID = UUID.fromString(this)

fun runSync(plugin: Plugin, delay: Long = 0L, block: () -> Unit) {
    if(delay < 0) return
    if(delay == 0L) Bukkit.getScheduler().runTask(plugin, Runnable { block() })
    else Bukkit.getScheduler().runTaskLater(plugin, Runnable { block() }, delay)
}

fun Block.isAir() = type.isAir

fun Block.coordinates(): String = "${location.x.toLong()} ${location.y.toLong()} ${location.z.toLong()}"

fun Location.prettyString(): String = "World: ${world?.name ?: "Unknown"}, ${x.toLong()}, ${y.toLong()}, ${z.toLong()}"
