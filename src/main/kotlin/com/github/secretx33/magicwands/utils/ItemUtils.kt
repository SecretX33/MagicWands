package com.github.secretx33.magicwands.utils

import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.utils.ItemUtils.castCountKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

@KoinApiExtension
object ItemUtils: CustomKoinComponent {
    private val plugin: Plugin by inject()
    private val formatter = DecimalFormat("#,###")
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<SpellType>>() {}.type
    val castCountKey = NamespacedKey(plugin, "spell_cast_count")
    val selectedSpell = NamespacedKey(plugin, "selected_spell")
    val availableSpells = NamespacedKey(plugin, "available_spell")

    fun turnIntoWand(item: ItemStack) {
        require(item.type.isWandMaterial()) { "Item ${item.type} cannot be a wand" }
        val meta = item.itemMeta ?: throw IllegalArgumentException("Could not get itemMeta from ${item.type}")

        meta.apply {
            persistentDataContainer.set(castCountKey, PersistentDataType.LONG, 0)
            persistentDataContainer.set(availableSpells, PersistentDataType.TAG_CONTAINER, persistentDataContainer.adapterContext.newPersistentDataContainer())
            setDisplayName("${ChatColor.BOLD}${ChatColor.BLUE}Magic Wand")
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1)
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS)
            lore = makeWandLore(meta, getWandSpell(item))
        }
        item.itemMeta = meta
    }

    private fun makeWandLore(itemMeta: ItemMeta, selectedSpell: SpellType): List<String> {
        val casts = itemMeta.persistentDataContainer.getOrDefault(castCountKey, PersistentDataType.LONG, 0L)
        return listOf("${ChatColor.GREEN}A long, long time ago a magician were brutally killed, he said \"Do you think it's over? Oh, no, it's ${ChatColor.BOLD}just${ChatColor.GREEN} the beginning\". Right after saying that, he casted a powerful spell that send many of his wands to those who could get vengeance in his name.${ChatColor.RESET}",
        "",
        "Selected spell: ${ChatColor.BLUE}${selectedSpell.name.replace('_', ' ')}${ChatColor.RESET}",
        "",
        "${ChatColor.GREEN}This wand has casted ${formatter.format(casts)} spells.")
    }

    fun increaseCastCount(item: ItemStack) {
        require(item.isWand()) { "Item ${item.type} cannot be a wand" }
        val meta = item.itemMeta ?: throw IllegalArgumentException("Could not get itemMeta from ${item.type}")
        meta.persistentDataContainer.apply {
            val actualValue = get(castCountKey, PersistentDataType.LONG)!!
            set(castCountKey, PersistentDataType.LONG, actualValue + 1)
        }
        meta.lore = makeWandLore(meta, getWandSpell(item))
        item.itemMeta = meta
    }

    fun getWandSpell(wand: ItemStack): SpellType {
        require(wand.isWand()) { "Item passed as wand is not a wand" }
        val string = wand.itemMeta?.persistentDataContainer?.get(selectedSpell, PersistentDataType.STRING)
            ?: throw IllegalStateException("Wand has no selected enchant")
        return SpellType.valueOf(string)
    }

    fun getAvailableSpells(wand: ItemStack): MutableList<SpellType> {
        require(wand.isWand()) { "Item passed as wand is not a wand" }
        val spells = wand.itemMeta?.persistentDataContainer?.get(availableSpells, PersistentDataType.STRING)
            ?: throw IllegalStateException("Wand doesn't have any saved spells in it")
        return gson.fromJson(spells, typeToken)
    }

    fun setAvailableSpells(wand: ItemStack, list: List<SpellType>) {
        require(wand.isWand()) { "Item passed as wand is not a wand" }
        val meta = wand.itemMeta ?: throw IllegalStateException("This should not happen")

        meta.persistentDataContainer.set(selectedSpell, PersistentDataType.STRING, gson.toJson(list, typeToken))
        wand.itemMeta = meta
    }
}

fun Material.isWandMaterial() = this == Material.STICK || this == Material.BLAZE_ROD || this == Material.BONE

@KoinApiExtension
fun ItemStack?.isWand(): Boolean = this != null && (type == Material.STICK || type == Material.BLAZE_ROD || type == Material.BONE) && (itemMeta?.persistentDataContainer?.has(castCountKey, PersistentDataType.LONG) == true)