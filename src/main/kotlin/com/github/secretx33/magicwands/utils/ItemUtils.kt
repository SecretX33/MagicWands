package com.github.secretx33.magicwands.utils

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.model.WandSkin
import com.github.secretx33.magicwands.utils.ItemUtils.castCountKey
import com.github.secretx33.magicwands.utils.ItemUtils.ownerUuidKey
import com.github.secretx33.magicwands.utils.Utils.consoleMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.*
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension
import java.text.DecimalFormat
import java.util.*

@KoinApiExtension
object ItemUtils: CustomKoinComponent {
    private val plugin: Plugin by inject()
    private val messages: Messages by inject()
    private val formatter = DecimalFormat("#,###")
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<SpellType>>() {}.type

    val castCountKey = NamespacedKey(plugin, "spell_cast_count")
    private val selectedSpell = NamespacedKey(plugin, "selected_spell")
    private val availableSpells = NamespacedKey(plugin, "available_spell")
    private val ownerNameKey = NamespacedKey(plugin, "wand_owner")
    val ownerUuidKey = NamespacedKey(plugin, "wand_owner")

    fun turnIntoWand(item: ItemStack, player: Player) {
        require(item.isWandMaterial()) { "Item ${item.type} cannot be a wand" }
        val meta = item.itemMeta ?: throw IllegalArgumentException("Could not get itemMeta from ${item.type}")

        meta.apply {
            persistentDataContainer.set(ownerNameKey, PersistentDataType.STRING, player.name)
            persistentDataContainer.set(ownerUuidKey, PersistentDataType.STRING, player.uniqueId.toString())
            persistentDataContainer.set(castCountKey, PersistentDataType.LONG, 0)
            persistentDataContainer.set(selectedSpell, PersistentDataType.STRING, "")
            persistentDataContainer.set(availableSpells, PersistentDataType.STRING, "[]")
            setDisplayName("${ChatColor.BLUE}${ChatColor.BOLD}Magic Wand ${WandSkin.of(item.type).wandComplement}")
            addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON)
            updateLore(null)
        }
        item.itemMeta = meta
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 1)
    }

    private fun makeWandLore(itemMeta: ItemMeta, selectedSpell: SpellType?): List<String> {
        val selected = selectedSpell?.displayName?.toUpperCase(Locale.US) ?: "${ChatColor.RESET}${ChatColor.GRAY}<none>"
        val availableSpells = getAvailableSpells(itemMeta).map { it.displayName.toUpperCase(Locale.US) }.sorted()
        val casts = itemMeta.persistentDataContainer.getOrDefault(castCountKey, PersistentDataType.LONG, 0L)
        val owner = getWandOwnerName(itemMeta)

        val lore = messages.getList(MessageKeys.WAND_LORE).map {
            it.replace("<selected_spell>", selected)
                .replace("<casts>", formatter.format(casts))
                .replace("<owner>", owner)
        } as MutableList

        println("wand lore read from messages is $lore")

        val maxLength = lore.map { it.length }.maxOrNull() ?: return emptyList()
        val tagIndex = lore.indexOfFirst { it.contains("<available_spells>") }
        // there is no <available_spells> tag in the wand lore
        if(tagIndex < 0) return lore
        // wand has no spell available
        if(availableSpells.isEmpty()) {
            lore[tagIndex] = lore[tagIndex].replace("<available_spells>", "${ChatColor.RESET}${ChatColor.GRAY}<none>")
            return lore
        }

        var currentLine = tagIndex
        // remove tag
        lore[tagIndex] = lore[tagIndex].replace("<available_spells>", "")

        availableSpells.forEachIndexed { i, spell ->
            if(lore[currentLine].length > maxLength) currentLine++
            if(i > 0 && lore[currentLine].length + spell.length + 2 > maxLength) {
                lore[currentLine] += ","
//                lore.add("")
                currentLine++
            }
            if(currentLine == tagIndex) {
                println("1. for $spell")
                if (i > 0) lore[currentLine] += ", "
                lore[currentLine] += spell
            } else {
                if(currentLine > lore.lastIndex || lore[currentLine].length + spell.length + 2 > maxLength) {
                    println("2. for $spell")
                    if(currentLine > lore.lastIndex) lore.add(spell)
                } else {
                    println("3. for $spell")
                    lore[currentLine] += ", "
                    lore[currentLine] += spell
                }
            }
        }
        return lore
    }

    fun increaseCastCount(item: ItemStack) {
        require(item.isWand()) { "Item ${item.type} is not a wand" }
        val meta = item.itemMeta ?: throw IllegalArgumentException("Could not get itemMeta from ${item.type}")
        meta.persistentDataContainer.apply {
            val actualValue = get(castCountKey, PersistentDataType.LONG)!!
            set(castCountKey, PersistentDataType.LONG, actualValue + 1)
        }
        meta.updateLore(getWandSpell(item))
        item.itemMeta = meta
    }

    fun changeSkin(wand: ItemStack, skin: WandSkin, player: Player): ItemStack {
        require(wand.isWand()) { "Item ${wand.type} is not a wand" }
        val newWand = ItemStack(skin.material).apply { turnIntoWand(this, player) }
        copyPluginKeys(wand, newWand)
        return newWand
    }

    private fun copyPluginKeys(old: ItemStack, new: ItemStack) {
        val oldMeta = old.itemMeta ?: throw IllegalStateException("itemMeta of oldWand came null")
        val newMeta = new.itemMeta ?: throw IllegalStateException("itemMeta of newWand came null")

        val selected: String?
        oldMeta.persistentDataContainer.run {
            val ownerUuid = get(ownerUuidKey, PersistentDataType.STRING)!!
            val ownerName = Bukkit.getPlayer(ownerUuid.toUuid())?.name ?: get(ownerNameKey, PersistentDataType.STRING)!!
            newMeta.persistentDataContainer.set(ownerNameKey, PersistentDataType.STRING, ownerName)
            newMeta.persistentDataContainer.set(ownerUuidKey, PersistentDataType.STRING, ownerUuid)
            newMeta.persistentDataContainer.set(castCountKey, PersistentDataType.LONG, get(castCountKey, PersistentDataType.LONG)!!)
            selected = get(selectedSpell, PersistentDataType.STRING)
            if(selected != null)
                newMeta.persistentDataContainer.set(selectedSpell, PersistentDataType.STRING, selected)
            else
                newMeta.persistentDataContainer.remove(selectedSpell) // removes the key so the selected spell will be null
            newMeta.persistentDataContainer.set(availableSpells, PersistentDataType.STRING, get(availableSpells, PersistentDataType.STRING)!!)
        }
        newMeta.updateLore(SpellType.ofOrNull(selected))
        new.itemMeta = newMeta
    }

    fun getWandSpell(wand: ItemStack): SpellType {
        require(wand.isWand()) { "Item ${wand.type} is not a wand" }
        val string = wand.itemMeta?.persistentDataContainer?.get(selectedSpell, PersistentDataType.STRING)
            ?: throw IllegalStateException("Wand has no selected enchant")
        return SpellType.valueOf(string)
    }

    fun getWandSpellOrNull(wand: ItemStack): SpellType? {
        require(wand.isWand()) { "Item ${wand.type} is not a wand" }
        val string = wand.itemMeta?.persistentDataContainer?.get(selectedSpell, PersistentDataType.STRING)
        return SpellType.ofOrNull(string)
    }

    fun setWandSpell(wand: ItemStack, type: SpellType) {
        require(wand.isWand()) { "Item ${wand.type} is not a wand" }
        val meta = wand.itemMeta ?: throw IllegalStateException("This should not happen")

        meta.apply {
            persistentDataContainer.set(selectedSpell, PersistentDataType.STRING, type.name)
            lore = makeWandLore(meta, type)
        }
        wand.itemMeta = meta
    }

    private fun getAvailableSpells(itemMeta: ItemMeta): MutableList<SpellType> {
        val spells = itemMeta.persistentDataContainer.get(availableSpells, PersistentDataType.STRING)
            ?: throw IllegalStateException("Wand doesn't have any saved spells in it")
        return gson.fromJson(spells, typeToken)
    }

    fun getAvailableSpells(wand: ItemStack): MutableList<SpellType> {
        require(wand.isWand()) { "Item ${wand.type} is not a wand" }
        val spells = wand.itemMeta?.persistentDataContainer?.get(availableSpells, PersistentDataType.STRING)
            ?: throw IllegalStateException("Wand doesn't have any saved spells in it")
        return gson.fromJson(spells, typeToken)
    }

    fun setAvailableSpells(wand: ItemStack, list: List<SpellType>) {
        require(wand.isWand()) { "Item ${wand.type} is not a wand" }
        val meta = wand.itemMeta ?: throw IllegalStateException("This should not happen")

        meta.apply {
            persistentDataContainer.set(availableSpells, PersistentDataType.STRING, gson.toJson(list, typeToken))
            updateLore(null)
        }
        wand.itemMeta = meta
    }

    private fun getWandOwnerName(itemMeta: ItemMeta): String {
        val uuid = itemMeta.persistentDataContainer.get(ownerUuidKey, PersistentDataType.STRING)?.toUuid() ?: return "Unknown".also { consoleMessage("${ChatColor.RED}Wand doesn't have a owner, something went wrong") }
        return Bukkit.getOfflinePlayer(uuid).name ?: itemMeta.persistentDataContainer.get(ownerNameKey, PersistentDataType.STRING) ?: "Unknown"
    }

    fun getWandOwnerName(wand: ItemStack): String {
        val meta = wand.itemMeta ?: throw IllegalStateException("This should not happen")
        return getWandOwnerName(meta)
    }

    private fun getWandOwnerUuid(itemMeta: ItemMeta): UUID? {
        return itemMeta.persistentDataContainer.get(ownerUuidKey, PersistentDataType.STRING)?.toUuid()
            ?: return null.also { consoleMessage("${ChatColor.RED}Wand doesn't have a owner, something went wrong") }
    }

    fun getWandOwnerUuid(wand: ItemStack): UUID? {
        val meta = wand.itemMeta ?: throw IllegalStateException("This should not happen")
        return getWandOwnerUuid(meta)
    }

    fun setWandOwner(player: Player, wand: ItemStack) {
        require(wand.isWand()) { "Item ${wand.type} is not a wand" }

        val meta = wand.itemMeta ?: return
        meta.apply {
            persistentDataContainer.set(ownerNameKey, PersistentDataType.STRING, player.name)
            persistentDataContainer.set(ownerUuidKey, PersistentDataType.STRING, player.uniqueId.toString())
            updateLore(wand)
        }
        wand.itemMeta = meta
    }

    private fun ItemMeta.updateLore(wand: ItemStack) { lore = makeWandLore(this, getWandSpellOrNull(wand)) }

    private fun ItemMeta.updateLore(selectedSpell: SpellType?) { lore = makeWandLore(this, selectedSpell) }
}

fun Material.isWandMaterial() = WandSkin.isWandMaterial(this)

fun ItemStack.isWandMaterial() = type.isWandMaterial()

@KoinApiExtension
fun ItemStack?.isWand(): Boolean = this != null && isWandMaterial() && (itemMeta?.persistentDataContainer?.has(castCountKey, PersistentDataType.LONG) == true)

@KoinApiExtension
fun ItemStack.isWandOwner(player: Player): Boolean = itemMeta?.persistentDataContainer?.get(ownerUuidKey, PersistentDataType.STRING) == player.uniqueId.toString()

fun Material.formattedTypeName(): String = name.replace('_', ' ').capitalizeFully()

fun ItemStack.formattedTypeName(): String = type.formattedTypeName()