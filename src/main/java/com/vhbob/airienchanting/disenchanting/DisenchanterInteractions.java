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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DisenchanterInteractions implements Listener {

    private static FileConfiguration config = AiriEnchanting.getPlugin().getConfig();
    private static String disenchanterTitle = ChatColor.translateAlternateColorCodes('&',
            config.getString("disenchanting.title"));

    @EventHandler
    public void onOpen(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null &&
                e.getClickedBlock().getType().toString().equalsIgnoreCase(AiriEnchanting.getPlugin().getConfig().getString("disenchanting.block"))) {
            // Create inventory and add fillers
            Inventory disenchanter = Bukkit.createInventory(null, 27, disenchanterTitle);
            // Add selectors
            ItemStack safe = Utils.getItem("disenchanting.safe");
            ItemStack unsafe = Utils.getItem("disenchanting.unsafe");
            ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            for (int i = 0; i < disenchanter.getSize(); ++i) {
                disenchanter.setItem(i, filler);
            }
            disenchanter.setItem(12, safe);
            disenchanter.setItem(14, unsafe);
            e.getPlayer().openInventory(disenchanter);
            e.setCancelled(true);
        }
    }

}
