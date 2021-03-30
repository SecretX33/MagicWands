package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.spell.SpellType
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SpellFuelManager(private val config: Config) {

    fun hasEnoughFuel(player: Player, spell: SpellType): Boolean {
        val quantityNeeded = config.get(spell.configFuelAmount, 5)
        val fuelList = config.get<List<String>>(ConfigKeys.SPELL_FUEL).map { ItemStack(Material.valueOf(it)) }
        val inv = player.inventory

        return fuelList.any { fuel -> inv.containsAtLeast(fuel, quantityNeeded) }
    }

    fun consumeFuel(player: Player, spell: SpellType) {
        var quantityNeeded = config.get(spell.configFuelAmount, 5)
        val fuelList = config.get<List<String>>(ConfigKeys.SPELL_FUEL).mapTo(HashSet()) { Material.valueOf(it) }

        for(item in player.inventory) {
            if(!fuelList.contains(item.type)) continue

            if(item.amount >= quantityNeeded) {
                item.amount -= quantityNeeded
                break
            }
            quantityNeeded -= item.amount
            item.amount = 0
            item.type = Material.AIR
            if(quantityNeeded <= 0) break
        }
        player.updateInventory()
    }
}