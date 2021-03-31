package com.github.secretx33.magicwands.commands.subcommands

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.model.WandSkin
import com.github.secretx33.magicwands.utils.*
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class ChangeSkinCommand : SubCommand(), CustomKoinComponent {

    override val name: String = "changeskin"
    override val permission: String = "skins"
    override val aliases: List<String> = listOf(name, "rel", "r")

    private val messages by inject<Messages>()

    override fun onCommandByPlayer(player: Player, alias: String, strings: Array<String>) {
        if(strings.size < 2) {
            player.sendMessage("${ChatColor.RED}Usage: /$alias $name <skinname>")
            return
        }
        val item = player.inventory.itemInMainHand
        val skinName = strings.sliceArray(IntRange(1, strings.lastIndex)).joinToString(separator = " ")

        // player not holding a wand
        if(!item.isWand()) {
            player.sendMessage(messages.get(MessageKeys.NOT_HOLDING_A_WAND))
            return
        }
        val skin = WandSkin.of(skinName)

        // skin name doesn't exist
        if(skin == null) {
            player.sendMessage(messages.get(MessageKeys.INVALID_SKIN_NAME).replace("<skin>", skinName))
            return
        }

        // player haven't bought skin yet
        if(!player.hasPermission(skin.permission)) {
            println("Permission is ${skin.permission}")
            player.sendMessage(messages.get(MessageKeys.WAND_SKIN_NOT_BOUGHT).replace("<skin>", skin.displayName))
            return
        }

        // player wand is already using the skin asked
        if(item.type == skin.material) {
            player.sendMessage(messages.get(MessageKeys.WAND_SKIN_IS_ALREADY_THAT).replace("<skin>", skin.displayName))
            return
        }

        val newWand = ItemUtils.changeSkin(item, skin)
        player.inventory.setItemInMainHand(newWand)
        player.updateInventory()
        player.sendMessage(messages.get(MessageKeys.SUCCESSFULLY_CHANGED_WAND_SKIN).replace("<skin>", skin.displayName))
    }

    override fun onCommandByConsole(sender: CommandSender, alias: String, strings: Array<String>) {
        sender.sendMessage(messages.get(MessageKeys.CONSOLE_CANNOT_USE))
    }

    override fun getCompletor(sender: CommandSender, length: Int, hint: String, strings: Array<String>): List<String> {
        if(sender !is Player) return emptyList()
        val list = if(length == 2) WandSkin.values().map { it.displayName } else emptyList()

        return list.filter { it.startsWith(hint, ignoreCase = true) }
    }
}