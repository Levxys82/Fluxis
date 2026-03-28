package com.skywind.fluxis.core.manager;

import com.skywind.fluxis.core.Fluxis;
import com.skywind.fluxis.core.model.MarketItem;
import com.skywind.fluxis.core.util.VersionUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarketManager {
    
    private final Fluxis core;
    
    @Getter
    private final Map<Material, MarketItem> marketItems = new ConcurrentHashMap<>();
    
    private static final double ACTIVITY_BASE = 1.0;

    public MarketManager(Fluxis core) {
        this.core = core;
        initDefaultItems();
    }

    private void initDefaultItems() {
        // Tier 1: Basics
        registerItemSafe("OAK_LOG", 4.0, 1.0, 15.0, 1000, 1);
        registerItemSafe("COBBLESTONE", 1.0, 0.2, 5.0, 5000, 1);
        registerItemSafe("DIRT", 0.5, 0.1, 2.0, 10000, 1);
        registerItemSafe("SAND", 1.5, 0.5, 6.0, 5000, 1);

        // Tier 2: Resources
        registerItemSafe("COAL", 8.0, 2.0, 30.0, 500, 2);
        registerItemSafe("IRON_INGOT", 50.0, 15.0, 150.0, 250, 2);
        registerItemSafe("WHEAT_SEEDS", 2.0, 0.5, 10.0, 1000, 2);
        registerItemSafe("OAK_SAPLING", 10.0, 2.0, 50.0, 100, 2);

        // Tier 3: Food
        registerItemSafe("BREAD", 25.0, 10.0, 80.0, 100, 3);
        registerItemSafe("COOKED_BEEF", 40.0, 15.0, 120.0, 50, 3);
        registerItemSafe("CARROT", 15.0, 5.0, 45.0, 200, 3);
        registerItemSafe("POTATO", 15.0, 5.0, 45.0, 200, 3);

        // Tier 4: Tools
        registerItemSafe("STONE_PICKAXE", 15.0, 5.0, 50.0, 100, 4);
        registerItemSafe("IRON_PICKAXE", 150.0, 50.0, 500.0, 25, 4);

        // 1.21 Content (Safe Registration)
        registerItemSafe("MACE", 5000.0, 2000.0, 25000.0, 5, 5);
        registerItemSafe("HEAVY_CORE", 10000.0, 5000.0, 50000.0, 2, 5);
        registerItemSafe("BREEZE_ROD", 150.0, 50.0, 1000.0, 50, 2);
    }

    public void registerItemSafe(String materialName, double basePrice, double minPrice, double maxPrice, int stock, int tier) {
        Material mat = VersionUtil.getSafeMaterial(materialName);
        if (mat == null) return; // Skip if item doesn't exist in this MC version
        
        registerItem(mat, basePrice, minPrice, maxPrice, stock, tier);
    }

    public void registerItem(Material material, double basePrice, double minPrice, double maxPrice, int stock, int tier) {
        MarketItem item = MarketItem.builder()
                .material(material)
                .basePrice(basePrice)
                .currentPrice(basePrice)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .stock(stock)
                .tier(tier)
                .buyVolume(0)
                .sellVolume(0)
                .trend(MarketItem.PriceTrend.STABLE)
                .build();
        marketItems.put(material, item);
    }

    public void buyItem(Material material, int quantity) {
        MarketItem item = marketItems.get(material);
        if (item == null || item.getStock() < quantity) return;

        double volatility = core.getConfig().getDouble("market.default_volatility", 0.005);
        double activityMultiplier = getActivityMultiplier();
        double priceChange = 1.0 + (quantity * volatility * activityMultiplier);
        
        double newPrice = item.getCurrentPrice() * priceChange;
        newPrice = Math.min(newPrice, item.getMaxPrice());
        
        item.setCurrentPrice(newPrice);
        item.setBuyVolume(item.getBuyVolume() + quantity);
        item.setStock(item.getStock() - quantity);
        item.updateTrend();
    }

    public void sellItem(Material material, int quantity) {
        MarketItem item = marketItems.get(material);
        if (item == null) return;

        double volatility = core.getConfig().getDouble("market.default_volatility", 0.005);
        double activityMultiplier = getActivityMultiplier();
        double priceChange = 1.0 - (quantity * volatility * activityMultiplier);
        
        double newPrice = item.getCurrentPrice() * priceChange;
        newPrice = Math.max(newPrice, item.getMinPrice());
        
        item.setCurrentPrice(newPrice);
        item.setSellVolume(item.getSellVolume() + quantity);
        item.setStock(item.getStock() + quantity);
        item.updateTrend();
    }

    public double getActivityMultiplier() {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        return ACTIVITY_BASE + (onlinePlayers / 5.0 * 0.1);
    }
}
