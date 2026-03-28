package com.skywind.fluxis.trade.manager;

import com.skywind.fluxis.core.Fluxis;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TradeManager {

    private final Fluxis core;
    // Request source -> Target
    private final Map<UUID, UUID> requests = new ConcurrentHashMap<>();
    // Active sessions
    private final Map<UUID, TradeSession> activeTrades = new ConcurrentHashMap<>();

    public TradeManager(Fluxis core) {
        this.core = core;
    }

    public void sendRequest(Player source, Player target) {
        requests.put(source.getUniqueId(), target.getUniqueId());
        source.sendMessage("§aTrade request sent to " + target.getName());
        target.sendMessage("§e" + source.getName() + " wants to trade! Use /trade " + source.getName() + " to accept.");
    }

    public void acceptRequest(Player source, Player target) {
        if (requests.containsKey(target.getUniqueId()) && requests.get(target.getUniqueId()).equals(source.getUniqueId())) {
            requests.remove(target.getUniqueId());
            startTrade(source, target);
        } else {
            sendRequest(source, target);
        }
    }

    private void startTrade(Player p1, Player p2) {
        TradeSession session = new TradeSession(core, p1, p2);
        activeTrades.put(p1.getUniqueId(), session);
        activeTrades.put(p2.getUniqueId(), session);
        session.open();
    }

    public void removeSession(UUID uuid) {
        TradeSession session = activeTrades.remove(uuid);
        if (session != null) {
            activeTrades.remove(session.getP1().getUniqueId());
            activeTrades.remove(session.getP2().getUniqueId());
        }
    }

    public TradeSession getSession(UUID uuid) {
        return activeTrades.get(uuid);
    }
}
