package com.vhbob.airienchanting.enchanter;

import com.vhbob.airienchanting.AiriEnchanting;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class EnchanterInteractions implements Listener {

    private static FileConfiguration config = AiriEnchanting.getPlugin().getConfig();

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Enchanter") && e.getClickedInventory() != null) {
            final Player p = (Player) e.getWhoClicked();
            if (e.getClick().isShiftClick() || (e.getClickedInventory().equals(e.getView().getTopInventory())) && e.getSlot() != 19) {
                e.setCancelled(true);
            }
            Inventory enchanter = e.getView().getTopInventory();
            if (e.getSlot() == 19 && !e.getClick().isShiftClick()) {
                updateEnchantments(enchanter, e.getCursor());
            }
            new BukkitRunnable() {
                public void run() {
                    p.updateInventory();
                }
            }.runTaskLater(AiriEnchanting.getPlugin(), 1);
        }
    }

    // Update the inventory to display the proper enchants
    private void updateEnchantments(Inventory enchanter, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            for (int i = 0; i < enchanter.getSize(); ++i) {
                if (i % 9 >= 3) {
                    enchanter.setItem(i, new ItemStack(Material.AIR, 1));
                }
            }
        } else {
            for (String type : config.getConfigurationSection("enchanting").getKeys(false)) {
                if (item.getType().toString().contains("_" + type.toUpperCase())) {
                    // Added type's enchantments to the inventory
                    int row = 0;
                    for (String enchantment : config.getConfigurationSection("enchanting." + type).getKeys(false)) {
                        Material iconType = Material.valueOf(config.getString("enchanting." + type + "." + enchantment + ".icon"));
                        ItemStack icon = new ItemStack(iconType, 1);
                        ItemMeta im = icon.getItemMeta();
                        String enchantmentFormatted = enchantment.toUpperCase().charAt(0) + enchantment.substring(1).toLowerCase();
                        im.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + enchantmentFormatted);
                        icon.setItemMeta(im);
                        enchanter.setItem(row * 9 + 3, icon);
                        // Add levels
                        for (int level = 1; level <= config.getInt("enchanting." + type + "." + enchantment + ".max_level"); level++) {
                            int cost = config.getInt("enchanting." + type + "." + enchantment + ".level_costs." + level);
                            int multi = AiriEnchanting.getPlugin().getMulti(item);
                            Enchantment enchantmentEnum = Enchantment.getByKey(NamespacedKey.minecraft(enchantment.toLowerCase()));
                            if (item.getEnchantmentLevel(enchantmentEnum) >= level) {
                                ItemStack lvlHas = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
                                ItemMeta lvlHasIM = lvlHas.getItemMeta();
                                lvlHasIM.setDisplayName(ChatColor.GREEN + "Level " + level + ": Achieved");
                                lvlHas.setItemMeta(lvlHasIM);
                                enchanter.setItem(row * 9 + 3 + level, lvlHas);
                            } else {
                                ItemStack lvlHas = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
                                ItemMeta lvlHasIM = lvlHas.getItemMeta();
                                lvlHasIM.setDisplayName(ChatColor.RED + "Level " + level + ": " + cost * multi + " tokens");
                                lvlHas.setItemMeta(lvlHasIM);
                                enchanter.setItem(row * 9 + 3 + level, lvlHas);
                            }
                        }
                        row++;
                    }
                    return;
                }
            }
        }
    }



}
