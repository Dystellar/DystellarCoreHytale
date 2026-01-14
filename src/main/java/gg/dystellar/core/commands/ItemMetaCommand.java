package gg.dystellar.core.commands;

import net.zylesh.dystellarcore.core.Msgs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets you change display name and description of an item you are holding in your hand.
 */
public final class ItemMetaCommand implements CommandExecutor {

    public ItemMetaCommand() {
        Bukkit.getPluginCommand("itemmeta").setExecutor(this);
        Bukkit.getPluginCommand("im").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (strings.length < 2) {
                sendHelp(player);
                return true;
            }
            switch (strings[0]) {
                case "displayname":
                    if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
                        player.sendMessage(ChatColor.RED + "You must hold an item.");
                        return true;
                    }
                    ItemMeta metaName = player.getItemInHand().getItemMeta();
                    metaName.setDisplayName(ChatColor.translateAlternateColorCodes('&', strings[1]));
                    player.getItemInHand().setItemMeta(metaName);
                    player.sendMessage(ChatColor.GREEN + "Item Meta applied!");
                    break;
                case "lore":
                    if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
                        player.sendMessage(ChatColor.RED + "You must hold an item.");
                        return true;
                    }
                    List<String> lore = new ArrayList<>();
                    for (String sa : strings[1].split(";")) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', sa).replaceAll("-", " "));
                    }
                    ItemMeta metaLore = player.getItemInHand().getItemMeta();
                    metaLore.setLore(lore);
                    player.getItemInHand().setItemMeta(metaLore);
                    player.sendMessage(ChatColor.GREEN + "Item Meta applied!");
                    break;
                default:
                    sendHelp(player);
                    break;
            }
        } else {
            commandSender.sendMessage(Msgs.ERROR_NOT_A_PLAYER);
        }
        return true;
    }

    private void sendHelp(Player player) {
        String[] strings = new String[] {
                ChatColor.AQUA + "/itemmeta displayname <name>",
                ChatColor.AQUA + "/itemmeta lore <example;my-custom-lore;selected;lol>"
        };
        player.sendMessage(strings);
    }
}

