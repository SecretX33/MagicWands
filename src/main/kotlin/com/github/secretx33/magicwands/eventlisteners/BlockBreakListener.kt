package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.manager.SpellTeacherManager
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.Plugin

class BlockBreakListener (
    plugin: Plugin,
    private val spellTeacher: SpellTeacherManager,
    private val messages: Messages,
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun BlockBreakEvent.onSpellTeacherBreak() {
        if(!spellTeacher.isSpellTeacher(block.location)) return
        player.sendMessage(messages.get(MessageKeys.BROKE_SPELLTEACHER))
        spellTeacher.removeSpellTeacher(block)
    }
}