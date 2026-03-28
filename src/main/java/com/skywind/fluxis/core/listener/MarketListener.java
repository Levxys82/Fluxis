package com.skywind.fluxis.core.listener;

import com.skywind.fluxis.core.Fluxis;
import com.skywind.fluxis.core.model.MarketItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class MarketListener implements Listener {

    private final Fluxis core;

    public MarketListener(Fluxis core) {
        this.core = core;
    }

    @EventHandler
    public void onMarketClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.contains("Fluxis Dynamic Market") && !title.contains("Fluxis Admin Panel")) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Admin Panel Logic
        if (title.contains("Fluxis Admin Panel")) {
            Material mat = clicked.getType();
            if (mat == Material.PAPER) {
                core.reloadConfig();
                player.sendMessage("§a[Fluxis] Config reloaded!");
                player.closeInventory();
            } else if (mat == Material.BLAZE_POWDER) {
                core.getMarketManager().getMarketItems().values().forEach(item -> {
                    item.setCurrentPrice(item.getBasePrice());
                    item.setBuyVolume(0);
                    item.setSellVolume(0);
                    item.getPriceHistory().clear();
                });
                player.sendMessage("§a[Fluxis] Market prices reset to base values!");
                player.closeInventory();
            } else if (mat == Material.BARRIER) {
                player.closeInventory();
            }
            return;
        }

        // Market Logic
        Material mat = clicked.getType();
        if (mat == Material.GOLD_NUGGET && event.getSlot() == 31) return;

        MarketItem item = core.getMarketManager().getMarketItems().get(mat);
        if (item == null) return;

        int quantity = event.isShiftClick() ? 16 : 1;

        if (event.isLeftClick()) {
            if (item.getStock() < quantity) {
                player.sendMessage("§cNot enough stock in the market!");
                return;
            }
            double totalCost = item.getBuyPrice() * quantity;
            if (core.getEconomyManager().withdraw(player.getUniqueId(), totalCost)) {
                player.getInventory().addItem(new ItemStack(mat, quantity));
                core.getMarketManager().buyItem(mat, quantity);
                player.sendMessage("§aBought " + quantity + "x " + mat.name() + " for " + String.format("%.2f", totalCost) + " Money");
                core.getMarketGUI().open(player);
            } else {
                player.sendMessage("§cYou don't have enough money!");
            }
        } else if (event.isRightClick()) {
            if (player.getInventory().containsAtLeast(new ItemStack(mat), quantity)) {
                double sellMultiplier = core.getConfig().getDouble("market.sell_price_multiplier", 0.7);
                double totalValue = item.getSellPrice(sellMultiplier) * quantity;
                player.getInventory().removeItem(new ItemStack(mat, quantity));
                core.getEconomyManager().deposit(player.getUniqueId(), totalValue);
                core.getMarketManager().sellItem(mat, quantity);
                player.sendMessage("§aSold " + quantity + "x " + mat.name() + " for " + String.format("%.2f", totalValue) + " Money");
                core.getMarketGUI().open(player);
            } else {
                player.sendMessage("§cYou don't have enough " + mat.name() + " to sell!");
            }
        }
    }

    @EventHandler
    public void onSellClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().contains("Fluxis Sell Vault")) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        double totalEarned = 0;
        int itemsSold = 0;
        double sellMultiplier = core.getConfig().getDouble("market.sell_price_multiplier", 0.7);

        for (ItemStack stack : event.getInventory().getContents()) {
            if (stack == null || stack.getType() == Material.AIR) continue;

            MarketItem item = core.getMarketManager().getMarketItems().get(stack.getType());
            if (item != null) {
                double value = item.getSellPrice(sellMultiplier) * stack.getAmount();
                totalEarned += value;
                itemsSold += stack.getAmount();
                core.getMarketManager().sellItem(stack.getType(), stack.getAmount());
            } else {
                player.getInventory().addItem(stack).values().forEach(remaining -> {
                    player.getWorld().dropItemNaturally(player.getLocation(), remaining);
                });
            }
        }

        if (totalEarned > 0) {
            core.getEconomyManager().deposit(player.getUniqueId(), totalEarned);
            player.sendMessage("§aFluxis Vault: Sold " + itemsSold + " items for " + String.format("%.2f", totalEarned) + " Money");
        }
    }
}
