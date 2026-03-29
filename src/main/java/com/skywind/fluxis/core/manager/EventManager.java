package com.skywind.fluxis.core.manager;

import com.skywind.fluxis.core.Fluxis;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EventManager {

    private final Fluxis core;
    @Getter
    private final List<FluxisEvent> events = new ArrayList<>();
    @Getter
    private boolean loopEnabled = false;
    private int cursor = 0;
    private BukkitTask loopTask;
    @Getter
    private double activeEconomyModifier = 1.0;
    private long modifierEndAt = 0L;

    public EventManager(Fluxis core) {
        this.core = core;
        reloadEvents();
    }

    public void reloadEvents() {
        events.clear();
        File dir = new File(core.getDataFolder(), "events");
        if (!dir.exists() && !dir.mkdirs()) return;

        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
            FluxisEvent evt = new FluxisEvent(
                file.getName(),
                yml.getString("title", "Market Event"),
                yml.getString("message", "A market event has started!"),
                yml.getDouble("reward", 0.0),
                yml.getDouble("economy_modifier", 1.0),
                yml.getInt("duration_seconds", 300)
            );
            events.add(evt);
        }
        events.sort(Comparator.comparing(FluxisEvent::fileName));
        cursor = 0;
    }

    public void toggleLoop() {
        if (loopEnabled) {
            stopLoop();
        } else {
            startLoop();
        }
    }

    public void startLoop() {
        if (loopEnabled) return;
        loopEnabled = true;
        long intervalSec = core.getIntegrationConfig().getLong("events.loop_interval_seconds", 900L);
        loopTask = Bukkit.getScheduler().runTaskTimer(core, this::triggerNextEvent, 20L, Math.max(20L, intervalSec * 20L));
    }

    public void stopLoop() {
        loopEnabled = false;
        if (loopTask != null) {
            loopTask.cancel();
            loopTask = null;
        }
    }

    public void triggerNextEvent() {
        if (events.isEmpty()) return;
        FluxisEvent evt = events.get(cursor);
        cursor = (cursor + 1) % events.size();
        triggerEvent(evt);
    }

    public void triggerEvent(FluxisEvent event) {
        double reward = event.reward();
        String title = colorize(event.title());
        String message = colorize(event.message());
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendTitle(title, message, 10, 60, 10);
            p.sendMessage(message);
            if (reward > 0) {
                core.getEconomyManager().deposit(p.getUniqueId(), reward);
            }
        }
        this.activeEconomyModifier = event.economyModifier();
        this.modifierEndAt = System.currentTimeMillis() + (event.durationSeconds() * 1000L);
    }

    public double getEffectiveEconomyModifier() {
        if (System.currentTimeMillis() > modifierEndAt) {
            activeEconomyModifier = 1.0;
        }
        return activeEconomyModifier;
    }

    private String colorize(String text) {
        return text == null ? "" : text.replace("&", "§");
    }

    public record FluxisEvent(
        String fileName,
        String title,
        String message,
        double reward,
        double economyModifier,
        int durationSeconds
    ) {}
}
