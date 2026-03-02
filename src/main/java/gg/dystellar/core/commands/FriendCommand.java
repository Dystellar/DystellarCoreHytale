package gg.dystellar.core.commands;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.utils.Pair;
import gg.dystellar.core.utils.Utils;

// TODO: Test this

/**
 * Friends command, lets any player manage their list of friends.
 * Should definitely be implemented in the proxy or redirecter if hytale has one, because otherwise it needs a bunch of protocols and packets and there is no need.
 */
public class FriendCommand extends AbstractCommandCollection {

	private static final Map<UUID, Instant> cooldowns = new ConcurrentHashMap<>(); // Player, cooldown expiration
	private static final Map<UUID, List<Pair<String, Instant>>> pending = new ConcurrentHashMap<>();

    public FriendCommand() {
		super("friend", "Friends system base command");
		this.addAliases("f");
		this.requirePermission("dystellar.friend");

		this.addSubCommand(new AddCommand());
		this.addSubCommand(new RemoveCommand());
		this.addSubCommand(new FindCommand());
		this.addSubCommand(new ListCommand());
		this.addSubCommand(new AcceptCommand());
		this.addSubCommand(new RejectCommand());
		this.addSubCommand(new ToggleCommand());
    }

	private static final class AddCommand extends AbstractPlayerCommand {
		private final RequiredArg<String> targetArg = this.withRequiredArg("target", "The player that receives the request", ArgTypes.STRING);

		AddCommand() {
			super("add", "Friend add command");
			this.requirePermission("dystellar.friend.add");
			this.addAliases("a");
		}

		@Override
		protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
			final var user = p.getHolder().getComponent(UserComponent.getComponentType());
			final var lang = DystellarCore.getInstance().getLang(user.language);
			final var cooldown = cooldowns.get(p.getUuid());

			if (cooldown != null) {
				p.sendMessage(lang.mCooldown.buildMessage().param("seconds", cooldown.getEpochSecond() - Instant.now().getEpochSecond()));
				return;
			}

			Instant cooldownExpiration = Instant.now().plusSeconds(4L);
			cooldowns.put(p.getUuid(), cooldownExpiration);

			HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> { cooldowns.remove(p.getUuid()); }, 4L, TimeUnit.SECONDS);
			var listPendings = pending.get(p.getUuid());

			if (listPendings != null) {
				final var entry = Utils.findList(listPendings, pair -> pair.first.equalsIgnoreCase(target));

				if (entry.isPresent()) {
					p.sendMessage(lang.mCooldown.buildMessage().param("seconds", entry.get().second.getEpochSecond() - Instant.now().getEpochSecond()));
					return;
				}
			} else pending.put(p.getUuid(), Collections.synchronizedList(new ArrayList<>()));

			final var targetPlayer = Universe.get().getPlayerByUsername(target, NameMatching.EXACT_IGNORE_CASE);
			final var list = listPendings == null ? pending.get(p.getUuid()) : listPendings;
			list.add(new Pair<>(target, Instant.now().plusSeconds(30L)));

