package com.vhbob.airienchanting;

import com.vhbob.airienchanting.enchanter.EnchanterInteractions;
import com.vhbob.airienchanting.enchanter.OpenEnchanter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AiriEnchanting extends JavaPlugin {

    private static AiriEnchanting plugin;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new OpenEnchanter(), this);
        Bukkit.getPluginManager().registerEvents(new EnchanterInteractions(), this);
    }

    public static AiriEnchanting getPlugin() {
        return plugin;
    }
}
