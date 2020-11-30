package com.vhbob.airienchanting.enchanter;

import com.vhbob.airienchanting.AiriEnchanting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class OpenEnchanter implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null &&
                e.getClickedBlock().getType().toString().equalsIgnoreCase(AiriEnchanting.getPlugin().getConfig().getString("enchanting.block"))) {
            // Create inventory and add fillers
            Inventory enchanter = Bukkit.createInventory(null, 45, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Enchanter");
            ItemStack purpleFiller = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE, 1);
            ItemStack magentaFiller = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE, 1);
            for (int i = 0; i < enchanter.getSize(); ++i) {
                if (i % 9 == 1) {
                    enchanter.setItem(i, magentaFiller);
                } else if (i % 9 < 3) {
                    enchanter.setItem(i, purpleFiller);
                }
            }
            enchanter.setItem(19, new ItemStack(Material.AIR, 1));
            e.getPlayer().openInventory(enchanter);
            e.setCancelled(true);
        }
    }

}
