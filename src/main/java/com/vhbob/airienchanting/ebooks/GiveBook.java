package com.vhbob.airienchanting.ebooks;

import com.vhbob.airienchanting.AiriEnchanting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class GiveBook implements CommandExecutor {

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        // Check for validity
        if (command.getName().equalsIgnoreCase("ebook")) {
            if (strings.length == 3) {
                Player target = Bukkit.getPlayer(strings[0]);
                if (target != null) {
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(strings[1].toLowerCase()));
                    if (enchantment != null) {
                        FileConfiguration config = AiriEnchanting.getPlugin().getConfig();;
                        // Generate book
                        int lvl = Integer.parseInt(strings[2]);
                        int cost = 30;
                        if (config.contains("ebook.level_cost." + strings[1].toLowerCase() + "." + lvl)) {
                            cost = config.getInt("ebook.level_cost." + strings[1].toLowerCase() + "." + lvl);
                        }
                        ItemStack ebook = new ItemStack(Material.ENCHANTED_BOOK, 1);
                        ebook.addUnsafeEnchantment(enchantment, lvl);
                        ItemMeta ebm = ebook.getItemMeta();
                        ebm.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("ebook.name")));
                        ArrayList<String> lore = new ArrayList<String>();
                        for (String line : config.getStringList("ebook.lore")) {
                            line = line.replace("%cost%", "" + cost);
                            lore.add(ChatColor.translateAlternateColorCodes('&', line));
                        }
                        ebm.setLore(lore);
                        ebook.setItemMeta(ebm);
                        // Give book
                        target.getInventory().addItem(ebook);
                        commandSender.sendMessage(ChatColor.GREEN + "ebook given!");
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "Error: Enchantment not found");
                    }
                } else {
                    commandSender.sendMessage(ChatColor.RED + "Error: Player not found");
                }
            } else {
                commandSender.sendMessage(ChatColor.RED + "Usage: /ebook (player) (enchant) (level)");
            }
        }
        return false;
    }
}
