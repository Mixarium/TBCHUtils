package org.tbch.tbchutils.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.util.config.ConfigUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PlaytimeReader {

    public static Map<String, Long> getPlaytimeDatabase(JavaPlugin plugin) {
        Map<String, Long> playtimes = new HashMap<>();
        File userdataDir = new File(plugin.getDataFolder(), "userdata");

        if (!userdataDir.exists() || !userdataDir.isDirectory()) {
            return playtimes;
        }

        File[] files = userdataDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {return playtimes;}

        for (File file : files) {
            String username = file.getName().replace(".yml", "");
            ConfigUtil config = new ConfigUtil(plugin, file.getName(), true);
            config.load();
            String actualUsername = String.valueOf(config.getProperty("actualUsername"));
            Object raw = config.getProperty("secondsPlayed");

            if (raw instanceof Number) {
                playtimes.put(actualUsername, ((Number) raw).longValue());
            }
        }

        return playtimes;
    }
}
