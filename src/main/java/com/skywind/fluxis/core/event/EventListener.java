package com.skywind.fluxis.core.event;

import com.skywind.fluxis.core.Fluxis;
import com.skywind.fluxis.core.manager.EventManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class EventListener implements Listener {

    private final Fluxis core;

    public EventListener(Fluxis core) {
        this.core = core;
    }

    @EventHandler
    public void onEventMenuClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().contains("Fluxis Events")) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player player)) return;
        if (!player.hasPermission("fluxis.admin")) return;
        if (event.getCurrentItem() == null || !event.getCurrentItem().hasItemMeta()) return;

        String name = event.getCurrentItem().getItemMeta().getDisplayName();
        if (name == null) return;

        if (name.startsWith("§eLoop:")) {
            core.getEventManager().toggleLoop();
            core.getEventGUI().open(player);
            return;
        }
        if (name.startsWith("§bTrigger Next Event")) {
            core.getEventManager().triggerNextEvent();
            core.getEventGUI().open(player);
            return;
        }
        if (name.startsWith("§fReload Event Files")) {
            core.getEventManager().reloadEvents();
            core.getEventGUI().open(player);
            return;
        }
        if (name.startsWith("§d")) {
            String fileName = name.replace("§d", "");
            EventManager.FluxisEvent eventDef = core.getEventManager().getEvents().stream()
                .filter(e -> e.fileName().equalsIgnoreCase(fileName))
                .findFirst()
                .orElse(null);
            if (eventDef != null) {
                core.getEventManager().triggerEvent(eventDef);
            }
            core.getEventGUI().open(player);
        }
    }
}
