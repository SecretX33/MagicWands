package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.manager.LearnedSpellsManager
import com.github.secretx33.magicwands.manager.SpellTeacherManager
import com.github.secretx33.magicwands.utils.isAir
import com.github.secretx33.magicwands.utils.isRightClick
import net.md_5.bungee.api.ChatColor
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class SpellTeacherListener (
    plugin: Plugin,
    private val config: Config,
    private val messages: Messages,
    private val learnedSpells: LearnedSpellsManager,
    private val spellTeacher: SpellTeacherManager,
    private val economy: Economy,
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun PlayerInteractEvent.onSpellteacherInteract() {
        println("1")
        val block = clickedBlock ?: return
        println("2")
        if(!isRightClick() || block.isAir()) return
        println("3")
        if(!spellTeacher.isSpellTeacher(block)) return
        println("4")

        val spellType = spellTeacher.getSpellType(block)
        if(learnedSpells.knows(player, spellType)) {
            player.sendMessage("${ChatColor.RED}You already know this ma man!")
            return
        }

        val price = config.get(spellType.configLearnPrice, -1.0).takeIf { it > -1 } ?: return
        if(!economy.has(player, price)) {
            player.sendMessage("${ChatColor.RED}No munny baby!")
            return
        }

        val response = economy.withdrawPlayer(player, price)
        if(!response.transactionSuccess()) {
            player.sendMessage("${ChatColor.RED}We could not validate your purchase: ${response.errorMessage}")
            return
        }
        player.sendMessage("${ChatColor.GREEN}You have purchased spell: ${ChatColor.GOLD}" + spellType.displayName)
        learnedSpells.addSpell(player, spellType)
    }
}