package org.tbch.tbchutils.listeners;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.memory.ConfigMemory;
import org.tbch.tbchutils.memory.PlaytimeMemory;

public class ConfigEventHandler extends PlayerListener {
    private final JavaPlugin plugin;

    public ConfigEventHandler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        String username = event.getPlayer().getName();
        ConfigMemory.setCorrespondingConfig(plugin, username);
        PlaytimeMemory.setLastTimestampSaved(username, false);
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        String username = event.getPlayer().getName();
        PlaytimeMemory.savePlaytime(username);
        PlaytimeMemory.removeMemoryEntry(username);
        ConfigMemory.removeMemoryEntry(username);
    }

    @Override
    public void onPlayerKick(PlayerKickEvent event) {
        String username = event.getPlayer().getName();
        PlaytimeMemory.savePlaytime(username);
        PlaytimeMemory.removeMemoryEntry(username);
        ConfigMemory.removeMemoryEntry(username);
    }

}