package com.skywind.fluxis.action;

import com.skywind.fluxis.action.listener.AuctionListener;
import com.skywind.fluxis.action.manager.AuctionManager;
import com.skywind.fluxis.action.ui.AuctionGUI;
import com.skywind.fluxis.core.Fluxis;
import lombok.Getter;

public class FluxisAction {
    private final Fluxis core;

    @Getter
    private AuctionManager auctionManager;
    @Getter
    private AuctionGUI auctionGUI;

    public FluxisAction(Fluxis core) {
        this.core = core;
    }

    public void init() {
        core.getLogger().info("Fluxis Action (İhale) has been loaded!");
        this.auctionManager = new AuctionManager(core);
        this.auctionGUI = new AuctionGUI(core);

        // Register Auction Listener
        core.getServer().getPluginManager().registerEvents(new AuctionListener(core), core);
    }
}
