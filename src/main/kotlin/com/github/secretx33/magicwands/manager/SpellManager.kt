package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.spell.Spell
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class SpellManager {

    private val cooldown = ConcurrentHashMap<Pair<Player, Spell>, Long>()


}