			// TODO: Move to friend command honestly
			if (targetPlayer != null) {

			} else {
				final int id = Handler.createMessageSession((source, input) -> {

				}, () -> {

					}, 30000L);
			}
			HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
				list.removeIf(pair -> pair.first.equalsIgnoreCase(target));
				if (list.isEmpty())
				pending.remove(p.getUuid());
			}, 30L, TimeUnit.SECONDS);
		}
	}

	private static final class RemoveCommand extends AbstractPlayerCommand {
		private final RequiredArg<String> targetArg = this.withRequiredArg("target", "The player to remove", ArgTypes.STRING);

		RemoveCommand() {
			super("remove", "Friend remove command");
			this.requirePermission("dystellar.friend.remove");
			this.addAliases("delete", "remove", "del", "d", "rm");
		}

		@Override
		protected void execute(CommandContext arg0, Store<EntityStore> arg1, Ref<EntityStore> arg2, PlayerRef arg3, World arg4) {
		}
	}

	private static final class FindCommand extends AbstractPlayerCommand {
		private final RequiredArg<String> targetArg = this.withRequiredArg("target", "The player to find", ArgTypes.STRING);

		FindCommand() {
			super("find", "Friend locate command");
			this.requirePermission("dystellar.friend.find");
			this.addAliases("f", "locate");
		}

		@Override
		protected void execute(CommandContext arg0, Store<EntityStore> arg1, Ref<EntityStore> arg2, PlayerRef arg3, World arg4) {
		}
	}

	private static final class ListCommand extends AbstractPlayerCommand {
		ListCommand() {
			super("list", "List friends");
			this.requirePermission("dystellar.friend.list");
			this.addAliases("l", "ls");
		}

		@Override
		protected void execute(CommandContext arg0, Store<EntityStore> arg1, Ref<EntityStore> arg2, PlayerRef arg3, World arg4) {
		}
	}

	private static final class AcceptCommand extends AbstractPlayerCommand {
		private final RequiredArg<String> targetArg = this.withRequiredArg("target", "The player to find", ArgTypes.STRING);

		AcceptCommand() {
			super("accept", "Friend accept command");
			this.requirePermission("dystellar.friend.accept");
		}

		@Override
		protected void execute(CommandContext arg0, Store<EntityStore> arg1, Ref<EntityStore> arg2, PlayerRef arg3, World arg4) {
		}
	}

	private static final class RejectCommand extends AbstractPlayerCommand {
		@Override
		protected void execute(CommandContext arg0, Store<EntityStore> arg1, Ref<EntityStore> arg2, PlayerRef arg3, World arg4) {
		}
	}

	private static final class ToggleCommand extends AbstractPlayerCommand {
		@Override
		protected void execute(CommandContext arg0, Store<EntityStore> arg1, Ref<EntityStore> arg2, PlayerRef arg3, World arg4) {
		}
	}

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        if (strings.length < 1) {
            commandSender.sendMessage(help);
            return true;
        }
        Player p = (Player) commandSender;
        
        return true;
	}

	switch (strings[0]) {
            case "add": {
                if (strings.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /f add <player>");
                    return true;
                }
                if (cooldowns.contains(p.getUniqueId())) {
                    p.sendMessage(Msgs.ON_COOLDOWN.replace("<seconds>", "20"));
                    return true;
                }
                requestsCache.add(p.getUniqueId());
                DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.FRIEND_ADD_REQUEST, strings[1]);
                break;
            }
            case "remove": {
                if (strings.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /f remove <player>");
                    return true;
                }
                Player pInt = Bukkit.getPlayer(strings[1]);
                User u = User.get(p);
                if (pInt != null && pInt.isOnline()) {
                    if (!u.friends.remove(pInt.getUniqueId())) {
                        p.sendMessage(Msgs.PLAYER_NOT_ON_FRIENDS_LIST);
                        return true;
                    }
                    User uInt = User.get(pInt);
                    uInt.friends.remove(u.getUUID());
                    p.sendMessage(Msgs.FRIEND_REMOVED_SENDER.replace("<player>", pInt.getName()));
                    pInt.sendMessage(Msgs.FRIEND_REMOVED_RECEIVER.replace("<player>", p.getName()));
                } else {
                    DystellarCore.getAsyncManager().submit(() -> {
                        UUID uuid;
                        if (uuidsCache.containsKey(p)) {
                            uuid = uuidsCache.get(p).get(strings[1]);
                        } else {
                            uuid = MariaDB.loadUUID(strings[1]);
                            Map<String, UUID> map = new HashMap<>();
                            map.put(strings[1], uuid);
                            uuidsCache.put(p, map);
                        }
                        if (uuid == null) {
                            p.sendMessage(Msgs.ERROR_PLAYER_NOT_FOUND);
                            return;
                        }
                        if (!u.friends.remove(uuid)) {
                            p.sendMessage(Msgs.PLAYER_NOT_ON_FRIENDS_LIST);
                            return;
                        }
                        DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.REMOVE_FRIEND, uuid.toString());
                    });
                }
                break;
            }
            case "find": {
                if (strings.length < 2) {
                    p.sendMessage(ChatColor.RED + "Usage: /f find <player>");
                    return true;
                }
                Player pInt = Bukkit.getPlayer(strings[1]);
                if (pInt != null && pInt.isOnline()) {
                    p.sendMessage(Msgs.FIND_SAME_SERVER_AS_SENDER.replace("<player>", pInt.getName()));
                    return true;
                } else {
                    DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.DEMAND_FIND_PLAYER, strings[1]);
                }
                break;
            }
            case "list": {
                if (cooldowns.contains(p.getUniqueId())) {
                    p.sendMessage(Msgs.ON_COOLDOWN.replace("<seconds>", "20"));
                    return true;
                }
                cooldowns.add(p.getUniqueId());
                Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> cooldowns.remove(p.getUniqueId()), 400L);
                User u = User.get(p);
                DystellarCore.getAsyncManager().submit(() -> {
                    List<String> friends = new ArrayList<>();
                    synchronized (u.friends) {
                        for (UUID uuid : u.friends) {
                            User us = User.get(uuid);
                            if (us == null) {
                                String name = MariaDB.loadName(uuid.toString());
                                if (name != null) friends.add(name);
                            } else {
                                friends.add(us.getName());
                            }
                        }
                    }
                    p.sendMessage(ChatColor.DARK_GREEN + "Friends list:");
                    for (String st : friends) {
                        p.sendMessage(" - " + ChatColor.DARK_AQUA + st);
                    }
                });
                break;
            }
            case "accept": {
                if (!DystellarCore.getInstance().requests.containsKey(p.getUniqueId())) {
                    p.sendMessage(Msgs.FRIEND_REQUEST_EXPIRED);
                    return true;
                }
                User u = User.get(p);
                boolean sendTip = false;
                if (u.friends.isEmpty() && u.tipsSent[Consts.FIRST_FRIEND_TIP_POS] == Consts.BYTE_FALSE) {
                    u.tipsSent[Consts.FIRST_FRIEND_TIP_POS] = Consts.BYTE_TRUE;
                    sendTip = true;
                }
                UUID uuid = DystellarCore.getInstance().requests.remove(p.getUniqueId());
                u.friends.add(uuid);
                p.sendMessage(Msgs.FRIEND_REQUEST_ACCEPTED_RECEIVER);
                if (sendTip) p.sendMessage(Consts.FIRST_FRIEND_TIP_MSG);
                DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.FRIEND_ADD_REQUEST_ACCEPT, uuid.toString());
                break;
            }
            case "reject": {
                if (!DystellarCore.getInstance().requests.containsKey(p.getUniqueId())) {
                    p.sendMessage(Msgs.FRIEND_REQUEST_EXPIRED);
                    return true;
                }
                UUID uuid = DystellarCore.getInstance().requests.remove(p.getUniqueId());
                p.sendMessage(Msgs.FRIEND_REQUEST_REJECTED_RECEIVER);
                DystellarCore.getInstance().sendPluginMessage(p, DystellarCore.FRIEND_ADD_REQUEST_REJECT, uuid.toString());
                break;
            }
            case "togglerequests": {
                User u = User.get(p);
                byte setting = u.extraOptions[Consts.EXTRA_OPTION_FRIEND_REQUESTS_ENABLED_POS];
                if (setting == Consts.BYTE_FALSE) {
                    u.extraOptions[Consts.EXTRA_OPTION_FRIEND_REQUESTS_ENABLED_POS] = Consts.BYTE_TRUE;
                    p.sendMessage(Msgs.FRIEND_REQUESTS_ENABLED);
                } else {
                    u.extraOptions[Consts.EXTRA_OPTION_FRIEND_REQUESTS_ENABLED_POS] = Consts.BYTE_FALSE;
                    p.sendMessage(Msgs.FRIEND_REQUESTS_DISABLED);
                }
                break;
            }
        }
}
