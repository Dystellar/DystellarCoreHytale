package gg.dystellar.core.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.User;

/**
 * This command lets you enable/disable the global chat.
 */
public class ToggleChatCommand extends AbstractPlayerCommand {

    public ToggleChatCommand() {
		super("toggleglobalchat", "Toggle chat command");
		this.addAliases("tgc", "togglechat");
		this.requirePermission("dystellar.togglechat");
    }

	@Override
	protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> r, PlayerRef p, World w) {
		final var user = User.getUser(p).get();
		final var lang = DystellarCore.getInstance().getLang(user.language);

		user.globalChatEnabled = !user.globalChatEnabled;
		if (user.globalChatEnabled)
			p.sendMessage(lang.globalChatEnabled.buildMessage());
		else
			p.sendMessage(lang.globalChatDisabled.buildMessage());
	}
}
