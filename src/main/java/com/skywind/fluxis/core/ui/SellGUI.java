package com.skywind.fluxis.core.ui;

import com.skywind.fluxis.core.Fluxis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SellGUI {
    private final Fluxis core;

    public SellGUI(Fluxis core) {
        this.core = core;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Fluxis Sell Vault (Drop Items)");

        ItemStack border = createBorderItem();

        // Üst satır (0-8)
        for (int i = 0; i <= 8; i++) inv.setItem(i, border);

        // Alt satır (45-53)
        for (int i = 45; i <= 53; i++) inv.setItem(i, border);

        // Sol kenar
        for (int i = 9; i <= 36; i += 9) inv.setItem(i, border);

        // Sağ kenar
        for (int i = 17; i <= 44; i += 9) inv.setItem(i, border);

        player.openInventory(inv);
    }

    private ItemStack createBorderItem() {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§r"); // İsim görünmesin
            glass.setItemMeta(meta);
        }
        return glass;
    }
}