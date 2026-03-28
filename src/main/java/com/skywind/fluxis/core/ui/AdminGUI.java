package com.skywind.fluxis.core.ui;

import com.skywind.fluxis.core.Fluxis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class AdminGUI {

    private final Fluxis core;

    public AdminGUI(Fluxis core) {
        this.core = core;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§cFluxis Admin Panel");

        // Reload Config
        inv.setItem(10, createItem(Material.PAPER, "§eReload Config", "§7Reload all settings from config.yml"));
        
        // Reset Market Prices
        inv.setItem(12, createItem(Material.BLAZE_POWDER, "§cReset Market Prices", "§7Reset all item prices to their base values"));
        
        // Server Info
        inv.setItem(14, createItem(Material.BEACON, "§bServer Activity", 
            "§7Current Multiplier: §f" + String.format("%.2f", core.getMarketManager().getActivityMultiplier()),
            "§7Online Players: §f" + Bukkit.getOnlinePlayers().size()));

        // Close
        inv.setItem(22, createItem(Material.BARRIER, "§4Close Menu", "§7Click to exit"));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> list = new ArrayList<>();
            for (String s : lore) list.add(s);
            meta.setLore(list);
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
