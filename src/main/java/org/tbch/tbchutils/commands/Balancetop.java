package org.tbch.tbchutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.util.EssentialsConfigReader;
import org.tbch.tbchutils.util.misc.ColorUtil;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Balancetop implements CommandExecutor {

    private final JavaPlugin plugin;

    public Balancetop (JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColorCodes("&cOnly players can run this command!"));
            return true;
        }

        Player player = (Player) sender;
        String integerBoundMessage = ColorUtil.translateColorCodes("&cChoose a positive integer up to 100 for the balance toplist.");
        int topAmount = 5;

        if (args.length >= 1) {
            try {
                topAmount = Integer.parseInt(args[0]);

                if (topAmount <= 0) {
                    player.sendMessage(integerBoundMessage);
                    return false;
                }

                topAmount = Math.min(topAmount, 100);

            } catch (NumberFormatException e) {
                return false;
            }
        }

        int finalTopAmount = topAmount;
        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, () -> {
            Map<String, Double> balances = EssentialsConfigReader.getAllBalances();
            List<Map.Entry<String, Double>> sorted = balances.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed()).limit(finalTopAmount).collect(Collectors.toList());

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                player.sendMessage(ColorUtil.translateColorCodes("&2Top &f" + finalTopAmount + "&2 Richest Players:"));

                int rank = 1;
                for (Map.Entry<String, Double> entry : sorted) {
                    double truncated = Math.floor(entry.getValue() * 100) / 100.0;
                    player.sendMessage(ColorUtil.translateColorCodes("&f" + rank + ". &2" + entry.getKey() +
                            "&f (&2" + truncated + " Dollars&f)"));
                    rank++;
                }
            });
        });
        return true;
    }
}
