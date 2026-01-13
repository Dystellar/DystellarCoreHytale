package net.zylesh.dystellarcore.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Msgs;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.utils.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplyCommand implements CommandExecutor {

    public ReplyCommand() {
        Bukkit.getPluginCommand("reply").setExecutor(this);
        Bukkit.getPluginCommand("r").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            User playerUser = User.get(player);
            if (playerUser.getPrivateMessagesMode() == User.PMS_DISABLED) {
                player.sendMessage(Msgs.CANT_SEND_PMS_DISABLED);
                return true;
            }
            if (strings.length - 1 >= 0) {
                if (playerUser.getLastMessagedPlayer() != null) {
                    Player lastMessaged = Bukkit.getPlayer(playerUser.getLastMessagedPlayer().getUUID());
                    if (lastMessaged != null) {
                        if (playerUser.getLastMessagedPlayer().getIgnoreList().contains(player.getUniqueId())) {
                            player.sendMessage(Msgs.PLAYER_HAS_BLOCKED_YOU);
                            return true;
                        }
                        StringBuilder message = new StringBuilder();
                        for (String string : strings) {
                            message.append(string).append(" ");
                        }
                        player.sendMessage(DystellarCore.MSG_SEND_FORMAT
                                .replace("-sender", player.getPlayerListName())
                                .replace("-receiver", lastMessaged.getPlayerListName())
                                .replace("-message", message.toString()));
                        lastMessaged.sendMessage(DystellarCore.MSG_RECEIVE_FORMAT
                                .replace("-sender", player.getPlayerListName())
                                .replace("-receiver", lastMessaged.getPlayerListName())
                                .replace("-message", message.toString()));
                    } else {
                        player.sendMessage(Msgs.ERROR_PLAYER_NOT_ONLINE);
                    }
                } else {
                    player.sendMessage(Msgs.ERROR_NO_REPLY_CACHE);
                }
            } else {
                player.sendMessage(ChatColor.RED + "Usage: /r <message>");
            }
        } else {
            commandSender.sendMessage(Msgs.ERROR_NOT_A_PLAYER);
        }
        return true;
    }
}
