package org.tbch.tbchutils.memory;

import de.fgtech.pomo4ka.AuthMe.AuthMe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.util.TeleportUtil;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class NoclipPendingMemory {
    private static final Map<Integer, Map<String, Object>> pendingTasks = new HashMap<>();

    public static Map<Integer, Map<String, Object>> getPendingTasks() {
        return pendingTasks;
    }

    public static Map<String, Object> getTaskInfo(Integer taskID) {
        return pendingTasks.get(taskID);
    }

    public static boolean isIDInTasks(Integer taskID) {
        return pendingTasks.containsKey(taskID);
    }

    public static void cancel(JavaPlugin plugin, Integer task) {
        if (pendingTasks.containsKey(task)) {
            pendingTasks.remove(task);
            plugin.getServer().getScheduler().cancelTask(task);
        }
    }

    public static void addTeleportTask(JavaPlugin plugin, String username, String worldName, long durationMins, double x, double y, double z) {
        Map<String, Object> taskInfo = new HashMap<>();
        taskInfo.put("username", username);
        taskInfo.put("worldName", worldName);
        taskInfo.put("timestamp", Instant.now().toEpochMilli() + (durationMins * 60 * 1000));
        taskInfo.put("targetX", x);
        taskInfo.put("targetY", y);
        taskInfo.put("targetZ", z);

        int[] taskID = new int[1];
        taskID[0] = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            pendingTasks.remove(taskID[0]);

            Player player = Bukkit.getServer().getPlayer(username);
            Plugin authMePlugin = Bukkit.getServer().getPluginManager().getPlugin("AuthMe");
            AuthMe authMeJP = (AuthMe) authMePlugin;

            if (player != null && player.isOnline() && authMeJP.getPlayercache().isPlayerAuthenticated(player)) {
                player.teleport(new Location(Bukkit.getServer().getWorld(worldName), x, y, z));
                player.sendMessage(TeleportUtil.getRandomMessage());
            }

        }, 20L * 60L * durationMins);

        pendingTasks.put(taskID[0], taskInfo);
    }

}
