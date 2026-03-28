package com.skywind.fluxis.action.listener;

import com.skywind.fluxis.action.model.AuctionItem;
import com.skywind.fluxis.core.Fluxis;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class AuctionListener implements Listener {

    private final Fluxis core;

    public AuctionListener(Fluxis core) {
        this.core = core;
    }

    @EventHandler
    public void onAuctionClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().contains("Fluxis Action")) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) return;

        // Check for navigation buttons
        if (meta.getDisplayName().equals("§ePrevious Page")) {
            int page = core.getAuctionModule().getAuctionGUI().getPage(player.getUniqueId());
            core.getAuctionModule().getAuctionGUI().open(player, page - 1);
            return;
        }
        if (meta.getDisplayName().equals("§eNext Page")) {
            int page = core.getAuctionModule().getAuctionGUI().getPage(player.getUniqueId());
            core.getAuctionModule().getAuctionGUI().open(player, page + 1);
            return;
        }

        // Try to parse Auction ID from lore
        List<String> lore = meta.getLore();
        if (lore == null || lore.size() < 2) return;
        
        String lastLine = lore.get(lore.size() - 1);
        if (lastLine.startsWith("§8ID: ")) {
            String shortId = lastLine.substring(6);
            
            // Find full auction ID from current auctions
            AuctionItem auction = core.getAuctionModule().getAuctionManager().getAuctions().stream()
                    .filter(a -> a.getId().toString().startsWith(shortId))
                    .findFirst()
                    .orElse(null);

            if (auction == null) {
                player.sendMessage("§cAuction not found or already ended.");
                core.getAuctionModule().getAuctionGUI().open(player, core.getAuctionModule().getAuctionGUI().getPage(player.getUniqueId()));
                return;
            }

            if (auction.getSellerId().equals(player.getUniqueId())) {
                player.sendMessage("§cYou cannot buy your own item!");
                return;
            }

            if (core.getAuctionModule().getAuctionManager().buyAction(player.getUniqueId(), auction.getId())) {
                player.getInventory().addItem(auction.getItemStack());
                player.sendMessage("§aPurchase successful: §f" + auction.getItemStack().getType().name() + " for " + String.format("%.2f", auction.getPrice()) + " Money");
                core.getAuctionModule().getAuctionGUI().open(player, core.getAuctionModule().getAuctionGUI().getPage(player.getUniqueId()));
            } else {
                player.sendMessage("§cPurchase failed: Not enough money!");
            }
        }
    }
}
