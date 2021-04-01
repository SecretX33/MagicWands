package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.utils.CustomKoinComponent
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.Flags
import org.bukkit.Bukkit
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
object WorldGuardHelper : CustomKoinComponent {

    private val isWorldGuardEnabled
        get() = Bukkit.getPluginManager().isPluginEnabled("WorldGuard")

    fun canBreakBlock(block: Block, player: Player): Boolean {
        if(!isWorldGuardEnabled) return true

        val wg = WorldGuard.getInstance()
        val loc = BukkitAdapter.adapt(block.location)
        val container = wg.platform.regionContainer
        val query = container.createQuery()
        val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)
        return query.testBuild(loc, localPlayer, Flags.BUILD) || wg.platform.sessionManager.hasBypass(localPlayer, localPlayer.world)
    }
}