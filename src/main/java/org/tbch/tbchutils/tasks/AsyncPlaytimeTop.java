package org.tbch.tbchutils.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.util.PlaytimeReader;
import org.tbch.tbchutils.util.misc.ColorUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AsyncPlaytimeTop implements Runnable {

    private final JavaPlugin plugin;
    private final Player player;
    private final int topAmount;

    public AsyncPlaytimeTop(JavaPlugin plugin, Player player, int topAmount) {
        this.plugin = plugin;
        this.player = player;
        this.topAmount = topAmount;
    }

    @Override
    public void run() {
        Map<String, Long> playtimes = PlaytimeReader.getPlaytimeDatabase(plugin);

        List<Map.Entry<String, Long>> sorted = playtimes.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(topAmount).collect(Collectors.toList());

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            player.sendMessage(ColorUtil.translateColorCodes("&2---- Playtime Toplist ----"));
            player.sendMessage(ColorUtil.translateColorCodes("&8(measured in hours)"));
            for (Map.Entry<String, Long> entry : sorted) {
                long hours = entry.getValue() / 3600;
                player.sendMessage(ColorUtil.translateColorCodes("  &a" + hours + " &2- &a" + entry.getKey()));
            }
        });
    }

}
