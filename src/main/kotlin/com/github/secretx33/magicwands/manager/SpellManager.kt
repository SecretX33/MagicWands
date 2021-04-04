package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.events.BlockSpellCastEvent
import com.github.secretx33.magicwands.events.EntitySpellCastEvent
import com.github.secretx33.magicwands.events.SpellCastEvent
import com.github.secretx33.magicwands.model.Cuboid
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.model.TempModification
import com.github.secretx33.magicwands.utils.runSync
import kotlinx.coroutines.*
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.koin.core.component.KoinApiExtension
import java.lang.StrictMath.pow
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashSet
import kotlin.math.*

@KoinApiExtension
class SpellManager (
    private val plugin: Plugin,
    private val config: Config,
    private val messages: Messages,
    private val particlesHelper: ParticlesHelper,
    private val hiddenPlayersHelper: HiddenPlayersHelper,
) {

    private val cooldown = HashMap<Pair<UUID, SpellType>, Long>()
    private val tempModification = ConcurrentHashMap<Job, TempModification>()
    private val blocksBlackList = HashSet<Location>()

    fun getSpellCD(player: Player, spellType: SpellType): Long
        = max(0L, (cooldown.getOrDefault(Pair(player.uniqueId, spellType), 0L) - System.currentTimeMillis()))

    fun addSpellCD(player: Player, spellType: SpellType) {
        cooldown[Pair(player.uniqueId, spellType)] = System.currentTimeMillis() + config.get(spellType.configCooldown, 7.0).toLong() * 1000
    }

    fun castBlind(event: EntitySpellCastEvent) {
        val target = event.target ?: throw IllegalStateException("Target cannot be null")
        val spellType = event.spellType
        val duration = config.get(spellType.configDuration, 1.0)

        target.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, (duration * 20).toInt(), 1))
        particlesHelper.sendFireworkParticle(target.location.apply { y += target.height * 0.7 }, spellType)
    }

    fun castBlink(event: BlockSpellCastEvent) {
        val player = event.player
        val block = event.block

        val previousLocation = player.location
        player.teleport(block.location.clone().apply {
            x += 0.5
            y += 1.15
            z += 0.5
            pitch = player.location.pitch
            yaw = player.location.yaw
        })
        particlesHelper.sendFireworkParticle(previousLocation, event.spellType)
    }

    fun castEnsnare(event: EntitySpellCastEvent) {
        val player = event.player
        val target = event.target ?: throw IllegalStateException("Target cannot be null")
        val spellType = event.spellType
        val duration = config.get(spellType.configDuration, 5) * 1000
        val fatiguePotency = config.get("${spellType.configRoot}.mining-fatigue-potency", 0)
        val fatigueDuration = config.get("${spellType.configRoot}.mining-fatigue-duration", 5.0)
        val cuboid = target.makeCuboidAround()

        val floorCeil = cuboid.getFloorAndCeil().filter { it.playerCanBreak(player) }
        val walls = cuboid.getWalls().filter { it.playerCanBreak(player) }

        val blockList = floorCeil + walls
        val blockListLocation = blockList.map { it.location }

        blocksBlackList.addAll(blockListLocation)

        val task = object : TempModification {
            val originalBlocks = blockList.map { it.state }

            override fun make() {
                floorCeil.forEach { block ->
                    (block.state as? InventoryHolder)?.inventory?.clear()
                    block.type = Material.AIR
                    block.type = Material.STONE_BRICKS
                }
                walls.forEach { block ->
                    (block.state as? InventoryHolder)?.inventory?.clear()
                    block.type = Material.AIR
                    block.type = Material.IRON_BARS
                }
            }

            override fun unmake() {
                originalBlocks.forEach { it.update(true, true) }
                blocksBlackList.removeAll(blockListLocation)
            }
        }
        task.make()
        target.teleport(cuboid.center.apply {
            y = cuboid.yMin + 1.0
            yaw = target.location.yaw
            pitch = target.location.pitch
        })
        val job = scheduleUnmake(task, duration.toLong())
        tempModification[job] = task
        particlesHelper.sendFireworkParticle(cuboid.center, spellType)
        if(fatiguePotency > 0)
            target.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, (fatigueDuration * 20).toInt(), max(fatiguePotency - 1, 0)))
    }

    private fun Block.playerCanBreak(player: Player): Boolean = type != Material.BEDROCK && !blocksBlackList.contains(location) && WorldGuardHelper.canBreakBlock(this, player)

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
        runSync(plugin) { task.unmake() }
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
        particlesHelper.sendFireworkParticle(player.location.apply { y += 0.25 }, type)
    }

    fun castPoison(event: EntitySpellCastEvent) {
        val player = event.player
        val target = event.target ?: throw IllegalStateException("Target cannot be null")
        val spellType = event.spellType
        val duration = config.get(spellType.configDuration, 0.0)
        val poisonPotency = config.get("${spellType.configRoot}.poison-potency", 4)

        target.addPotionEffect(PotionEffect(PotionEffectType.POISON, (duration * 20).toInt(), max(poisonPotency - 1, 0)))
        player.sendMessage(messages.get(MessageKeys.POISONED_TARGET).replace("<target>", target.customName ?: target.name))
        if(target is Player)
            target.sendMessage(messages.get(MessageKeys.GOT_POISONED).replace("<caster>", player.name))
        particlesHelper.sendFireworkParticle(target.location.apply { y += target.height * 0.65 }, spellType)
    }

    fun castSlow(event: EntitySpellCastEvent) {
        val player = event.player
        val target = event.target ?: throw IllegalStateException("Target cannot be null")
        val spellType = event.spellType
        val duration = config.get(spellType.configDuration, 2.0)
        val slowPotency = config.get("${spellType.configRoot}.potency", 1)

        target.addPotionEffect(PotionEffect(PotionEffectType.SLOW, (duration * 20).toInt(), max(slowPotency - 1, 0)))
        player.sendMessage(messages.get(MessageKeys.SLOWED_TARGET).replace("<target>", target.customName ?: target.name))
        if(target is Player)
            target.sendMessage(messages.get(MessageKeys.GOT_SLOWED).replace("<caster>", player.name))
        particlesHelper.sendFireworkParticle(target.location.apply { y += target.height * 0.65 }, spellType)
    }

    fun castThrust(event: EntitySpellCastEvent) {
        val player = event.player
        val target = event.target ?: throw IllegalStateException("Target cannot be null")
        val type = event.spellType
        val h = config.get("${type.configRoot}.height-multiplier", 1.0)
        val heightMulti = if(h <= 1) h else sqrt(h)
        val distanceMulti = config.get("${type.configRoot}.distance-multiplier", 1.0)

        target.thrustBy(player, heightMulti = heightMulti, distanceMulti = distanceMulti)
        particlesHelper.sendFireworkParticle(target.location.apply { y += target.height * 0.6 }, type)
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

    fun castVanish(event: SpellCastEvent) {
        val player = event.player
        val spellType = event.spellType
        val duration = config.get(spellType.configDuration, 0.0)
        val fullInvisible = config.get<Boolean>(ConfigKeys.VANISH_FULL_INVISIBLE)

        println("Vanish mode: $fullInvisible")

        player.sendMessage(messages.get(MessageKeys.CASTED_VANISH))

        if(fullInvisible) hiddenPlayersHelper.hidePlayer(player, duration)
        else player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, (duration * 20).toInt(), 1, false, false))
        particlesHelper.sendFireworkParticle(player.location.apply { y += player.height * 0.7 }, spellType)
    }

    fun finalizeTasks() {
        tempModification.forEach { (job, mod) ->
            job.cancel()
            mod.unmake()
        }
    }
}
