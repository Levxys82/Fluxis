package com.skywind.fluxis.action.ui;

import com.skywind.fluxis.action.model.AuctionItem;
import com.skywind.fluxis.core.Fluxis;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class AuctionGUI {

    private final Fluxis core;
    private final Map<UUID, Integer> playerPage = new ConcurrentHashMap<>();

    public AuctionGUI(Fluxis core) {
        this.core = core;
    }

    public void open(Player player, int page) {
        playerPage.put(player.getUniqueId(), page);
        Inventory inv = Bukkit.createInventory(null, 54, "§8Fluxis Action (Page " + (page + 1) + ")");

        List<AuctionItem> allAuctions = core.getAuctionModule().getAuctionManager().getAuctions();
        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, allAuctions.size());

        for (int i = startIndex; i < endIndex; i++) {
            inv.addItem(createAuctionIcon(allAuctions.get(i)));
        }

        // Pagination buttons
        if (page > 0) {
            inv.setItem(45, createButton(Material.ARROW, "§ePrevious Page"));
        }
        if (endIndex < allAuctions.size()) {
            inv.setItem(53, createButton(Material.ARROW, "§eNext Page"));
        }

        // Info button
        inv.setItem(49, createButton(Material.PAPER, "§fTotal Auctions: §e" + allAuctions.size()));

        player.openInventory(inv);
    }

    private ItemStack createAuctionIcon(AuctionItem auction) {
        ItemStack stack = auction.getItemStack().clone();
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.add("");
            lore.add("§7Seller: §e" + auction.getSellerName());
            lore.add("§7Price: §a" + String.format("%.2f", auction.getPrice()) + " Money");
            lore.add("");
            lore.add("§eClick to Purchase");
            lore.add("§8ID: " + auction.getId().toString().substring(0, 8));
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack createButton(Material material, String name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public int getPage(UUID uuid) {
        return playerPage.getOrDefault(uuid, 0);
    }
}
