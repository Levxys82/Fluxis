package com.skywind.fluxis.core.manager;

import com.skywind.fluxis.core.Fluxis;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.util.UUID;
import java.util.logging.Level;

public class EconomyManager {

    private final Fluxis core;
    private Economy economy;

    public EconomyManager(Fluxis core) {
        this.core = core;
        setupEconomy();
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = core.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            this.economy = rsp.getProvider();
        }
    }

    public boolean isReady() {
        return economy != null;
    }

    public Economy getProvider() {
        return economy;
    }

    public double getBalance(UUID uuid) {
        if (!isReady()) return 0.0;
        OfflinePlayer player = core.getServer().getOfflinePlayer(uuid);
        return economy.getBalance(player);
    }

    public boolean withdraw(UUID uuid, double amount) {
        if (!isReady() || amount <= 0) return false;
        OfflinePlayer player = core.getServer().getOfflinePlayer(uuid);
        if (economy.getBalance(player) < amount) {
            if (player.isOnline() && player.getPlayer() != null) {
                player.getPlayer().sendMessage(core.message("errors.not_enough_money", "&cYou don't have enough money!"));
            }
            return false;
        }
        var response = economy.withdrawPlayer(player, amount);
        if (!response.transactionSuccess()) {
            core.getLogger().log(Level.WARNING, "Vault withdraw failed for {0}: {1}",
                new Object[]{player.getName() != null ? player.getName() : uuid.toString(), response.errorMessage});
        }
        return response.transactionSuccess();
    }

    public void deposit(UUID uuid, double amount) {
        if (!isReady() || amount <= 0) return;
        OfflinePlayer player = core.getServer().getOfflinePlayer(uuid);
        var response = economy.depositPlayer(player, amount);
        if (!response.transactionSuccess()) {
            core.getLogger().log(Level.WARNING, "Vault deposit failed for {0}: {1}",
                new Object[]{player.getName() != null ? player.getName() : uuid.toString(), response.errorMessage});
        }
    } // ← deposit burада bitti

    public double getTotalEarnings(UUID uuid) {
        return 0.0;
    }

    public int getTotalTransactions(UUID uuid) {
        return 0;
    }

    public Material getTopSoldItem(UUID uuid) {
        return null;
    }
}