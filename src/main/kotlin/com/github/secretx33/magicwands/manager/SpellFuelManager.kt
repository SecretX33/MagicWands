package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.model.SpellType
import org.bukkit.Material
import org.bukkit.entity.Player

class SpellFuelManager(private val config: Config) {

    fun hasEnoughFuel(player: Player, spell: SpellType): Boolean {
        val quantityNeeded = config.get(spell.configFuelAmount, 5)
        if(quantityNeeded <= 0) return true
        val fuelList = configFuels
        val inv = player.inventory

        val quantity = inv.contents.asSequence()
            .filter { it != null && fuelList.contains(it.type) }
            .map { it.amount }
            .reduceOrNull { acc, item -> acc + item } ?: 0
        return quantity >= quantityNeeded
    }

    fun consumeFuel(player: Player, spell: SpellType) {
        var quantityNeeded = config.get(spell.configFuelAmount, 5)
        if(quantityNeeded <= 0) return
        val fuelList = configFuels

        for(item in player.inventory.filterNotNull()) {
            if(!fuelList.contains(item.type)) continue

            if(item.amount >= quantityNeeded) {
                item.amount -= quantityNeeded
                if(item.amount == 0) item.type = Material.AIR
                break
            }
            quantityNeeded -= item.amount
            item.amount = 0
            item.type = Material.AIR
            if(quantityNeeded <= 0) break
        }
        player.updateInventory()
    }

    private val configFuels: Set<Material>
        get() = config.get<List<String>>(ConfigKeys.SPELL_FUEL).mapTo(HashSet()) { Material.valueOf(it) }
}
