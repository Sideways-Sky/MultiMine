package net.sideways_sky.multimine;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class MultiMine extends JavaPlugin {
    public static MultiMine instance;
    public static boolean debug = false;
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        debug = getConfig().getBoolean("debug");
        DamagedBlock.fadeStartDelay = getConfig().getInt("fade.start-delay");
        DamagedBlock.fadeIntervalDelay = getConfig().getInt("fade.interval");
        DamagedBlock.fadeDamageReduction = (float) getConfig().getDouble("fade.amount");
        Bukkit.getPluginManager().registerEvents(new Events(), this);
    }

    @Override
    public void onDisable() {
        Events.damagedBlockMap.clear();
        Bukkit.getScheduler().cancelTasks(this);
    }

    public static void debugMessage(String message){
        if(debug){
            Bukkit.getLogger().info("[MultiMine] Debug | "+message);
        }
    }
}
