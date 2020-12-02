package com.vhbob.airienchanting.util;

import com.vhbob.airienchanting.AiriEnchanting;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    // Spawn particles around a location
    public static void particleEffects(Particle particle, int num, Location center) {
        for (int i = 0; i < num; ++i) {
            double offsetX = 1 * (Math.random() - 0.5);
            double offsetY = 2.0 * (Math.random() - 0.5) + 1;
            double offsetZ = 1 * (Math.random() - 0.5);
            center.getWorld().spawnParticle(particle, center.add(offsetX, offsetY, offsetZ), 5);
        }
    }

    // Get an item from the config
    public static ItemStack getItem(String configLoc) {
        // Get info
        FileConfiguration config = AiriEnchanting.getPlugin().getConfig();
        ItemStack item = new ItemStack(Material.valueOf(config.getString(configLoc + ".icon")), 1);
        String name = ChatColor.translateAlternateColorCodes('&', config.getString(configLoc + ".name"));
        List<String> lore = config.getStringList(configLoc + ".lore");
        List<String> coloredLore = new ArrayList<String>();
        for (String s : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        // Set info and return item
        ItemMeta im = item.getItemMeta();
        im.setDisplayName(name);
        im.setLore(coloredLore);
        item.setItemMeta(im);
        return item;
    }

}
