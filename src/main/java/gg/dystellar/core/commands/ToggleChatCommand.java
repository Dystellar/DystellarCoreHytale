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
 * This command lets you enable/disable the global chat.
 */
public class ToggleChatCommand implements CommandExecutor {

    public ToggleChatCommand() {
        Bukkit.getPluginCommand("toggleglobalchat").setExecutor(this);
        Bukkit.getPluginCommand("togglechat").setExecutor(this);
        Bukkit.getPluginCommand("tgc").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            User playerUser = User.get(player);
            playerUser.toggleGlobalChat();
            if (playerUser.isGlobalChatEnabled()) {
                player.sendMessage(Msgs.GLOBAL_CHAT_ENABLED);
            } else {
                player.sendMessage(Msgs.GLOBAL_CHAT_DISABLED);
            }
        } else {
            commandSender.sendMessage(Msgs.ERROR_NOT_A_PLAYER);
        }
        return true;
    }
}
