package com.github.secretx33.magicwands.manager

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.BooleanFlag
import com.sk89q.worldguard.protection.flags.Flag
import com.sk89q.worldguard.protection.flags.Flags
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.util.logging.Logger

// Used when worldguard is absent
class WorldGuardCheckerDummy : WorldGuardChecker {
    override fun canBreakBlock(block: Block, player: Player): Boolean = true
    override fun isInsideAntimagicZone(caster: Player, target: Location): Boolean = false
}

// Used when worldguard is present
class WorldGuardCheckerImpl(private val log: Logger) : WorldGuardChecker {

    private var antimagicZoneFlag: BooleanFlag? = null

    init { hookWithWG() }

    private fun hookWithWG() {
        val registry = WorldGuard.getInstance().flagRegistry
        try {
            val flag = BooleanFlag("mw-antimagiczone")
            registry.register(flag)
            antimagicZoneFlag = flag
        } catch (e: com.sk89q.worldguard.protection.flags.registry.FlagConflictException) {
            log.severe("Oops! Seems like another plugin already registered using the flag 'mw-antimagiczone', this should not happen unless perhaps you've used /reload, if not then contact SecretX asap.")
            val existing: Flag<*>? = registry["mw-antimagiczone"]
            if (existing is BooleanFlag) {
                antimagicZoneFlag = existing
            } else {
                log.severe("And even worse, the flag types doesn't match, so MagicWands cannot even try to use the other plugin's flag aka \"compatilibity mode\".")
            }
        }
    }

    override fun canBreakBlock(block: Block, player: Player): Boolean {
        if(!isWorldGuardEnabled) return true

        val wg = WorldGuard.getInstance()
        val loc = BukkitAdapter.adapt(block.location)
        val container = wg.platform.regionContainer
        val query = container.createQuery()
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        return !isInsideAntimagicZone(player, block.location) && (query.testBuild(loc, localPlayer, Flags.BUILD) || wg.platform.sessionManager.hasBypass(localPlayer, localPlayer.world))
    }

    override fun isInsideAntimagicZone(caster: Player, target: Location): Boolean {
        if(!isWorldGuardEnabled || antimagicZoneFlag == null || caster.hasPermission("magicwands.bypass.antimagiczone")) return false

        val wg = WorldGuard.getInstance()
        val loc = BukkitAdapter.adapt(target)
        val container = wg.platform.regionContainer
        val query = container.createQuery()
        val regions = query.getApplicableRegions(loc)
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(caster)
        return regions.queryValue(localPlayer, antimagicZoneFlag as BooleanFlag) ?: false
    }

    private val isWorldGuardEnabled
        get() = Bukkit.getPluginManager().isPluginEnabled("WorldGuard")
}

interface WorldGuardChecker {
    fun canBreakBlock(block: Block, player: Player): Boolean
    fun isInsideAntimagicZone(caster: Player, target: Location): Boolean
}
