package gg.dystellar.core.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * This command lets you manage your suffixes. Suffixes will be seen in the chat.
 * These are intended to be sold as cosmetics, for supporters.
 */
public class SuffixCommand extends AbstractPlayerCommand {

    public SuffixCommand() {
		super("suffix", "Ui to see suffixes");
		this.addAliases("suffixs", "suffixes");
		this.requirePermission("dystellar.suffix");
    }

	@Override
	protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> r, PlayerRef p, World w) {
		p.sendMessage(Message.raw("Not implemented yet"));
	}
}
