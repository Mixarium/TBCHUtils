package org.tbch.tbchutils.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.util.log.LogUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WhitelistReader {

    public static List<String> getWhitelist(JavaPlugin plugin) {
        File whitelistFile = new File(plugin.getDataFolder().getParentFile().getParentFile(), "white-list.txt");
        List<String> names = new ArrayList<>();

        if (!whitelistFile.exists()) {
            return names;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(whitelistFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    names.add(line);
                }
            }
        } catch (IOException e) {
            LogUtil.logConsoleWarning("Could not read white-list.txt: " + e.getMessage());
        }
        return names;
    }

}
