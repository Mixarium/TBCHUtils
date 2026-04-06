package org.tbch.tbchutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.util.WhitelistReader;
import org.tbch.tbchutils.util.misc.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public class WLSearch implements CommandExecutor {

    private final JavaPlugin plugin;

    public WLSearch(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cYou must be an operator to use this command.");
            return true;
        }

        if (args == null || args.length == 0) {return false;}

        String beginningSuggestion = args[0];
        String noMatchesMessage = "&cNo players found matching \"&f" + beginningSuggestion + "&c\" entry.";

        Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, () -> {
            StringBuilder matchesMessage = new StringBuilder("&aResults: &f");
            List<String> searchResults = new ArrayList<>();
            List<String> whitelist = WhitelistReader.getWhitelist(plugin);
            for (String username : whitelist) {
                if (username.startsWith(beginningSuggestion.toLowerCase())) {
                    searchResults.add(username);
                }
            }

            for (String username : searchResults) {
                matchesMessage.append(username).append(" ");
            }

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (searchResults.isEmpty()) {
                    sender.sendMessage(ColorUtil.translateColorCodes(noMatchesMessage));
                } else {
                    sender.sendMessage(ColorUtil.translateColorCodes("&aEntry: &f" + beginningSuggestion));
                    sender.sendMessage(ColorUtil.translateColorCodes(String.valueOf(matchesMessage)));
                }
            });
        });

        return true;
    }
}
