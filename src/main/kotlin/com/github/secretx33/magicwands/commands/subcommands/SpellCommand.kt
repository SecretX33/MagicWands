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

    override val name: String = "reload"
    override val permission: String = "reload"
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
            bindSpell(item, player)

    }

    private fun bindSpell(player: Player, item: ItemStack, spellType: SpellType) {
        if (!item.type.isWandMaterial()) {
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

    private fun removeSpell(item: ItemStack, player: Player) {
        if (!item.type.isWandMaterial()) {
            player.sendMessage(
                messages.get(MessageKeys.INVALID_WAND_MATERIAL)
                    .replace("<item>", item.type.name)
                    .replace("<allowed_material>", "Stick")
            )
            return
        }
        if (item.isWand()) {
            player.sendMessage(messages.get(MessageKeys.ITEM_IS_ALREADY_WAND))
            return
        }
    }

    override fun onCommandByConsole(sender: CommandSender, alias: String, strings: Array<String>) {
        sender.sendMessage(messages.get(MessageKeys.CONSOLE_CANNOT_USE))
    }

    override fun getCompletor(sender: CommandSender, length: Int, hint: String, strings: Array<String>): List<String> {
        if(sender !is Player) return emptyList()

        val list: List<String> = when(length) {
            2 -> listOf("bind", "remove")
            3 -> SpellType.values().map { it.name.toLowerCase(Locale.US).upperFirst() }
            else -> emptyList()
        }
        return list.filter { it.startsWith(hint, ignoreCase = true) }
    }
}