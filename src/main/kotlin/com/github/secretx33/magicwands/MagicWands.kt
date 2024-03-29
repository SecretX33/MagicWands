package com.github.secretx33.magicwands

import com.github.secretx33.magicwands.commands.Commands
import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.database.SQLite
import com.github.secretx33.magicwands.eventlisteners.*
import com.github.secretx33.magicwands.eventlisteners.sideeffectsmitigation.FallDamageListener
import com.github.secretx33.magicwands.eventlisteners.sideeffectsmitigation.FireworkDamageWorkaroundListener
import com.github.secretx33.magicwands.eventlisteners.spellcasts.BlockSpellCastListener
import com.github.secretx33.magicwands.eventlisteners.spellcasts.EntitySpellCastListener
import com.github.secretx33.magicwands.eventlisteners.spellcasts.SpellCastListener
import com.github.secretx33.magicwands.eventlisteners.spellteacher.SpellTeacherBreakListener
import com.github.secretx33.magicwands.eventlisteners.spellteacher.SpellTeacherUseListener
import com.github.secretx33.magicwands.eventlisteners.wand.WandPickupPreventListener
import com.github.secretx33.magicwands.eventlisteners.wand.WandPreventCraftListener
import com.github.secretx33.magicwands.eventlisteners.wand.WandSpellSwitchListener
import com.github.secretx33.magicwands.eventlisteners.wand.WandUseListener
import com.github.secretx33.magicwands.manager.*
import com.github.secretx33.magicwands.packetlisteners.WandDropPacketListener
import com.github.secretx33.magicwands.repositories.LearnedSpellsRepo
import com.github.secretx33.magicwands.repositories.SpellTeacherRepo
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
        single { get<Plugin>().logger }
        single(named("firework")) { NamespacedKey(get<Plugin>(), "custom_firework") }
        single<WorldGuardChecker> { WorldGuardCheckerDummy() }
        single { Messages(get()) }
        single { Config(get()) }
        single { SQLite(get(), get()) }
        single { LearnedSpellsRepo(get()) }
        single { SpellTeacherRepo(get()) }
        single { SpellFuelManager(get()) }
        single { HiddenPlayersHelper(get()) }
        single { ParticlesHelper(get(), get(), get(named("firework"))) }
        single { SpellManager(get(), get(), get(), get(), get(), get()) }
        single { FallDamageListener(get(), get()) }
        single { FireworkDamageWorkaroundListener(get(), get(named("firework"))) }
        single { BlockSpellCastListener(get(), get(), get()) }
        single { EntitySpellCastListener(get(), get()) }
        single { SpellCastListener(get(), get(), get(), get(), get(), get(), get()) }
        single { SpellTeacherBreakListener(get(), get(), get()) }
        single { SpellTeacherUseListener(get(), get(), get(), get(), get(), get()) }
        single { WandPickupPreventListener(get()) }
        single { WandPreventCraftListener(get(), get()) }
        single { WandSpellSwitchListener(get(), get(), get()) }
        single { WandUseListener(get(), get(), get(), get(), get()) }
        single { PlayerDeathListener(get(), get(), get()) }
        single { PlayerLeaveListener(get(), get()) }
        single { Commands(get()) }
        single { WandDropPacketListener(get()) }
    }

    override fun onLoad() {
        // if worldguard is enabled, replace dummy module with real one
        if(isWorldGuardEnabled) {
            // creation of the WorldGuardChecker happens here because WG is bae and requires hooking to happen on method onLoad
            val wgChecker = WorldGuardCheckerImpl(logger)
            mod.single<WorldGuardChecker>(override = true) { wgChecker }
        }
    }

    override fun onEnable() {
        val economy = server.servicesManager.load(Economy::class.java) ?: throw IllegalStateException("Vault was not found")
        mod.single(override = true) { economy }
        startKoin {
            printLogger(Level.ERROR)
            loadKoinModules(mod)
        }
        get<FallDamageListener>()
        get<FireworkDamageWorkaroundListener>()
        get<BlockSpellCastListener>()
        get<EntitySpellCastListener>()
        get<SpellCastListener>()
        get<SpellTeacherBreakListener>()
        get<SpellTeacherUseListener>()
        get<WandPickupPreventListener>()
        get<WandPreventCraftListener>()
        get<WandSpellSwitchListener>()
        get<PlayerDeathListener>()
        get<PlayerLeaveListener>()
        get<WandUseListener>()
        get<Commands>()
        if(wandsAreExclusive) get<WandDropPacketListener>()
    }

    override fun onDisable() {
        get<SpellManager>().finalizeTasks()
        get<SQLite>().close()
        unloadKoinModules(mod)
        stopKoin()
    }

    private val wandsAreExclusive
        get() = Bukkit.getPluginManager().isPluginEnabled("ProtocolLib") && get<Config>().get(ConfigKeys.PLAYERS_ONLY_SEE_THEIR_OWN_WANDS)

    private val isWorldGuardEnabled
        get() = Bukkit.getPluginManager().getPlugin("WorldGuard") != null
}
