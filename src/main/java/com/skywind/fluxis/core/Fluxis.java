package com.skywind.fluxis.core;

import com.skywind.fluxis.action.FluxisAction;
import com.skywind.fluxis.core.event.EventGUI;
import com.skywind.fluxis.core.event.EventListener;
import com.skywind.fluxis.core.manager.EventManager;
import com.skywind.fluxis.core.command.FluxisCommand;
import com.skywind.fluxis.core.listener.MarketListener;
import com.skywind.fluxis.core.manager.DataManager;
import com.skywind.fluxis.core.manager.DatabaseManager;
import com.skywind.fluxis.core.manager.EconomyManager;
import com.skywind.fluxis.core.manager.MarketManager;
import com.skywind.fluxis.core.placeholder.FluxisPlaceholder;
import com.skywind.fluxis.core.task.MarketTask;
import com.skywind.fluxis.core.ui.MarketGUI;
import com.skywind.fluxis.core.ui.SellGUI;
import com.skywind.fluxis.core.ui.AdminGUI;
import com.skywind.fluxis.core.util.VersionUtil;
import com.skywind.fluxis.plus.FluxisPlus;
import com.skywind.fluxis.trade.FluxisTradeGUI;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class Fluxis extends JavaPlugin {

    @Getter
    private static Fluxis instance;
    
    @Getter
    private static Logger pluginLogger;
    @Getter
    private FileConfiguration messagesConfig;
    @Getter
    private FileConfiguration economyConfig;
    @Getter
    private FileConfiguration shopConfig;
    @Getter
    private FileConfiguration databaseConfig;
    @Getter
    private FileConfiguration integrationConfig;
    @Getter
    private FileConfiguration shopMenuConfig;

    // Core Managers
    @Getter
    private MarketManager marketManager;
    @Getter
    private DataManager dataManager;
    @Getter
    private DatabaseManager databaseManager;
    @Getter
    private EconomyManager economyManager;
    @Getter
    private EventManager eventManager;
    
    // Core UI
    @Getter
    private MarketGUI marketGUI;
    @Getter
    private SellGUI sellGUI;
    @Getter
    private AdminGUI adminGUI;
    @Getter
    private EventGUI eventGUI;

    // Sub-Modules
    @Getter
    private FluxisPlus visualsModule;
    @Getter
    private FluxisAction auctionModule;
    @Getter
    private FluxisTradeGUI tradeModule;

    @Override
    public void onEnable() {
        instance = this;
        pluginLogger = getLogger();

        initializeConfigs();

        pluginLogger.info("Fluxis Core is starting...");
        pluginLogger.info("SkyWind Alliance: Modular Ecosystem");

        // 1. Core Systems
        this.databaseManager = new DatabaseManager(this);
        this.dataManager = new DataManager(this);
        this.marketManager = new MarketManager(this);
        this.economyManager = new EconomyManager(this);
        this.eventManager = new EventManager(this);
        if (!this.economyManager.isReady()) {
            pluginLogger.severe("Vault economy provider not found. Disabling Fluxis.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.marketGUI = new MarketGUI(this);
        this.sellGUI = new SellGUI(this);
        this.adminGUI = new AdminGUI(this);
        this.eventGUI = new EventGUI(this);
        
        // 2. Load Market Data
        var loadedItems = dataManager.loadMarketData();
        if (!loadedItems.isEmpty()) {
            marketManager.getMarketItems().putAll(loadedItems);
        }

        // 3. Register Core Listeners & Commands
        getServer().getPluginManager().registerEvents(new MarketListener(this), this);
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        FluxisCommand cmd = new FluxisCommand(this);
        getCommand("fluxis").setExecutor(cmd);
        getCommand("market").setExecutor(cmd);
        getCommand("shop").setExecutor(cmd);
        getCommand("sell").setExecutor(cmd);
        getCommand("auction").setExecutor(cmd);
        getCommand("trade").setExecutor(cmd);

        // 3a. Register Placeholders
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new FluxisPlaceholder(this).register();
            pluginLogger.info("PlaceholderAPI expansion registered!");
        }

        // 4. Start Core Tasks
        long interval = getShopConfig().getLong("market.task_interval", 1200L);
        if (VersionUtil.isFolia()) {
            try {
                Object scheduler = getServer().getClass().getMethod("getGlobalRegionScheduler").invoke(getServer());
                scheduler.getClass().getMethod("runAtFixedRate", org.bukkit.plugin.Plugin.class, java.util.function.Consumer.class, long.class, long.class)
                    .invoke(scheduler, this, (java.util.function.Consumer<Object>) (task) -> new MarketTask(this).run(), 1L, interval);
                pluginLogger.info("Fluxis is running in Folia mode (Reflective Scheduler)!");
            } catch (Exception e) {
                pluginLogger.warning("Folia detected but failed to use GlobalRegionScheduler: " + e.getMessage());
            }
        } else {
            new MarketTask(this).runTaskTimer(this, interval, interval);
        }

        // 5. Load Sub-Modules
        this.visualsModule = new FluxisPlus(this);
        this.visualsModule.init();

        this.auctionModule = new FluxisAction(this);
        this.auctionModule.init();

        this.tradeModule = new FluxisTradeGUI(this);
        this.tradeModule.init();

        pluginLogger.info("Fluxis Ecosystem has been enabled!");
    }

    @Override
    public void onDisable() {
        if (eventManager != null) {
            eventManager.stopLoop();
        }
        if (marketManager != null && dataManager != null) {
            dataManager.saveMarketData(marketManager.getMarketItems());
        }
    }

    private void initializeConfigs() {
        saveDefaultConfig();
        saveResourceIfMissing("messages.yml");
        saveResourceIfMissing("economy.yml");
        saveResourceIfMissing("shop.yml");
        saveResourceIfMissing("database.yml");
        saveResourceIfMissing("integration.yml");
        saveResourceIfMissing("shopmenu.yml");
        saveResourceIfMissing("events/event1.yml");
        saveResourceIfMissing("events/event2.yml");
        reloadAllConfigs();
    }

    private void saveResourceIfMissing(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
        }
    }

    public void reloadAllConfigs() {
        reloadConfig();
        this.messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        this.economyConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "economy.yml"));
        this.shopConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "shop.yml"));
        this.databaseConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "database.yml"));
        this.integrationConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "integration.yml"));
        this.shopMenuConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "shopmenu.yml"));
    }

    public String message(String path, String fallback) {
        String prefix = messagesConfig.getString("prefix", "&8[&eFluxis&8] ");
        String raw = messagesConfig.getString(path, fallback).replace("{prefix}", prefix);
        return raw.replace("&", "§");
    }
}
