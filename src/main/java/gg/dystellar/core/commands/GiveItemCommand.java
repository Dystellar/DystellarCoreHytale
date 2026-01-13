package net.zylesh.dystellarcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import gg.zylesh.skywars.SkywarsAPI;

import java.util.Arrays;

public final class GiveItemCommand implements CommandExecutor {

    public GiveItemCommand() {
        Bukkit.getPluginCommand("giveitem").setExecutor(this);
        Bukkit.getPluginCommand("gi").setExecutor(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player))
            return true;
        Player player = (Player) commandSender;
        if (strings.length < 2) {
            sendHelp(player);
            return true;
        }
        if (!strings[1].matches("[0-9]+")) {
            player.sendMessage(ChatColor.RED + "Amount must be a valid number.");
            return true;
        }
        switch (strings[0]) {
            case "pot":
                ItemStack pot = new ItemStack(16421, Integer.parseInt(strings[1]));
                player.setItemInHand(pot);
                break;
            case "goldenhead":
                ItemStack head = new ItemStack(Material.GOLDEN_APPLE, Integer.parseInt(strings[1]));
                ItemMeta meta = head.getItemMeta();
                meta.setDisplayName(SkywarsAPI.GOLDEN_HEAD);
                head.setItemMeta(meta);
                player.setItemInHand(head);
                break;
            default:
                ItemStack stack;
                if (Arrays.stream(Material.values()).noneMatch(material -> material.toString().equals(strings[0].toUpperCase()))) {
                    player.sendMessage(ChatColor.RED + "This material does not exist.");
                    return true;
                }
                if (strings.length > 2 && strings[2].matches("[0-9]+")) {
                    stack = new ItemStack(Material.valueOf(strings[0].toUpperCase()), Integer.parseInt(strings[1]), Short.parseShort(strings[2]));
                } else {
                    stack = new ItemStack(Material.valueOf(strings[0].toUpperCase()), Integer.parseInt(strings[1]));
                }
                player.setItemInHand(stack);
                break;
        }
        return true;
    }

    private void sendHelp(Player player) {
        String[] message = new String[] {
                ChatColor.AQUA + "/giveitem pot <amount>",
                ChatColor.AQUA + "/giveitem goldenhead <amount>",
                ChatColor.AQUA + "/giveitem <MATERIAL_ENUM> <amount> <damage>"
        };
        player.sendMessage(message);
    }
}

