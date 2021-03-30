package com.github.secretx33.magicwands.utils

import com.github.secretx33.magicwands.utils.ItemUtils.isWandKey
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
object ItemUtils: CustomKoinComponent {
    private val plugin: Plugin by inject()
    val isWandKey = NamespacedKey(plugin, "is_wand")
}

@KoinApiExtension
fun ItemStack?.isWand(): Boolean = this != null && (type == Material.STICK || type == Material.BLAZE_ROD || type == Material.BONE) && (itemMeta?.persistentDataContainer?.get(isWandKey, PersistentDataType.SHORT) == 1.toShort())