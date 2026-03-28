package com.skywind.fluxis.plus.util;

import java.util.List;

public class EconomyGraph {

    /**
     * Generates a Sparkline graph for a list of values.
     * Example: ▃▄▅▇▆▅▃
     */
    public static String generateSparkline(List<Double> values) {
        if (values == null || values.isEmpty()) return "§8No data";

        char[] bars = {' ', '▂', '▃', '▄', '▅', '▆', '▇', '█'};
        double min = values.stream().min(Double::compare).orElse(0.0);
        double max = values.stream().max(Double::compare).orElse(1.0);
        double range = max - min;

        // Color based on overall trend
        String color = "§7";
        if (values.size() >= 2) {
            double first = values.get(0);
            double last = values.get(values.size() - 1);
            if (last > first) color = "§a";
            else if (last < first) color = "§c";
        }

        StringBuilder sb = new StringBuilder(color);
        for (double val : values) {
            int index = (range == 0) ? 4 : (int) (((val - min) / range) * (bars.length - 1));
            sb.append(bars[index]);
        }
        return sb.toString();
    }

    /**
     * Generates a multi-line ASCII graph for item lore.
     */
    public static List<String> generateLoreGraph(List<Double> values) {
        // We can make this more complex, but for now a colored sparkline + trend info is best for MC lore
        String spark = generateSparkline(values);
        
        // Color the sparkline based on overall trend
        String color = "§7";
        if (values.size() >= 2) {
            double first = values.get(0);
            double last = values.get(values.size() - 1);
            if (last > first) color = "§a";
            else if (last < first) color = "§c";
        }

        return List.of("§8[ " + color + spark + " §8]");
    }
}
