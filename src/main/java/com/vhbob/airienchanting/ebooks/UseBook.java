package com.vhbob.airienchanting.ebooks;

import com.vhbob.airienchanting.AiriEnchanting;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
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
                    // Apply enchantments
                    Map<Enchantment, Integer> enchants = cursor.getEnchantments();
                    for (Enchantment enchantment : enchants.keySet()) {
                        String id = enchantment.getKey().getKey();
                        // Ensure enchant can be applied
                        List<String> types = config.getStringList("ebook.level_cost." + id + ".valid");
                        boolean valid = false;
                        for (String type : types) {
                            if (clicked.getType().toString().contains("_" + type.toUpperCase())) {
                                valid = true;
                            }
                        }
                        if (!valid || enchants.get(enchantment) < clicked.getEnchantmentLevel(enchantment)) {
                            e.getWhoClicked().sendMessage(ChatColor.RED + "That book can not be used on that item!");
                            return;
                        }
                        clicked.addUnsafeEnchantment(enchantment, enchants.get(enchantment));
                    }
                    cursor.setAmount(0);
                }
            }
        }
    }

}
