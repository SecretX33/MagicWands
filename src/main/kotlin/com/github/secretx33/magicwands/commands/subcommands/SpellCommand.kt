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
import kotlin.collections.ArrayList

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

        if(sub == "bind")
            bindSpell(item, player)
        else if(sub == "remove")
            bindSpell(item, player)

    }

    private fun bindSpell(item: ItemStack, player: Player) {
        if (!item.type.isWandMaterial()) {
            player.sendMessage(
                messages.get(MessageKeys.INVALID_WAND_MATERIAL)
                    .replace("<item>", item.type.name)
                    .replace("<allowed_material>", "Stick")
            )
            return
        }
        if(!item.isWand()) ItemUtils.turnIntoWand(item)


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
            player.sendMessage(messages.get(MessageKeys.ITEM_IS_ALREARY_WAND))
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