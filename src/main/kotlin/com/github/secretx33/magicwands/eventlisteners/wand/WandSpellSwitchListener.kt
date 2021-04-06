package com.github.secretx33.magicwands.eventlisteners.wand

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.events.WandSpellSwitchEvent
import com.github.secretx33.magicwands.utils.ItemUtils
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class WandSpellSwitchListener (
    plugin: Plugin,
    private val config: Config,
    private val messages: Messages,
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private fun WandSpellSwitchEvent.trySwitchCast() {
        val availableSpells = ItemUtils.getAvailableSpells(wand).sorted()

        if(availableSpells.isEmpty()) {
            isCancelled = true
            return
        }
        val selected = ItemUtils.getWandSpellOrNull(wand)
        var index = if(selected != null) availableSpells.indexOf(selected) + 1 else 0
        if(index > availableSpells.lastIndex) index = 0
        ItemUtils.setWandSpell(wand, availableSpells[index])
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun WandSpellSwitchEvent.onSwitch() {
        val selected = ItemUtils.getWandSpell(wand)
        if(config.get(ConfigKeys.ENABLE_MESSAGE_ON_SPELL_SWITCH)) {
            player.sendMessage(messages.get(MessageKeys.SWITCHED_SPELL).replace("<spell>", selected.displayName))
        }
    }
}
