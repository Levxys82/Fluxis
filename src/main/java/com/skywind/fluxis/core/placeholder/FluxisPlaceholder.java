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

    @Override
    public @NotNull String getIdentifier() {
        return "fluxis";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SkyWind";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        // %fluxis_balance%
        if (params.equalsIgnoreCase("balance")) {
            return String.format("%.2f", core.getEconomyManager().getBalance(player.getUniqueId()));
        }

        // %fluxis_market_price_<material>%
        if (params.startsWith("market_price_")) {
            String matName = params.substring(13).toUpperCase();
            try {
                Material mat = Material.valueOf(matName);
                MarketItem item = core.getMarketManager().getMarketItems().get(mat);
                return item != null ? String.format("%.2f", item.getCurrentPrice()) : "0.00";
            } catch (IllegalArgumentException e) {
                return "Invalid Item";
            }
        }

        // %fluxis_market_trend_<material>%
        if (params.startsWith("market_trend_")) {
            String matName = params.substring(13).toUpperCase();
            try {
                Material mat = Material.valueOf(matName);
                MarketItem item = core.getMarketManager().getMarketItems().get(mat);
                return item != null ? item.getTrend().getFormatted() : "N/A";
            } catch (IllegalArgumentException e) {
                return "Invalid Item";
            }
        }

        // %fluxis_market_stock_<material>%
        if (params.startsWith("market_stock_")) {
            String matName = params.substring(13).toUpperCase();
            try {
                Material mat = Material.valueOf(matName);
                MarketItem item = core.getMarketManager().getMarketItems().get(mat);
                return item != null ? String.valueOf(item.getStock()) : "0";
            } catch (IllegalArgumentException e) {
                return "Invalid Item";
            }
        }

        return null;
    }
}
