package com.skywind.fluxis.core.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;

public class VersionUtil {

    private static String version;
    private static boolean isFolia;

    static {
        try {
            version = Bukkit.getServer().getBukkitVersion().split("-")[0];
            Class.forName("io.papermc.paper.threadedregions.RegionScheduler");
            isFolia = true;
        } catch (Exception e) {
            isFolia = false;
        }
    }

    /**
     * Safely gets a material by name. Returns null if it doesn't exist in current version.
     */
    public static Material getSafeMaterial(String name) {
        try {
            return Material.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isFolia() {
        return isFolia;
    }

    public static String getVersion() {
        return version;
    }

    /**
     * Checks if current version is at least the target version (e.g. "1.21")
     */
    public static boolean isAtLeast(int major, int minor) {
        String[] parts = version.split("\\.");
        int currentMajor = Integer.parseInt(parts[0]);
        int currentMinor = Integer.parseInt(parts[1]);
        
        if (currentMajor > major) return true;
        return currentMajor == major && currentMinor >= minor;
    }
}
