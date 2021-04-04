package com.github.secretx33.magicwands.config

import com.github.secretx33.magicwands.utils.YamlManager
import org.bukkit.ChatColor
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Messages(plugin: Plugin) {
    private val manager = YamlManager(plugin, "messages/messages")
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
    SUCCESSFULLY_PURCHASED_SPELL("${ChatColor.GREEN}You have purchased the spell <spell>, your new balance is <balance>."),
    TRANSACTION_FAILED("${ChatColor.RED}We could not validate your purchase: <error>"),
    NOT_ENOUGH_MONEY("${ChatColor.RED}You don't have enough money to buy <spell>, it costs <price> but only got <balance>!"),
    CANNOT_PURCHASE_ALREADY_KNOW("${ChatColor.RED}You cannot purchase this because you already know <spell>!"),
    BROKE_SPELLTEACHER("${ChatColor.RED}You just broke a Spellteacher!"),
    BLOCK_IS_NOW_SPELLTEACHER("${ChatColor.GREEN}Block at <world> <x> <y> <z> is now a spellteacher for spell <type>."),
    REPLACED_SPELLTEACHER_SPELL("${ChatColor.GREEN}Block at <world> <x> <y> <z> is now a spellteacher for spell <type> (previously it was spellteacher of <previous_spell>)."),
    SPELLTEACHER_IS_ALREADY_THIS_TYPE("${ChatColor.RED}Spellteacher is already type <type>."),
    CANNOT_TRANSFORM_AIR_IN_SPELLTEACHER("${ChatColor.RED}Cannot transform Air into Spellteacher."),
    WAND_LORE(""),
    CANNOT_BIND_UNKNOWN_SPELL("${ChatColor.RED}You cannot bind spells you don't know."),
    CANNOT_CAST_UNKNOWN_SPELL("${ChatColor.RED}You cannot use spell <spell> because you don't know it yet."),
    PLAYER_NOT_FOUND("${ChatColor.RED}Player <player> was not found, you may only use online players for this command."),
    COMMAND_PARAMETER_IS_INVALID("${ChatColor.RED}Command parameter <parameter> is invalid."),
    HAVENT_BOUGHT_THIS_MATERIAL_SKIN("${ChatColor.RED}You may not turn <item> into a wand, you haven't bought it's skin yet."),
    CANNOT_USE_WAND_TO_CRAFT("${ChatColor.RED}You may not use a wand to craft items."),
    GOT_POISONED("${ChatColor.GREEN}You have been poisoned by <caster>."),
    GOT_SLOWED("${ChatColor.GREEN}You have been slowed by <caster>."),
    POISONED_TARGET("${ChatColor.GREEN}You have poisoned <target>."),
    SLOWED_TARGET("${ChatColor.GREEN}You have slowed <target>."),
    ADDED_SPELL_TO_WAND("${ChatColor.GREEN}Successfully added <spell> to your wand."),
    CANNOT_BLINK_TO_THERE("Sorry, you cannot blink to there."),
    CASTED_VANISH("You vanished!"),
    CHANGED_WAND_OWNER("${ChatColor.GREEN}Wand's new owner is now ${ChatColor.GOLD}<player>!"),
    CANNOT_USE_ANOTHERS_WAND("${ChatColor.RED}The wand is your hand is not yours! It's owned by <owner>, you cannot use it!"),
    TAB_COMPLETION_WAND_HAS_ALL_SPELLS("<wand_already_has_all_spells>"),
    TAB_COMPLETION_WAND_HAS_NO_SPELLS("<wand_has_no_spells>"),
    TAB_COMPLETION_NOT_HOLDING_WAND("<not_holding_wand>"),
    NOT_HOLDING_A_WAND("${ChatColor.RED}The item in your hand is not a wand."),
    INVALID_SKIN_NAME("${ChatColor.RED}There is no skin named <skin>, please try again."),
    WAND_SKIN_NOT_BOUGHT("${ChatColor.RED}Seems like you haven't bought out amazing skin <skin> yet :("),
    CONFIGS_RELOADED("Reloaded configs."),
    WAND_SKIN_IS_ALREADY_THAT("${ChatColor.RED}Your wand is already using skin <skin>."),
    CANNOT_BIND_SPELLS_TO_MULTIPLE_ITEMS("${ChatColor.RED}You may only bind spells to one item at a time."),
    CONSOLE_CANNOT_USE("${ChatColor.RED}Sorry, the console cannot use this command."),
    INVALID_WAND_MATERIAL("${ChatColor.RED}<item> cannot be an wand, please use a <allowed_material>."),
    ITEM_NOT_A_WAND("${ChatColor.RED}This item is not a wand!"),
    NOT_ENOUGH_FUEL("You don't have enough fuel to cast this spell."),
    SWITCHED_SPELL("${ChatColor.BLUE}Changed active spell to <spell>."),
    SUCCESSFULLY_CHANGED_WAND_SKIN("${ChatColor.GREEN}Successfully set your wand's skin to <skin>"),
    REMOVED_SPELL_OF_WAND("${ChatColor.GREEN}Successfully removed <spell> of your wand."),
    SPELL_ALREADY_PRESENT("${ChatColor.RED}This spell already exist in your wand."),
    LEARNED_SPELL("${ChatColor.GREEN}You learned spell <spell>!"),
    FORGOT_SPELL("${ChatColor.GREEN}You forgot spell <spell>!"),
    YOU_DONT_KNOW_THIS_SPELL("${ChatColor.RED}You cannot forget a spell you don't know."),
    YOU_ALREADY_KNOW_THIS_SPELL("${ChatColor.RED}You already already know <spell>."),
    SPELL_DOESNT_EXIST("${ChatColor.RED}Spell <spell> doesn't exist, please type a valid one."),
    SPELL_IN_COOLDOWN("${ChatColor.GOLD}<spell> is in cooldown right now, <cooldown> seconds remaining."),
    SPELL_NOT_PRESENT("${ChatColor.RED}This wand doesn't have <spell> bound to it.");

    val configEntry = name.toLowerCase(Locale.US).replace('_','-')
}
