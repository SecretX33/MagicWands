package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.utils.CustomKoinComponent
import com.github.secretx33.magicwands.utils.inject
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.Flag
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.koin.core.component.KoinApiExtension
import java.util.logging.Logger

@KoinApiExtension
object WorldGuardHelper : CustomKoinComponent {

    private val log by inject<Logger>()

    private var antimagicZoneFlag: Any? = null
        set(value) {
            if(value == null) return
            field = value
        }

    private val isWorldGuardEnabled
        get() = Bukkit.getPluginManager().isPluginEnabled("WorldGuard")

    fun canBreakBlock(block: Block, player: Player): Boolean {
        if(!isWorldGuardEnabled) return true

        val wg = WorldGuard.getInstance()
        val loc = BukkitAdapter.adapt(block.location)
        val container = wg.platform.regionContainer
        val query = container.createQuery()
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        return !isInsideAntimagicZone(player, block.location) && (query.testBuild(loc, localPlayer, Flags.BUILD) || wg.platform.sessionManager.hasBypass(localPlayer, localPlayer.world))
    }

    fun isInsideAntimagicZone(caster: Player, target: Location): Boolean {
        if(!isWorldGuardEnabled || antimagicZoneFlag == null || caster.hasPermission("magicwands.hooks.wg.antimagiczone_bypass")) return false

        val wg = WorldGuard.getInstance()
        val loc = BukkitAdapter.adapt(target)
        val container = wg.platform.regionContainer
        val query = container.createQuery()
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(caster)
        return query.testState(loc, localPlayer, antimagicZoneFlag as StateFlag)
    }

    fun hookOnWG() {
        val registry = WorldGuard.getInstance().flagRegistry
        try {
            val flag = StateFlag("mw-antimagiczone", true)
            registry.register(flag)
            antimagicZoneFlag = flag
        } catch (e: FlagConflictException) {
            log.severe("Oops! Seems like another plugin already registered using the flag 'mw-antimagiczone', this should not happen unless perhaps you've used /reload, if not then contact SecretX asap.")
            val existing: Flag<*> = registry["mw-antimagiczone"] ?: return
            if (existing is StateFlag) {
                antimagicZoneFlag = existing
            } else {
                log.severe("And even worse, the flag types doesn't match, so MagicWands cannot even try to use the other plugin's flag aka \"compatilibity mode\".")
            }
        }
    }
}
