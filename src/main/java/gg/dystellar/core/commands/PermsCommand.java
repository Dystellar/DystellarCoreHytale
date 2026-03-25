package gg.dystellar.core.commands;

import java.awt.Color;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.messaging.Subchannel;
import gg.dystellar.core.perms.Group;
import gg.dystellar.core.utils.Utils;

public final class PermsCommand extends AbstractCommandCollection {

	public PermsCommand() {
		super("perms", "Permissions command");
		this.requirePermission("dystellar.admin");

		this.addAliases("p");
		this.addSubCommand(new SetDefaultCommand());
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
}
