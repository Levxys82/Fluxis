package com.skywind.fluxis.action.ui;

import com.skywind.fluxis.action.model.AuctionItem;
import com.skywind.fluxis.core.Fluxis;
import org.bukkit.NamespacedKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class AuctionGUI {

    private final Fluxis core;
    private final Map<UUID, Integer> playerPage = new ConcurrentHashMap<>();
    private final Map<UUID, TimeFilter> playerTimeFilter = new ConcurrentHashMap<>();
    private final Map<UUID, PriceFilter> playerPriceFilter = new ConcurrentHashMap<>();

    public enum TimeFilter {
        ALL("All"),
        NEWEST("Newest"),
        OLDEST("Oldest");

        private final String label;
        TimeFilter(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum PriceFilter {
        ALL("All"),
        HIGHEST("Highest"),
        LOWEST("Lowest");

        private final String label;
        PriceFilter(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public AuctionGUI(Fluxis core) {
        this.core = core;
    }

    public void open(Player player, int page) {
        UUID uuid = player.getUniqueId();
        List<AuctionItem> allAuctions = getFilteredAuctions(uuid);
        int maxPage = allAuctions.isEmpty() ? 0 : (allAuctions.size() - 1) / 45;
        int safePage = Math.max(0, Math.min(page, maxPage));
        playerPage.put(uuid, safePage);
        Inventory inv = Bukkit.createInventory(null, 54, "§8Fluxis Action (Page " + (safePage + 1) + ")");

        int startIndex = safePage * 45;
        int endIndex = Math.min(startIndex + 45, allAuctions.size());

        for (int i = startIndex; i < endIndex; i++) {
            inv.addItem(createAuctionIcon(allAuctions.get(i)));
        }

        // Pagination buttons
        if (safePage > 0) {
            inv.setItem(45, createButton(Material.ARROW, "§ePrevious Page"));
        }
        if (endIndex < allAuctions.size()) {
            inv.setItem(53, createButton(Material.ARROW, "§eNext Page"));
        }

        inv.setItem(46, createButton(Material.CLOCK, "§bTime Filter: §f" + getTimeFilter(uuid).getLabel(), "§7Click to cycle: All -> Newest -> Oldest"));
        inv.setItem(52, createButton(Material.GOLD_INGOT, "§6Price Filter: §f" + getPriceFilter(uuid).getLabel(), "§7Click to cycle: All -> Highest -> Lowest"));

        // Info + reset button
        inv.setItem(49, createButton(Material.PAPER,
            "§fFiltered: §e" + allAuctions.size() + "§7/§e" + core.getAuctionModule().getAuctionManager().getAuctions().size(),
            "§7Click to reset filters"));

        player.openInventory(inv);
    }

    private List<AuctionItem> getFilteredAuctions(UUID uuid) {
        List<AuctionItem> auctions = new ArrayList<>(core.getAuctionModule().getAuctionManager().getAuctions());
        TimeFilter timeFilter = getTimeFilter(uuid);
        PriceFilter priceFilter = getPriceFilter(uuid);

        Comparator<AuctionItem> comparator = null;

        if (timeFilter == TimeFilter.NEWEST) {
            comparator = Comparator.comparingLong(AuctionItem::getListedAt).reversed();
        } else if (timeFilter == TimeFilter.OLDEST) {
            comparator = Comparator.comparingLong(AuctionItem::getListedAt);
        }

        Comparator<AuctionItem> priceComparator = null;
        if (priceFilter == PriceFilter.HIGHEST) {
            priceComparator = Comparator.comparingDouble(AuctionItem::getPrice).reversed();
        } else if (priceFilter == PriceFilter.LOWEST) {
            priceComparator = Comparator.comparingDouble(AuctionItem::getPrice);
        }

        if (comparator == null && priceComparator != null) {
            comparator = priceComparator;
        } else if (comparator != null && priceComparator != null) {
            comparator = comparator.thenComparing(priceComparator);
        }

        if (comparator != null) {
            auctions.sort(comparator);
        }
        return auctions;
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
            meta.getPersistentDataContainer().set(new NamespacedKey(core, "auction_id"), PersistentDataType.STRING, auction.getId().toString());
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack createButton(Material material, String name) {
        return createButton(material, name, new String[0]);
    }

    private ItemStack createButton(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                List<String> loreList = new ArrayList<>();
                for (String line : lore) loreList.add(line);
                meta.setLore(loreList);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public int getPage(UUID uuid) {
        return playerPage.getOrDefault(uuid, 0);
    }

    public TimeFilter getTimeFilter(UUID uuid) {
        return playerTimeFilter.getOrDefault(uuid, TimeFilter.ALL);
    }

    public PriceFilter getPriceFilter(UUID uuid) {
        return playerPriceFilter.getOrDefault(uuid, PriceFilter.ALL);
    }

    public void cycleTimeFilter(UUID uuid) {
        TimeFilter current = getTimeFilter(uuid);
        TimeFilter next = switch (current) {
            case ALL -> TimeFilter.NEWEST;
            case NEWEST -> TimeFilter.OLDEST;
            case OLDEST -> TimeFilter.ALL;
        };
        playerTimeFilter.put(uuid, next);
    }

    public void cyclePriceFilter(UUID uuid) {
        PriceFilter current = getPriceFilter(uuid);
        PriceFilter next = switch (current) {
            case ALL -> PriceFilter.HIGHEST;
            case HIGHEST -> PriceFilter.LOWEST;
            case LOWEST -> PriceFilter.ALL;
        };
        playerPriceFilter.put(uuid, next);
    }

    public void resetFilters(UUID uuid) {
        playerTimeFilter.put(uuid, TimeFilter.ALL);
        playerPriceFilter.put(uuid, PriceFilter.ALL);
    }
}
