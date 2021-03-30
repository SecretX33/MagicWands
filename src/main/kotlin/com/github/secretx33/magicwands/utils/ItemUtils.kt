package com.github.secretx33.magicwands.utils

import com.github.secretx33.magicwands.utils.ItemUtils.castCountKey
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
object ItemUtils: CustomKoinComponent {
    private val plugin: Plugin by inject()
    val castCountKey = NamespacedKey(plugin, "spell_cast_count")

    fun turnIntoWand(item: ItemStack) {
        require(item.type.isWandMaterial()) { "Item ${item.type} cannot be a wand" }
        val meta = item.itemMeta ?: throw IllegalArgumentException("Could not get itemMeta from ${item.type}")
        meta.persistentDataContainer.set(castCountKey, PersistentDataType.LONG, 0)
        item.itemMeta = meta
    }

    fun increaseCastCount(item: ItemStack) {
        require(item.isWand()) { "Item ${item.type} cannot be a wand" }
        val meta = item.itemMeta ?: throw IllegalArgumentException("Could not get itemMeta from ${item.type}")
        meta.persistentDataContainer.apply {
            val actualValue = get(castCountKey, PersistentDataType.LONG)!!
            set(castCountKey, PersistentDataType.LONG, actualValue + 1)
        }
        item.itemMeta = meta
    }
}

fun Material.isWandMaterial() = this == Material.STICK || this == Material.BLAZE_ROD || this == Material.BONE

@KoinApiExtension
fun ItemStack?.isWand(): Boolean = this != null && (type == Material.STICK || type == Material.BLAZE_ROD || type == Material.BONE) && (itemMeta?.persistentDataContainer?.has(castCountKey, PersistentDataType.LONG) == true)