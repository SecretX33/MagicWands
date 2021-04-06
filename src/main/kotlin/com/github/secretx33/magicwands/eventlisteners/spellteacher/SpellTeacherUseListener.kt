package com.github.secretx33.magicwands.eventlisteners.spellteacher

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.repositories.LearnedSpellsRepo
import com.github.secretx33.magicwands.repositories.SpellTeacherRepo
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
import java.text.DecimalFormat

@KoinApiExtension
class SpellTeacherUseListener (
    plugin: Plugin,
    private val config: Config,
    private val messages: Messages,
    private val learnedSpells: LearnedSpellsRepo,
    private val spellTeacher: SpellTeacherRepo,
    private val economy: Economy,
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun PlayerInteractEvent.onSpellteacherInteract() {
        if(hand != EquipmentSlot.HAND) return
        val block = clickedBlock ?: return
        if(!isRightClick() || block.isAir() || !spellTeacher.isSpellTeacher(block)) return

        val spellType = spellTeacher.getTeacherType(block) ?: return
        if(learnedSpells.knows(player.uniqueId, spellType)) {
            player.sendMessage(messages.get(MessageKeys.CANNOT_PURCHASE_ALREADY_KNOW).replace("<spell>", spellType.displayName))
            return
        }

        val price = config.get(spellType.configLearnPrice, -1.0).takeIf { it >= 0.0 } ?: return
        if(price == 0.0) {
            val balance = economy.getBalance(player, player.world.name)
            player.sendMessage(messages.get(MessageKeys.SUCCESSFULLY_PURCHASED_FREE_SPELL)
                .replace("<spell>", spellType.displayName))
            learnedSpells.teachSpell(player.uniqueId, spellType)
            return
        }
        if(!economy.has(player, price)) {
            val balance = economy.getBalance(player, player.world.name)
            player.sendMessage(messages.get(MessageKeys.NOT_ENOUGH_MONEY)
                .replace("<spell>", spellType.displayName)
                .replace("<price>", formatter.format(price))
                .replace("<balance>", formatter.format(balance)))
            return
        }

        val response = economy.withdrawPlayer(player, price)
        if(!response.transactionSuccess()) {
            player.sendMessage(messages.get(MessageKeys.TRANSACTION_FAILED).replace("<error>", response.errorMessage))
            return
        }
        val balance = economy.getBalance(player)
        player.sendMessage(messages.get(MessageKeys.SUCCESSFULLY_PURCHASED_SPELL)
            .replace("<price>", formatter.format(price))
            .replace("<spell>", spellType.displayName)
            .replace("<balance>", formatter.format(balance)))
        learnedSpells.teachSpell(player.uniqueId, spellType)
    }

    private companion object {
        val formatter = DecimalFormat("###.##")
    }
}
