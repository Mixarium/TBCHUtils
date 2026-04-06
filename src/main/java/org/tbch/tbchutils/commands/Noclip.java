package org.tbch.tbchutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.memory.NoclipPendingMemory;
import org.tbch.tbchutils.util.TeleportUtil;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Noclip implements CommandExecutor {
    /*
     /noclip (help) <player> <world> <delay> <min-x> <min-y> <min-z> <max-x> <max-y> <max-z>
    */
    private final JavaPlugin plugin;

    public Noclip(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cYou must be an operator to use this command.");
            return true;
        }

        if (args == null || args.length == 0) {return false;}

        if (args[0].equalsIgnoreCase("help")) {
            sender.sendMessage("§3/noclip list - list all task IDs of pending teleportation task");
            sender.sendMessage("§3/noclip view {taskID} - view information of pending task");
            sender.sendMessage("§3/noclip cancel {taskID} - cancel pending task");
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            StringBuilder listMessage = new StringBuilder("§aNoclip pending task IDs: §f");
            for (int taskID : NoclipPendingMemory.getPendingTasks().keySet()) {
                listMessage.append(taskID).append(" ");
            }
            sender.sendMessage(String.valueOf(listMessage));
            return true;
        }

        if (args.length == 1) {return false;}

        if (args[0].equalsIgnoreCase("cancel")) {
            int taskID;
            try {
                taskID = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid task ID format.");
                return true;
            }

            if (!NoclipPendingMemory.isIDInTasks(taskID)) {
                sender.sendMessage("§cNo task with ID §f" + taskID + "§c is running.");
                return true;
            }

            NoclipPendingMemory.cancel(plugin, taskID);
            sender.sendMessage("§3Task with ID §f" + taskID + "§3 was cancelled.");
            return true;
        }

        if (args[0].equalsIgnoreCase("view")) {
            int taskID;
            try {
                taskID = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid task ID format.");
                return true;
            }

            Map<String, Object> taskInfo = NoclipPendingMemory.getTaskInfo(taskID);
            if (taskInfo == null) {
                sender.sendMessage("§cNo such task ID could be found.");
                return true;
            }
            long secondsRemaining = ((Long) taskInfo.get("timestamp") - Instant.now().toEpochMilli()) / 1000;

            sender.sendMessage("§3targetUser: §f" + taskInfo.get("username") + "§3; world: §f" + taskInfo.get("worldName"));
            sender.sendMessage("§3secondsLeft: §f" + secondsRemaining + "§3; finalCoordinates: §f(" +
                    taskInfo.get("targetX") + ", " + taskInfo.get("targetY") + ", " + taskInfo.get("targetZ") + ")");
            return true;
        }

        // start of argument processing for TP processes
        if (args.length <= 8) {return false;}

        Player targetPlayer = Bukkit.getServer().getPlayer(args[0]);

        if (targetPlayer == null || !targetPlayer.getName().equalsIgnoreCase(args[0]) || !targetPlayer.isOnline()) {
            sender.sendMessage("§cNo player by the name of §f" + args[0] + "&c was found online.");
            return true;
        }

        World world = Bukkit.getServer().getWorld(args[1]);
        if (world == null) {
            sender.sendMessage("§cNo world named §f" + args[1] + " §cwas found.");
            return true;
        }

        long durationMins;
        try {
            durationMins = Long.parseLong(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cDuration in minutes must be an integer number.");
            return true;
        }

        if (durationMins < 0) {
            sender.sendMessage("§cDuration in minutes must be non-negative.");
            return true;
        }

        List<Long> convertedCoordinates = new ArrayList<>();
        try {
            for (int i=3; i <= 8; i++) {
                Long converted = Long.parseLong(args[i]);
                convertedCoordinates.add(converted);
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cCoordinates must be integer numbers.");
            return true;
        }

        List<Double> finalCoordinates = TeleportUtil.searchSafeLocation(args[1], convertedCoordinates);
        if (finalCoordinates == null) {
            sender.sendMessage("§cNo safe teleportation coordinates could be found by brute-force. Try again.");
            return true;
        }

        NoclipPendingMemory.addTeleportTask(plugin, targetPlayer.getName(), world.getName(), durationMins,
                finalCoordinates.get(0), finalCoordinates.get(1), finalCoordinates.get(2));

        sender.sendMessage("§3Player §f" + targetPlayer.getName() + " §3will be teleported to §f" +
                "(" + finalCoordinates.get(0) + ", " + finalCoordinates.get(1) + ", " + finalCoordinates.get(2) +
                ") §3in §f" + world.getName() + " §3after §f" + durationMins + ((durationMins == 1) ? " minute." : " minutes."));
        return true;
    }
}