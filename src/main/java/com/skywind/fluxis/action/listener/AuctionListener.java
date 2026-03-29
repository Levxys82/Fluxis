package com.skywind.fluxis.action.listener;

import com.skywind.fluxis.action.model.AuctionItem;
import com.skywind.fluxis.core.Fluxis;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

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
        if (meta.getDisplayName().startsWith("§bTime Filter:")) {
            core.getAuctionModule().getAuctionGUI().cycleTimeFilter(player.getUniqueId());
            core.getAuctionModule().getAuctionGUI().open(player, 0);
            return;
        }
        if (meta.getDisplayName().startsWith("§6Price Filter:")) {
            core.getAuctionModule().getAuctionGUI().cyclePriceFilter(player.getUniqueId());
            core.getAuctionModule().getAuctionGUI().open(player, 0);
            return;
        }
        if (meta.getDisplayName().startsWith("§fFiltered:")) {
            core.getAuctionModule().getAuctionGUI().resetFilters(player.getUniqueId());
            core.getAuctionModule().getAuctionGUI().open(player, 0);
            return;
        }

        // Try to parse Auction ID from lore
        AuctionItem auction = null;
        String fullId = meta.getPersistentDataContainer().get(new NamespacedKey(core, "auction_id"), PersistentDataType.STRING);
        if (fullId != null) {
            try {
                UUID auctionId = UUID.fromString(fullId);
                auction = core.getAuctionModule().getAuctionManager().getAuctions().stream()
                    .filter(a -> a.getId().equals(auctionId))
                    .findFirst()
                    .orElse(null);
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Backward compatibility for older listed items with lore short IDs
        if (auction == null) {
            List<String> lore = meta.getLore();
            if (lore == null || lore.size() < 2) return;
            String lastLine = lore.get(lore.size() - 1);
            if (!lastLine.startsWith("§8ID: ")) return;
            String shortId = lastLine.substring(6);
            auction = core.getAuctionModule().getAuctionManager().getAuctions().stream()
                .filter(a -> a.getId().toString().startsWith(shortId))
                .findFirst()
                .orElse(null);
        }

        if (auction == null) {
            player.sendMessage(core.message("auction.errors.not_found", "{prefix}&cAuction not found or already ended."));
            core.getAuctionModule().getAuctionGUI().open(player, core.getAuctionModule().getAuctionGUI().getPage(player.getUniqueId()));
            return;
        }

        if (!player.hasPermission("fluxis.auction.buy")) {
            player.sendMessage(core.message("errors.no_permission", "{prefix}&cYou don't have permission!"));
            return;
        }

        if (auction.getSellerId().equals(player.getUniqueId())) {
            player.sendMessage(core.message("auction.errors.self_buy", "{prefix}&cYou cannot buy your own item!"));
            return;
        }

        if (core.getAuctionModule().getAuctionManager().buyAction(player.getUniqueId(), auction.getId())) {
            player.getInventory().addItem(auction.getItemStack());
            String msg = core.message("auction.info.purchase_success", "{prefix}&aPurchase successful: &f{item} for {amount} {currency}");
            msg = msg.replace("{item}", auction.getItemStack().getType().name())
                .replace("{amount}", String.format("%.2f", auction.getPrice()))
                .replace("{currency}", core.getEconomyConfig().getString("currency.name", "Money"));
            player.sendMessage(msg);
            core.getAuctionModule().getAuctionGUI().open(player, core.getAuctionModule().getAuctionGUI().getPage(player.getUniqueId()));
        } else {
            player.sendMessage(core.message("auction.errors.purchase_failed", "{prefix}&cPurchase failed: Not enough money!"));
        }
    }
}
