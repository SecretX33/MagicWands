package com.github.secretx33.magicwands.commands.subcommands

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.model.WandSkin
import com.github.secretx33.magicwands.utils.*
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class SpellbindCommand : SubCommand(), CustomKoinComponent {

    override val name: String = "spellbind"
    override val permission: String = "spellbind"
    override val aliases: List<String> = listOf(name, "spellb", "sb")

    private val messages by inject<Messages>()

    override fun onCommandByPlayer(player: Player, alias: String, strings: Array<String>) {
        val item = player.inventory.itemInMainHand

        if(strings.size < 2) {
            player.sendMessage("${ChatColor.RED}Usage: /${alias} $name <spell>")
            return
        }
        val spellType = runCatching { SpellType.of(strings[1]) }.getOrElse {
            player.sendMessage(messages.get(MessageKeys.SPELL_DOESNT_EXIST).replace("<spell>", strings[1]))
            return
        }
        bindSpell(player, item, spellType)
    }

    private fun bindSpell(player: Player, item: ItemStack, spellType: SpellType) {
        if (!item.isWandMaterial()) {
            player.sendMessage(messages.get(MessageKeys.INVALID_WAND_MATERIAL)
                .replace("<item>", item.formattedTypeName())
                .replace("<allowed_material>", "Stick"))
            return
        }
        val skin = WandSkin.of(item.type)
        if(!player.hasPermission(skin.permission)) {
            player.sendMessage(messages.get(MessageKeys.HAVENT_BOUGHT_THIS_MATERIAL_SKIN).replace("<item>", skin.displayName))
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

    override fun onCommandByConsole(sender: CommandSender, alias: String, strings: Array<String>) {
        sender.sendMessage(messages.get(MessageKeys.CONSOLE_CANNOT_USE))
    }

    override fun getCompletor(sender: CommandSender, length: Int, hint: String, strings: Array<String>): List<String> {
        if(sender !is Player) return emptyList()

        return when(length) {
            2 -> {
                val item = sender.inventory.itemInMainHand
                val spells = SpellType.values().toMutableList()
                if(item.isWand())
                    spells.removeAll(ItemUtils.getAvailableSpells(item))
                return spells.map { it.displayName }.filter { it.startsWith(hint, ignoreCase = true) }.sorted()
            }
            else -> emptyList()
        }
    }
}