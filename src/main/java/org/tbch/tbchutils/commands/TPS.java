package org.tbch.tbchutils.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.tbch.tbchutils.tasks.TPSMeasurer;

public class TPS implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int ticksToEvaluate = 100;
        /*if (args[0].isEmpty()) {
            ticksToEvaluate = 100;
        } else {
            try {
                int inputTicks = Integer.parseInt(args[0]);
                ticksToEvaluate = inputTicks;
            } catch (NumberFormatException e) {
                return false;
            }
        } */
        double tps = TPSMeasurer.getTPS(ticksToEvaluate); // average over last 100 ticks (~5 seconds)
        String color = (tps >= 18 ? "§a" : tps >= 15 ? "§e" : "§c");
        sender.sendMessage(color + String.format("Current TPS: %.2f", tps));
        return true;
    }
}
