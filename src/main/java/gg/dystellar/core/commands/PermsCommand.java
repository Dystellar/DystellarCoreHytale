package gg.dystellar.core.commands;

import java.awt.Color;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.messaging.Subchannel;
import gg.dystellar.core.perms.Group;
import gg.dystellar.core.utils.Utils;

public final class PermsCommand extends AbstractCommandCollection {

	public PermsCommand() {
		super("perms", "Permissions command");
		this.requirePermission("dystellar.admin");

		this.addAliases("p");
		this.addSubCommand(new SetDefaultCommand());
		this.addSubCommand(new SetGroupCommand());
		this.addSubCommand(new ListGroupsCommand());
	}

	private static final class SetDefaultCommand extends CommandBase {
		private final RequiredArg<String> groupArg = this.withRequiredArg("group", "The group to set the default as", ArgTypes.STRING);

		SetDefaultCommand() {
			super("setdafult", "Set default group network-wide");
			this.requirePermission("dystellar.admin");
		}

		@Override
		protected void executeSync(CommandContext ctx) {
			final var groupName = ctx.get(groupArg);
			final var group = Group.getGroup(groupName);
			if (group.isEmpty()) {
				final var color = new Color(0xFF0000);
				ctx.sender().sendMessage(Message.raw("WARNING! This group doesn't seem to exist internally.").color(color));
				ctx.sender().sendMessage(Message.raw("The plugin will try to update the group network-wide anyways, but a plugin reload will be required for changes to take effect").color(color));
			} else {
				Group.setDefaultGroup(group.get());
			}

			HytaleServer.SCHEDULED_EXECUTOR.execute(() -> {
				try {
					DystellarCore.getApi().setDefaultGroup(groupName)
						.ifOk(_ -> {
							ctx.sender().sendMessage(Message.raw("Group updated!").color(Color.GREEN));
							Utils.sendPropagatedOutputStream(Subchannel.DEFAULT_GROUP_UPDATE, 28, out -> out.writePrefixedUTF8(groupName));
						}).ifErr(s -> ctx.sender().sendMessage(Message.raw("Failed to set default group: " + s).color(Color.RED)));
				} catch (Exception e) {
					e.printStackTrace();
					ctx.sender().sendMessage(Message.raw("Exception thrown: " + e.getMessage()).color(Color.RED));
				}
			});
		}
	}

	private static final class SetGroupCommand extends CommandBase {
		private final RequiredArg<String> userArg = this.withRequiredArg("user", "The user to set the group to", ArgTypes.STRING);
		private final RequiredArg<String> groupArg = this.withRequiredArg("group", "The group to set", ArgTypes.STRING);

		SetGroupCommand() {
			super("setgroup", "Set a group to a user");
			this.requirePermission("dystellar.admin");
		}

		@Override
		protected void executeSync(CommandContext ctx) {
			final var username = ctx.get(userArg);
			final var groupName = ctx.get(groupArg);

			final var group = Group.getGroup(groupName);
			if (group.isEmpty()) {
				ctx.sender().sendMessage(Message.raw("This group does not exist").color(Color.RED));
				return;
			}

			final var target = Universe.get().getPlayerByUsername(username, NameMatching.EXACT_IGNORE_CASE);

			if (target != null) {
				final var user = target.getHolder().getComponent(UserComponent.getComponentType());
				user.group = group;
				ctx.sender().sendMessage(Message.raw("Group updated!").color(Color.GREEN));
			} else {
				HytaleServer.SCHEDULED_EXECUTOR.execute(() -> {
					Utils.sendPropagatedOutputStream(Subchannel.USER_GROUP_UPDATE, 40, out -> {
						out.writePrefixedUTF8(username);
						out.writePrefixedUTF8(groupName);
					});
					try {
						DystellarCore.getApi().setGroupToUserByName(groupName, username)
							.ifOk(_ -> ctx.sender().sendMessage(Message.raw("Group updated!").color(Color.GREEN)))
							.ifErr(s -> ctx.sender().sendMessage(Message.raw("Failed to set user group: " + s).color(Color.RED)));
					} catch (Exception e) {
						e.printStackTrace();
						ctx.sender().sendMessage(Message.raw("Exception thrown: " + e.getMessage()).color(Color.RED));
					}
				});
			}
		}
	}

	private static final class ListGroupsCommand extends CommandBase {
		ListGroupsCommand() {
			super("listgroups", "List active groups");
			this.requirePermission("dystellar.admin");
		}

		@Override
		protected void executeSync(CommandContext ctx) {
			ctx.sender().sendMessage(Message.raw("Active groups:").color(Color.MAGENTA));
			for (final var group : Group.groups.keySet())
				ctx.sender().sendMessage(Message.join(Message.raw(" - "), Message.raw(group).color(Color.GREEN)));
		}
	}
}
