package org.tbch.tbchutils.memory;

import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.util.config.ConfigUtil;

import java.util.HashMap;

public class ConfigMemory {

    private static final HashMap<String, ConfigUtil> correspondingConfigs = new HashMap<>();

    public static ConfigUtil getCorrespondingConfig(String username) {
        return correspondingConfigs.get(username);
    }

    public static void setCorrespondingConfig(JavaPlugin plugin, String username) {
        String filename = username.toLowerCase() + ".yml";
        ConfigUtil playerConfig = new ConfigUtil(plugin, filename, true);

        playerConfig.load();

        if (playerConfig.getProperty("actualUsername") == null) {
            playerConfig.setProperty("actualUsername", username);
        }
        if (playerConfig.getProperty("secondsPlayed") == null) {
            playerConfig.setProperty("secondsPlayed", 0);
            playerConfig.save();
        }
        correspondingConfigs.put(username, playerConfig);
    }

    public static void removeMemoryEntry(String username) {
        correspondingConfigs.remove(username);
    }
}
