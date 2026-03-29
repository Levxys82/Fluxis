package com.skywind.fluxis.core.manager;

import com.skywind.fluxis.core.Fluxis;
import com.skywind.fluxis.core.model.MarketItem;
import com.skywind.fluxis.core.util.VersionUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarketManager {

    private final Fluxis core;
    private final Map<Material, MarketItem> marketItems = new ConcurrentHashMap<>();

    private static final double ACTIVITY_BASE = 1.0;

    private long serverTotalTransactions = 0;
    private double serverTotalVolume = 0.0;
    private String lastTransactionTime = "N/A";
    private long nextUpdateTime = System.currentTimeMillis() + 60_000;

    public MarketManager(Fluxis core) {
        this.core = core;
        initDefaultItems();
    }

    public Map<Material, MarketItem> getMarketItems() {
        return this.marketItems;
    }

    private void initDefaultItems() {
        registerItemSafe("OAK_LOG",       4.0,     1.0,    15.0,   1000, 1);
        registerItemSafe("COBBLESTONE",   1.0,     0.2,     5.0,   5000, 1);
        registerItemSafe("DIRT",          0.5,     0.1,     2.0,  10000, 1);
        registerItemSafe("SAND",          1.5,     0.5,     6.0,   5000, 1);
        registerItemSafe("COAL",          8.0,     2.0,    30.0,    500, 2);
        registerItemSafe("IRON_INGOT",   50.0,    15.0,   150.0,    250, 2);
        registerItemSafe("WHEAT_SEEDS",   2.0,     0.5,    10.0,   1000, 2);
        registerItemSafe("OAK_SAPLING",  10.0,     2.0,    50.0,    100, 2);
        registerItemSafe("BREAD",        25.0,    10.0,    80.0,    100, 3);
        registerItemSafe("COOKED_BEEF",  40.0,    15.0,   120.0,     50, 3);
        registerItemSafe("CARROT",       15.0,     5.0,    45.0,    200, 3);
        registerItemSafe("POTATO",       15.0,     5.0,    45.0,    200, 3);
        registerItemSafe("STONE_PICKAXE",15.0,     5.0,    50.0,    100, 4);
        registerItemSafe("IRON_PICKAXE", 150.0,   50.0,   500.0,     25, 4);
        registerItemSafe("MACE",        5000.0, 2000.0, 25000.0,      5, 5);
        registerItemSafe("HEAVY_CORE", 10000.0, 5000.0, 50000.0,      2, 5);
        registerItemSafe("BREEZE_ROD",  150.0,   50.0,  1000.0,      50, 2);
    }

    public void registerItemSafe(String materialName, double basePrice, double minPrice, double maxPrice, int stock, int tier) {
        Material mat = VersionUtil.getSafeMaterial(materialName);
        if (mat == null) return;
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

        double volatility = core.getShopConfig() != null
                ? core.getShopConfig().getDouble("market.default_volatility", 0.005) : 0.005;
        double priceChange = 1.0 + (quantity * volatility * getActivityMultiplier());

        item.setCurrentPrice(Math.min(item.getCurrentPrice() * priceChange, item.getMaxPrice()));
        item.setBuyVolume(item.getBuyVolume() + quantity);
        item.setStock(item.getStock() - quantity);
        item.updateTrend();

        recordTransaction(item.getCurrentPrice() * quantity);
    }

    public void sellItem(Material material, int quantity) {
        MarketItem item = marketItems.get(material);
        if (item == null) return;

        double volatility = core.getShopConfig() != null
                ? core.getShopConfig().getDouble("market.default_volatility", 0.005) : 0.005;
        double priceChange = 1.0 - (quantity * volatility * getActivityMultiplier());

        item.setCurrentPrice(Math.max(item.getCurrentPrice() * priceChange, item.getMinPrice()));
        item.setSellVolume(item.getSellVolume() + quantity);
        item.setStock(item.getStock() + quantity);
        item.updateTrend();

        recordTransaction(item.getCurrentPrice() * quantity);
    }

    private void recordTransaction(double volume) {
        serverTotalTransactions++;
        serverTotalVolume += volume;
        lastTransactionTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public double getActivityMultiplier() {
        return ACTIVITY_BASE + (Bukkit.getOnlinePlayers().size() / 5.0 * 0.1);
    }

    public Material getMostTradedItem() {
        return marketItems.entrySet().stream()
                .max((a, b) -> (a.getValue().getBuyVolume() + a.getValue().getSellVolume())
                             - (b.getValue().getBuyVolume() + b.getValue().getSellVolume()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public long getServerTotalTransactions()  { return serverTotalTransactions; }
    public double getServerTotalVolume()      { return serverTotalVolume; }
    public int getOnlineTraderCount()         { return Bukkit.getOnlinePlayers().size(); }
    public String getLastTransactionTime()    { return lastTransactionTime; }

    public String getNextUpdateFormatted() {
        long diff = nextUpdateTime - System.currentTimeMillis();
        if (diff <= 0) return "Şimdi";
        return (diff / 1000) + "s";
    }

    public double calculateTaxRate(MarketItem item) {
        return switch (item.getTier()) {
            case 1 -> 0.02;
            case 2 -> 0.05;
            case 3 -> 0.08;
            case 4 -> 0.10;
            case 5 -> 0.15;
            default -> 0.05;
        };
    }
}