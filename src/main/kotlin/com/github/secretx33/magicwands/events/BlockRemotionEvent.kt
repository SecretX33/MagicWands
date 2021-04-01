package com.github.secretx33.magicwands.events

import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent

class BlockRemotionEvent(block: Block, player: Player) : BlockBreakEvent(block, player) {

    init {
        expToDrop = 0
        isDropItems = false
        block.drops.clear()
    }
}