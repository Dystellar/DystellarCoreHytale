package gg.dystellar.core.commands;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

// TODO: Test this

/**
 * Friends command, lets any player manage their list of friends.
 * Should definitely be implemented in the proxy or redirecter if hytale has one, because otherwise it needs a bunch of protocols and packets and there is no need.
 */
public class FriendCommand extends AbstractCommandCollection {

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
		protected void execute(CommandContext arg0, Store<EntityStore> arg1, Ref<EntityStore> arg2, PlayerRef arg3, World arg4) {
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
}
