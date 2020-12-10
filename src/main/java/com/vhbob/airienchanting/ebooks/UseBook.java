package com.vhbob.airienchanting.ebooks;

import com.vhbob.airienchanting.AiriEnchanting;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class UseBook implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getAction() == InventoryAction.SWAP_WITH_CURSOR) {
            FileConfiguration config = AiriEnchanting.getPlugin().getConfig();
            ItemStack cursor = e.getCursor();
            ItemStack clicked = e.getCurrentItem();
            // Check if cursor is an ebook
            if (cursor.hasItemMeta() && cursor.getItemMeta().hasDisplayName()) {
                String ebookTitle = config.getString("ebook.name");
                ebookTitle = ChatColor.translateAlternateColorCodes('&', ebookTitle);
                if (cursor.getItemMeta().getDisplayName().equalsIgnoreCase(ebookTitle)) {
                    Player p = (Player) e.getWhoClicked();
                    // Apply enchantments
                    Map<Enchantment, Integer> enchants = cursor.getEnchantments();
                    for (Enchantment enchantment : enchants.keySet()) {
                        String id = enchantment.getKey().getKey();
                        // Ensure enchant can be applied
                        List<String> types = config.getStringList("ebook.level_cost." + id + ".valid");
                        boolean valid = false;
                        for (String type : types) {
                            if (clicked.getType().toString().contains("_" + type.toUpperCase()) || clicked.getType().toString().equalsIgnoreCase(type)) {
                                valid = true;
                            }
                        }
                        for (String section : AiriEnchanting.getPlugin().getConfig().getConfigurationSection("exclusive_groups").getKeys(false)) {
                            if (!valid)
                                break;
                            List<String> exclusive = AiriEnchanting.getPlugin().getConfig().getStringList("exclusive_groups." + section);
                            // Check if the given enchantment is in the list
                            if (exclusive.contains(id)) {
                                for (Enchantment itemEnchant : clicked.getEnchantments().keySet()) {
                                    String itemEnchantKey = itemEnchant.getKey().getKey();
                                    if (!itemEnchantKey.equalsIgnoreCase(id) && exclusive.contains(itemEnchantKey)) {
                                        valid = false;
                                        break;
                                    }
                                }
                            }
                        }
                        // Buy enchantment, if play can afford it
                        if (!valid || enchants.get(enchantment) < clicked.getEnchantmentLevel(enchantment)) {
                            p.sendMessage(ChatColor.RED + "That book can not be used on that item!");
                            return;
                        }
                        // Ensure they have enough levels
                        Enchantment ench = (Enchantment) cursor.getEnchantments().keySet().toArray()[0];
                        int lvl = cursor.getEnchantments().get(ench);
                        int cost = 30;
                        if (config.contains("ebook.level_cost." + ench.getKey().getKey() + "." + lvl)) {
                            cost = config.getInt("ebook.level_cost." + ench.getKey().getKey() + "." + lvl);
                        }
                        if (cost > p.getLevel()) {
                            p.sendMessage(ChatColor.RED + "You do not have enough levels to use that book!");
                            return;
                        }
                        p.setLevel(p.getLevel() - cost);
                        clicked.addUnsafeEnchantment(enchantment, enchants.get(enchantment));
                    }
                    cursor.setAmount(0);
                }
            }
        }
    }

}
