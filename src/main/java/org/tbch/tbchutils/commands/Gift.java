package org.tbch.tbchutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.memory.GiftsHandler;
import org.tbch.tbchutils.util.EssentialsConfigReader;
import org.tbch.tbchutils.util.config.ConfigUtil;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Gift implements CommandExecutor {

    private final JavaPlugin plugin;

    public Gift(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can run this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args == null || args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("stats")) {
            int unopenedGiftsAmount = GiftsHandler.getUnopenedGiftsAmount(player.getName());
            int giftsToBeRetrievedAmount = GiftsHandler.getExpiredGiftsAmount(player.getName());

            player.sendMessage("§bYou have §3" + unopenedGiftsAmount + "§b unopened " +
                    (unopenedGiftsAmount == 1 ? "gift" : "gifts") + ", §3" + giftsToBeRetrievedAmount + " §b" +
                    (giftsToBeRetrievedAmount == 1 ? "gift" : "gifts") + " to be retrieved.");
            player.sendMessage("§bUse §3/gift open §bto open " + (unopenedGiftsAmount == 1 ? "it." : "one."));
            player.sendMessage("§bUse §3/gift retrieve §bto retrieve gifts unreached by recipients.");
            return true;
        }

        if (args[0].equalsIgnoreCase("open")) {
            Map<String, Object> giftData = GiftsHandler.openGift(player.getName());
            if (giftData == null) {
                player.sendMessage("§bYou don't have any gifts to open right now.");
                return true;
            }

            int meta = ((Number) giftData.get("meta")).intValue();
            int amount = ((Number) giftData.get("amount")).intValue();
            int itemID = ((Number) giftData.get("itemID")).intValue();

            ItemStack stack = new ItemStack(itemID, amount, (short) 0, (byte) meta);
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);

            if (!leftover.isEmpty()) {
                player.getWorld().dropItemNaturally(player.getLocation(), stack);
            }

            player.sendMessage("§bYou got §3" + amount + " x " + stack.getType().name() + " §bfrom §3" +
                    giftData.get("sender") + "§b!");
            return true;
        }

        if (args[0].equalsIgnoreCase("retrieve")) {
            List<Map<String, Object>> retrievals = GiftsHandler.getAllRetrievedGifts(player.getName());
            if (retrievals == null) {
                player.sendMessage("§bYou don't have any expired gifts to be retrieved.");
                return true;
            }

            StringBuilder retrievalMessage = new StringBuilder("§bYou got back ");
            boolean firstIteration = true;

            for (Map<String, Object> giftData : retrievals) {
                int meta = ((Number) giftData.get("meta")).intValue();
                int amount = ((Number) giftData.get("amount")).intValue();
                int itemID = ((Number) giftData.get("itemID")).intValue();

                ItemStack stack = new ItemStack(itemID, amount, (short) 0, (byte) meta);
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);

                if (!leftover.isEmpty()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), stack);
                }

                if (!firstIteration) {
                    retrievalMessage.append(", ");
                }

                retrievalMessage.append("§3").append(amount).append(" x ").append(stack.getType().name()).append("§b");
                firstIteration = false;
            }

            player.sendMessage(retrievalMessage + "!");
            return true;
        }

        if (args.length < 2) {
            return false;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (player.getName().equalsIgnoreCase(args[1])) {
                player.sendMessage("§cYou cannot give gifts to yourself.");
                return true;
            }

            if (!EssentialsConfigReader.hasUserPlayedBefore(args[1])) {
                player.sendMessage("§cThis player has not played on the server before.");
                return true;
            }

            if (GiftsHandler.hasReachedUnopenedSlots(args[1])) {
                player.sendMessage("§cUser has reached the maximum amount of unopened gifts.");
                return true;
            }

            ItemStack stack = player.getItemInHand();
            if (stack == null || stack.getType().name().equalsIgnoreCase("AIR")) {
                player.sendMessage("§cYou must be holding an item to give a gift.");
                return true;
            }

            String recipient = args[1].toLowerCase();
            String filename = recipient + ".yml";

            File localUserFolder = new File(plugin.getDataFolder(), "userdata");
            File localUserdata = new File(localUserFolder, filename);

            if (localUserdata.exists()) {
                ConfigUtil localUserConfig = new ConfigUtil(plugin, filename, true);
                localUserConfig.load();
                recipient = localUserConfig.getProperty("actualUsername").toString();
            }

            Map<String, Object> giftData = new HashMap<>();
            int amount = stack.getAmount();
            String itemName = stack.getType().name();

            giftData.put("amount", amount);
            giftData.put("itemID", stack.getType().getId());
            giftData.put("meta", (int) stack.getDurability());
            giftData.put("sender", player.getName());

            GiftsHandler.addGiftToDatabase(giftData, Instant.now().toEpochMilli(), recipient);
            player.sendMessage("§bYou gave §3" + amount + " x " + itemName + " §bto §3" + recipient + "§b!");

            Player targetPlayer = Bukkit.getServer().getPlayer(recipient);
            if (targetPlayer != null && targetPlayer.isOnline() && targetPlayer.getName().equalsIgnoreCase(recipient)) {
                targetPlayer.sendMessage("§bYou got a new gift! Open gifts with §3/gift open§b.");
            }

            player.setItemInHand(null);
            return true;
        }

        return false;
    }
}
