package org.tbch.tbchutils.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class OverallConfigHandler implements Runnable {

    private final JavaPlugin plugin;

    public OverallConfigHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        List<String> usernames = new ArrayList<>();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            usernames.add(player.getName());
        }
        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new AsyncPlaytimeHandler(usernames));
    }
}
