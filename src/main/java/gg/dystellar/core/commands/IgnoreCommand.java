package gg.dystellar.core.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.common.UserComponent.UserMapping;
import gg.dystellar.core.utils.Utils;

/**
 * This lets you block a player from messaging you, useful for obnoxious and toxic people.
 */
public class IgnoreCommand extends AbstractPlayerCommand {

	private final RequiredArg<PlayerRef> targetArg = this.withRequiredArg("target", "The player you want to block", ArgTypes.PLAYER_REF);

    public IgnoreCommand() {
		super("ignore", "Ignore a player");
		this.addAliases("noreply", "dismiss", "snub", "nopm");
    }

	@Override
	protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
		final var target = ctx.get(targetArg);
		final var user = p.getHolder().getComponent(UserComponent.getComponentType());
		final var lang = DystellarCore.getInstance().getLang(user.language);

		if (Utils.find(user.ignoreList, m -> m.uuid().equals(target.getUuid())).isPresent()) {
			p.sendMessage(lang.errorPlayerAlreadyBlocked.buildMessage());
			return;
		}

		user.ignoreList.add(new UserMapping(target.getUuid(), target.getUsername()));
		p.sendMessage(lang.playerBlocked.buildMessage().param("player", target.getUsername()));
	}
}
