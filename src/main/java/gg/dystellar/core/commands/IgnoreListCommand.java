package gg.dystellar.core.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.utils.Utils;

/**
 * This command lets you manage your ignored players list.
 */
public class IgnoreListCommand extends AbstractCommandCollection {

    public IgnoreListCommand() {
		super("ignorelist", "Manage your ignores list");
		this.requirePermission("dystellar.ignore");
		this.addAliases("blockslist");

		this.addSubCommand(new RemoveCommand());
		this.addSubCommand(new ListCommand());
    }

	private final static class RemoveCommand extends AbstractPlayerCommand {
		private final RequiredArg<String> playerArg = this.withRequiredArg("player", "The player to remove", ArgTypes.STRING);

		public RemoveCommand() {
			super("remove", "Remove blocked player");
			this.addAliases("delete", "rm", "del", "d");
		}

		@Override
		protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
			final var name = ctx.get(playerArg);
			final var user = p.getHolder().getComponent(UserComponent.getComponentType());
			final var lang = DystellarCore.getInstance().getLang(user.language);

			if (Utils.removeFirst(user.ignoreList, m -> m.name().equalsIgnoreCase(name)).isPresent())
				p.sendMessage(lang.blacklistPlayerRemoved.buildMessage().param("player", name));
			else
				p.sendMessage(lang.errorPlayerNotOnBlocklist.buildMessage());
		}
	}

	private final static class ListCommand extends AbstractPlayerCommand {
		public ListCommand() {
			super("list", "List blocked players");
			this.addAliases("l", "ls");
		}

		@Override
		protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
			final var user = p.getHolder().getComponent(UserComponent.getComponentType());
			final var lang = DystellarCore.getInstance().getLang(user.language);

			p.sendMessage(lang.blockedPlayersListTitle.buildMessage());
			for (final var map : user.ignoreList)
				p.sendMessage(lang.blockedPlayersListEntry.buildMessage().param("player", map.name()));
		}
	}
}
