package gg.dystellar.core.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;

/**
 * This lets you toggle private messages mode.
 */
public class TogglePrivateMessagesCommand extends AbstractPlayerCommand {

    public TogglePrivateMessagesCommand() {
		super("toggleprivatemessages", "Toggle private messages mode");
		this.addAliases("togglemessages", "tpms", "tpm", "pms");
		this.requirePermission("dystellar.togglemessages");
    }

	@Override
	protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> r, PlayerRef p, World w) {
	    final var user = p.getHolder().getComponent(UserComponent.getComponentType());
		final var lang = DystellarCore.getInstance().getLang(user.language);

		user.togglePms();
		switch (user.privateMessagesMode) {
			case UserComponent.PMS_ENABLED: p.sendMessage(lang.pmsEnabled.buildMessage()); break;
			case UserComponent.PMS_ENABLED_FRIENDS_ONLY: p.sendMessage(lang.pmsEnabledFriendsOnly.buildMessage()); break;
			case UserComponent.PMS_DISABLED: p.sendMessage(lang.pmsDisabled.buildMessage()); break;
		}
	}
}
