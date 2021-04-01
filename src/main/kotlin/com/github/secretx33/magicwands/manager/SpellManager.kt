package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.events.BlockSpellCastEvent
import com.github.secretx33.magicwands.events.EntitySpellCastEvent
import com.github.secretx33.magicwands.events.SpellCastEvent
import com.github.secretx33.magicwands.model.Cuboid
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.model.TempModification
import com.github.secretx33.magicwands.utils.YamlManager
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.koin.core.component.KoinApiExtension
import java.lang.Runnable
import java.lang.StrictMath.pow
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashSet
import kotlin.math.*

@KoinApiExtension
class SpellManager(
    private val plugin: Plugin,
    private val config: Config,
    private val messages: Messages,
    private val particlesHelper: ParticlesHelper,
) {

    private val manager = YamlManager(plugin, "spells_learned/spells_learned")
    private val cooldown = HashMap<Pair<UUID, SpellType>, Long>()
    private val tempModification = ConcurrentHashMap<Job, TempModification>()
    private val blocksBlackList = HashSet<Location>()

    fun getSpellCD(player: Player, spellType: SpellType): Long
        = max(0L, (cooldown.getOrDefault(Pair(player.uniqueId, spellType), 0L) - System.currentTimeMillis()))

    fun addSpellCD(player: Player, spellType: SpellType) {
        cooldown[Pair(player.uniqueId, spellType)] = System.currentTimeMillis() + config.get(spellType.configCooldown, 7) * 1000
    }

    fun knows(player: Player, spellType: SpellType): Boolean {
        return manager.contains("${player.uniqueId}.${spellType.name}")
//        manager.getStringList(player.uniqueId.toString()).contains(spell.name)
    }

    fun teach(player: Player, spellType: SpellType) {
        val path = player.uniqueId.toString()
        val spellList = manager.getStringList(path)
        if(!spellList.contains(spellType.name)) {
            spellList.add(spellType.name)
            manager.set(path, spellList)
            manager.save()
        }
    }

    fun reload() = manager.reload()

    fun castVanish(event: SpellCastEvent) {
        val player = event.player
        val spellType = event.spellType
        val duration = config.get(spellType.configDuration, 0)

        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, duration * 20, 1, false, false))
        player.sendMessage(messages.get(MessageKeys.CASTED_VANISH))
    }

    fun castBlind(event: EntitySpellCastEvent) {
        val target = event.target ?: throw IllegalStateException("Target cannot be null")
        val spellType = event.spellType
        val duration = config.get(spellType.configDuration, 0)

        target.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, duration * 20, 1))
    }

    fun castEnsnare(event: EntitySpellCastEvent) {
        val player = event.player
        val target = event.target ?: throw IllegalStateException("Target cannot be null")
        val spellType = event.spellType
        val duration: Long = config.get(spellType.configDuration, 5) * 1000L
        val cuboid = target.makeCuboidAround()
        val blockList = cuboid.allSidesBlockList().filter { it.type != Material.BEDROCK
                && !blocksBlackList.contains(it.location) && WorldGuardHelper.canBreakBlock(it, player) }
        val blockListLocation = blockList.map { it.location }

        blocksBlackList.addAll(blockListLocation)

        val task = object : TempModification {
            val originalBlocks = blockList.map { it.state }

            override fun make() {
                blockList.forEach { block ->
                    (block.state as? InventoryHolder)?.inventory?.clear()
                    block.type = Material.AIR
                    block.type = Material.CRYING_OBSIDIAN
                }
            }

            override fun unmake() {
                originalBlocks.forEach { it.update(true, false) }
                blocksBlackList.removeAll(blockListLocation)
            }
        }
        task.make()
        target.teleport(cuboid.center.apply {
            y = cuboid.yMin + 1.0
            yaw = target.location.yaw
            pitch = target.location.pitch
        })
        val job = scheduleUnmake(task, duration)
        tempModification[job] = task
    }

    private fun LivingEntity.makeCuboidAround(): Cuboid {
        val lowerBound = location.clone().apply {
            x -= ceil(width)
            y -= 1
            z -= ceil(width)
        }
        val upperBound = location.clone().apply {
            x += ceil(width)
            y += ceil(height)
            z += ceil(width)
        }
        return Cuboid(lowerBound, upperBound)
    }

    private fun scheduleUnmake(task: TempModification, delay: Long) = CoroutineScope(Dispatchers.Default).launch {
        delay(delay)
        if(!isActive) return@launch
        tempModification.remove(coroutineContext.job)
        runSync { task.unmake() }
    }

    fun castPoison(event: EntitySpellCastEvent) {
        val player = event.player
        val target = event.target ?: throw IllegalStateException("Target cannot be null")
        val spellType = event.spellType
        val duration = config.get(spellType.configDuration, 0)

        target.addPotionEffect(PotionEffect(PotionEffectType.POISON, duration * 20, 7))
        player.sendMessage(messages.get(MessageKeys.POISONED_TARGET).replace("<target>", target.customName ?: target.name))
        if(target is Player) target.sendMessage(messages.get(MessageKeys.GOT_POISONED).replace("<caster>", player.name))
    }

    fun castBlink(event: BlockSpellCastEvent) {
        val player = event.player
        val block = event.block

        player.teleport(block.location.clone().apply {
            x += 0.5
            y += 0.25
            z += 0.5
            pitch = player.location.pitch
            yaw = player.location.yaw
        })
    }

    fun castLeap(event: SpellCastEvent) {
        val player = event.player
        val type = event.spellType
        val h = config.get("${type.configRoot}.height-multiplier", 1.0)
        val heightMulti = if(h <= 1) h else sqrt(h)
        val distanceMulti = config.get("${type.configRoot}.distance-multiplier", 1.0)

        val impulse = player.location.direction.apply {
            x *= distanceMulti
            y = heightMulti
            z *= distanceMulti
        }
        player.velocity = impulse
        particlesHelper.sendFireworkParticle(player.location, type)
    }

    fun castThrust(event: EntitySpellCastEvent) {
        val player = event.player
        val target = event.target ?: throw IllegalStateException("Target cannot be null")
        val type = event.spellType
        val h = config.get("${type.configRoot}.height-multiplier", 1.0)
        val heightMulti = if(h <= 1) h else sqrt(h)
        val distanceMulti = config.get("${type.configRoot}.distance-multiplier", 1.0)

        target.thrustBy(player, heightMulti = heightMulti, distanceMulti = distanceMulti)
    }

    private fun LivingEntity.thrustBy(atk: Entity, heightMulti: Double, distanceMulti: Double) {
        val src = atk.location.apply {
            y += atk.height / 2
        }
        val dest = this.location
        val difX = dest.x - src.x
        val difZ = dest.z - src.z
        val difY = dest.y - src.y
        val difXZ = sqrt(pow(difX, 2.0) + pow(difZ, 2.0))
        val pitch = Math.toDegrees(atan(-difY / difXZ))
        val yaw = Math.toDegrees(atan2(difZ, difX))
        src.pitch = pitch.toFloat()
        src.yaw = yaw.toFloat() - 90f
        val impulse = src.direction
        impulse.x *= distanceMulti
        impulse.y = heightMulti
        impulse.z *= distanceMulti
        velocity = impulse
    }

    private fun runSync(delay: Long = 0L, block: () -> Unit) {
        if(delay < 0) return
        if(delay == 0L) Bukkit.getScheduler().runTask(plugin, Runnable { block() })
        else Bukkit.getScheduler().runTaskLater(plugin, Runnable { block() }, delay)
    }

    fun close() {
        tempModification.forEach { (job, mod) ->
            job.cancel()
            mod.unmake()
        }
    }
}