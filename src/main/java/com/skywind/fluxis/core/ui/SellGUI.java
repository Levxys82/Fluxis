package com.skywind.fluxis.core.ui;

import com.skywind.fluxis.core.Fluxis;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SellGUI {

    private final Fluxis core;

    public SellGUI(Fluxis core) {
        this.core = core;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8Fluxis Sell Vault (Drop Items)");
        player.openInventory(inv);
    }
}
