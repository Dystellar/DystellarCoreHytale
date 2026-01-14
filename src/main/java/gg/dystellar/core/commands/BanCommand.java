package gg.dystellar.core.commands;

import java.time.LocalDateTime;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;

/**
 * Custom ban command, generally the same as the builtin ban command but this also notifies the backend and updates the player's profile.
 * Porting this to hytale's API
 */
public class BanCommand extends CommandBase {

    public BanCommand(String name, String description) {
		super(name, description);
    }

	@Override
	protected void executeSync(CommandContext ctx) {
	    final var sender = ctx.sender();
		if (!sender.hasPermission("dystellar.punish")) {
			ctx.sendMessage(Message.raw("§4No permission"));
			return;
		}
	}

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!commandSender.hasPermission("dystellar.admin")) {
            commandSender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (strings.length < 3) {
            commandSender.sendMessage(ChatColor.RED + "Usage: /ban <player> <time> <reason> [Optional: BanAlsoIP: <true> (only write if true, if you don't write anything at the end it defaults to false)]");
            return true;
        }
        Player playerInt = Bukkit.getPlayer(strings[0]);
        if (playerInt != null && playerInt.isOnline()) {
            User userInt = User.get(playerInt);
            LocalDateTime time = LocalDateTime.now();
            for (String e : strings[1].split(",")) {
                if (!e.matches("[0-9]+[dhm]")) {
                    commandSender.sendMessage(ChatColor.RED + "The format is incorrect.");
                    return true;
                }
                int integer = Integer.parseInt(e.substring(0, 1));
                switch (e.charAt(e.length() - 1)) {
                    case 'd': time = time.plusDays(integer); break;
                    case 'h': time = time.plusHours(integer); break;
                    case 'm': time = time.plusMinutes(integer); break;
                }
            }
            StringBuilder reason = new StringBuilder();
            boolean isIpBan = false;
            for (int i = 2; i < strings.length; i++) {
                if (i == strings.length - 1 && strings[i].equalsIgnoreCase("true")) {
                    isIpBan = true;
                    break;
                }
                if (i == 2) reason.append(strings[i]);
                else reason.append(" ").append(strings[i]);
            }
            Ban ban = new Ban(time, reason.toString());
            ban.setAlsoIP(isIpBan);
            userInt.punish(ban);
        } else {
            DystellarCore.getAsyncManager().execute(() -> {
                Mapping mapping = MariaDB.loadMapping(strings[1]);
                if (mapping == null) {
                    commandSender.sendMessage(ChatColor.RED + "This player does not exist in the database.");
                } else {
                    User user = MariaDB.loadPlayerFromDatabase(mapping.getUUID(), mapping.getIP(), mapping.getName());
                    LocalDateTime time;
                    if (strings[1].equalsIgnoreCase("null")) {
                        time = null;
                    } else {
                        time = LocalDateTime.now();
                        for (String e : strings[1].split(",")) {
                            if (!e.matches("[0-9]+[dhm]")) {
                                commandSender.sendMessage(ChatColor.RED + "The format is incorrect.");
                                return;
                            }
                            int integer = Integer.parseInt(e.substring(0, e.length() - 1));
                            switch (e.charAt(e.length() - 1)) {
                                case 'd': time = time.plusDays(integer); break;
                                case 'h': time = time.plusHours(integer); break;
                                case 'm': time = time.plusMinutes(integer); break;
                            }
                        }
                    }
                    StringBuilder reason = new StringBuilder();
                    boolean isIpBan = false;
                    for (int i = 2; i < strings.length; i++) {
                        if (i == strings.length - 1 && strings[i].equalsIgnoreCase("true")) {
                            isIpBan = true;
                            break;
                        }
                        if (i == 2) reason.append(strings[i]);
                        else reason.append(" ").append(strings[i]);
                    }
                    Ban ban = new Ban(time, reason.toString());
                    ban.setAlsoIP(isIpBan);
                    user.addPunishment(ban);
                    MariaDB.savePlayerToDatabase(user);
                }
            });
        }

        return true;
    }
}

