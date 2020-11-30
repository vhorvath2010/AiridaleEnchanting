package com.vhbob.airienchanting.enchanter;

import com.vhbob.airienchanting.AiriEnchanting;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EnchanterInteractions implements Listener {

    private static FileConfiguration config = AiriEnchanting.getPlugin().getConfig();

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Enchanter")) {
            if (e.getClickedInventory().equals(e.getView().getTopInventory()) && e.getSlot() != 19) {
                e.setCancelled(true);
            }
            Inventory enchanter = e.getView().getTopInventory();
            updateEnchantments(enchanter, enchanter.getItem(19));
        }
    }

    // Update the inventory to display the proper enchants
    public void updateEnchantments(Inventory enchanter, ItemStack item) {
        if (item == null) {
            for (int i = 0; i < enchanter.getSize(); ++i) {
                if (i % 9 >= 3) {
                    enchanter.setItem(i, new ItemStack(Material.AIR, 1));
                }
            }
        } else {
            for (String type : config.getConfigurationSection("enchanting").getKeys(false)) {
                if (item.getType().toString().contains(type.toUpperCase())) {
                    // Added type's enchantments to the inventory
                    int row = 0;
                    for (String enchantment : config.getConfigurationSection("enchanting." + type).getKeys(false)) {
                        Material iconType = Material.valueOf(config.getString("enchanting." + type + ".icon"));
                        ItemStack icon = new ItemStack(iconType, 1);
                        enchanter.setItem(row * 9 + 3, icon);
                        row++;
                    }
                }
            }
        }
    }

}
