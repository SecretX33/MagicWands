package com.github.secretx33.magicwands.commands.subcommands

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.repositories.LearnedSpellsRepo
import com.github.secretx33.magicwands.utils.CustomKoinComponent
import com.github.secretx33.magicwands.utils.inject
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class ForgetCommand : SubCommand(), CustomKoinComponent {

    override val name: String = "forget"
    override val permission: String = "forget"
    override val aliases: List<String> = listOf(name, "for", "f")

    private val messages by inject<Messages>()
    private val learnedSpells by inject<LearnedSpellsRepo>()

    override fun onCommandByPlayer(player: Player, alias: String, strings: Array<String>) {
        if(strings.size < 2) {
            player.sendMessage("${ChatColor.RED}Usage: /$alias $name <spell>")
            return
        }
        val spellType = SpellType.ofOrNull(strings[1]) ?: run {
            player.sendMessage(messages.get(MessageKeys.SPELL_DOESNT_EXIST).replace("<spell>", strings[1]))
            return
        }
        // player don't know the spell he's trying to forget
        if(!learnedSpells.knows(player.uniqueId, spellType)) {
            player.sendMessage(messages.get(MessageKeys.YOU_DONT_KNOW_THIS_SPELL))
            return
        }
        learnedSpells.forgetSpell(player.uniqueId, spellType)
        player.sendMessage(messages.get(MessageKeys.FORGOT_SPELL).replace("<spell>", spellType.displayName))
    }

    override fun onCommandByConsole(sender: CommandSender, alias: String, strings: Array<String>) {
        sender.sendMessage(messages.get(MessageKeys.CONSOLE_CANNOT_USE))
    }

    override fun getCompletor(sender: CommandSender, length: Int, hint: String, strings: Array<String>): List<String> {
        if(sender !is Player || length != 2) return emptyList()

        val knownSpells = learnedSpells.getKnownSpells(sender.uniqueId)
        return SpellType.values().filter { knownSpells.contains(it) && it.displayName.startsWith(hint, ignoreCase = true) }.map { it.displayName }
    }
}
