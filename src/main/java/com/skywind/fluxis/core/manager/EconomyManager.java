package com.skywind.fluxis.core.manager;

import com.skywind.fluxis.core.Fluxis;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class EconomyManager {
    
    private final Fluxis core;
    private final Map<UUID, Double> balances = new ConcurrentHashMap<>();

    public EconomyManager(Fluxis core) {
        this.core = core;
    }

    public double getBalance(UUID uuid) {
        double starting = core.getConfig().getDouble("economy.starting_balance", 0.0);
        return balances.getOrDefault(uuid, starting);
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, amount);
    }

    public boolean withdraw(UUID uuid, double amount) {
        double current = getBalance(uuid);
        if (current >= amount) {
            setBalance(uuid, current - amount);
            return true;
        }
        return false;
    }

    public void deposit(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }
}
