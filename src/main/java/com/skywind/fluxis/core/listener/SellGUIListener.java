package com.skywind.fluxis.core.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SellGUIListener implements Listener {

    private static final int[] BORDER_SLOTS = {
        0,1,2,3,4,5,6,7,8,        // Üst satır
        9,17,18,26,27,35,36,44,    // Kenarlar
        45,46,47,48,49,50,51,52,53 // Alt satır
    };

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("§8Fluxis Sell Vault (Drop Items)")) {

            int slot = e.getRawSlot();

            // Kenar slotlarına tıklamayı engelle
            for (int borderSlot : BORDER_SLOTS) {
                if (slot == borderSlot) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }
}