package gg.dystellar.core.commands;

import net.zylesh.dystellarcore.core.Msgs;
import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This lets you block a player from messaging you, useful for obnoxious and toxic people.
 */
public class IgnoreCommand implements CommandExecutor {

    public IgnoreCommand() {
        Bukkit.getPluginCommand("ignore").setExecutor(this);
        Bukkit.getPluginCommand("block").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player p = (Player) commandSender;
        if (strings.length < 1) {
            p.sendMessage(ChatColor.RED + "Usage: /block <player>");
            return true;
        }
        Player pInt = Bukkit.getPlayer(strings[0]);
        if (pInt == null || !pInt.isOnline()) {
            p.sendMessage(Msgs.ERROR_PLAYER_DOES_NOT_EXIST);
            return true;
        }
        User u = User.get(p);
        if (u.getIgnoreList().add(pInt.getUniqueId())) {
            p.sendMessage(Msgs.PLAYER_BLOCKED.replace("<player>", pInt.getName()));
            if (u.getPrivateMessagesMode() == User.PMS_ENABLED) {
                p.sendMessage(Msgs.BLOCKING_POINTLESS_HINT);
            }
        } else {
            p.sendMessage(Msgs.ERROR_PLAYER_ALREADY_BLOCKED);
        }
        return true;
    }
}
