package com.github.secretx33.magicwands.commands.subcommands

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.utils.CustomKoinComponent
import com.github.secretx33.magicwands.utils.ItemUtils
import com.github.secretx33.magicwands.utils.inject
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class SetOwnerCommand : SubCommand(), CustomKoinComponent {

    override val name: String = "setowner"
    override val permission: String = "setowner"
    override val aliases: List<String> = listOf(name, "seto", "so")

    private val messages by inject<Messages>()

    override fun onCommandByPlayer(player: Player, alias: String, strings: Array<String>) {
        if(strings.size < 2) {
            player.sendMessage("${ChatColor.RED}Usage: /$alias $name <playername>")
            return
        }
        val item = player.inventory.itemInMainHand
        // player not holding a wand
        if(!item.isWand()) {
            player.sendMessage(messages.get(MessageKeys.NOT_HOLDING_A_WAND))
            return
        }

        val newOwner = Bukkit.getPlayerExact(strings[1]) ?: run {
            player.sendMessage(messages.get(MessageKeys.PLAYER_NOT_FOUND).replace("<player>", strings[1]))
            return
        }

        ItemUtils.setWandOwner(newOwner, item)
        player.sendMessage(messages.get(MessageKeys.CHANGED_WAND_OWNER).replace("<player>", newOwner.name))
    }

    override fun onCommandByConsole(sender: CommandSender, alias: String, strings: Array<String>) {
        sender.sendMessage(messages.get(MessageKeys.CONSOLE_CANNOT_USE))
    }

    override fun getCompletor(sender: CommandSender, length: Int, hint: String, strings: Array<String>): List<String> {
        if(sender !is Player || length != 2) return emptyList()

        return Bukkit.getOnlinePlayers().asSequence()
            .filter { it.name.startsWith(hint, ignoreCase = true) }
            .map { it.name }
            .toList()
    }
}