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
        boolean isAdminPanel = title.contains("Fluxis Admin Panel");
        boolean isGroupMenu = title.contains("Fluxis Shop Groups");
        boolean isMarketGroup = title.contains("Fluxis Market -");
        if (!isAdminPanel && !isGroupMenu && !isMarketGroup) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Admin Panel Logic
        if (isAdminPanel) {
            if (!player.hasPermission("fluxis.admin")) {
                player.sendMessage(core.message("errors.no_permission", "{prefix}&cYou don't have permission!"));
                player.closeInventory();
                return;
            }
            Material mat = clicked.getType();
            if (mat == Material.PAPER) {
                core.reloadAllConfigs();
                player.sendMessage(core.message("info.config_reloaded", "{prefix}&aAll Fluxis configs reloaded!"));
                player.closeInventory();
            } else if (mat == Material.BLAZE_POWDER) {
                core.getMarketManager().getMarketItems().values().forEach(item -> {
                    item.setCurrentPrice(item.getBasePrice());
                    item.setBuyVolume(0);
                    item.setSellVolume(0);
                    item.getPriceHistory().clear();
                });
                player.sendMessage(core.message("info.market_reset", "{prefix}&aMarket prices reset to base values!"));
                player.closeInventory();
            } else if (mat == Material.BARRIER) {
                player.closeInventory();
            }
            return;
        }

        if (isGroupMenu) {
            String groupId = core.getMarketGUI().getGroupIdFromIcon(clicked);
            if (groupId != null) {
                core.getMarketGUI().openGroup(player, groupId);
            }
            return;
        }

        // Market Logic
        Material mat = clicked.getType();
        if (mat == Material.GOLD_NUGGET) return;
        if (mat == Material.BARRIER) {
            core.getMarketGUI().open(player);
            return;
        }

        MarketItem item = core.getMarketManager().getMarketItems().get(mat);
        if (item == null) return;

        int quantity = event.isShiftClick() ? 16 : 1;

        if (event.isLeftClick()) {
            if (!player.hasPermission("fluxis.market.buy")) {
                player.sendMessage(core.message("errors.no_permission", "{prefix}&cYou don't have permission!"));
                return;
            }
            if (item.getStock() < quantity) {
                player.sendMessage(core.message("errors.not_enough_stock", "{prefix}&cNot enough stock in the market!"));
                return;
            }
            double totalCost = item.getBuyPrice() * quantity;
            double taxRate = core.getMarketManager().calculateTaxRate(item) * core.getEventManager().getEffectiveEconomyModifier();
            double taxAmount = totalCost * taxRate;
            double totalWithTax = totalCost + taxAmount;
            if (core.getEconomyManager().withdraw(player.getUniqueId(), totalWithTax)) {
                player.getInventory().addItem(new ItemStack(mat, quantity));
                core.getMarketManager().buyItem(mat, quantity);
                String msg = core.message("info.bought", "{prefix}&aBought {qty}x {item} for {amount} {currency}");
                msg = msg.replace("{qty}", String.valueOf(quantity))
                    .replace("{item}", mat.name())
                    .replace("{amount}", String.format("%.2f", totalWithTax))
                    .replace("{currency}", core.getEconomyConfig().getString("currency.name", "Money"));
                player.sendMessage(msg);
                String taxMsg = core.message("info.tax_applied", "{prefix}&7Tax: &f{tax} ({rate}%)");
                player.sendMessage(taxMsg
                    .replace("{tax}", String.format("%.2f", taxAmount))
                    .replace("{rate}", String.format("%.2f", taxRate * 100.0)));
                core.getMarketGUI().reopenSelectedOrRoot(player);
            } else {
                player.sendMessage(core.message("errors.not_enough_money", "{prefix}&cYou don't have enough money!"));
            }
        } else if (event.isRightClick()) {
            if (!player.hasPermission("fluxis.market.sell")) {
                player.sendMessage(core.message("errors.no_permission", "{prefix}&cYou don't have permission!"));
                return;
            }
            if (player.getInventory().containsAtLeast(new ItemStack(mat), quantity)) {
                double sellMultiplier = core.getShopConfig().getDouble("market.sell_price_multiplier", 0.7);
                double totalValue = item.getSellPrice(sellMultiplier) * quantity;
                player.getInventory().removeItem(new ItemStack(mat, quantity));
                core.getEconomyManager().deposit(player.getUniqueId(), totalValue);
                core.getMarketManager().sellItem(mat, quantity);
                String msg = core.message("info.sold", "{prefix}&aSold {qty}x {item} for {amount} {currency}");
                msg = msg.replace("{qty}", String.valueOf(quantity))
                    .replace("{item}", mat.name())
                    .replace("{amount}", String.format("%.2f", totalValue))
                    .replace("{currency}", core.getEconomyConfig().getString("currency.name", "Money"));
                player.sendMessage(msg);
                core.getMarketGUI().reopenSelectedOrRoot(player);
            } else {
                String msg = core.message("errors.not_enough_items", "{prefix}&cYou don't have enough {item} to sell!");
                player.sendMessage(msg.replace("{item}", mat.name()));
            }
        }
    }

    @EventHandler
    public void onSellClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().contains("Fluxis Sell Vault")) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        double totalEarned = 0;
        int itemsSold = 0;
        double sellMultiplier = core.getShopConfig().getDouble("market.sell_price_multiplier", 0.7);

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
            String msg = core.message("info.bulk_sold", "{prefix}&aFluxis Vault: Sold {qty} items for {amount} {currency}");
            msg = msg.replace("{qty}", String.valueOf(itemsSold))
                .replace("{amount}", String.format("%.2f", totalEarned))
                .replace("{currency}", core.getEconomyConfig().getString("currency.name", "Money"));
            player.sendMessage(msg);
        }
    }
}
