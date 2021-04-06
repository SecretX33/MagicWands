package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.manager.HiddenPlayersHelper
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class PlayerDeathListener (
    plugin: Plugin,
    private val config: Config,
    private val hiddenPlayersHelper: HiddenPlayersHelper
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun PlayerDeathEvent.onPlayerDeath() {
        hiddenPlayersHelper.invalidateHiddenState(entity)
        if(keepInventory || !deleteWandOnDeath) return
        // removing all wands present in the drop
        drops.removeIf { it.isWand() }
    }

    private val deleteWandOnDeath: Boolean
        get() = config.get(ConfigKeys.DELETE_WAND_ON_DEATH)
}
