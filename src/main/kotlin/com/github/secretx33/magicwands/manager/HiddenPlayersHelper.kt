package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.utils.runSync
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class HiddenPlayersHelper(private val plugin: Plugin) {

    private val hiddenPlayers = ConcurrentHashMap<UUID, Job>()

    fun hidePlayer(player: Player, showAgainIn: Int) {
        if(showAgainIn == 0) return
        val uuid = player.uniqueId
        hiddenPlayers[uuid]?.cancel()
        Bukkit.getOnlinePlayers().asSequence().filter { it.uniqueId != uuid }
            .forEach { it.hidePlayer(plugin, player) }
        hiddenPlayers[uuid] = showPlayerLater(player, showAgainIn)
    }

    private fun showPlayerLater(player: Player, showAgainIn: Int) = CoroutineScope(Dispatchers.Default).launch {
        delay(showAgainIn * 1000L)
        if(!isActive) return@launch
        showPlayer(player)
    }

    private fun showPlayer(player: Player) {
        val uuid = player.uniqueId
        runSync(plugin) {
            Bukkit.getOnlinePlayers().asSequence().filter { it.uniqueId != uuid }
                .forEach { it.showPlayer(plugin, player) }
        }
        hiddenPlayers.remove(uuid)
    }

    fun invalidateHiddenState(player: Player) {
        val uuid = player.uniqueId
        hiddenPlayers[uuid]?.apply {
            cancel()
            showPlayer(player)
        }
    }
}