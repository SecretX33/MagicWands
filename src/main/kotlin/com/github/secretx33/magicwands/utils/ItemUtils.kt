package com.github.secretx33.magicwands.utils

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.model.WandSkin
import com.github.secretx33.magicwands.utils.ItemUtils.castCountKey
import com.github.secretx33.magicwands.utils.ItemUtils.ownerUuidKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension
import java.text.DecimalFormat
import java.util.*
import java.util.logging.Logger

@KoinApiExtension
object ItemUtils: CustomKoinComponent {
    private val plugin: Plugin by inject()
    private val messages: Messages by inject()
    private val log: Logger by inject()

    private val COLOR_PATTERN = """((?:§\w)+)<available_spells>""".toPattern()
    private val formatter = DecimalFormat("#,###")
    private val gson = Gson()
    private val listSpellTypeToken = object : TypeToken<List<SpellType>>() {}.type

    val castCountKey = NamespacedKey(plugin, "spell_cast_count")
    private val selectedSpell = NamespacedKey(plugin, "selected_spell")
    private val availableSpells = NamespacedKey(plugin, "available_spell")
    private val ownerNameKey = NamespacedKey(plugin, "wand_owner")
    val ownerUuidKey = NamespacedKey(plugin, "wand_owner")

    fun turnIntoWand(item: ItemStack, player: Player) {
        require(item.isWandMaterial()) { "Item ${item.type} cannot be a wand" }
        val meta = item.itemMeta ?: throw IllegalArgumentException("Could not get itemMeta from ${item.type}")

        meta.apply {
            pdc.apply {
                set(ownerNameKey, PersistentDataType.STRING, player.name)
                set(ownerUuidKey, PersistentDataType.STRING, player.uniqueId.toString())
                set(castCountKey, PersistentDataType.LONG, 0)
                remove(selectedSpell)
                set(availableSpells, PersistentDataType.STRING, "[]")
            }
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

        val maxLength = lore.map { it.length }.maxOrNull()?.times(0.9) ?: return emptyList()
        val tagIndex = lore.indexOfFirst { it.contains("<available_spells>") }
        // there is no <available_spells> tag in the wand lore
        if(tagIndex < 0) return lore
        // wand has no spell available
        if(availableSpells.isEmpty()) {
            lore[tagIndex] = lore[tagIndex].replace("<available_spells>", "${ChatColor.RESET}${ChatColor.GRAY}<none>")
            return lore
        }

        var currentLine = tagIndex
        val colorMatcher = COLOR_PATTERN.matcher(lore[tagIndex])
        val color = if(!colorMatcher.find()) "" else colorMatcher.group(1)
        // remove tag
        lore[tagIndex] = lore[tagIndex].replace("<available_spells>", "")

        // adjust available spells in the lore in a manner that doesn't allow that
        // the spells added to the lore make it wider than the widest line
        availableSpells.forEachIndexed { i, spell ->
            if(lore[currentLine].length > maxLength) currentLine++
            if(currentLine > lore.lastIndex) lore.add(color)
            if(i > 0 && lore[currentLine].length + spell.length + 1 > maxLength) {
                lore[currentLine] += ","
                currentLine++
                if(currentLine > lore.lastIndex) lore.add(color)
                else lore.add(currentLine, color)
            }
            if(currentLine == tagIndex) {
                if (i > 0) lore[currentLine] += ", "
                lore[currentLine] += spell
            } else {
                if(i > 0 && lore[currentLine].length > color.length) lore[currentLine] += ", "
                lore[currentLine] += spell
            }
        }
        return lore
    }

    fun increaseCastCount(item: ItemStack) {
        require(item.isWand()) { "Item ${item.type} is not a wand" }
        val meta = item.itemMeta ?: throw IllegalStateException("Could not get itemMeta from ${item.type}")
        meta.pdc.apply {
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
        oldMeta.pdc.let { oldPdc ->
            newMeta.pdc.apply {
                val ownerUuid = oldPdc.get(ownerUuidKey, PersistentDataType.STRING)!!
                val ownerName = Bukkit.getPlayer(ownerUuid.toUuid())?.name ?: oldPdc.get(ownerNameKey, PersistentDataType.STRING)!!
                set(ownerNameKey, PersistentDataType.STRING, ownerName)
                set(ownerUuidKey, PersistentDataType.STRING, ownerUuid)
                set(castCountKey, PersistentDataType.LONG, oldPdc.get(castCountKey, PersistentDataType.LONG)!!)
                selected = oldPdc.get(selectedSpell, PersistentDataType.STRING)
                if(selected != null)
                    set(selectedSpell, PersistentDataType.STRING, selected)
                else
                    remove(selectedSpell) // removes the key so the selected spell will be null
                set(availableSpells, PersistentDataType.STRING, oldPdc.get(availableSpells, PersistentDataType.STRING)!!)
            }
        }
        newMeta.updateLore(SpellType.ofOrNull(selected))
        new.itemMeta = newMeta
    }

    fun getWandSpell(wand: ItemStack): SpellType {
        require(wand.isWand()) { "Item ${wand.type} is not a wand" }
        val string = wand.itemMeta?.pdc?.get(selectedSpell, PersistentDataType.STRING)
            ?: throw IllegalStateException("Wand has no selected enchant")
        return SpellType.valueOf(string)
    }

    fun getWandSpellOrNull(wand: ItemStack): SpellType? {
        require(wand.isWand()) { "Item ${wand.type} is not a wand" }
        val string = wand.itemMeta?.pdc?.get(selectedSpell, PersistentDataType.STRING)
        return SpellType.ofOrNull(string)
    }

    fun setWandSpell(wand: ItemStack, type: SpellType) {
        require(wand.isWand()) { "Item ${wand.type} is not a wand" }
        val meta = wand.itemMeta ?: throw IllegalStateException("This should not happen")

        meta.apply {
            pdc.set(selectedSpell, PersistentDataType.STRING, type.name)
            updateLore(type)
        }
        wand.itemMeta = meta
    }

    private fun getAvailableSpells(itemMeta: ItemMeta): MutableList<SpellType> {
        val spells = itemMeta.pdc.get(availableSpells, PersistentDataType.STRING)
            ?: throw IllegalStateException("Wand doesn't have any saved spells in it")
        return gson.fromJson(spells, listSpellTypeToken)
    }

    fun getAvailableSpells(wand: ItemStack): MutableList<SpellType> {
        require(wand.isWand()) { "Item ${wand.type} is not a wand" }
        val spells = wand.itemMeta?.pdc?.get(availableSpells, PersistentDataType.STRING)
            ?: throw IllegalStateException("Wand doesn't have any saved spells in it")
        return gson.fromJson(spells, listSpellTypeToken)
    }

    fun setAvailableSpells(wand: ItemStack, list: List<SpellType>) {
        require(wand.isWand()) { "Item ${wand.type} is not a wand" }
        val meta = wand.itemMeta ?: throw IllegalStateException("This should not happen")

        meta.apply {
            pdc.set(availableSpells, PersistentDataType.STRING, gson.toJson(list, listSpellTypeToken))
            updateLore(null)
        }
        wand.itemMeta = meta
    }

    private fun getWandOwnerName(itemMeta: ItemMeta): String {
        val uuid = itemMeta.pdc.get(ownerUuidKey, PersistentDataType.STRING)?.toUuid() ?: return "Unknown".also { log.severe("${ChatColor.RED}Wand doesn't have a owner, something went wrong") }
        return Bukkit.getOfflinePlayer(uuid).name ?: itemMeta.pdc.get(ownerNameKey, PersistentDataType.STRING) ?: "Unknown"
    }

    fun getWandOwnerName(wand: ItemStack): String {
        val meta = wand.itemMeta ?: throw IllegalStateException("This should not happen")
        return getWandOwnerName(meta)
    }

    private fun getWandOwnerUuid(itemMeta: ItemMeta): UUID? {
        return itemMeta.pdc.get(ownerUuidKey, PersistentDataType.STRING)?.toUuid()
            ?: return null.also { log.severe("${ChatColor.RED}Wand doesn't have a owner, something went wrong") }
    }

    fun getWandOwnerUuid(wand: ItemStack): UUID? {
        val meta = wand.itemMeta ?: throw IllegalStateException("This should not happen")
        return getWandOwnerUuid(meta)
    }

    fun setWandOwner(player: Player, wand: ItemStack) {
        require(wand.isWand()) { "Item ${wand.type} is not a wand" }

        val meta = wand.itemMeta ?: return
        meta.apply {
            pdc.set(ownerNameKey, PersistentDataType.STRING, player.name)
            pdc.set(ownerUuidKey, PersistentDataType.STRING, player.uniqueId.toString())
            updateLore(wand)
        }
        wand.itemMeta = meta
    }

    private fun ItemMeta.updateLore(wand: ItemStack) { lore = makeWandLore(this, getWandSpellOrNull(wand)) }

    private fun ItemMeta.updateLore(selectedSpell: SpellType?) { lore = makeWandLore(this, selectedSpell) }

    private val ItemMeta.pdc: PersistentDataContainer
        get() = persistentDataContainer
}

fun Material.isWandMaterial() = WandSkin.isWandMaterial(this)

fun ItemStack.isWandMaterial() = type.isWandMaterial()

@KoinApiExtension
fun ItemStack?.isWand(): Boolean = this != null && isWandMaterial() && (itemMeta?.persistentDataContainer?.has(castCountKey, PersistentDataType.LONG) == true)

@KoinApiExtension
fun ItemStack.isWandOwner(player: Player): Boolean = itemMeta?.persistentDataContainer?.get(ownerUuidKey, PersistentDataType.STRING) == player.uniqueId.toString()

fun Material.formattedTypeName(): String = name.replace('_', ' ').capitalizeFully()

fun ItemStack.formattedTypeName(): String = type.formattedTypeName()
