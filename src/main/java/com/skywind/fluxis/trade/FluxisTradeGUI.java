package com.skywind.fluxis.trade;

import com.skywind.fluxis.core.Fluxis;
import com.skywind.fluxis.trade.listener.TradeListener;
import com.skywind.fluxis.trade.manager.TradeManager;
import lombok.Getter;

public class FluxisTradeGUI {
    private final Fluxis core;

    @Getter
    private TradeManager tradeManager;

    public FluxisTradeGUI(Fluxis core) {
        this.core = core;
    }

    public void init() {
        core.getLogger().info("Fluxis TradeGUI (Ticaret) has been loaded!");
        this.tradeManager = new TradeManager(core);

        // Register Trade Listener
        core.getServer().getPluginManager().registerEvents(new TradeListener(core), core);
    }
}
