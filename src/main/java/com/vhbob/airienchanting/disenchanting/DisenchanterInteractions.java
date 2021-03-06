package com.vhbob.airienchanting.disenchanting;

import com.vhbob.airienchanting.AiriEnchanting;
import com.vhbob.airienchanting.ebooks.GiveBook;
import com.vhbob.airienchanting.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.HashMap;
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
    private static HashMap<Player, Block> clicked;
    private ArrayList<Player> avoidGive;

    public DisenchanterInteractions() {
        clicked = new HashMap<Player, Block>();
        avoidGive = new ArrayList<Player>();
    }

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
            clicked.put(e.getPlayer(), e.getClickedBlock());
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
                final Player p = (Player) e.getWhoClicked();
                if (e.getSlot() == 5) {
                    if (disenchanter.getItem(3) != null) {
                        final ItemStack playerItem = disenchanter.getItem(3);
                        // Stop if no enchantments
                        if (playerItem.getEnchantments().size() == 0) {
                            p.closeInventory();
                            p.sendMessage(ChatColor.RED + "That item has no enchantments on it!");
                            return;
                        }
                        // Remove enchantments
                        for (Enchantment ench : playerItem.getEnchantments().keySet()) {
                            playerItem.removeEnchantment(ench);
                        }
                        // Spawn animations
                        Location animLoc = clicked.get(p).getLocation().add(0.5, -0.5, 0.5);
                        playDisenchantEffect(animLoc, playerItem, 60);
                        new BukkitRunnable() {
                            public void run() {
                                p.getInventory().addItem(playerItem);
                            }
                        }.runTaskLater(AiriEnchanting.getPlugin(), 60);
                        avoidGive.add(p);
                        p.closeInventory();
                    } else {
                        p.sendMessage(ChatColor.RED + "There is no item in the disenchanter!");
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
                    final Player p = (Player) e.getWhoClicked();
                    if (disenchanter.getItem(3) != null) {
                        ItemStack playerItem = disenchanter.getItem(3);
                        // Stop if no enchantments
                        if (playerItem.getEnchantments().size() == 0) {
                            p.closeInventory();
                            p.sendMessage(ChatColor.RED + "That item has no enchantments on it!");
                            return;
                        }
                        // Setup cool animation stuff
                        Location animLoc = clicked.get(p).getLocation().add(0.5, -0.5, 0.5);
                        playDisenchantEffect(animLoc, playerItem, 80);
                        // Roll for enchantment books
                        final Map<Enchantment, Integer> enchs = playerItem.getEnchantments();
                        final double chance = getChance(playerItem);
                        new BukkitRunnable() {
                            public void run() {
                                for (Enchantment ench : enchs.keySet()) {
                                    ItemStack ebook = GiveBook.generateEBook(enchs.get(ench), ench);
                                    double roll = Math.random() * 100;
                                    if (roll < chance) {
                                        p.sendMessage(ChatColor.GREEN + "You obtained a(n) " + ench.getKey().getKey() + " book!");
                                        p.getInventory().addItem(ebook);
                                    } else {
                                        p.sendMessage(ChatColor.RED + "The " + ench.getKey().getKey().replace("_", " ") + " enchantment disappeared!");
                                    }
                                }
                            }
                        }.runTaskLater(AiriEnchanting.getPlugin(), 80);
                        avoidGive.add(p);
                        p.closeInventory();
                    } else {
                        p.sendMessage(ChatColor.RED + "There is no item in the disenchanter!");
                        p.closeInventory();
                    }
                }
            }
        }
    }

    @EventHandler
    public void saveItems(InventoryCloseEvent e) {
        if (e.getView().getTitle().equalsIgnoreCase(unsafeTitle) || e.getView().getTitle().equalsIgnoreCase(safeTitle)) {
            ItemStack pItem = e.getView().getTopInventory().getItem(3);
            if (pItem != null && !avoidGive.contains(e.getPlayer())) {
                e.getPlayer().getInventory().addItem(pItem);
            }
            avoidGive.remove(e.getPlayer());
        }
    }

    // Play the disenchantment effect at a given location
    private void playDisenchantEffect(final Location loc, ItemStack itemStack, int delay) {
        // Create rotating item
        final ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc.add(0, 0.5, 0), EntityType.ARMOR_STAND);
        stand.getEquipment().setItemInMainHand(itemStack);
        stand.setVisible(false);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setRightArmPose(new EulerAngle(4.6, 0, 0));
        // Spin it
        final int spin = new BukkitRunnable() {
            public void run() {
                Location loc = stand.getLocation();
                float yaw = loc.getYaw();
                yaw += 5;
                stand.setRotation(yaw, 0);
            }
        }.runTaskTimer(AiriEnchanting.getPlugin(), 0, 1).getTaskId();
        // Schedule removal
        new BukkitRunnable() {
            public void run() {
                Bukkit.getScheduler().cancelTask(spin);
                stand.remove();
            }
        }.runTaskLater(AiriEnchanting.getPlugin(), delay);
        // Spawn lightning
        new BukkitRunnable() {
            public void run() {
                for (int i = 0; i < 5; ++i) {
                    loc.getWorld().strikeLightningEffect(loc);
                }
            }
        }.runTaskLater(AiriEnchanting.getPlugin(), delay);
    }

    private double getChance(ItemStack item) {
        for (String type : config.getConfigurationSection("disenchanting.unsafe_odds").getKeys(false)) {
            if (item.getType().toString().contains(type.toUpperCase() + "_") || item.getType().toString().equalsIgnoreCase(type)) {
                return config.getInt("disenchanting.unsafe_odds." + type);
            }
        }
        return 100;
    }

}
