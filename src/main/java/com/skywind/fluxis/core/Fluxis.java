package com.skywind.fluxis.core;

import com.skywind.fluxis.action.FluxisAction;
import com.skywind.fluxis.core.command.FluxisCommand;
import com.skywind.fluxis.core.listener.MarketListener;
import com.skywind.fluxis.core.manager.DataManager;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class Fluxis extends JavaPlugin {

    @Getter
    private static Fluxis instance;
    
    @Getter
    private static Logger pluginLogger;

    // Core Managers
    @Getter
    private MarketManager marketManager;
    @Getter
    private DataManager dataManager;
    @Getter
    private EconomyManager economyManager;
    
    // Core UI
    @Getter
    private MarketGUI marketGUI;
    @Getter
    private SellGUI sellGUI;
    @Getter
    private AdminGUI adminGUI;

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

        saveDefaultConfig(); // Save config.yml if it doesn't exist

        pluginLogger.info("Fluxis Core is starting...");
        pluginLogger.info("SkyWind Alliance: Modular Ecosystem");

        // 1. Core Systems
        this.dataManager = new DataManager(this);
        this.marketManager = new MarketManager(this);
        this.economyManager = new EconomyManager(this);
        this.marketGUI = new MarketGUI(this);
        this.sellGUI = new SellGUI(this);
        this.adminGUI = new AdminGUI(this);
        
        // 2. Load Market Data
        var loadedItems = dataManager.loadMarketData();
        if (!loadedItems.isEmpty()) {
            marketManager.getMarketItems().putAll(loadedItems);
        }

        // 3. Register Core Listeners & Commands
        getServer().getPluginManager().registerEvents(new MarketListener(this), this);
        FluxisCommand cmd = new FluxisCommand(this);
        getCommand("fluxis").setExecutor(cmd);
        getCommand("market").setExecutor(cmd);
        getCommand("shop").setExecutor(cmd);
        getCommand("sell").setExecutor(cmd);

        // 3a. Register Placeholders
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new FluxisPlaceholder(this).register();
            pluginLogger.info("PlaceholderAPI expansion registered!");
        }

        // 4. Start Core Tasks
        long interval = getConfig().getLong("market.task_interval", 1200L);
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
        if (marketManager != null && dataManager != null) {
            dataManager.saveMarketData(marketManager.getMarketItems());
        }
    }
}
