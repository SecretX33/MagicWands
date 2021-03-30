package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.manager.SpellFuelManager
import com.github.secretx33.magicwands.manager.SpellManager
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

class SpellCastListener (
    plugin: Plugin,
    private val fuelManager: SpellFuelManager,
    private val spellManager: SpellManager,
    private val messages: Messages,
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    
}