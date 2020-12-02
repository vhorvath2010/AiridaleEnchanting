package com.vhbob.airienchanting.disenchanting;

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



}
