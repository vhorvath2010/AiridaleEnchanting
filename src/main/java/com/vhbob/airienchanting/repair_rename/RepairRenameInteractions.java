package com.vhbob.airienchanting.repair_rename;

import com.vhbob.airienchanting.AiriEnchanting;
import com.vhbob.airienchanting.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class RepairRenameInteractions implements Listener {

    private static final FileConfiguration config = AiriEnchanting.getPlugin().getConfig();
    private static final ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    private static final String chooseTitle = ChatColor.translateAlternateColorCodes('&', config.getString("repair_rename.title"));
    private static final String repairTitle = ChatColor.translateAlternateColorCodes('&', config.getString("repair.title"));
    private static final String renameTitle = ChatColor.translateAlternateColorCodes('&', config.getString("rename.title"));


    @EventHandler
    public void openInv(PlayerInteractEvent e) {
        // Open inventory if block clicked
        if (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null &&
                e.getClickedBlock().getType().toString().equalsIgnoreCase(config.getString("repair_rename.block"))) {
            Inventory inv = Bukkit.createInventory(null, 9, chooseTitle);
            ItemStack repair = Utils.getItem("repair");
            ItemStack replace = Utils.getItem("rename");
            for (int i = 0; i < inv.getSize(); ++i) {
                inv.setItem(i, filler);
            }
            inv.setItem(3, repair);
            inv.setItem(5, replace);
            e.getPlayer().openInventory(inv);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void pickAction(InventoryClickEvent e) {
        // See if we need to open a repair rename
        if (e.getView().getTitle().equalsIgnoreCase(chooseTitle) &&
        e.getClickedInventory().equals(e.getView().getTopInventory())) {
            e.setCancelled(true);
            if (e.getSlot() == 3) {
                // Open repair inv
                Inventory repair = Bukkit.createInventory(null, 9, repairTitle);
                for (int i = 0; i < repair.getSize(); ++i) {
                    repair.setItem(i, filler);
                }
                repair.setItem(3, null);
                ItemStack confirm = Utils.getItem("repair.confirm");
                updateLore(confirm, 0);
                repair.setItem(5, confirm);
                e.getWhoClicked().openInventory(repair);
            } else if (e.getSlot() == 5) {
                // Open rename inv
                Inventory rename = Bukkit.createInventory(null, 9, renameTitle);
                for (int i = 0; i < rename.getSize(); ++i) {
                    rename.setItem(i, filler);
                }
                rename.setItem(3, null);
                ItemStack confirm = Utils.getItem("rename.confirm");
                updateLore(confirm, 0);
                rename.setItem(5, confirm);
                e.getWhoClicked().openInventory(rename);
            }
        }
    }



    // Update the lore of an item to update the dispalyed cost
    private void updateLore(ItemStack item, int cost) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        ArrayList<String> updatedLore = new ArrayList<String>();
        for (String s : lore) {
            updatedLore.add(s.replaceAll("%cost%", cost + ""));
        }
        meta.setLore(updatedLore);
        item.setItemMeta(meta);
    }

}
