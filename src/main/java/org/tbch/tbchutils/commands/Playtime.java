package org.tbch.tbchutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.memory.ConfigMemory;
import org.tbch.tbchutils.tasks.AsyncPlaytimeTop;
import org.tbch.tbchutils.util.TimestampFormatter;
import org.tbch.tbchutils.util.config.ConfigUtil;
import org.tbch.tbchutils.util.EssentialsConfigReader;
import org.tbch.tbchutils.util.misc.ColorUtil;

import java.util.Map;

public class Playtime implements CommandExecutor {

    private final JavaPlugin plugin;

    public Playtime(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColorCodes("&cOnly players can run this command!"));
            return true;
        }

        Player player = (Player) sender;
        String internalErrorMessage = ColorUtil.translateColorCodes("&cAn Internal error has occurred. Try again later.");

        if (args == null || args.length == 0) {
            String username;
            username = player.getName();

            Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, () -> {
                Map<String, Object> userTimestamps = EssentialsConfigReader.getUserTimestamps(username);

                if (userTimestamps == null) {
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                       player.sendMessage(internalErrorMessage);
                    });
                    return;
                }

                long lastLogin = ((Number) userTimestamps.get("login")).longValue();
                String formattedCurrentSessionTime = TimestampFormatter.getFormattedTime(lastLogin, true, false);

                ConfigUtil playerConfig = ConfigMemory.getCorrespondingConfig(username);
                Object rawSeconds = playerConfig.getProperty("secondsPlayed");
                int seconds;
                String formattedOverallTime;

                if (rawSeconds instanceof Number) {
                    seconds = ((Number) rawSeconds).intValue();
                    formattedOverallTime = TimestampFormatter.convertSeconds(seconds);
                } else {
                    formattedOverallTime = "0 minutes";
                }

                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    player.sendMessage(ColorUtil.translateColorCodes("&aCurrent session: &2" + formattedCurrentSessionTime));
                    player.sendMessage(ColorUtil.translateColorCodes("&bTotal time: &3" + formattedOverallTime));
                });
            });
            return true;
        }

        if (args[0].equalsIgnoreCase("top")) {
            String integerBoundMessage = ColorUtil.translateColorCodes("&cChoose a positive integer up to 15 for the playtime toplist.");
            int topAmount = 5;

            if (args.length >= 2) {
                try {
                    topAmount = Integer.parseInt(args[1]);

                    if (topAmount <= 0) {
                        player.sendMessage(integerBoundMessage);
                        return false;
                    }

                    topAmount = Math.min(topAmount, 15);

                } catch (NumberFormatException e) {
                    return false;
                }
            }

            Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new AsyncPlaytimeTop(plugin, player, topAmount));
            return true;
        }

        return false;
    }
}
