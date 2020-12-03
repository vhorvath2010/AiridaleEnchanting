package com.vhbob.airienchanting.repair_rename;

import com.vhbob.airienchanting.AiriEnchanting;
import com.vhbob.airienchanting.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class RepairRenameInteractions implements Listener {

    private static final FileConfiguration config = AiriEnchanting.getPlugin().getConfig();
    private static final ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    private static final String chooseTitle = ChatColor.translateAlternateColorCodes('&', config.getString("repair_rename.title"));
    private static final String repairTitle = ChatColor.translateAlternateColorCodes('&', config.getString("repair.title"));
    private static final String renameTitle = ChatColor.translateAlternateColorCodes('&', config.getString("rename.title"));
    private static final int renameCost = config.getInt("rename.cost");


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
                ItemMeta confirmMeta = confirm.getItemMeta();
                ArrayList<String> newLore = new ArrayList<String>();
                if (confirmMeta.hasLore()) {
                    for (String s : confirmMeta.getLore()) {
                        newLore.add(s.replaceAll("%cost%", renameCost + ""));
                    }
                }
                confirmMeta.setLore(newLore);
                confirm.setItemMeta(confirmMeta);
                rename.setItem(5, confirm);
                e.getWhoClicked().openInventory(rename);
            }
        }
    }

    @EventHandler
    public void repairActions(InventoryClickEvent e) {
        if (e.getView().getTitle().equalsIgnoreCase(repairTitle)) {
            // Stop clicks outside of the player item slot
            if (e.getClickedInventory() != null && e.getClickedInventory().equals(e.getView().getTopInventory())) {
                Inventory repairInv = e.getClickedInventory();
                final Player p = (Player) e.getWhoClicked();
                // Check if we need to update the lore cost
                if (e.getSlot() == 3) {
                    ItemStack newConfirm = Utils.getItem("repair.confirm");
                    int cost = 30;
                    if (e.getCursor() == null || e.getCursor().getType() == Material.AIR) {
                        cost = 0;
                    } else {
                        for (String type : config.getConfigurationSection("repair.cost").getKeys(false)) {
                            if (e.getCursor().getType().toString().contains(type + "_")) {
                                cost = config.getInt("repair.cost." + type);
                            }
                        }
                    }
                    cost *= getUsed(e.getCursor());
                    updateLore(newConfirm, cost);
                    repairInv.setItem(5, newConfirm);
                    new BukkitRunnable() {
                        public void run() {
                            p.updateInventory();
                        }
                    }.runTaskLater(AiriEnchanting.getPlugin(), 4);
                } else {
                    e.setCancelled(true);
                    // Check if this is a repair event
                    ItemStack playerItem = repairInv.getItem(3);
                    if (e.getSlot() == 5 && playerItem != null && playerItem.hasItemMeta() && playerItem.getItemMeta() instanceof Damageable) {
                        ItemMeta meta = playerItem.getItemMeta();
                        Damageable damagedMeta = (Damageable) meta;
                        int cost = 30;
                        if (repairInv.getItem(3) == null || repairInv.getItem(3).getType() == Material.AIR) {
                            cost = 0;
                        } else {
                            for (String type : config.getConfigurationSection("repair.cost").getKeys(false)) {
                                if (repairInv.getItem(3).getType().toString().contains(type + "_")) {
                                    cost = config.getInt("repair.cost." + type);
                                }
                            }
                        }
                        cost *= getUsed(repairInv.getItem(3));
                        if (p.getLevel() > cost) {
                            p.setLevel(p.getLevel() - cost);
                            damagedMeta.setDamage(0);
                            p.closeInventory();
                            playerItem.setItemMeta((ItemMeta) damagedMeta);
                            p.getInventory().addItem(playerItem);
                        } else {
                            p.sendMessage(ChatColor.RED + "You do not have enough levels!");
                            p.closeInventory();
                        }
                    }
                }
            }
        }
    }

    // Get the percentage durability of an item
    private double getUsed(ItemStack itemStack) {
        if (itemStack.getItemMeta() instanceof Damageable) {
            return ((Damageable) itemStack.getItemMeta()).getDamage() / (double) itemStack.getType().getMaxDurability();
        }
        return 0;
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
