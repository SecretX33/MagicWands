package com.github.secretx33.magicwands.commands.subcommands

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.manager.LearnedSpellsManager
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.model.WandSkin
import com.github.secretx33.magicwands.utils.*
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class BindSpellCommand : SubCommand(), CustomKoinComponent {

    override val name: String = "bindspell"
    override val permission: String = "bindspell"
    override val aliases: List<String> = listOf(name, "binds", "bs")

    private val learnedSpells by inject<LearnedSpellsManager>()
    private val messages by inject<Messages>()

    override fun onCommandByPlayer(player: Player, alias: String, strings: Array<String>) {
        val item = player.inventory.itemInMainHand

        if(strings.size < 2) {
            player.sendMessage("${ChatColor.RED}Usage: /${alias} $name <spell>")
            return
        }
        val spellType = SpellType.ofOrNull(strings[1]) ?: run {
            player.sendMessage(messages.get(MessageKeys.SPELL_DOESNT_EXIST).replace("<spell>", strings[1]))
            return
        }
        if(!learnedSpells.knows(player, spellType)) {
            player.sendMessage(messages.get(MessageKeys.CANNOT_BIND_UNKNOWN_SPELL))
            return
        }
        bindSpell(player, item, spellType)
    }

    private fun bindSpell(player: Player, item: ItemStack, spellType: SpellType) {
        if (!item.isWandMaterial()) {
            val allowedMaterials = WandSkin.values().filter { player.hasPermission(it.permission) }.joinToString { it.displayName }
            player.sendMessage(messages.get(MessageKeys.INVALID_WAND_MATERIAL)
                .replace("<item>", item.formattedTypeName())
                .replace("<allowed_material>", allowedMaterials))
            return
        }
        if(item.amount > 1) {
            player.sendMessage(messages.get(MessageKeys.CANNOT_BIND_SPELLS_TO_MULTIPLE_ITEMS))
            return
        }
        val skin = WandSkin.of(item.type)
        if(!player.hasPermission(skin.permission)) {
            player.sendMessage(messages.get(MessageKeys.HAVENT_BOUGHT_THIS_MATERIAL_SKIN).replace("<item>", skin.displayName))
            return
        }
        if(!item.isWand()) ItemUtils.turnIntoWand(item, player)

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
        if(sender !is Player || length != 2) return emptyList()

        val spells = SpellType.values().map { it.displayName } as MutableList
        val item = sender.inventory.itemInMainHand
        val knownSpells = learnedSpells.getKnownSpells(sender).map { it.displayName }
        if(item.isWand()) {
            spells.removeAll(ItemUtils.getAvailableSpells(item).map { it.displayName })
            if(spells.isEmpty() && hint.isBlank())
                spells.add(messages.get(MessageKeys.TAB_COMPLETION_WAND_HAS_ALL_SPELLS))
        }
        return spells.filter { knownSpells.contains(it) && it.startsWith(hint, ignoreCase = true) }.sorted()
    }
}