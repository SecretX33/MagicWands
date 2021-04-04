package com.github.secretx33.magicwands

import com.github.secretx33.magicwands.commands.Commands
import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.database.SQLite
import com.github.secretx33.magicwands.eventlisteners.*
import com.github.secretx33.magicwands.manager.*
import com.github.secretx33.magicwands.packetlisteners.WandDropPacketListener
import com.github.secretx33.magicwands.utils.*
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinApiExtension
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

@KoinApiExtension
class MagicWands : JavaPlugin(), CustomKoinComponent {

    private val mod = module {
        single<Plugin> { this@MagicWands } bind JavaPlugin::class
        single { server.consoleSender }
        single(named("firework")) { NamespacedKey(get<Plugin>(), "custom_firework") }
        single { Messages(get()) }
        single { Config(get()) }
        single { SpellFuelManager(get()) }
        single { LearnedSpellsManager(get()) }
        single { SpellTeacherManager(get(), get()) }
        single { HiddenPlayersHelper(get()) }
        single { ParticlesHelper(get(), get(named("firework"))) }
        single { SpellManager(get(), get(), get(), get(), get()) }
        single { BlockBreakListener(get(), get(), get()) }
        single { BlockSpellCastListener(get(), get(), get()) }
        single { EntitySpellCastListener(get(), get()) }
        single { FireworkDamageWorkaroundListener(get(), get(named("firework"))) }
        single { PlayerDeathListener(get(), get(), get()) }
        single { PlayerLeaveListener(get(), get()) }
        single { PreventCraftListener(get(), get()) }
        single { PreventWandPickupListener(get()) }
        single { SpellCastListener(get(), get(), get(), get(), get(), get()) }
        single { SpellTeacherListener(get(), get(), get(), get(), get(), get()) }
        single { WandSpellSwitchListener(get(), get(), get()) }
        single { WandUseListener(get(), get(), get(), get()) }
        single { Commands(get()) }
        single { WandDropPacketListener(get()) }
    }

    override fun onEnable() {
        val economy = server.servicesManager.load(Economy::class.java)?: throw IllegalStateException("Vault was not found")
        mod.single(override = true) { economy }
        startKoin {
            printLogger(Level.ERROR)
            loadKoinModules(mod)
        }
        get<BlockBreakListener>()
        get<BlockSpellCastListener>()
        get<EntitySpellCastListener>()
        get<FireworkDamageWorkaroundListener>()
        get<SpellCastListener>()
        get<PlayerDeathListener>()
        get<PlayerLeaveListener>()
        get<PreventCraftListener>()
        get<PreventWandPickupListener>()
        get<SpellCastListener>()
        get<SpellTeacherListener>()
        get<WandSpellSwitchListener>()
        get<WandUseListener>()
        get<Commands>()
        if(isProtocolLibEnabled) {
            get<WandDropPacketListener>()
        }
    }

    override fun onDisable() {
        get<SpellManager>().finalizeTasks()
        get<SQLite>().close()
        unloadKoinModules(mod)
        stopKoin()
    }

    private val isProtocolLibEnabled
        get() = Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")
}