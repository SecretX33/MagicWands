package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.manager.LearnedSpellsManager
import com.github.secretx33.magicwands.manager.SpellTeacherManager
import com.github.secretx33.magicwands.utils.isAir
import com.github.secretx33.magicwands.utils.isRightClick
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
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
        if(hand != EquipmentSlot.HAND) return
        val block = clickedBlock ?: return
        if(!isRightClick() || block.isAir()) return
        if(!spellTeacher.isSpellTeacher(block)) return

        val spellType = spellTeacher.getSpellType(block)
        if(learnedSpells.knows(player, spellType)) {
            player.sendMessage(messages.get(MessageKeys.CANNOT_PURCHASE_ALREADY_KNOW).replace("<spell>", spellType.displayName))
            return
        }

        val price = config.get(spellType.configLearnPrice, -1.0).takeIf { it > -1 } ?: return
        if(!economy.has(player, price)) {
            val balance = economy.getBalance(player, player.world.name)
            player.sendMessage(messages.get(MessageKeys.NOT_ENOUGH_MONEY)
                .replace("<spell>", spellType.displayName)
                .replace("<price>", price.toString())
                .replace("<balance>", balance.toString()))
            return
        }

        val response = economy.withdrawPlayer(player, price)
        if(!response.transactionSuccess()) {
            player.sendMessage(messages.get(MessageKeys.TRANSACTION_FAILED).replace("<error>", response.errorMessage))
            return
        }
        player.sendMessage(messages.get(MessageKeys.SUCCESSFULLY_PURCHASED_SPELL).replace("<spell>", spellType.displayName))
        learnedSpells.addSpell(player, spellType)
    }
}