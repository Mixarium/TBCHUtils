package org.tbch.tbchutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.util.TimestampFormatter;
import org.tbch.tbchutils.util.EssentialsConfigReader;
import org.tbch.tbchutils.util.misc.ColorUtil;

import java.util.Arrays;
import java.util.Map;

public class Seen implements CommandExecutor {

    private final JavaPlugin plugin;

    public Seen(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtil.translateColorCodes("&cOnly players can run this command!"));
            return true;
        }

        if (args == null || args.length == 0) {return false;}

        Player player = (Player) sender;
        String testUsername = args[0];

        String actualUsername = Arrays.stream(Bukkit.getServer().getOnlinePlayers())
                .filter(p -> p.getName().equalsIgnoreCase(testUsername)).findFirst()
                .map(HumanEntity::getName).orElse("");

        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, () -> {
            String fallbackAlreadyOnlineMessage = ColorUtil.translateColorCodes("&2" + actualUsername + "&a is already online!");
            String absentInDatabaseMessage = ColorUtil.translateColorCodes("&8" + testUsername + "&7 could not be found in the database.");
            String internalErrorMessage = ColorUtil.translateColorCodes("&cAn Internal error has occurred. Try again later.");

            Map<String, Object> timestamps = EssentialsConfigReader.getUserTimestamps(testUsername);

            if (timestamps == null) {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    player.sendMessage(actualUsername.isEmpty() ? internalErrorMessage : fallbackAlreadyOnlineMessage);
                });
                return;
            }

            if (timestamps.isEmpty()) {
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                   player.sendMessage(absentInDatabaseMessage);
                });
                return;
            }

            if (!actualUsername.isEmpty()) {
                long lastLogin = ((Number) timestamps.get("login")).longValue();
                String timeDiff = TimestampFormatter.getFormattedTime(lastLogin, true, true);

                String rawAlreadyOnlineMessage = "&2" + actualUsername + "&a has been online for &2" + timeDiff + "&a!";

                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    player.sendMessage(ColorUtil.translateColorCodes(rawAlreadyOnlineMessage));
                });

            } else {
                long lastLogout = ((Number) timestamps.get("logout")).longValue();
                String formattedLogoutTime = TimestampFormatter.getFormattedTime(lastLogout, false, true);
                String timeDiff = TimestampFormatter.getFormattedTime(lastLogout, true, true) + " ago";

                String rawLogoutTimeMessage = "&3" + testUsername + "&b was last seen on &7" + formattedLogoutTime + " (UTC)";
                String timeDiffMessage = "&9(" + timeDiff + ")";

                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    player.sendMessage(ColorUtil.translateColorCodes(rawLogoutTimeMessage));
                    player.sendMessage(ColorUtil.translateColorCodes(timeDiffMessage));
                });
            }
        });

        return true;
    }
}
