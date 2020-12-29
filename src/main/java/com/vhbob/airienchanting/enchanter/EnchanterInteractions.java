package com.vhbob.airienchanting.enchanter;

import com.vhbob.airienchanting.AiriEnchanting;
import com.vhbob.airienchanting.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class EnchanterInteractions implements Listener {

    private static FileConfiguration config = AiriEnchanting.getPlugin().getConfig();
    private static HashMap<Player, Block> interaction;
    private static String enchanterTitle = ChatColor.translateAlternateColorCodes('&', config.getString("enchanting.title"));
    private static HashMap<Player, Integer> pages;
    private static ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
    private static ItemStack nextPage = Utils.getItem("enchanting.next_page");
    private static ItemStack prevPage = Utils.getItem("enchanting.prev_page");

    public EnchanterInteractions() {
        interaction = new HashMap<Player, Block>();
        pages = new HashMap<Player, Integer>();
    }

    @EventHandler
    public void onOpen(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null &&
                e.getClickedBlock().getType().toString().equalsIgnoreCase(AiriEnchanting.getPlugin().getConfig().getString("enchanting.block"))) {
            // Create inventory and add fillers
            Inventory enchanter = Bukkit.createInventory(null, 45, enchanterTitle);
            ItemStack purpleFiller = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE, 1);
            ItemStack magentaFiller = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE, 1);
            for (int i = 0; i < enchanter.getSize(); ++i) {
                enchanter.setItem(i, filler);
            }
            for (int i = 0; i < enchanter.getSize(); ++i) {
                if (i % 9 == 1) {
                    enchanter.setItem(i, magentaFiller);
                } else if (i % 9 < 3) {
                    enchanter.setItem(i, purpleFiller);
                }
            }
            enchanter.setItem(19, new ItemStack(Material.AIR, 1));
            e.getPlayer().openInventory(enchanter);
            interaction.put(e.getPlayer(), e.getClickedBlock());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onUse(InventoryClickEvent e) {
        if (e.getView().getTitle().equals(enchanterTitle) && e.getClickedInventory() != null) {
            final Player p = (Player) e.getWhoClicked();
            if (e.getClickedInventory().equals(e.getView().getTopInventory()) && e.getSlot() != 19) {
                e.setCancelled(true);
            }
            // Stop swaps
            if (e.getSlot() == 19 && e.getClickedInventory().equals(e.getView().getTopInventory()) && e.getCurrentItem() != null
            && e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                e.setCancelled(true);
                return;
            }
            Inventory enchanter = e.getView().getTopInventory();
            if (e.getSlot() == 19) {
                updateEnchantments(enchanter, e.getCursor(), 1, p);
            } else if (e.getSlot() == 19 && e.getClick().isShiftClick() && e.getClickedInventory().equals(e.getView().getTopInventory())) {
                updateEnchantments(enchanter, null, 1, p);
            } else if (e.getClick().isShiftClick() && e.getClickedInventory().equals(e.getView().getBottomInventory())
                    && (e.getView().getTopInventory().getItem(19) == null) || e.getView().getTopInventory().getItem(19).getType() == Material.AIR) {
                updateEnchantments(enchanter, e.getCurrentItem(), 1, p);
            }
            new BukkitRunnable() {
                public void run() {
                    p.updateInventory();
                }
            }.runTaskLater(AiriEnchanting.getPlugin(), 0);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getView().getTitle().equals(enchanterTitle)) {
            if (e.getView().getTopInventory().getItem(19) != null)
                e.getView().getBottomInventory().addItem(e.getView().getTopInventory().getItem(19));
            interaction.remove((Player) e.getPlayer());
            pages.remove((Player) e.getPlayer());
        }
    }

    @EventHandler
    public void changePage(InventoryClickEvent e) {
        if (e.getCurrentItem() != null && e.getView().getTitle().equalsIgnoreCase(enchanterTitle)) {
            Inventory enchanter = e.getView().getTopInventory();
            Player p = (Player) e.getWhoClicked();
            // Check for page changes
            if (e.getCurrentItem().equals(nextPage)) {
                updateEnchantments(enchanter, enchanter.getItem(19), pages.get(p) + 1, p);
            } else if (e.getCurrentItem().equals(prevPage)) {
                updateEnchantments(enchanter, enchanter.getItem(19), pages.get(p) - 1, p);
            }
        }
    }

    // Update the inventory to display the proper enchants
    private void updateEnchantments(Inventory enchanter, ItemStack item, int page, final Player player) {
        if (item == null || item.getType() == Material.AIR) {
            for (int i = 0; i < enchanter.getSize(); ++i) {
                if (i % 9 >= 3) {
                    enchanter.setItem(i, filler);
                }
            }
            pages.remove(player);
            enchanter.setItem(38, new ItemStack(Material.PURPLE_STAINED_GLASS_PANE, 1));
            enchanter.setItem(36, new ItemStack(Material.PURPLE_STAINED_GLASS_PANE, 1));
        } else {
            for (String type : config.getConfigurationSection("enchanting").getKeys(false)) {
                if (item.getType().toString().contains("_" + type.toUpperCase()) || item.getType().toString().equalsIgnoreCase(type)) {
                    // Add previous page icon if needed and clear last enchants
                    if (page > 1) {
                        for (int i = 0; i < enchanter.getSize(); ++i) {
                            if (i % 9 >= 3) {
                                enchanter.setItem(i, filler);
                            }
                        }
                        enchanter.setItem(36, prevPage);
                    } else {
                        enchanter.setItem(36, new ItemStack(Material.PURPLE_STAINED_GLASS_PANE, 1));
                    }
                    // Added type's enchantments to the inventory
                    int row = 0, enchNum = 1;
                    for (String enchantment : config.getConfigurationSection("enchanting." + type).getKeys(false)) {
                        // Stop if we are at max rows
                        if (row == 5) {
                            enchanter.setItem(38, nextPage);
                            pages.put(player, page);
                            return;
                        }
                        // Ensure we are on the right page
                        if (enchNum > (page - 1) * 5) {
                            // Get enchantment data
                            Material iconType = Material.valueOf(config.getString("enchanting." + type + "." + enchantment + ".icon"));
                            ItemStack icon = new ItemStack(iconType, 1);
                            ItemMeta im = icon.getItemMeta();
                            String enchantmentFormatted = enchantment.toUpperCase().charAt(0) + enchantment.substring(1).toLowerCase();
                            im.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + enchantmentFormatted.replace("_", " "));
                            icon.setItemMeta(im);
                            enchanter.setItem(row * 9 + 3, icon);
                            // Add levels
                            for (int level = 1; level <= config.getInt("enchanting." + type + "." + enchantment + ".max_level"); level++) {
                                int cost = config.getInt("enchanting." + type + "." + enchantment + ".level_costs." + level);
                                int multi = AiriEnchanting.getPlugin().getMulti(item);
                                Enchantment enchantmentEnum = Enchantment.getByKey(NamespacedKey.minecraft(enchantment.toLowerCase()));
                                if (enchantmentEnum == null) {
                                    Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error: " + enchantment + " is not a valid enchantment!");
                                    return;
                                }
                                if (item.getEnchantmentLevel(enchantmentEnum) >= level) {
                                    ItemStack lvlHas = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
                                    ItemMeta lvlHasIM = lvlHas.getItemMeta();
                                    lvlHasIM.setDisplayName(ChatColor.GREEN + "Level " + level + ": Achieved");
                                    lvlHas.setItemMeta(lvlHasIM);
                                    enchanter.setItem(row * 9 + 3 + level, lvlHas);
                                } else {
                                    ItemStack lvlHas = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
                                    ItemMeta lvlHasIM = lvlHas.getItemMeta();
                                    lvlHasIM.setDisplayName(ChatColor.RED + "Level " + level + ": " + ChatColor.GRAY + cost * multi + " Souls");
                                    lvlHas.setItemMeta(lvlHasIM);
                                    enchanter.setItem(row * 9 + 3 + level, lvlHas);
                                }
                            }
                            row++;
                        }
                        enchNum++;
                    }
                    enchanter.setItem(38, new ItemStack(Material.PURPLE_STAINED_GLASS_PANE, 1));
                    pages.put(player, page);
                    return;
                }
            }
        }
    }

    public static Block getClicked(Player p) {
        return interaction.get(p);
    }

}
