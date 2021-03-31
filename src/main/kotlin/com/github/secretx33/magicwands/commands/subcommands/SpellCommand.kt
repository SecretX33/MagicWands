package com.github.secretx33.magicwands.commands.subcommands

import com.github.secretx33.chestquest.commands.subcommands.SubCommand
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.utils.*
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinApiExtension
import java.util.*

@KoinApiExtension
class SpellCommand : SubCommand(), CustomKoinComponent {

    override val name: String = "spell"
    override val permission: String = "spell"
    override val aliases: List<String> = listOf(name, "rel", "r")

    private val messages by inject<Messages>()

    override fun onCommandByPlayer(player: Player, alias: String, strings: Array<String>) {
        val item = player.inventory.itemInMainHand

        if(strings.size < 3) {
            player.sendMessage("${ChatColor.RED}Usage: /${alias} spell <bind/remove> <spell>")
            return
        }
        val sub = strings[2].toLowerCase(Locale.US)

        val spellType = runCatching { SpellType.valueOf(strings[3]) }.getOrElse {
            player.sendMessage(messages.get(MessageKeys.SPELL_DOESNT_EXIST).replace("<spell>", strings[3]))
            return
        }

        if(sub == "bind")
            bindSpell(player, item, spellType)
        else if(sub == "remove")
            removeSpell(player, item, spellType)
    }

    private fun bindSpell(player: Player, item: ItemStack, spellType: SpellType) {
        if (!item.isWandMaterial()) {
            player.sendMessage(
                messages.get(MessageKeys.INVALID_WAND_MATERIAL)
                    .replace("<item>", item.type.name)
                    .replace("<allowed_material>", "Stick")
            )
            return
        }
        if(!item.isWand()) ItemUtils.turnIntoWand(item)
        val spellList = ItemUtils.getAvailableSpells(item)

        if(spellList.contains(spellType)) {
            player.sendMessage(messages.get(MessageKeys.SPELL_ALREADY_PRESENT))
            return
        }
        spellList.add(spellType)
        ItemUtils.setAvailableSpells(item, spellList)
        player.sendMessage(messages.get(MessageKeys.ADDED_SPELL_TO_WAND).replace("<spell>", spellType.displayName))
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

    override fun onCommandByConsole(sender: CommandSender, alias: String, strings: Array<String>) {
        sender.sendMessage(messages.get(MessageKeys.CONSOLE_CANNOT_USE))
    }

    override fun getCompletor(sender: CommandSender, length: Int, hint: String, strings: Array<String>): List<String> {
        if(sender !is Player) return emptyList()

        val list = when(length) {
            2 -> listOf("bind", "remove")
            3 -> SpellType.values().map { it.displayName }
            else -> emptyList()
        }
        return list.filter { it.startsWith(hint, ignoreCase = true) }
    }
}