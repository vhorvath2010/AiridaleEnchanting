package com.vhbob.airienchanting;

import com.vhbob.airienchanting.ebooks.GiveBook;
import com.vhbob.airienchanting.ebooks.UseBook;
import com.vhbob.airienchanting.enchanter.EnchanterInteractions;
import com.vhbob.airienchanting.enchanter.PurchaseEnchant;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AiriEnchanting extends JavaPlugin {

    private static AiriEnchanting plugin;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new EnchanterInteractions(), this);
        Bukkit.getPluginManager().registerEvents(new PurchaseEnchant(), this);
        Bukkit.getPluginManager().registerEvents(new UseBook(), this);
        Bukkit.getPluginCommand("ebook").setExecutor(new GiveBook());
    }

    public static AiriEnchanting getPlugin() {
        return plugin;
    }

    public int getMulti(ItemStack item) {
        for (String type : getConfig().getConfigurationSection("cost_multis").getKeys(false)) {
            if (item.getType().toString().contains(type.toUpperCase() + "_")) {
                return getConfig().getInt("cost_multis." + type);
            }
        }
        return 1;
    }

}
