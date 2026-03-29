package com.skywind.fluxis.core.placeholder;

import com.skywind.fluxis.core.Fluxis;
import com.skywind.fluxis.core.model.MarketItem;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class FluxisPlaceholder extends PlaceholderExpansion {

    private final Fluxis core;

    public FluxisPlaceholder(Fluxis core) {
        this.core = core;
    }

    @Override public @NotNull String getIdentifier() { return "fluxis"; }
    @Override public @NotNull String getAuthor()     { return "SkyWind"; }
    @Override public @NotNull String getVersion()    { return "1.0.0"; }
    @Override public boolean persist()               { return true; }

    @Override
    public boolean register() {
        if (!core.getConfig().getBoolean("placeholder.ecloud-verified", false)) {
            core.getLogger().warning("[Fluxis] Placeholder'lar devre dışı!");
            core.getLogger().warning("[Fluxis] Aktif etmek için: /papi ecloud download Fluxis");
            return false;
        }
        core.getLogger().info("[Fluxis] Placeholder'lar aktif.");
        return super.register();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        if (!core.getConfig().getBoolean("placeholder.ecloud-verified", false))
            return "§c/papi ecloud download Fluxis";

        // ── OYUNCU ───────────────────────────────────────────────────────────

        if (params.equalsIgnoreCase("balance"))
            return String.format("%.2f", core.getEconomyManager().getBalance(player.getUniqueId()));

        if (params.equalsIgnoreCase("balance_formatted"))
            return String.format("%,.2f", core.getEconomyManager().getBalance(player.getUniqueId()));

        if (params.equalsIgnoreCase("balance_fixed"))
            return formatFixed(core.getEconomyManager().getBalance(player.getUniqueId()));

        if (params.equalsIgnoreCase("total_earnings"))
            return String.format("%.2f", core.getEconomyManager().getTotalEarnings(player.getUniqueId()));

        if (params.equalsIgnoreCase("total_transactions"))
            return String.valueOf(core.getEconomyManager().getTotalTransactions(player.getUniqueId()));

        if (params.equalsIgnoreCase("top_sold_item")) {
            Material top = core.getEconomyManager().getTopSoldItem(player.getUniqueId());
            return top != null ? top.name() : "N/A";
        }

        // ── MARKET (MATERYAL BAZLI) ───────────────────────────────────────────

        if (params.startsWith("market_price_"))
            return getMarketString(params.substring(13), item -> String.format("%.2f", item.getCurrentPrice()));

        if (params.startsWith("market_trend_"))
            return getMarketString(params.substring(13), item -> item.getTrend().getFormatted());

        if (params.startsWith("market_stock_"))
            return getMarketString(params.substring(13), item -> String.valueOf(item.getStock()));

        if (params.startsWith("market_base_price_"))
            return getMarketString(params.substring(18), item -> String.format("%.2f", item.getBasePrice()));

        if (params.startsWith("market_min_price_"))
            return getMarketString(params.substring(17), item -> String.format("%.2f", item.getMinPrice()));

        if (params.startsWith("market_max_price_"))
            return getMarketString(params.substring(17), item -> String.format("%.2f", item.getMaxPrice()));

        // ── MARKET (GENEL) ────────────────────────────────────────────────────

        if (params.equalsIgnoreCase("multiplier"))
            return String.format("%.2f", core.getMarketManager().getActivityMultiplier());

        if (params.equalsIgnoreCase("market_item_count"))
            return String.valueOf(core.getMarketManager().getMarketItems().size());

        if (params.equalsIgnoreCase("most_traded_item")) {
            Material mat = core.getMarketManager().getMostTradedItem();
            return mat != null ? mat.name() : "N/A";
        }

        // ── SUNUCU ────────────────────────────────────────────────────────────

        if (params.equalsIgnoreCase("server_total_transactions"))
            return String.valueOf(core.getMarketManager().getServerTotalTransactions());

        if (params.equalsIgnoreCase("server_total_volume"))
            return String.format("%,.2f", core.getMarketManager().getServerTotalVolume());

        if (params.equalsIgnoreCase("online_traders"))
            return String.valueOf(core.getMarketManager().getOnlineTraderCount());

        // ── ZAMAN ─────────────────────────────────────────────────────────────

        if (params.equalsIgnoreCase("last_transaction_time"))
            return core.getMarketManager().getLastTransactionTime();

        if (params.equalsIgnoreCase("next_market_update"))
            return core.getMarketManager().getNextUpdateFormatted();

        return null;
    }

    // ── YARDIMCI ──────────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface MarketItemMapper {
        String apply(MarketItem item);
    }

    private String getMarketString(String matName, MarketItemMapper mapper) {
        try {
            Material mat = Material.valueOf(matName.toUpperCase());
            MarketItem item = core.getMarketManager().getMarketItems().get(mat);
            return item != null ? mapper.apply(item) : "0.00";
        } catch (IllegalArgumentException e) {
            return "Invalid Item";
        }
    }

    private String formatFixed(double amount) {
        if (amount >= 1_000_000_000) return String.format("%.1fB", amount / 1_000_000_000);
        if (amount >= 1_000_000)     return String.format("%.1fM", amount / 1_000_000);
        if (amount >= 1_000)         return String.format("%.1fK", amount / 1_000);
        return String.format("%.0f", amount);
    }
}