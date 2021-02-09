package com.vhbob.airienchanting.repair_rename;

import com.jojodmo.itembridge.ItemBridge;
import com.jojodmo.itembridge.ItemBridgeKey;
import com.vhbob.airienchanting.AiriEnchanting;
import com.vhbob.airienchanting.util.Utils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RepairRenameInteractions implements Listener {

    private final FileConfiguration config = AiriEnchanting.getPlugin().getConfig();
    private final ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    private final String chooseTitle = ChatColor.translateAlternateColorCodes('&', config.getString("repair_rename.title"));
    private final String repairTitle = ChatColor.translateAlternateColorCodes('&', config.getString("repair.title"));
    private final String renameTitle = ChatColor.translateAlternateColorCodes('&', config.getString("rename.title"));
    private final int renameCost = config.getInt("rename.cost");
    private HashMap<Player, ItemStack> renaming;
    private ArrayList<Player> cuiRepair;

    public RepairRenameInteractions() {
        renaming = new HashMap<Player, ItemStack>();
        cuiRepair = new ArrayList<Player>();
    }

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
    public void repairActions(final InventoryClickEvent e) {
        if (e.getView().getTitle().equalsIgnoreCase(repairTitle)) {
            final Inventory repairInv = e.getView().getTopInventory();
            final Player p = (Player) e.getWhoClicked();
            // Stop clicks outside of the player item slot
            if (e.getClickedInventory() != null && e.getClickedInventory().equals(e.getView().getTopInventory())) {
                if (e.getSlot() != 3)
                    e.setCancelled(true);
                // Update lore cost display
                if (e.getSlot() == 5) {
                    ItemStack playerItem = repairInv.getItem(3);
                    if (playerItem == null || !playerItem.hasItemMeta() || !(playerItem.getItemMeta() instanceof Damageable)) {
                        p.closeInventory();
                        p.sendMessage(ChatColor.RED + "There is no item in the repair slot!");
                        return;
                    }
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
                        cost = (int) Math.ceil(cost * getUsed(playerItem));
                    }
                    // Repair if we can
                    if (p.getLevel() >= cost) {
                        p.setLevel(p.getLevel() - cost);
                        // Repair custom item or regular item
                        ItemBridgeKey key = ItemBridge.getItemKey(playerItem);
                        if (key != null && key.getNamespace().equalsIgnoreCase("customitems")) {
                            // Swap hand items
                            ItemStack hand = p.getInventory().getItemInMainHand();
                            String permString = config.getString("repair_cui_perm");
                            // Run command
                            p.getInventory().setItemInMainHand(playerItem);
                            if (!p.hasPermission(permString)) {
                                User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
                                Node node = Node.builder(permString).build();
                                user.data().add(node);
                                Bukkit.dispatchCommand(p, config.getString("repair_cui_cmd"));
                                user.data().remove(node);
                            } else {
                                Bukkit.dispatchCommand(p, config.getString("repair_cui_cmd"));
                            }
                            // Reset hand
                            ItemStack repairedCUI = p.getInventory().getItemInMainHand();
                            p.getInventory().setItemInMainHand(hand);
                            p.getInventory().addItem(repairedCUI);
                            cuiRepair.add(p);
                        } else {
                            damagedMeta.setDamage(0);
                            playerItem.setItemMeta((ItemMeta) damagedMeta);
                        }
                        p.closeInventory();
                        String soundText = config.getString("repair.sound");
                        if (!soundText.equalsIgnoreCase("none")) {
                            p.getWorld().playSound(p.getLocation(), Sound.valueOf(soundText), 1, 1);
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "You do not have enough levels to do that!");
                        p.closeInventory();
                    }
                }
            }
            // Update cost
            new BukkitRunnable() {
                public void run() {
                    ItemStack newConfirm = Utils.getItem("repair.confirm");
                    int cost = 30;
                    ItemStack repairing = repairInv.getItem(3);
                    if (repairing == null ||repairing.getType() == Material.AIR) {
                        cost = 0;
                    } else {
                        for (String type : config.getConfigurationSection("repair.cost").getKeys(false)) {
                            if (repairing.getType().toString().contains(type + "_")) {
                                cost = config.getInt("repair.cost." + type);
                            }
                        }
                        cost = (int) Math.ceil(cost * getUsed(repairing));
                    }
                    updateLore(newConfirm, cost);
                    repairInv.setItem(5, newConfirm);
                    p.updateInventory();
                }
            }.runTaskLater(AiriEnchanting.getPlugin(), 4);
        }
    }

    @EventHandler
    public void purchaseRename(InventoryClickEvent e) {
        // Check if the user clicked the confirm button
        Inventory renameInv = e.getView().getTopInventory();
        if (e.getView().getTitle().equalsIgnoreCase(renameTitle) && e.getClickedInventory().equals(renameInv) && e.getSlot() != 3) {
            e.setCancelled(true);
            if (e.getSlot() == 5) {
                Player p = (Player) e.getWhoClicked();
                ItemStack playerItem = renameInv.getItem(3);
                if (playerItem != null) {
                    // Complete the action if they have enough levels
                    if (p.getLevel() > renameCost) {
                        if (playerItem.getType() != Material.AIR) {
                            p.setLevel(p.getLevel() - renameCost);
                            renaming.put(p, playerItem);
                            p.sendMessage(ChatColor.GREEN + "Enter the item's new name in chat");
                            if (p.hasPermission("rename.color")) {
                                p.sendMessage(ChatColor.GREEN + "Color codes are enabled!");
                            } else {
                                p.sendMessage(ChatColor.RED + "Color codes are disabled!");
                            }
                            p.closeInventory();
                        }
                    } else {
                        p.closeInventory();
                        p.sendMessage(ChatColor.RED + "You do not have enough levels to do that!");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "There is no item to rename in the slot!");
                    p.closeInventory();
                }
            }
        }
    }

    @EventHandler
    public void renameItem(AsyncPlayerChatEvent e) {
        // Complete rename
        Player p = e.getPlayer();
        if (renaming.containsKey(p)) {
            e.setCancelled(true);
            String name = e.getMessage();
            // Ensure name is valid
            if (name.length() <= config.getInt("rename.max_length")) {
                ItemStack playerItem = renaming.get(p);
                ItemMeta itemMeta = playerItem.getItemMeta();
                if (p.hasPermission("rename.color")) {
                    itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                } else {
                    itemMeta.setDisplayName(name);
                }
                playerItem.setItemMeta(itemMeta);
                p.getInventory().addItem(playerItem);
                renaming.remove(p);
                p.sendMessage(ChatColor.GREEN + "Rename successful!");
                String soundText = config.getString("rename.sound");
                if (!soundText.equalsIgnoreCase("none")) {
                    p.getWorld().playSound(p.getLocation(), Sound.valueOf(soundText), 1, 1);
                }
            } else {
                p.sendMessage(ChatColor.RED + "That name is too long! Try again!");
            }
        }
    }

    @EventHandler
    public void dcWhileRenaming(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (renaming.containsKey(p)) {
            // Give item they were renaming
            p.getInventory().addItem(renaming.get(p));
        }
    }

    @EventHandler
    public void saveItems(InventoryCloseEvent e) {
        if (e.getView().getTitle().equalsIgnoreCase(repairTitle) || e.getView().getTitle().equalsIgnoreCase(renameTitle)) {
            ItemStack pItem = e.getView().getTopInventory().getItem(3);
            if (pItem != null && !renaming.containsKey(e.getPlayer()) && !cuiRepair.remove(e.getPlayer())) {
                e.getPlayer().getInventory().addItem(pItem);
            }
        }
    }

    @EventHandler
    public void onDC(PlayerQuitEvent e) {
        e.getPlayer().closeInventory();
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
