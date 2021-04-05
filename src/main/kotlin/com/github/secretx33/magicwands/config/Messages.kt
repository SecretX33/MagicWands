package com.github.secretx33.magicwands.config

import com.github.secretx33.magicwands.utils.YamlManager
import org.bukkit.ChatColor
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Messages(plugin: Plugin) {
    private val manager = YamlManager(plugin, "messages")
    private val stringCache = ConcurrentHashMap<MessageKeys, String>()
    private val listCache = ConcurrentHashMap<MessageKeys, List<String>>()

    fun get(key: MessageKeys, default: String? = null): String {
        return stringCache.getOrPut(key) {
            manager.getString(key.configEntry)?.correctColorCodes() ?: default ?: key.default
        }
    }

    fun getList(key: MessageKeys): List<String> {
        return listCache.getOrPut(key) {
            manager.getStringList(key.configEntry).map { it.correctColorCodes() }
        }
    }


    fun reload() {
        stringCache.clear()
        listCache.clear()
        manager.reload()
    }

    private fun String.correctColorCodes(): String = ChatColor.translateAlternateColorCodes('&', this)
}

enum class MessageKeys(val default: String) {
    ADDED_SPELL_TO_WAND("${ChatColor.GREEN}Successfully added <spell> to your wand."),
    BLOCK_IS_NOW_SPELLTEACHER("${ChatColor.GREEN}Block at <world> <x> <y> <z> is now a spellteacher for spell <type>."),
    BROKE_SPELLTEACHER("${ChatColor.RED}You just broke a Spellteacher!"),
    CANNOT_BIND_SPELLS_TO_MULTIPLE_ITEMS("${ChatColor.RED}You may only bind spells to one item at a time."),
    CANNOT_BIND_UNKNOWN_SPELL("${ChatColor.RED}You cannot bind spells you don't know."),
    CANNOT_BLINK_TO_THERE("Sorry, you cannot blink to there."),
    CANNOT_CAST_UNKNOWN_SPELL("${ChatColor.RED}You cannot use spell <spell> because you don't know it yet."),
    CANNOT_PURCHASE_ALREADY_KNOW("${ChatColor.RED}You cannot purchase this because you already know <spell>!"),
    CANNOT_TRANSFORM_AIR_IN_SPELLTEACHER("${ChatColor.RED}Cannot transform Air into Spellteacher."),
    CANNOT_USE_ANOTHERS_WAND("${ChatColor.RED}The wand is your hand is not yours! It's owned by <owner>, you cannot use it!"),
    CANNOT_USE_WAND_TO_CRAFT("${ChatColor.RED}You may not use a wand to craft items."),
    CASTED_VANISH("You vanished!"),
    CHANGED_WAND_OWNER("${ChatColor.GREEN}Wand's new owner is now ${ChatColor.GOLD}<player>!"),
    COMMAND_PARAMETER_IS_INVALID("${ChatColor.RED}Command parameter <parameter> is invalid."),
    CONFIGS_RELOADED("Reloaded configs."),
    CONSOLE_CANNOT_USE("${ChatColor.RED}Sorry, the console cannot use this command."),
    FORGOT_SPELL("${ChatColor.GREEN}You forgot spell <spell>!"),
    GOT_POISONED("${ChatColor.GREEN}You have been poisoned by <caster>."),
    GOT_SLOWED("${ChatColor.GREEN}You have been slowed by <caster>."),
    HAVENT_BOUGHT_THIS_MATERIAL_SKIN("${ChatColor.RED}You may not turn <item> into a wand, you haven't bought it's skin yet."),
    INVALID_SKIN_NAME("${ChatColor.RED}There is no skin named <skin>, please try again."),
    INVALID_WAND_MATERIAL("${ChatColor.RED}<item> cannot be an wand, please use a <allowed_material>."),
    ITEM_NOT_A_WAND("${ChatColor.RED}This item is not a wand!"),
    LEARNED_SPELL("${ChatColor.GREEN}You learned spell <spell>!"),
    NOT_ENOUGH_FUEL("You don't have enough fuel to cast this spell."),
    NOT_ENOUGH_MONEY("${ChatColor.RED}You don't have enough money to buy <spell>, it costs <price> but only got <balance>!"),
    NOT_HOLDING_A_WAND("${ChatColor.RED}The item in your hand is not a wand."),
    PLAYER_NOT_FOUND("${ChatColor.RED}Player <player> was not found, you may only use online players for this command."),
    POISONED_TARGET("${ChatColor.GREEN}You have poisoned <target>."),
    REMOVED_SPELL_OF_WAND("${ChatColor.GREEN}Successfully removed <spell> of your wand."),
    REPLACED_SPELLTEACHER_SPELL("${ChatColor.GREEN}Block at <world> <x> <y> <z> is now a spellteacher for spell <type> (previously it was spellteacher of <previous_spell>)."),
    SLOWED_TARGET("${ChatColor.GREEN}You have slowed <target>."),
    SPELL_ALREADY_PRESENT("${ChatColor.RED}This spell already exist in your wand."),
    SPELL_DOESNT_EXIST("${ChatColor.RED}Spell <spell> doesn't exist, please type a valid one."),
    SPELL_IN_COOLDOWN("${ChatColor.GOLD}<spell> is in cooldown right now, <cooldown> seconds remaining."),
    SPELL_NOT_PRESENT("${ChatColor.RED}This wand doesn't have <spell> bound to it."),
    SPELLTEACHER_IS_ALREADY_THIS_TYPE("${ChatColor.RED}Spellteacher is already type <type>."),
    SUCCESSFULLY_CHANGED_WAND_SKIN("${ChatColor.GREEN}Successfully set your wand's skin to <skin>"),
    SUCCESSFULLY_PURCHASED_SPELL("${ChatColor.GREEN}You have used <price> to purchase the spell <spell>, your new balance is <balance>."),
    SWITCHED_SPELL("${ChatColor.BLUE}Changed active spell to <spell>."),
    TAB_COMPLETION_NOT_HOLDING_WAND("<not_holding_wand>"),
    TAB_COMPLETION_WAND_HAS_ALL_SPELLS("<wand_already_has_all_spells>"),
    TAB_COMPLETION_WAND_HAS_NO_SPELLS("<wand_has_no_spells>"),
    TOGGLED_SPELL_EFFECT("${ChatColor.GREEN}Spell effects are now ${ChatColor.GOLD}<state>${ChatColor.GREEN}."),
    TRANSACTION_FAILED("${ChatColor.RED}We could not validate your purchase: <error>"),
    WAND_LORE(""),
    WAND_SKIN_IS_ALREADY_THAT("${ChatColor.RED}Your wand is already using skin <skin>."),
    WAND_SKIN_NOT_BOUGHT("${ChatColor.RED}Seems like you haven't bought out amazing skin <skin> yet :("),
    YOU_ALREADY_KNOW_THIS_SPELL("${ChatColor.RED}You already already know <spell>."),
    YOU_DONT_KNOW_THIS_SPELL("${ChatColor.RED}You cannot forget a spell you don't know.");

    val configEntry = name.toLowerCase(Locale.US).replace('_','-')
}
