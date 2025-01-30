package at.lowdfx.lowdfx;

import at.lowdfx.lowdfx.command.*;
import at.lowdfx.lowdfx.event.*;
import at.lowdfx.lowdfx.inventory.ChestData;
import at.lowdfx.lowdfx.managers.ChestShopManager;
import at.lowdfx.lowdfx.managers.HomeManager;
import at.lowdfx.lowdfx.managers.SpawnManager;
import at.lowdfx.lowdfx.managers.WarpManager;
import at.lowdfx.lowdfx.moderation.VanishingHandler;
import at.lowdfx.lowdfx.util.Permissions;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class LowdFX extends JavaPlugin {
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
    public static final List<Component> OP_LORE = List.of(Component.text("OP Kit", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
    public static final List<Component> STARTER_LORE = List.of(Component.text("Starter Kit", NamedTextColor.DARK_PURPLE).decoration(TextDecoration.ITALIC, false));

    public static Logger LOG;
    public static FileConfiguration CONFIG;
    public static LowdFX PLUGIN;
    public static Path DATA_DIR;

    public static ChestData CHESTS_DATA;
    public static VanishingHandler INVISIBLE_HANDLER;
    public static ChestShopManager SHOP_MANAGER;

    private static HomeManager HOME_MANAGER;
    private static WarpManager WARP_MANAGER;
    private static SpawnManager SPAWN_MANAGER;

    @Override
    public void onEnable() {
        LOG = getSLF4JLogger();
        PLUGIN = this;
        DATA_DIR = getDataPath();

        // === Permissions Klasse laden === //
        new Permissions().loadPermissions();

        // === Config Datei === //
        saveDefaultConfig();
        CONFIG = getConfig();

        // === ChestShop Manager === //
        File shopFolder = DATA_DIR.resolve("ChestShops").toFile();
        if (shopFolder.mkdirs())
            LOG.info("ChestShops-Ordner wurde erstellt.");

        // === Statische Manager === //
        HOME_MANAGER = new HomeManager();
        WARP_MANAGER = new WarpManager();
        SPAWN_MANAGER = new SpawnManager();
        INVISIBLE_HANDLER = new VanishingHandler();

        SHOP_MANAGER = new ChestShopManager(shopFolder);
        SHOP_MANAGER.loadAllShops();

        // === Events === //
        getServer().getPluginManager().registerEvents(new ConnectionEvents(), this);
        getServer().getPluginManager().registerEvents(new KitEvents(), this);
        getServer().getPluginManager().registerEvents(new ChestShopEvents(), this);
        getServer().getPluginManager().registerEvents(new ChestLockEvents(), this);
        getServer().getPluginManager().registerEvents(new VanishEvents(), this);
        getServer().getPluginManager().registerEvents(new MuteEvents(), this);

        // === ChestData Datei === //
        ensureDataFolderExists();
        CHESTS_DATA = new ChestData();

        // === Vanished Spieler Datei === //
        INVISIBLE_HANDLER.loadVanishedPlayers();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands registrar = event.registrar();
            registrar.register(ChestLockCommand.command(), "Sperrt eine Kiste.");
            registrar.register(HomeCommand.command(), "Teleportiert dich zu deinem Home.");
            registrar.register(InventoryCommands.anvilCommand(), "Öffnet einen Amboss.", List.of("amboss"));
            registrar.register(InventoryCommands.enderseeCommand(), "Öffnet die Enderchest von einem Spieler.");
            registrar.register(InventoryCommands.invseeCommand(), "Öffnet das Inventar von einem Spieler.");
            registrar.register(InventoryCommands.trashCommand(), "Öffnet einen Mülleimer.", List.of("rubbish", "mülleimer"));
            registrar.register(InventoryCommands.workbenchCommand(), "Öffnet eine Werkbank.", List.of("crafting", "crafting-table"));
            registrar.register(LowCommand.command(), "Generelle features vom Plugin.");
            registrar.register(MuteCommands.muteCommand(), "Schaltet einen Spieler stumm.");
            registrar.register(MuteCommands.unmuteCommand(), "Entfernt den Mute eines Spielers.");
            registrar.register(SpawnCommand.command(), "Teleportiert dich zum Spawn.");
            registrar.register(StatCommands.feedCommand(), "Füttert einen Spieler.", List.of("saturate"));
            registrar.register(StatCommands.healCommand(), "Heilt einen Spieler und löscht alle negativen effekte.", List.of("regen"));
            registrar.register(TimeCommands.dayCommand(), "Stellt die Zeit zu Tag.");
            registrar.register(TimeCommands.nightCommand(), "Stellt die Zeit zu Mitternacht.", List.of("midnight"));
            registrar.register(TpCommands.backCommand(), "Teleportiert dich zurück an deinen letzten Ort.");
            registrar.register(TpCommands.tpallCommand(), "Teleportiere alle Spieler zu dir.");
            registrar.register(TpCommands.tphereCommand(), "Teleportiere einen oder mehrere Spieler zu dir.");
            registrar.register(TpaCommand.command(), "Versendet eine TPA an einen Spieler.");
            registrar.register(UtilityCommands.flyCommand(), "Erlaubt einen Spieler zu fliegen.");
            registrar.register(UtilityCommands.gmCommand(), "Setzt den Spielmodus eines Spielers.");
            registrar.register(VanishCommand.command(), "Macht dich unsichtbar oder wieder sichtbar.");
            registrar.register(WarpCommand.command(), "Teleportiert dich zu einem Warp.");
        });

        LOG.info("LowdFX Plugin gestartet!");
    }

    @Override
    public void onDisable() {
        CONFIG = getConfig();

        boolean vanish = CONFIG.getBoolean("basic.vanish", false);
        if (CONFIG.getBoolean("basic.vanish")) {
            INVISIBLE_HANDLER.saveVanishedPlayers();}
        LOG.info("Vanish war: {}", vanish ? "An" : "Aus");

        // Speichere alle Shops in die Spielerdateien
        SHOP_MANAGER.saveAllShops();

        //------------------------------------------
        if (!this.getServer().isPrimaryThread()) {
            // Hier asynchrone Aufgaben starten oder Operationen durchführen, die nicht während dem Shutdown laufen
            if (HOME_MANAGER != null) HOME_MANAGER.onDisable();
            if (WARP_MANAGER != null) WARP_MANAGER.onDisable();
            if (SPAWN_MANAGER != null) SPAWN_MANAGER.onDisable();
            if (CHESTS_DATA != null) CHESTS_DATA.save();
        }
        saveConfig();
    }

    // Diese Methode stellt sicher, dass der Plugin-Ordner und die Datei existieren.
    private void ensureDataFolderExists() {
        if (DATA_DIR.toFile().mkdirs()) {
            LOG.info("Datenordner erstellt: {}", getDataFolder().getAbsolutePath());
        }

        // Überprüfen und sicherstellen, dass die Datei erstellt wird, falls sie nicht existiert.
        File dataFile = DATA_DIR.resolve("chestdata.yml").toFile();
        if (!dataFile.exists()) {
            try {
                boolean created = dataFile.createNewFile();
                if (created) {
                    LOG.info("Daten-Datei erstellt: {}", dataFile.getAbsolutePath());
                } else {
                    LOG.warn("Die Datei 'chestdata.yml' konnte nicht erstellt werden.");
                }
            } catch (IOException e) {
                LOG.error("Fehler beim Erstellen der Datei 'chestdata.yml'.", e);
            }
        }
    }

    public static @NotNull Component serverMessage(Component message) {
        return Component.text(Objects.requireNonNullElse(LowdFX.CONFIG.getString("basic.servername"), "???"), NamedTextColor.GOLD, TextDecoration.BOLD)
                .append(Component.text(" >> ", NamedTextColor.GRAY))
                .append(message);
    }
}
