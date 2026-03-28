package com.skywind.fluxis.trade.listener;

import com.skywind.fluxis.core.Fluxis;
import com.skywind.fluxis.trade.manager.TradeSession;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class TradeListener implements Listener {

    private final Fluxis core;

    public TradeListener(Fluxis core) {
        this.core = core;
    }

    @EventHandler
    public void onTradeClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().contains("Trade:")) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        TradeSession session = core.getTradeModule().getTradeManager().getSession(player.getUniqueId());
        if (session == null) return;

        int slot = event.getSlot();

        // Prevent moving separator items
        if (slot % 9 == 4) {
            event.setCancelled(true);
            return;
        }

        // P1 interaction
        if (session.isP1(player)) {
            if (slot == 45) { // Ready button
                event.setCancelled(true);
                session.setReady(player, true);
                return;
            }
            // Check if player is trying to place in P2 slots
            if (isP2Slot(slot)) {
                event.setCancelled(true);
            }
        }
        // P2 interaction
        else if (session.isP2(player)) {
            if (slot == 53) { // Ready button
                event.setCancelled(true);
                session.setReady(player, true);
                return;
            }
            // Check if player is trying to place in P1 slots
            if (isP1Slot(slot)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isP1Slot(int slot) {
        int col = slot % 9;
        return col < 4 && slot < 36;
    }

    private boolean isP2Slot(int slot) {
        int col = slot % 9;
        return col > 4 && slot < 36;
    }

    @EventHandler
    public void onTradeClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().contains("Trade:")) return;
        if (!(event.getPlayer() instanceof Player player)) return;

        TradeSession session = core.getTradeModule().getTradeManager().getSession(player.getUniqueId());
        if (session != null) {
            session.cancel(player);
            core.getTradeModule().getTradeManager().removeSession(player.getUniqueId());
        }
    }
}
