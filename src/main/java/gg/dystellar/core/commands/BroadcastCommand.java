package gg.dystellar.core.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.config.Messages;

/**
 * With this command you can send a message that all the people in the server will see, like a public announcement.
 * This probably should be implemented in the proxy or redirecter if hytale has one instead of here.
 */
public class BroadcastCommand extends CommandBase {

	private final RequiredArg<String> messageArg = this.withRequiredArg("message", "Provide a message", ArgTypes.STRING);

    public BroadcastCommand() {
		super("broadcast", "Broadcast a message");
		this.requirePermission("dystellar.broadcast");
		this.addAliases("bc");
    }

	@Override
	protected void executeSync(CommandContext ctx) {
		final var message = ctx.get(messageArg);

		final Messages lang;
		if (ctx.sender() instanceof Player player) {
			final Ref<EntityStore> ref = player.getReference();
			final var p = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
			final var user = p.getHolder().getComponent(UserComponent.getComponentType());
			lang = DystellarCore.getInstance().getLang(user.language);
		} else lang = DystellarCore.getInstance().getLang("en");

		final var msg = Message.join(lang.broadcastFormat).param("message", message);
		Universe.get().getPlayers().forEach(p -> p.sendMessage(msg));
	}
}
