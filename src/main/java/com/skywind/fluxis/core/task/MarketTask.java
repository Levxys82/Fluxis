package com.skywind.fluxis.core.task;

import com.skywind.fluxis.core.Fluxis;
import com.skywind.fluxis.core.model.MarketItem;
import org.bukkit.scheduler.BukkitRunnable;

public class MarketTask extends BukkitRunnable {

    private final Fluxis core;

    public MarketTask(Fluxis core) {
        this.core = core;
    }

    @Override
    public void run() {
        double stabilizationRate = core.getConfig().getDouble("market.stabilization_rate", 0.001);
        
        core.getMarketManager().getMarketItems().values().forEach(item -> {
            // Price Stabilization (Drift back to base price)
            double diff = item.getBasePrice() - item.getCurrentPrice();
            if (Math.abs(diff) > 0.01) {
                double drift = diff * stabilizationRate;
                item.setCurrentPrice(item.getCurrentPrice() + drift);
            }

            // Record History Snapshot
            item.addPricePoint(item.getCurrentPrice());

            // Slowly decay volume to reset trends
            if (item.getBuyVolume() > 0) item.setBuyVolume(item.getBuyVolume() - 1);
            if (item.getSellVolume() > 0) item.setSellVolume(item.getSellVolume() - 1);
            item.updateTrend();
        });
    }
}
