package gg.dystellar.core.commands;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.common.UserComponent.UserMapping;
import gg.dystellar.core.messaging.Handler;
import gg.dystellar.core.messaging.Subchannel;
import gg.dystellar.core.utils.Pair;
import gg.dystellar.core.utils.Triple;
import gg.dystellar.core.utils.Utils;

// TODO: Test this

/**
 * Friends command, lets any player manage their list of friends.
 * Should definitely be implemented in the proxy or redirecter if hytale has one, because otherwise it needs a bunch of protocols and packets and there is no need.
 */
public class FriendCommand extends AbstractCommandCollection {

	private static final Map<UUID, Instant> cooldowns = new ConcurrentHashMap<>(); // Player, cooldown expiration
	private static final Map<UUID, List<Triple<PlayerRef, Instant, ScheduledFuture<?>>>> pending = new ConcurrentHashMap<>();

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

		// Handle cleanup on disconnect
		final var plugin = DystellarCore.getInstance();
		plugin.getEventRegistry().register(PlayerDisconnectEvent.class, e -> {
			final var rem = pending.remove(e.getPlayerRef().getUuid());
			if (rem != null) {
				for (final var t : rem)
					t.third.cancel(false);
			}
			for (final var value : pending.values()) {
				final var entry = Utils.removeFirst(value, triple -> triple.first.getUuid().equals(e.getPlayerRef().getUuid()));
				entry.ifPresent(en -> en.third.cancel(true));
			}
		});
    }

	private static final class AddCommand extends AbstractPlayerCommand {
		private final RequiredArg<PlayerRef> targetArg = this.withRequiredArg("target", "The player that receives the request", ArgTypes.PLAYER_REF);

		AddCommand() {
			super("add", "Friend add command");
			this.requirePermission("dystellar.friend.add");
			this.addAliases("a");
		}

		@Override
		protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
			final var user = p.getHolder().getComponent(UserComponent.getComponentType());
			final var target = ctx.get(targetArg);
			final var lang = DystellarCore.getInstance().getLang(user.language);
			final var cooldown = cooldowns.get(p.getUuid());

			if (cooldown != null) {
				p.sendMessage(lang.mCooldown.buildMessage().param("seconds", cooldown.getEpochSecond() - Instant.now().getEpochSecond()));
				return;
			} else if (p.getUsername().equalsIgnoreCase(target.getUsername())) {
				p.sendMessage(lang.errorFriendAddYourself.buildMessage());
				return;
			}

			Instant cooldownExpiration = Instant.now().plusSeconds(4L);
			cooldowns.put(p.getUuid(), cooldownExpiration);

			if (user.friends.contains(target.getUuid())) {
				p.sendMessage(lang.playerAlreadyOnFriendsList.buildMessage());
				return;
			}
			final var targetUser = target.getHolder().getComponent(UserComponent.getComponentType());
			if (!targetUser.friendRequests) {
				p.sendMessage(lang.targetRequestsDisabled.buildMessage());
				return;
			}
			final var targetLang = DystellarCore.getInstance().getLang(targetUser.language);

			HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> cooldowns.remove(p.getUuid()), 4L, TimeUnit.SECONDS);
			var listPendings = pending.get(p.getUuid());

			if (listPendings != null) {
				final var entry = Utils.find(listPendings, pair -> pair.first.getUsername().equalsIgnoreCase(target.getUsername()));

				if (entry.isPresent()) {
					p.sendMessage(lang.mCooldown.buildMessage().param("seconds", entry.get().second.getEpochSecond() - Instant.now().getEpochSecond()));
					return;
				}
			} else {
				listPendings = Collections.synchronizedList(new ArrayList<>());
				pending.put(p.getUuid(), listPendings);
			}

			final var future = HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
				listPendings.removeIf(pair -> pair.first.getUuid().equals(target.getUuid()));
				if (listPendings.isEmpty())
					pending.remove(p.getUuid());
			}, 30L, TimeUnit.SECONDS);
			listPendings.add(new Triple<>(target, Instant.now().plusSeconds(30L), future));

			p.sendMessage(lang.friendRequestSent.buildMessage());
			target.sendMessage(targetLang.friendRequestReceived.buildMessage().param("player", p.getUsername()));
			target.sendMessage(targetLang.commandHint.buildMessage().param("command", "/f accept " + p.getUsername()));
		}
	}

	// TODO: Test all variables here
	private static final class RemoveCommand extends AbstractPlayerCommand {
		private final RequiredArg<String> targetArg = this.withRequiredArg("target", "The player to remove", ArgTypes.STRING);

		RemoveCommand() {
			super("remove", "Friend remove command");
			this.requirePermission("dystellar.friend.remove");
			this.addAliases("delete", "remove", "del", "d", "rm");
		}

		@Override
		protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
			final var user = p.getHolder().getComponent(UserComponent.getComponentType());
			final var lang = DystellarCore.getInstance().getLang(user.language);
			final var targetName = ctx.get(targetArg);
			final var mapping = Utils.removeFirst(user.friends, map -> map.name().equalsIgnoreCase(targetName));
			if (!mapping.isPresent()) {
				p.sendMessage(lang.playerNotOnFriendsList.buildMessage());
				return;
			}

			p.sendMessage(lang.friendRemovedSender.buildMessage().param("player", mapping.get().name()));
			final var target = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);
			if (target != null && target.isValid()) {
				final var targetUser = target.getHolder().getComponent(UserComponent.getComponentType());
				final var targetLang = DystellarCore.getInstance().getLang(targetUser.language);
				target.sendMessage(targetLang.friendRemovedReceiver.buildMessage().param("player", p.getUsername()));
			} else {
				Utils.sendPropagatedOutputStream(Subchannel.FRIEND_REMOVE, 70, out -> {
					out.writePrefixedUTF8(p.getUuid().toString());
					out.writePrefixedUTF8(mapping.get().uuid().toString());
				});
				HytaleServer.SCHEDULED_EXECUTOR.execute(() -> DystellarCore.getApi().playerFriendRemove(p.getUuid(), mapping.get().uuid()));
			}
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
		protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
			final var user = p.getHolder().getComponent(UserComponent.getComponentType());
			final var lang = DystellarCore.getInstance().getLang(user.language);
			final var targetName = ctx.get(targetArg);
			final var target = Universe.get().getPlayerByUsername(targetName, NameMatching.EXACT_IGNORE_CASE);

			if (target != null && target.isValid())
				p.sendMessage(lang.findSameServerAsSender.buildMessage());
			else {
				int id = Handler.createMessageSession((source, payload) -> {
					final var realName = payload.readPrefixedUTF8();
					p.sendMessage(lang.findFound.buildMessage().param("player", realName).param("server", source));
				}, () -> {
					p.sendMessage(lang.findNotFound.buildMessage().param("player", targetName));
				});
				Utils.sendPropagatedOutputStream(Subchannel.DEMAND_FIND_PLAYER, 26, out -> {
					out.writePrefixedUTF8(targetName);
					out.writeInt(id);
				});
			}
		}
	}

	private static final class ListCommand extends AbstractPlayerCommand {
		ListCommand() {
			super("list", "List friends");
			this.requirePermission("dystellar.friend.list");
			this.addAliases("l", "ls");
		}

		@Override
		protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
			final var user = p.getHolder().getComponent(UserComponent.getComponentType());
			final var lang = DystellarCore.getInstance().getLang(user.language);

			p.sendMessage(lang.listFriendsTitle.buildMessage());
			for (UserMapping map : user.friends)
				p.sendMessage(lang.listFriendsEntry.buildMessage().param("player", map.name()));
		}
	}

	private static final class AcceptCommand extends AbstractPlayerCommand {
		private final RequiredArg<PlayerRef> targetArg = this.withRequiredArg("target", "The player to accept", ArgTypes.PLAYER_REF);

		AcceptCommand() {
			super("accept", "Friend accept command");
			this.requirePermission("dystellar.friend.accept");
		}

		@Override
		protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
			final var user = p.getHolder().getComponent(UserComponent.getComponentType());
			final var lang = DystellarCore.getInstance().getLang(user.language);
			final var target = ctx.get(targetArg);

			final var pend = pending.get(target.getUuid());
			final Optional<Triple<PlayerRef, Instant, ScheduledFuture<?>>> entry;

			// Assign and check presence in one step if pend != null
			if (pend == null || !(entry = Utils.removeFirst(pend, t -> t.first.getUuid().equals(p.getUuid()))).isPresent()) {
				p.sendMessage(lang.friendRequestExpired.buildMessage());
				return;
			}
			entry.get().third.cancel(true);

			final var targetUser = target.getHolder().getComponent(UserComponent.getComponentType());
			final var targetLang = DystellarCore.getInstance().getLang(targetUser.language);

			targetUser.friends.add(new UserMapping(p.getUuid(), p.getUsername()));
			user.friends.add(new UserMapping(target.getUuid(), target.getUsername()));
			target.sendMessage(targetLang.friendRequestAcceptedSender.buildMessage().param("player", p.getUsername()));
			p.sendMessage(lang.friendRequestAcceptedReceiver.buildMessage());

			if (pend.isEmpty())
				pending.remove(target.getUuid());
		}
	}

	private static final class RejectCommand extends AbstractPlayerCommand {
		@Override
		protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
		}
	}

	private static final class ToggleCommand extends AbstractPlayerCommand {
		@Override
		protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
		}
	}

	switch (strings[0]) {
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
