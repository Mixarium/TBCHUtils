package org.tbch.tbchutils.memory;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.ConfigurationNode;
import org.tbch.tbchutils.util.config.ConfigUtil;

import java.security.SecureRandom;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.*;

public class AutoMessage {
    private static ConfigUtil autoMessageConfig;
    private static final Map<String, Integer> amountsPerGroupWithMultiplier = new HashMap<>();
    private static final String regexPattern = "^(\\d{1,2})/(\\d{1,2})-(\\d{1,2})/(\\d{1,2})$";
    private static String prefix;
    private static int seconds;

    private static final SecureRandom secureRandom = new SecureRandom();

    private static void setAutoMessageConfig(JavaPlugin plugin, String filename) {
        autoMessageConfig = new ConfigUtil(plugin, filename, false);
        autoMessageConfig.load();

        if (autoMessageConfig.getProperty("timeInSeconds") == null) {
            autoMessageConfig.setProperty("timeInSeconds", 900);
        }
        seconds = ((Number) autoMessageConfig.getProperty("timeInSeconds")).intValue();

        if (autoMessageConfig.getProperty("prefix") == null) {
            autoMessageConfig.setProperty("prefix", "&7[&3Server&7]&7");
        }
        prefix = String.valueOf(autoMessageConfig.getProperty("prefix"));

        if (autoMessageConfig.getProperty("messageGroups") == null) {
            autoMessageConfig.setProperty("messageGroups", new ArrayList<>());
        }
    }

    private static boolean isCurrentTimeBetweenDates(String monthDayIndicators) {
        // monthDayIndicators example: "6/1-8/31" - from June 1st to August 31st
        MonthDay today = MonthDay.from(LocalDateTime.now());

        String[] dates = monthDayIndicators.split("-", 2);
        String[] firstDateArgs = dates[0].split("/", 2);
        String[] secondDateArgs = dates[1].split("/", 2);
        MonthDay start = MonthDay.of(Integer.parseInt(firstDateArgs[0]), Integer.parseInt(firstDateArgs[1]));
        MonthDay end = MonthDay.of(Integer.parseInt(secondDateArgs[0]), Integer.parseInt(secondDateArgs[1]));

        if (start.equals(end)) {
            return today.equals(start);
        }
        if (!start.isAfter(end)) {
            return !today.isBefore(start) && !today.isAfter(end);
        } else {
            return !today.isBefore(start) || !today.isAfter(end);
        }
    }

    private static boolean isWeekend() {
        DayOfWeek day = LocalDate.now().getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private static boolean isDayOfWeek(String dayOfWeekInput) {
        try {
            DayOfWeek day = LocalDate.now().getDayOfWeek();
            DayOfWeek dowInput = DayOfWeek.valueOf(dayOfWeekInput.toUpperCase());
            return day == dowInput;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void saveGroupsForRandomCalculation() {
        ConfigurationNode groups = autoMessageConfig.getNode("messageGroups");

        for (String groupName : groups.getKeys()) {
            ConfigurationNode group = autoMessageConfig.getNode("messageGroups." + groupName);

            String intervalToProcess = group.getString("intervalIndicated", "-");
            List<String> messages = group.getStringList("messages", new ArrayList<>());
            int multiplier = group.getInt("multiplier", 1);

            if (intervalToProcess.equals("-")) continue;
            amountsPerGroupWithMultiplier.put(groupName, messages.size() * multiplier);
        }
    }

    private static boolean doesIntervalPassChecks(String interval) {
        return interval.equals("*")
                || (interval.matches(regexPattern) && isCurrentTimeBetweenDates(interval))
                || (interval.equalsIgnoreCase("weekends") && isWeekend())
                || isDayOfWeek(interval);
    }

    private static String getRandomValidGroup() {
        List<String> validGroups = new ArrayList<>();
        int possibleCases = 0;

        for (Map.Entry<String, Integer> entry : amountsPerGroupWithMultiplier.entrySet()) {
            ConfigurationNode group = autoMessageConfig.getNode("messageGroups." + entry.getKey());
            String intervalToCheck = String.valueOf(group.getProperty("intervalIndicated"));

            if (!doesIntervalPassChecks(intervalToCheck)) continue;
            possibleCases += entry.getValue();
            validGroups.add(entry.getKey());
        }

        int randInt = secureRandom.nextInt(possibleCases);
        int counter = 0;

        for (String groupName : validGroups) {
            counter += amountsPerGroupWithMultiplier.get(groupName);
            if (randInt < counter) {
                return groupName;
            }
        }

        return null;
    }

    private static void broadcastMessage(JavaPlugin plugin, String groupName) {
        String path = "messageGroups." + groupName + ".messages";
        List<String> messages = autoMessageConfig.getStringList(path, new ArrayList<>());

        String randomMessage = messages.get(secureRandom.nextInt(messages.size()));
        String[] sequences = randomMessage.split("\\$n");

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for (int j = 0; j < sequences.length; j++) {
                String line = sequences[j];
                String rawLineToBroadcast = ((j == 0) ? prefix : "") + line;

                char[] translation = rawLineToBroadcast.toCharArray();
                for (int i = 0; i < translation.length - 1; ++i) {
                    if (translation[i] == '$' && "0123456789AaBbCcDdEeFf".indexOf(translation[i + 1]) > -1) {
                        translation[i] = 167;
                        translation[i + 1] = Character.toLowerCase(translation[i + 1]);
                    }
                }
                Bukkit.getServer().broadcastMessage(new String(translation));
            }
        }, 0L);
    }

    public static void initialize(JavaPlugin plugin, String filename) {
        setAutoMessageConfig(plugin, filename);
        saveGroupsForRandomCalculation();

        Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin,
                () -> broadcastMessage(plugin, getRandomValidGroup()),
                (long) seconds * 20L, (long) seconds * 20L
        );
    }
}
