package com.github.secretx33.magicwands.config

import org.bukkit.ChatColor

object Const {
    const val PLUGIN_NAME = "MagicWands"
    const val PLUGIN_COMMAND_PREFIX = "wand"
    private val PLUGIN_CHAT_COLOR_PREFIX = ChatColor.GOLD

    val PLUGIN_CHAT_PREFIX = "$PLUGIN_CHAT_COLOR_PREFIX[$PLUGIN_NAME]${ChatColor.WHITE}"
}
