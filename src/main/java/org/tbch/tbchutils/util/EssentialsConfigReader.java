package org.tbch.tbchutils.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.TBCHUtils;
import org.tbch.tbchutils.util.config.ConfigUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EssentialsConfigReader {

    private static final Plugin essPlugin = Bukkit.getServer().getPluginManager().getPlugin("Essentials");
    private static final JavaPlugin essentialsJava = (JavaPlugin) essPlugin;

    private static final double startingBalance;

    static {
        ConfigUtil generalConfig = new ConfigUtil(essentialsJava, "config.yml", false);
        generalConfig.load();
        startingBalance = generalConfig.getDouble("starting-balance", 0);
    }

    public static Map<String, Double> getAllBalances() {
        Map<String, Double> balances = new HashMap<>();
        File userdataFolder = new File("plugins/Essentials/userdata");
        if (!userdataFolder.exists() || !userdataFolder.isDirectory()) {
            return null;
        }

        for (File userFile : Objects.requireNonNull(userdataFolder.listFiles())) {
            if (!userFile.isFile() || !userFile.getName().endsWith(".yml")) continue;
            String filename = userFile.getName();
            String username = filename.replace(".yml", "");
            ConfigUtil userConfig = new ConfigUtil(essentialsJava, filename, true);
            userConfig.load();

            Object rawBalance = userConfig.getProperty("money");
            double balance;

            if (rawBalance instanceof Number) {
                balance = ((Number) rawBalance).doubleValue();
            } else {
                balance = startingBalance;
            }

            JavaPlugin instance = TBCHUtils.getInstance();
            File localUserFolder = new File(instance.getDataFolder(), "userdata");
            File localUserdata = new File(localUserFolder, filename);
            if (localUserdata.exists()) {
                ConfigUtil localUserConfig = new ConfigUtil(instance, filename, true);
                localUserConfig.load();
                username = localUserConfig.getProperty("actualUsername").toString();
            }

            balances.put(username, balance);
        }

        return balances;
    }

    public static Map<String, Object> getUserTimestamps(String username) {
        try {
            File file = new File("plugins/Essentials/userdata/" + username.toLowerCase() + ".yml");
            if (!file.exists()) {return new HashMap<>();}

            Yaml yaml = new Yaml();

            try (InputStream input = Files.newInputStream(file.toPath())) {
                Object rawData = yaml.load(input);
                if (!(rawData instanceof Map)) {return null;}

                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) rawData;
                Object rawTimestamps = data.get("timestamps");

                if (!(rawTimestamps instanceof Map)) {return null;}

                @SuppressWarnings("unchecked")
                Map<String, Object> timestamps = (Map<String, Object>) rawTimestamps;
                return timestamps;
            } catch (Exception strE) {
                strE.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
