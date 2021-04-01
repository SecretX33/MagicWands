package com.github.secretx33.magicwands.commands.subcommands

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.utils.CustomKoinComponent
import com.github.secretx33.magicwands.utils.ItemUtils
import com.github.secretx33.magicwands.utils.inject
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class SpellremoveCommand  : SubCommand(), CustomKoinComponent {

    override val name: String = "spellremove"
    override val permission: String = "spellremove"
    override val aliases: List<String> = listOf(name, "spellr", "sr")

    private val messages by inject<Messages>()

    override fun onCommandByPlayer(player: Player, alias: String, strings: Array<String>) {
        val item = player.inventory.itemInMainHand

        if(strings.size < 2) {
            player.sendMessage("${ChatColor.RED}Usage: /${alias} $name <spell>")
            return
        }
        val spellType = runCatching { SpellType.of(strings[1]) }.getOrElse {
            player.sendMessage(messages.get(MessageKeys.SPELL_DOESNT_EXIST).replace("<spell>", strings[1]))
            return
        }
        removeSpell(player, item, spellType)
    }

    override fun onCommandByConsole(sender: CommandSender, alias: String, strings: Array<String>) {
        sender.sendMessage(messages.get(MessageKeys.CONSOLE_CANNOT_USE))
    }

    private fun removeSpell(player: Player, item: ItemStack, spellType: SpellType) {
        if (!item.isWand()) {
            player.sendMessage(messages.get(MessageKeys.ITEM_NOT_A_WAND))
            return
        }
        val spellList = ItemUtils.getAvailableSpells(item)

        if(!spellList.remove(spellType)) {
            player.sendMessage(messages.get(MessageKeys.SPELL_NOT_PRESENT))
            return
        }
        ItemUtils.setAvailableSpells(item, spellList)
        player.sendMessage(messages.get(MessageKeys.REMOVED_SPELL_OF_WAND).replace("<spell>", spellType.displayName))
    }

    override fun getCompletor(sender: CommandSender, length: Int, hint: String, strings: Array<String>): List<String> {
        if(sender !is Player) return emptyList()

        return when(length) {
            2 -> {
                val item = sender.inventory.itemInMainHand
                val enchants = if(item.isWand()) {
                    ItemUtils.getAvailableSpells(item)
                } else {
                    SpellType.values().toList()
                }
                return enchants.map { it.displayName }.filter { it.startsWith(hint, ignoreCase = true) }.sorted()
            }
            else -> emptyList()
        }
    }
}