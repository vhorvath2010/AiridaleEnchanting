package com.vhbob.airienchanting.enchanter;

import com.vhbob.airienchanting.AiriEnchanting;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PurchaseEnchant implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Enchanter")
                && e.getClickedInventory() != null
                && e.getClickedInventory().equals(e.getView().getTopInventory())
                && e.getCurrentItem() != null
                && e.getView().getTopInventory().getItem(19) != null
                && e.getCurrentItem().getType() == Material.RED_STAINED_GLASS_PANE) {
            // Check to see if they clicked an enchantment
            if (e.getCurrentItem().getType() != Material.AIR && e.getSlot() % 9 > 3) {
                // Get enchantment info
                int level = e.getSlot() % 9 - 3;
                String enchantKey = e.getView().getTopInventory().getItem(e.getSlot() - level).getItemMeta().getDisplayName();
                enchantKey = ChatColor.stripColor(enchantKey).toLowerCase();
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantKey));
                String costString = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName());
                costString = costString.replace("Level " + level + ": ", "").replace(" tokens", "");
                int cost = Integer.parseInt(costString);
                // Buy enchantment, if play can afford it
                Inventory playerInv = e.getView().getBottomInventory();
                ItemStack playerItem = e.getView().getTopInventory().getItem(19);
                if (cost > countTokens(playerInv)) {
                    e.getWhoClicked().sendMessage(ChatColor.RED + "You do not have enough tokens in your inventory!");
                } else {
                    removeTokens(playerInv, cost);
                    playerItem.addEnchantment(enchantment, level);
                    e.getWhoClicked().sendMessage(ChatColor.GREEN + "Enchantment applied!");
                }
                playerInv.addItem(playerItem);
                e.getWhoClicked().closeInventory();
            }
        }
    }

    // Method to remove tokens from an inventory
    private void removeTokens(Inventory inv, int amt) {
        for (ItemStack item : inv.getContents()) {
            if (isToken(item)) {
                if (item.getAmount() <= amt) {
                    amt -= item.getAmount();
                    item.setAmount(0);
                } else {
                    item.setAmount(item.getAmount() - amt);
                    return;
                }
            }
        }
    }

    // Method to check how many tokens a player has
    private int countTokens(Inventory inv) {
        int count = 0;
        for (ItemStack item : inv.getContents()) {
            if (isToken(item)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    // Method to check if an item is a token
    private boolean isToken(ItemStack item) {
        if (item != null && item.getItemMeta() != null && item.getItemMeta().hasLore()) {
            // Check for token lore
            boolean token = true;
            for (String line : AiriEnchanting.getPlugin().getConfig().getStringList("enchanting.token_lore")) {
                if (!item.getItemMeta().getLore().contains(ChatColor.translateAlternateColorCodes('&', line))) {
                    token = false;
                }
            }
            return token;
        }
        return false;
    }

}
