package com.github.secretx33.magicwands

import com.github.secretx33.magicwands.events.PreventCraftEvent
import com.github.secretx33.magicwands.events.WandUseEvent
import com.github.secretx33.magicwands.utils.*
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinApiExtension
import org.koin.core.logger.Level
import org.koin.dsl.bind
import org.koin.dsl.module

@KoinApiExtension
class MagicWands : JavaPlugin(), CustomKoinComponent {

    private val mod = module {
        single<Plugin> { this@MagicWands } bind JavaPlugin::class
        single { PreventCraftEvent(get()) }
        single { WandUseEvent(get()) }
    }

    override fun onEnable() {
        saveDefaultConfig()
        val economy = server.servicesManager.load(Economy::class.java)?: throw IllegalStateException("Vault was not found")
        mod.single { economy }
        startKoin {
            printLogger(Level.ERROR)
            loadKoinModules(mod)
        }
        get<PreventCraftEvent>()
        get<WandUseEvent>()
    }

    override fun onDisable() {
        unloadKoinModules(mod)
        stopKoin()
    }
}