package com.skywind.fluxis.core.event;

import com.skywind.fluxis.core.Fluxis;
import com.skywind.fluxis.core.manager.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EventGUI {

    private final Fluxis core;

    public EventGUI(Fluxis core) {
        this.core = core;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§5Fluxis Events");

        inv.setItem(10, createButton(core.getEventManager().isLoopEnabled() ? Material.LIME_DYE : Material.GRAY_DYE,
            "§eLoop: " + (core.getEventManager().isLoopEnabled() ? "§aON" : "§cOFF"),
            "§7Click to toggle loop mode"));
        inv.setItem(12, createButton(Material.CLOCK, "§bTrigger Next Event", "§7Run next event in cycle now"));
        inv.setItem(14, createButton(Material.PAPER, "§fReload Event Files", "§7Reload /plugins/Fluxis/events/*.yml"));

        int slot = 27;
        for (EventManager.FluxisEvent event : core.getEventManager().getEvents()) {
            if (slot >= 54) break;
            inv.setItem(slot++, createEventItem(event));
        }

        player.openInventory(inv);
    }

    private ItemStack createEventItem(EventManager.FluxisEvent event) {
        return createButton(Material.NETHER_STAR,
            "§d" + event.fileName(),
            "§7Title: §f" + event.title(),
            "§7Reward: §a" + String.format("%.2f", event.reward()),
            "§7Modifier: §e" + String.format("%.2f", event.economyModifier()),
            "§7Duration: §f" + event.durationSeconds() + "s",
            "§eClick to run now");
    }

    private ItemStack createButton(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            for (String line : lore) loreList.add(line);
            meta.setLore(loreList);
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
