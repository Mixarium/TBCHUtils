package org.tbch.tbchutils.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.memory.GiftsHandler;

public class UnopenedGiftsNotif extends PlayerListener {
    private final JavaPlugin plugin;

    public UnopenedGiftsNotif(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String username = player.getName();

        int unopenedGiftsAmount = GiftsHandler.getUnopenedGiftsAmount(username);
        int giftsToBeRetrievedAmount = GiftsHandler.getExpiredGiftsAmount(username);
        if (unopenedGiftsAmount == 0 && giftsToBeRetrievedAmount == 0) {
            return;
        }

        String statsMessage = "§bYou have §3";

        if (unopenedGiftsAmount != 0) {
            statsMessage += unopenedGiftsAmount + "§b unopened " + (unopenedGiftsAmount == 1 ? "gift" : "gifts");
        }

        if (giftsToBeRetrievedAmount != 0) {
            statsMessage += ((unopenedGiftsAmount == 0) ? "" : ", ") + "§3" + giftsToBeRetrievedAmount +
                    " §b" + (giftsToBeRetrievedAmount == 1 ? "gift" : "gifts") + " to be retrieved";
        }

        statsMessage += ".";
        String openGiftMessage = "§bUse §3/gift open §bto open " + (unopenedGiftsAmount == 1 ? "it." : "one.");
        String openRetrievedGiftsMessage = "§bUse §3/gift retrieve §bto retrieve gifts unreached by recipients.";

        String finalStatsMessage = statsMessage;
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            player.sendMessage(finalStatsMessage);
            if (unopenedGiftsAmount != 0) {
                player.sendMessage(openGiftMessage);
            }
            if (giftsToBeRetrievedAmount != 0) {
                player.sendMessage(openRetrievedGiftsMessage);
            }
        }, 20L * GiftsHandler.getSecondsAfterJoinForNotif());
    }
}
