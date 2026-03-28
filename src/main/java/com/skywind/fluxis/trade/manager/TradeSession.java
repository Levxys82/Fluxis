package com.skywind.fluxis.trade.manager;

import com.skywind.fluxis.core.Fluxis;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TradeSession {

    private final Fluxis core;
    @Getter
    private final Player p1;
    @Getter
    private final Player p2;
    private final Inventory inventory;

    private boolean p1Ready = false;
    private boolean p2Ready = false;

    // Slots for P1: 0-3, 9-12, 18-21, 27-30 (Left 4 columns)
    // Slots for P2: 5-8, 14-17, 23-26, 32-35 (Right 4 columns)
    // Separator: Column 4 (Slots 4, 13, 22, 31, 40, 49)
    // Buttons: P1 (45), P2 (53)

    public TradeSession(Fluxis core, Player p1, Player p2) {
        this.core = core;
        this.p1 = p1;
        this.p2 = p2;
        this.inventory = Bukkit.createInventory(null, 54, "§8Trade: " + p1.getName() + " & " + p2.getName());
        setupInventory();
    }

    private void setupInventory() {
        ItemStack separator = createItem(Material.IRON_BARS, "§7< Fluxis Trade >");
        for (int i = 4; i < 54; i += 9) inventory.setItem(i, separator);
        
        updateButtons();
    }

    public void open() {
        p1.openInventory(inventory);
        p2.openInventory(inventory);
    }

    public void setReady(Player player, boolean ready) {
        if (player.equals(p1)) p1Ready = ready;
        else if (player.equals(p2)) p2Ready = ready;

        updateButtons();
        checkTrade();
    }

    private void updateButtons() {
        inventory.setItem(45, createItem(p1Ready ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, 
                p1Ready ? "§a" + p1.getName() + " is Ready!" : "§c" + p1.getName() + " not ready."));
        inventory.setItem(53, createItem(p2Ready ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE, 
                p2Ready ? "§a" + p2.getName() + " is Ready!" : "§c" + p2.getName() + " not ready."));
    }

    private void checkTrade() {
        if (p1Ready && p2Ready) {
            completeTrade();
        }
    }

    private void completeTrade() {
        // Exchange items
        exchange(p1, p2, true); // P1 to P2
        exchange(p2, p1, false); // P2 to P1

        p1.closeInventory();
        p2.closeInventory();
        p1.sendMessage("§aTrade completed successfully!");
        p2.sendMessage("§aTrade completed successfully!");
    }

    private void exchange(Player source, Player target, boolean p1ToP2) {
        int[] slots = p1ToP2 ? new int[]{0,1,2,3, 9,10,11,12, 18,19,20,21, 27,28,29,30} : 
                                new int[]{5,6,7,8, 14,15,16,17, 23,24,25,26, 32,33,34,35};
        
        for (int slot : slots) {
            ItemStack stack = inventory.getItem(slot);
            if (stack != null && stack.getType() != Material.AIR) {
                target.getInventory().addItem(stack).values().forEach(rem -> {
                    target.getWorld().dropItemNaturally(target.getLocation(), rem);
                });
            }
        }
    }

    public void cancel(Player canceller) {
        // Return items to respective players
        returnItems(p1, true);
        returnItems(p2, false);
        
        p1.sendMessage("§cTrade cancelled by " + canceller.getName());
        p2.sendMessage("§cTrade cancelled by " + canceller.getName());
        
        p1.closeInventory();
        p2.closeInventory();
    }

    private void returnItems(Player p, boolean isP1) {
        int[] slots = isP1 ? new int[]{0,1,2,3, 9,10,11,12, 18,19,20,21, 27,28,29,30} : 
                             new int[]{5,6,7,8, 14,15,16,17, 23,24,25,26, 32,33,34,35};
        
        for (int slot : slots) {
            ItemStack stack = inventory.getItem(slot);
            if (stack != null && stack.getType() != Material.AIR) {
                p.getInventory().addItem(stack).values().forEach(rem -> {
                    p.getWorld().dropItemNaturally(p.getLocation(), rem);
                });
            }
        }
    }

    private ItemStack createItem(Material mat, String name) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public boolean isP1(Player p) { return p.equals(p1); }
    public boolean isP2(Player p) { return p.equals(p2); }

    public int[] getP1Slots() { return new int[]{0,1,2,3, 9,10,11,12, 18,19,20,21, 27,28,29,30}; }
    public int[] getP2Slots() { return new int[]{5,6,7,8, 14,15,16,17, 23,24,25,26, 32,33,34,35}; }
}
