package com.skywind.fluxis.core.ui;

import com.skywind.fluxis.core.Fluxis;
import com.skywind.fluxis.core.model.MarketItem;
import com.skywind.fluxis.plus.util.EconomyGraph;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MarketGUI {

    private final Fluxis core;
    private final Map<UUID, String> selectedGroup = new HashMap<>();

    public MarketGUI(Fluxis core) {
        this.core = core;
    }

    public void open(Player player) {
        String title = core.getShopMenuConfig().getString("menu.title", "&8Fluxis Shop Groups").replace("&", "§");
        int size = core.getShopMenuConfig().getInt("menu.size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);
        ConfigurationSection groups = core.getShopMenuConfig().getConfigurationSection("groups");
        if (groups != null) {
            int slot = 10;
            for (String groupId : groups.getKeys(false)) {
                if (slot >= size) break;
                inv.setItem(slot++, createGroupIcon(groupId));
            }
        }
        inv.setItem(Math.max(0, size - 5), createBalanceItem(player));
        player.openInventory(inv);
    }

    public void openGroup(Player player, String groupId) {
        selectedGroup.put(player.getUniqueId(), groupId);
        String groupDisplay = core.getShopMenuConfig().getString("groups." + groupId + ".display", groupId).replace("&", "§");
        Inventory inv = Bukkit.createInventory(null, 54, "§8Fluxis Market - " + groupDisplay);

        List<String> materials = core.getShopMenuConfig().getStringList("groups." + groupId + ".items");
        for (String materialName : materials) {
            Material material = Material.matchMaterial(materialName);
            if (material == null) continue;
            MarketItem item = core.getMarketManager().getMarketItems().get(material);
            if (item == null) continue;
            inv.addItem(createMarketIcon(item));
        }

        inv.setItem(45, createNavItem(Material.BARRIER, "§cBack to Groups"));
        inv.setItem(49, createBalanceItem(player));
        player.openInventory(inv);
    }

    public String getGroupIdFromIcon(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) return null;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(new NamespacedKey(core, "shop_group"), PersistentDataType.STRING);
    }

    public String getSelectedGroup(UUID uuid) {
        return selectedGroup.get(uuid);
    }

    public void reopenSelectedOrRoot(Player player) {
        String groupId = selectedGroup.get(player.getUniqueId());
        if (groupId != null) {
            openGroup(player, groupId);
            return;
        }
        open(player);
    }

    private ItemStack createBalanceItem(Player player) {
        double bal = core.getEconomyManager().getBalance(player.getUniqueId());
        ItemStack stack = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eYour Balance");
            List<String> lore = new ArrayList<>();
            lore.add("§7Current: §f" + String.format("%.2f", bal) + " Money");
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack createGroupIcon(String groupId) {
        String display = core.getShopMenuConfig().getString("groups." + groupId + ".display", groupId).replace("&", "§");
        String iconName = core.getShopMenuConfig().getString("groups." + groupId + ".icon", "CHEST");
        Material icon = Material.matchMaterial(iconName);
        if (icon == null) icon = Material.CHEST;

        ItemStack stack = new ItemStack(icon);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(display);
            List<String> lore = new ArrayList<>();
            lore.add("§7Click to open this group");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(new NamespacedKey(core, "shop_group"), PersistentDataType.STRING, groupId);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack createMarketIcon(MarketItem item) {
        ItemStack stack = new ItemStack(item.getMaterial());
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + formatName(item.getMaterial().name()));
            List<String> lore = new ArrayList<>();
            lore.add("§8Tier " + item.getTier());
            lore.add("");
            double sellMultiplier = core.getShopConfig().getDouble("market.sell_price_multiplier", 0.7);
            lore.add("§7Buy: §a" + String.format("%.2f", item.getBuyPrice()) + " Money");
            lore.add("§7Sell: §c" + String.format("%.2f", item.getSellPrice(sellMultiplier)) + " Money");
            lore.add("§7Stock: §f" + item.getStock());
            lore.add("§7Trend: " + item.getTrend().getFormatted());
            
            // Add Visual Graph (Fluxis+)
            if (item.getPriceHistory() != null && !item.getPriceHistory().isEmpty()) {
                lore.add("");
                lore.add("§7Performance (10m):");
                lore.add(" §8[" + EconomyGraph.generateSparkline(item.getPriceHistory()) + "§8]");
            }

            lore.add("");
            lore.add("§eLeft-Click to Buy (x1)");
            lore.add("§6Right-Click to Sell (x1)");
            lore.add("§8Shift-Click for x16");
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack createNavItem(Material material, String name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private String formatName(String name) {
        String[] split = name.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}
