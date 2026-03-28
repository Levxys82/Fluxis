package com.skywind.fluxis.core.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.skywind.fluxis.core.Fluxis;
import com.skywind.fluxis.core.model.MarketItem;
import org.bukkit.Material;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private final Fluxis core;
    private final File dataFile;
    private final Gson gson;

    public DataManager(Fluxis core) {
        this.core = core;
        this.dataFile = new File(core.getDataFolder(), "market_memory.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        if (!core.getDataFolder().exists()) {
            core.getDataFolder().mkdirs();
        }
    }

    public void saveMarketData(Map<Material, MarketItem> marketItems) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(dataFile), StandardCharsets.UTF_8)) {
            gson.toJson(marketItems, writer);
        } catch (IOException e) {
            core.getLogger().severe("Could not save market memory: " + e.getMessage());
        }
    }

    public Map<Material, MarketItem> loadMarketData() {
        if (!dataFile.exists()) return new ConcurrentHashMap<>();

        try (Reader reader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<ConcurrentHashMap<Material, MarketItem>>() {}.getType();
            Map<Material, MarketItem> loadedItems = gson.fromJson(reader, type);
            return loadedItems != null ? loadedItems : new ConcurrentHashMap<>();
        } catch (IOException e) {
            core.getLogger().severe("Could not load market memory: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }
}
