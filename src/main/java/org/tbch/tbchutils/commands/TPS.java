package org.tbch.tbchutils.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.tbch.tbchutils.tasks.TPSMeasurer;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/* admittedly, code was taken from Project-Poseidon's /tps command implementation
 * https://github.com/retromcorg/Project-Poseidon/commit/9435b4df9d3949a316d6dbd509e0a6324f3e772d */

public class TPS implements CommandExecutor {

    private final LinkedHashMap<String, Integer> intervals = new LinkedHashMap<>();

    public TPS() {
        // Define the intervals for TPS calculation
        intervals.put("5s", 5);
        intervals.put("30s", 30);
        intervals.put("1m", 60);
        intervals.put("5m", 300);
        intervals.put("10m", 600);
        intervals.put("15m", 900);
    }

    private double calculateAverage(LinkedList<Double> records, int seconds) {
        int size = Math.min(records.size(), seconds);
        if (size == 0) return 20.0;

        double total = 0;
        for (int i = 0; i < size; i++) {
            total += records.get(i);
        }
        return total / size;
    }

    private String formatTps(double tps) {
        String colorCode;
        if (tps >= 19) {
            colorCode = "§a";
        } else if (tps >= 15) {
            colorCode = "§e";
        } else {
            colorCode = "§c";
        }
        return colorCode + String.format("%.2f", tps);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        LinkedList<Double> tpsRecords = TPSMeasurer.getTpsRecords();
        StringBuilder message = new StringBuilder("§bServer TPS: ");

        // Calculate and format TPS for each interval dynamically
        for (Map.Entry<String, Integer> entry : intervals.entrySet()) {
            double averageTps = calculateAverage(tpsRecords, entry.getValue());
            message.append(formatTps(averageTps)).append(" (").append(entry.getKey()).append("), ");
        }

        // Remove the trailing comma and space
        if (message.length() > 0) {
            message.setLength(message.length() - 2);
        }

        sender.sendMessage(message.toString());
        return true;
    }
}
