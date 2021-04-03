package com.github.secretx33.magicwands.commands.subcommands

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.manager.SpellTeacherManager
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.utils.CustomKoinComponent
import com.github.secretx33.magicwands.utils.inject
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class SpellTeacherCommand : SubCommand(), CustomKoinComponent {

    override val name: String = "spellteacher"
    override val permission: String = "spellteacher"
    override val aliases: List<String> = listOf(name, "teacher", "spellt", "st")

    private val messages by inject<Messages>()
    private val spellTeacher by inject<SpellTeacherManager>()

    override fun onCommandByPlayer(player: Player, alias: String, strings: Array<String>) {
        if(strings.size < 2) {
            player.sendMessage("${ChatColor.RED}Usage: /$alias $name <playername>")
            return
        }
        val spellType = SpellType.ofOrNull(strings[1]) ?: run {
            player.sendMessage(messages.get(MessageKeys.SPELL_DOESNT_EXIST).replace("<spell>", strings[1]))
            return
        }

        val block = player.getTargetBlock(setOf(Material.AIR, Material.GRASS, Material.TALL_GRASS), 15)
        println("Block is $block")
        if(block.type.isAir) {
            player.sendMessage(messages.get(MessageKeys.CANNOT_TRANSFORM_AIR_IN_SPELLTEACHER))
            return
        }
        if(!spellTeacher.isSpellTeacher(block.location))



    }

    override fun onCommandByConsole(sender: CommandSender, alias: String, strings: Array<String>) {
        sender.sendMessage(messages.get(MessageKeys.CONSOLE_CANNOT_USE))
    }

    override fun getCompletor(sender: CommandSender, length: Int, hint: String, strings: Array<String>): List<String> {
        if(sender !is Player || length != 2) return emptyList()

        return SpellType.values()
            .filter { it.displayName.startsWith(hint, ignoreCase = true) }
            .map { it.displayName }
    }
}