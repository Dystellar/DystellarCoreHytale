package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Msgs;
import net.zylesh.dystellarcore.core.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleGlobalTabComplete implements CommandExecutor {

    public ToggleGlobalTabComplete() {
        Bukkit.getPluginCommand("toggleglobaltabcomplete").setExecutor(this);
        Bukkit.getPluginCommand("tgtc").setExecutor(this);
        Bukkit.getPluginCommand("toggletabcomplete").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player p = (Player) commandSender;
        User user = User.get(p);
        user.toggleGlobalTabComplete();
        if (user.isGlobalTabComplete()) {
            DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.GLOBAL_TAB_REGISTER);
            p.sendMessage(Msgs.GLOBAL_TAB_ENABLED);
        } else {
            DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.GLOBAL_TAB_UNREGISTER);
            p.sendMessage(Msgs.GLOBAL_TAB_DISABLED);
        }
        return true;
    }
}
