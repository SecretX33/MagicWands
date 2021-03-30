package com.github.secretx33.magicwands.commands.subcommands

import com.github.secretx33.chestquest.commands.subcommands.SubCommand
import com.github.secretx33.magicwands.config.*
import com.github.secretx33.magicwands.manager.SpellManager
import com.github.secretx33.magicwands.utils.CustomKoinComponent
import com.github.secretx33.magicwands.utils.Utils.consoleMessage
import com.github.secretx33.magicwands.utils.inject
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class ReloadCommand : SubCommand(), CustomKoinComponent {

    override val name: String = "reload"
    override val permission: String = "reload"
    override val aliases: List<String> = listOf(name, "rel", "r")

    private val config by inject<Config>()
    private val spellManager by inject<SpellManager>()
    private val messages by inject<Messages>()

    override fun onCommandByPlayer(player: Player, strings: Array<String>) {
        onCommandByConsole(player, strings)
    }

    override fun onCommandByConsole(sender: CommandSender, strings: Array<String>) {
        config.reload()
        spellManager.reload()
        messages.reload()
        sender.sendMessage(messages.get(MessageKeys.CONFIGS_RELOADED))
        if(sender is Player) consoleMessage(messages.get(MessageKeys.CONFIGS_RELOADED))
    }

    override fun getCompletor(sender: CommandSender, length: Int, hint: String, strings: Array<String>): List<String> {
        return emptyList()
    }
}
