package com.skywind.fluxis.action.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.skywind.fluxis.action.model.AuctionItem;
import com.skywind.fluxis.core.Fluxis;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {

    private final Fluxis core;
    private final List<AuctionItem> auctions = new CopyOnWriteArrayList<>();
    private final File dataFile;
    private final Gson gson;

    public AuctionManager(Fluxis core) {
        this.core = core;
        this.dataFile = new File(core.getDataFolder(), "auctions.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadAuctions();
    }

    public List<AuctionItem> getAuctions() {
        return auctions;
    }

    public void listAction(UUID sellerId, String sellerName, ItemStack item, double price) {
        AuctionItem auctionItem = AuctionItem.builder()
                .id(UUID.randomUUID())
                .sellerId(sellerId)
                .sellerName(sellerName)
                .itemStack(item)
                .price(price)
                .listedAt(System.currentTimeMillis())
                .build();
        auctions.add(auctionItem);
        saveAuctions();
    }

    public boolean buyAction(UUID buyerId, UUID auctionId) {
        AuctionItem auction = auctions.stream()
                .filter(a -> a.getId().equals(auctionId))
                .findFirst()
                .orElse(null);
        
        if (auction == null) return false;

        if (core.getEconomyManager().withdraw(buyerId, auction.getPrice())) {
            core.getEconomyManager().deposit(auction.getSellerId(), auction.getPrice());
            auctions.remove(auction);
            saveAuctions();
            return true;
        }
        return false;
    }

    public void saveAuctions() {
        if (core.getDatabaseManager() != null && core.getDatabaseManager().isMysqlEnabled()) {
            core.getDatabaseManager().saveJson("auctions", gson.toJson(auctions));
            return;
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(dataFile), StandardCharsets.UTF_8)) {
            gson.toJson(auctions, writer);
        } catch (IOException e) {
            core.getLogger().severe("Could not save auctions: " + e.getMessage());
        }
    }

    private void loadAuctions() {
        if (core.getDatabaseManager() != null && core.getDatabaseManager().isMysqlEnabled()) {
            String json = core.getDatabaseManager().loadJson("auctions");
            if (json == null || json.isBlank()) return;
            Type type = new TypeToken<ArrayList<AuctionItem>>() {}.getType();
            List<AuctionItem> loaded = gson.fromJson(json, type);
            if (loaded != null) auctions.addAll(loaded);
            return;
        }

        if (!dataFile.exists()) return;
        try (Reader reader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<ArrayList<AuctionItem>>() {}.getType();
            List<AuctionItem> loaded = gson.fromJson(reader, type);
            if (loaded != null) auctions.addAll(loaded);
        } catch (IOException e) {
            core.getLogger().severe("Could not load auctions: " + e.getMessage());
        }
    }
}
