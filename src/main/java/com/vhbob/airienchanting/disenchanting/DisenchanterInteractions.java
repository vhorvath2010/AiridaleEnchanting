package com.vhbob.airienchanting.disenchanting;

import com.vhbob.airienchanting.AiriEnchanting;
import com.vhbob.airienchanting.ebooks.GiveBook;
import com.vhbob.airienchanting.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class DisenchanterInteractions implements Listener {

    private static final FileConfiguration config = AiriEnchanting.getPlugin().getConfig();
    private static final String disenchanterTitle = ChatColor.translateAlternateColorCodes('&',
            config.getString("disenchanting.title"));
    private static final String safeTitle = ChatColor.translateAlternateColorCodes('&',
            config.getString("disenchanting.safe_title"));
    private static final String unsafeTitle = ChatColor.translateAlternateColorCodes('&',
            config.getString("disenchanting.unsafe_title"));
    private static final ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);

    @EventHandler
    public void onOpen(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null &&
                e.getClickedBlock().getType().toString().equalsIgnoreCase(AiriEnchanting.getPlugin().getConfig().getString("disenchanting.block"))) {
            // Create inventory and add fillers
            Inventory disenchanter = Bukkit.createInventory(null, 27, disenchanterTitle);
            // Add selectors
            ItemStack safe = Utils.getItem("disenchanting.safe");
            ItemStack unsafe = Utils.getItem("disenchanting.unsafe");
            for (int i = 0; i < disenchanter.getSize(); ++i) {
                disenchanter.setItem(i, filler);
            }
            disenchanter.setItem(12, safe);
            disenchanter.setItem(14, unsafe);
            e.getPlayer().openInventory(disenchanter);
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void selectType(InventoryClickEvent e) {
        // Check if they clicked a selector
        if (e.getView().getTitle().equalsIgnoreCase(disenchanterTitle)) {
            e.setCancelled(true);
            if (e.getCurrentItem() != null) {
                ItemStack safe = Utils.getItem("disenchanting.safe");
                ItemStack unsafe = Utils.getItem("disenchanting.unsafe");
                if (e.getCurrentItem().equals(safe)) {
                    // Open safe disenchanting
                    Inventory safeDisenchanter = Bukkit.createInventory(null, 9, safeTitle);
                    for (int i = 0; i < 9; ++i) {
                        if (i != 3)
                            safeDisenchanter.setItem(i, filler);
                    }
                    safeDisenchanter.setItem(5, Utils.getItem("disenchanting.safe_confirm"));
                    e.getWhoClicked().openInventory(safeDisenchanter);
                } else if (e.getCurrentItem().equals(unsafe)) {
                    // Open unsafe disenchanting
                    Inventory unsafeDisenchanter = Bukkit.createInventory(null, 9, unsafeTitle);
                    for (int i = 0; i < 9; ++i) {
                        if (i != 3)
                            unsafeDisenchanter.setItem(i, filler);
                    }
                    unsafeDisenchanter.setItem(5, Utils.getItem("disenchanting.unsafe_confirm"));
                    e.getWhoClicked().openInventory(unsafeDisenchanter);
                }
            }
        }
    }

    @EventHandler
    public void safeDisenchant(InventoryClickEvent e) {
        // Ensure we are safe disenchanting
        if (e.getView().getTitle().equalsIgnoreCase(safeTitle)) {
            Inventory disenchanter = e.getView().getTopInventory();
            if (e.getClickedInventory() == disenchanter) {
                if (e.getSlot() != 3)
                    e.setCancelled(true);
                if (e.getSlot() == 5) {
                    Player p = (Player) e.getWhoClicked();
                    if (disenchanter.getItem(3) != null) {
                        ItemStack playerItem = disenchanter.getItem(3);
                        // Stop if no enchantments
                        if (playerItem.getEnchantments().size() == 0) {
                            p.closeInventory();
                            p.sendMessage(ChatColor.RED + "That item has no enchantments on it!");
                            e.getWhoClicked().getInventory().addItem(playerItem);
                            return;
                        }
                        // Setup cool animation stuff
                        // Remove enchantments
                        for (Enchantment ench : playerItem.getEnchantments().keySet()) {
                            playerItem.removeEnchantment(ench);
                        }
                        p.getInventory().addItem(playerItem);
                        p.closeInventory();
                    }
                }
            }
        }
    }

    @EventHandler
    public void unsafeDisenchant(InventoryClickEvent e) {
        // Ensure we are safe disenchanting
        if (e.getView().getTitle().equalsIgnoreCase(unsafeTitle)) {
            Inventory disenchanter = e.getView().getTopInventory();
            if (e.getClickedInventory() == disenchanter) {
                if (e.getSlot() != 3)
                    e.setCancelled(true);
                if (e.getSlot() == 5) {
                    Player p = (Player) e.getWhoClicked();
                    if (disenchanter.getItem(3) != null) {
                        ItemStack playerItem = disenchanter.getItem(3);
                        // Stop if no enchantments
                        if (playerItem.getEnchantments().size() == 0) {
                            p.closeInventory();
                            p.sendMessage(ChatColor.RED + "That item has no enchantments on it!");
                            e.getWhoClicked().getInventory().addItem(playerItem);
                            return;
                        }
                        // Setup cool animation stuff
                        // Remove enchantments
                        Map<Enchantment, Integer> enchs = playerItem.getEnchantments();
                        double chance = getChance(playerItem);
                        for (Enchantment ench : enchs.keySet()) {
                            ItemStack ebook = GiveBook.generateEBook(enchs.get(ench), ench);
                            double roll = Math.random() * 100;
                            if (roll < chance) {
                                p.getInventory().addItem(ebook);
                            }
                        }
                        p.closeInventory();
                    }
                }
            }
        }
    }

    private double getChance(ItemStack item) {
        for (String type : config.getConfigurationSection("disenchanting.unsafe_odds").getKeys(false)) {
            if (item.getType().toString().contains(type.toUpperCase() + "_")) {
                return config.getInt("disenchanting.unsafe_odds." + type);
            }
        }
        return 100;
    }

}
