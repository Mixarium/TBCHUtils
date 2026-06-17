package org.tbch.tbchutils.memory;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.ConfigurationNode;
import org.tbch.tbchutils.util.config.ConfigUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GiftsHandler {
    private static ConfigUtil giftsConfig;

    private static String getProperCase(String username, String nodeStr) {
        ConfigurationNode data = giftsConfig.getNode(nodeStr);
        if (data == null) {
            return null;
        }

        for (String user : data.getKeys()) {
            if (user.equalsIgnoreCase(username)) {
                return user;
            }
        }

        return null;
    }

    public static int getSecondsAfterJoinForNotif() {
        return giftsConfig.getInt("secondsUntilNotifAfterJoin", 30);
    }

    public static Map<String, Object> openGift(String username) {
        ConfigurationNode giftsData = giftsConfig.getNode("recipients");
        String usernameKey = getProperCase(username, "recipients");

        if (usernameKey == null) {
            return null;
        }

        ConfigurationNode recipientGifts = giftsData.getNode(usernameKey);

        String path = recipientGifts.getKeys().get(0);
        if (path == null) {
            return null;
        }

        ConfigurationNode firstGift = recipientGifts.getNode(path);
        Map<String, Object> res = new HashMap<>();

        res.put("amount", firstGift.getProperty("amount"));
        res.put("itemID", firstGift.getProperty("itemID"));
        res.put("meta", firstGift.getProperty("meta"));
        res.put("sender", firstGift.getProperty("sender"));

        recipientGifts.removeProperty(path);
        if (recipientGifts.getKeys().isEmpty()) {
            giftsData.removeProperty(usernameKey);
        }

        giftsConfig.save();
        return res;
    }

    public static int getUnopenedGiftsAmount(String username) {
        ConfigurationNode giftsData = giftsConfig.getNode("recipients");

        String usernameKey = getProperCase(username, "recipients");
        if (usernameKey == null) {
            return 0;
        }
        ConfigurationNode recipientGifts = giftsData.getNode(usernameKey);
        return recipientGifts.getKeys().toArray().length;
    }

    public static boolean hasReachedUnopenedSlots(String username) {
        int maxRecipientUnopenedSlots = (int) giftsConfig.getProperty("maxRecipientUnopenedSlots");
        return getUnopenedGiftsAmount(username) >= maxRecipientUnopenedSlots;
    }

    public static void addGiftToDatabase(Map<String, Object> data, long timestamp, String recipient) {
        giftsConfig.setProperty("recipients." + recipient + "." + timestamp, data);
        giftsConfig.save();
    }

    public static void setGiftsConfig(JavaPlugin plugin, String filename) {
        giftsConfig = new ConfigUtil(plugin, filename, false);
        giftsConfig.load();

        if (giftsConfig.getProperty("maxRecipientUnopenedSlots") == null) {
            giftsConfig.setProperty("maxRecipientUnopenedSlots", 20);
        }
        if (giftsConfig.getProperty("unopenedGiftExpirationInDays") == null) {
            giftsConfig.setProperty("unopenedGiftExpirationInDays", 14);
        }
        if (giftsConfig.getProperty("recipients") == null) {
            giftsConfig.setProperty("recipients", null);
        }
        if (giftsConfig.getProperty("secondsUntilNotifAfterJoin") == null) {
            giftsConfig.setProperty("secondsUntilNotifAfterJoin", 20);
        }
        if (giftsConfig.getProperty("retrievals") == null) {
            giftsConfig.setProperty("retrievals", null);
        }
        giftsConfig.save();
    }

    public static void setExpiredGiftsForRetrieval() {
        long daysUntilExpiration = ((Number) giftsConfig.getProperty("unopenedGiftExpirationInDays")).longValue();
        long msDifference = daysUntilExpiration * 24L * 3600L * 1000L;
        ConfigurationNode giftsData = giftsConfig.getNode("recipients");

        if (giftsData == null){
            return;
        }

        long currentTimestamp = Instant.now().toEpochMilli();
        for (String username : new ArrayList<>(giftsData.getKeys())) {
            ConfigurationNode recipientGifts = giftsData.getNode(username);

            for (String tsStr : new ArrayList<>(recipientGifts.getKeys())) {
                long timestamp = Long.parseLong(tsStr);

                if (currentTimestamp - timestamp < msDifference) {
                    continue;
                }

                ConfigurationNode giftToBeRetrieved = recipientGifts.getNode(tsStr);
                Map<String, Object> res = new HashMap<>();

                res.put("amount", giftToBeRetrieved.getProperty("amount"));
                res.put("itemID", giftToBeRetrieved.getProperty("itemID"));
                res.put("meta", giftToBeRetrieved.getProperty("meta"));

                giftsConfig.setProperty("retrievals." + giftToBeRetrieved.getProperty("sender") + "." + tsStr, res);
                recipientGifts.removeProperty(tsStr);
            }

            if (recipientGifts.getKeys().isEmpty()) {
                giftsData.removeProperty(username);
            }
        }

        giftsConfig.save();
    }

    public static List<Map<String, Object>> getAllRetrievedGifts(String username) {
        ConfigurationNode retrievalsData = giftsConfig.getNode("retrievals");
        String usernameKey = getProperCase(username, "retrievals");

        if (usernameKey == null) {
            return null;
        }

        ConfigurationNode giftsToBeRetrieved = retrievalsData.getNode(usernameKey);
        List<Map<String, Object>> res = new ArrayList<>();

        for (String ts : new ArrayList<>(giftsToBeRetrieved.getKeys())) {
            ConfigurationNode giftData = giftsToBeRetrieved.getNode(ts);
            Map<String, Object> gift = new HashMap<>();

            gift.put("amount", giftData.getProperty("amount"));
            gift.put("itemID", giftData.getProperty("itemID"));
            gift.put("meta", giftData.getProperty("meta"));

            res.add(gift);
            giftsToBeRetrieved.removeProperty(ts);
        }

        if (giftsToBeRetrieved.getKeys().isEmpty()) {
            retrievalsData.removeProperty(usernameKey);
        }

        if (res.isEmpty()) {
            return null;
        }

        giftsConfig.save();
        return res;
    }

    public static int getExpiredGiftsAmount(String username) {
        ConfigurationNode retrievalsData = giftsConfig.getNode("retrievals");

        String usernameKey = getProperCase(username, "retrievals");
        if (usernameKey == null) {
            return 0;
        }
        ConfigurationNode giftsToBeRetrieved = retrievalsData.getNode(usernameKey);
        return giftsToBeRetrieved.getKeys().toArray().length;
    }
}
