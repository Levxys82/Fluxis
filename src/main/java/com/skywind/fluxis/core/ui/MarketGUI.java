package com.skywind.fluxis.core.ui;

import com.skywind.fluxis.core.Fluxis;
import com.skywind.fluxis.core.model.MarketItem;
import com.skywind.fluxis.plus.util.EconomyGraph;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MarketGUI {

    private final Fluxis core;

    public MarketGUI(Fluxis core) {
        this.core = core;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8Fluxis Dynamic Market");
        core.getMarketManager().getMarketItems().forEach((material, item) -> {
            inv.addItem(createMarketIcon(item));
        });
        inv.setItem(31, createBalanceItem(player));
        player.openInventory(inv);
    }

    private ItemStack createBalanceItem(Player player) {
        double bal = core.getEconomyManager().getBalance(player.getUniqueId());
        ItemStack stack = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eYour Balance");
            List<String> lore = new ArrayList<>();
            lore.add("§7Current: §f" + String.format("%.2f", bal) + " Money");
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack createMarketIcon(MarketItem item) {
        ItemStack stack = new ItemStack(item.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + formatName(item.getMaterial().name()));
            List<String> lore = new ArrayList<>();
            lore.add("§8Tier " + item.getTier());
            lore.add("");
            double sellMultiplier = core.getConfig().getDouble("market.sell_price_multiplier", 0.7);
            lore.add("§7Buy: §a" + String.format("%.2f", item.getBuyPrice()) + " Money");
            lore.add("§7Sell: §c" + String.format("%.2f", item.getSellPrice(sellMultiplier)) + " Money");
            lore.add("§7Stock: §f" + item.getStock());
            lore.add("§7Trend: " + item.getTrend().getFormatted());
            
            // Add Visual Graph (Fluxis+)
            if (item.getPriceHistory() != null && !item.getPriceHistory().isEmpty()) {
                lore.add("");
                lore.add("§7Performance (10m):");
                lore.add(" §8[" + EconomyGraph.generateSparkline(item.getPriceHistory()) + "§8]");
            }

            lore.add("");
            lore.add("§eLeft-Click to Buy (x1)");
            lore.add("§6Right-Click to Sell (x1)");
            lore.add("§8Shift-Click for x16");
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String formatName(String name) {
        String[] split = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}
