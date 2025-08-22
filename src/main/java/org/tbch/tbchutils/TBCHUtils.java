package org.tbch.tbchutils;

import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.tbch.tbchutils.commands.Balancetop;
import org.tbch.tbchutils.commands.Playtime;
import org.tbch.tbchutils.commands.Seen;
import org.tbch.tbchutils.commands.TPS;
import org.tbch.tbchutils.listeners.ConfigEventHandler;
import org.tbch.tbchutils.tasks.OverallConfigHandler;
import org.tbch.tbchutils.tasks.TPSMeasurer;
import org.tbch.tbchutils.util.log.LogUtil;

public class TBCHUtils extends JavaPlugin {

    private static TBCHUtils instance;

    @Override
    public void onEnable() {
        instance = this;

        long configRefreshSeconds = 30L;
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new OverallConfigHandler(this), 0L, configRefreshSeconds * 20L);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new TPSMeasurer(), 0L, 1L);

        ConfigEventHandler configEventHandler = new ConfigEventHandler(this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, configEventHandler, Event.Priority.Highest, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, configEventHandler, Event.Priority.Highest, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_KICK, configEventHandler, Event.Priority.High, this);

        // override all of essentials's initial aliases of the balancetop command
        getCommand("balancetop").setExecutor(new Balancetop(this));
        getCommand("baltop").setExecutor(new Balancetop(this));
        getCommand("ebalancetop").setExecutor(new Balancetop(this));
        getCommand("ebaltop").setExecutor(new Balancetop(this));

        getCommand("playtime").setExecutor(new Playtime(this));
        getCommand("seen").setExecutor(new Seen(this));
        getCommand("tps").setExecutor(new TPS());
        LogUtil.logConsoleInfo(String.format("[%s] Enabled.", getDescription().getName()));

    }

    @Override
    public void onDisable() {
        LogUtil.logConsoleInfo(String.format("[%s] v%s Disabled.", getDescription().getName(), getDescription().getVersion()));
    }

    public static JavaPlugin getInstance() {
        return instance;
    }
}