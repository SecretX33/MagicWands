package com.github.secretx33.magicwands.model

import org.bukkit.Location
import org.bukkit.Material

private data class SpellTeacher (
    val location: Location,
    val spellType: SpellType,
    val blockMaterial: Material
)
