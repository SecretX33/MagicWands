package com.github.secretx33.magicwands.commands.subcommands

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.utils.CustomKoinComponent
import com.github.secretx33.magicwands.utils.inject
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.koin.core.component.KoinApiExtension
import java.util.*

@KoinApiExtension
class EffectCommand : SubCommand(), CustomKoinComponent {

    override val name: String = "effect"
    override val permission: String = "toggleeffect"
    override val aliases: List<String> = listOf(name)

    private val messages by inject<Messages>()
    private val config by inject<Config>()

    override fun onCommandByPlayer(player: Player, alias: String, strings: Array<String>) {
        onCommandByConsole(player, alias, strings)
    }

    override fun onCommandByConsole(sender: CommandSender, alias: String, strings: Array<String>) {
        if(strings.size < 2) {
            sender.sendMessage("${ChatColor.RED}Usage: /$alias $name <toggle|enable|disable>")
            return
        }
        val sub = strings[1].toLowerCase(Locale.US)

        when(sub){
            "toggle" -> {
                val current = config.get<Boolean>(ConfigKeys.ENABLE_EFFECTS)
                toggleEffects(!current, sender)
            }
            "enable" -> toggleEffects(true, sender)
            "disable" -> toggleEffects(false, sender)
            else -> {
                sender.sendMessage(messages.get(MessageKeys.COMMAND_PARAMETER_IS_INVALID)
                    .replace("<parameter>", strings[1]))
                return
            }
        }
    }

    private fun toggleEffects(newState: Boolean, sender: CommandSender) {
        sender.sendMessage(messages.get(MessageKeys.TOGGLED_SPELL_EFFECT).replace("<state>", newState.asOnOff()))
        config.set(ConfigKeys.ENABLE_EFFECTS, newState)
        config.save()
    }

    private fun Boolean.asOnOff() = if(this) "ON" else "OFF"

    override fun getCompletor(sender: CommandSender, length: Int, hint: String, strings: Array<String>): List<String> {
        if(length != 2) return emptyList()

        return listOf("toggle", "enable", "disable").filter { it.startsWith(hint, ignoreCase = true) }
    }

}
