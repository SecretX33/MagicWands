package com.github.secretx33.magicwands.commands.subcommands

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.manager.LearnedSpellsManager
import com.github.secretx33.magicwands.manager.SpellManager
import com.github.secretx33.magicwands.manager.SpellTeacherManager
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
    private val learnedSpells by inject<LearnedSpellsManager>()
    private val spellTeacher by inject<SpellTeacherManager>()
    private val messages by inject<Messages>()

    override fun onCommandByPlayer(player: Player, alias: String, strings: Array<String>) {
        onCommandByConsole(player, alias, strings)
    }

    override fun onCommandByConsole(sender: CommandSender, alias: String, strings: Array<String>) {
        config.reload()
        spellManager.reload()
        learnedSpells.reload()
        spellTeacher.reload()
        messages.reload()
        sender.sendMessage(messages.get(MessageKeys.CONFIGS_RELOADED))
    }

    override fun getCompletor(sender: CommandSender, length: Int, hint: String, strings: Array<String>): List<String> {
        return emptyList()
    }
}
