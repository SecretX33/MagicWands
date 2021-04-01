package com.github.secretx33.magicwands

import com.github.secretx33.magicwands.commands.Commands
import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.eventlisteners.*
import com.github.secretx33.magicwands.manager.SpellFuelManager
import com.github.secretx33.magicwands.manager.SpellManager
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
        single { server.consoleSender }
        single { Messages(get()) }
        single { Config(get()) }
        single { SpellFuelManager(get()) }
        single { SpellManager(get(), get(), get()) }
        single { BlockSpellCastListener(get(), get(), get()) }
        single { EntitySpellCastListener(get(), get()) }
        single { PreventCraftListener(get()) }
        single { SpellCastListener(get(), get(), get(), get(), get()) }
        single { WandSpellSwitchListener(get(), get(), get()) }
        single { WandUseListener(get(), get()) }
        single { Commands(get()) }
    }

    override fun onEnable() {
        val economy = server.servicesManager.load(Economy::class.java)?: throw IllegalStateException("Vault was not found")
        mod.single(override = true) { economy }
        startKoin {
            printLogger(Level.ERROR)
            loadKoinModules(mod)
        }
        get<BlockSpellCastListener>()
        get<EntitySpellCastListener>()
        get<SpellCastListener>()
        get<PreventCraftListener>()
        get<SpellCastListener>()
        get<WandUseListener>()
        get<WandSpellSwitchListener>()
        get<Commands>()
    }

    override fun onDisable() {
        get<SpellManager>().close()
        unloadKoinModules(mod)
        stopKoin()
    }
}