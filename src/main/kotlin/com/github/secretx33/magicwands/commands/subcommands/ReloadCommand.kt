package com.github.secretx33.magicwands.commands.subcommands

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.manager.SpellManager
import com.github.secretx33.magicwands.utils.CustomKoinComponent
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
    private val messages by inject<Messages>()
    private val spellManager by inject<SpellManager>()

    override fun onCommandByPlayer(player: Player, alias: String, strings: Array<String>) {
        onCommandByConsole(player, alias, strings)
    }

    override fun onCommandByConsole(sender: CommandSender, alias: String, strings: Array<String>) {
        config.reload()
        messages.reload()
        spellManager.reload()
        sender.sendMessage(messages.get(MessageKeys.CONFIGS_RELOADED))
    }

    override fun getCompletor(sender: CommandSender, length: Int, hint: String, strings: Array<String>): List<String> {
        return emptyList()
    }
}
