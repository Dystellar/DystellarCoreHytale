package gg.dystellar.core.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Msgs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This command lets you switch between servers, it tries to send a plugin message and the proxy handles the rest.
 * Useful because with this you can join players in an automatized way, as /server can't be used from the servers.
 */
public class JoinCommand implements CommandExecutor {

    public JoinCommand() {
        Bukkit.getPluginCommand("join").setExecutor(this);
        Bukkit.getPluginCommand("j").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player p = (Player)commandSender;
        if (strings.length < 1) {
            p.sendMessage(ChatColor.RED + "Usage: /join <server>");
            return true;
        }
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(strings[0]);
        p.sendMessage(ChatColor.GREEN + "Connecting...");
        p.sendPluginMessage(DystellarCore.getInstance(), "BungeeCord", out.toByteArray());
        Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> {
            if (p.isOnline()) {
                p.sendMessage(Msgs.SERVER_CONNECTION_ERROR);
            }
        }, 30L);
        return true;
    }
}
