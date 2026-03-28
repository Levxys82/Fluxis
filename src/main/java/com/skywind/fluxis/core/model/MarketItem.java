package com.skywind.fluxis.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketItem {
    private Material material;
    private double basePrice;
    private double currentPrice;
    private double minPrice;
    private double maxPrice;
    private int stock;
    private int tier; // 1 to 5
    
    // Tracking for dynamics
    private int buyVolume;
    private int sellVolume;
    private PriceTrend trend;

    // Price History (Last 10 points)
    @Builder.Default
    private List<Double> priceHistory = new ArrayList<>();

    public void addPricePoint(double price) {
        if (priceHistory == null) priceHistory = new ArrayList<>();
        priceHistory.add(price);
        if (priceHistory.size() > 10) {
            priceHistory.remove(0);
        }
    }

    public double getBuyPrice() {
        return currentPrice;
    }

    public double getSellPrice(double multiplier) {
        // Sell price is always lower than Buy price (Spread)
        return currentPrice * multiplier;
    }

    public void updateTrend() {
        if (buyVolume > sellVolume) {
            trend = PriceTrend.RISING;
        } else if (sellVolume > buyVolume) {
            trend = PriceTrend.FALLING;
        } else {
            trend = PriceTrend.STABLE;
        }
    }

    public enum PriceTrend {
        RISING("↑ Rising", "§a"),
        FALLING("↓ Falling", "§c"),
        STABLE("→ Stable", "§7");

        private final String display;
        private final String color;

        PriceTrend(String display, String color) {
            this.display = display;
            this.color = color;
        }

        public String getFormatted() {
            return color + display;
        }
    }
}
