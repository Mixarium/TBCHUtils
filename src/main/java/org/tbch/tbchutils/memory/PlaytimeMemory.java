package org.tbch.tbchutils.memory;

import org.tbch.tbchutils.util.config.ConfigUtil;

import java.util.HashMap;

public class PlaytimeMemory {

    private static final HashMap<String, Long> lastTimestampSaved = new HashMap<>();

    public static Long getLastTimestampSaved(String username) {
        return lastTimestampSaved.get(username);
    }

    public static void setLastTimestampSaved(String username, boolean saveLocally) {
        long currentTimestamp = System.currentTimeMillis();
        if (saveLocally) {savePlaytime(username);}
        lastTimestampSaved.put(username, currentTimestamp);
    }

    public static void savePlaytime(String username) {
        ConfigUtil userConfig = ConfigMemory.getCorrespondingConfig(username);
        double tempSecondsPlayed = userConfig.getDouble("secondsPlayed", 0);
        long secondsPlayed = (long) tempSecondsPlayed;

        Long lastSaved = getLastTimestampSaved(username);
        if (lastSaved == null) {return;}

        secondsPlayed += (System.currentTimeMillis() - lastSaved) / 1000L;
        userConfig.setProperty("secondsPlayed", secondsPlayed);
        userConfig.save();
    }

    public static void removeMemoryEntry(String username) {
        lastTimestampSaved.remove(username);
    }

}
