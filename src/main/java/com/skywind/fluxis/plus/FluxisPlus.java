package com.skywind.fluxis.plus;

import com.skywind.fluxis.core.Fluxis;
import lombok.RequiredArgsConstructor;

public class FluxisPlus {
    private final Fluxis core;

    public FluxisPlus(Fluxis core) {
        this.core = core;
    }

    public void init() {
        core.getLogger().info("Fluxis+ (Ekonomi Grafik) has been loaded!");
        // Visual logic here (Graphs, Chart Lore, Enhanced UI)
    }
}
