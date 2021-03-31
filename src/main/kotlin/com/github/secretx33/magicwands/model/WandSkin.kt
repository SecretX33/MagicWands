package com.github.secretx33.magicwands.model

import com.github.secretx33.magicwands.utils.Colors
import com.github.secretx33.magicwands.utils.capitalizeFully
import com.github.secretx33.magicwands.utils.formattedTypeName
import org.bukkit.ChatColor
import org.bukkit.Material
import java.util.*

enum class WandSkin(val material: Material, val wandComplement: String) {
    STICK(Material.STICK, "${ChatColor.GRAY}${ChatColor.BOLD}(${Material.STICK.formattedTypeName()})"),
    BONE(Material.BONE, "${Colors.BONE_RED}${ChatColor.BOLD}(${Material.BONE.formattedTypeName()})"),
    BLAZE_ROD(Material.BLAZE_ROD, "${Colors.BLAZE_ROD_ORANGE}${ChatColor.BOLD}(${Material.BLAZE_ROD.formattedTypeName()})");

    val permission = "magicwands.skins.${material.name.toLowerCase(Locale.US)}"
    val displayName = material.name.replace('_', ' ').capitalizeFully()

    companion object {
        fun of(material: Material): WandSkin = values().firstOrNull { it.material == material } ?: throw IllegalArgumentException("Material $material is not a wand material")

        fun of(name: String): WandSkin? = values().firstOrNull { it.material.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }

        fun isWandMaterial(material: Material): Boolean = values().any { it.material == material }
    }
}