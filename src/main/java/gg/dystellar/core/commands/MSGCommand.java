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
import gg.dystellar.core.utils.Utils;

/**
 * Command to send a private message to another person. Should definitely be implemented in the proxy, not here.
 */
public class MSGCommand extends AbstractPlayerCommand {
	private final RequiredArg<PlayerRef> targetArg = this.withRequiredArg("target", "The player to send the message to", ArgTypes.PLAYER_REF);
	private final RequiredArg<String> messageArg = this.withRequiredArg("message", "The message to send", ArgTypes.STRING);

    public MSGCommand() {
		super("message", "Send a private message");
		this.requirePermission("dystellar.message");
		this.addAliases("msg", "tell");
    }

	@Override
	protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
		final var target = ctx.get(targetArg);
		final var user = p.getHolder().getComponent(UserComponent.getComponentType());
		final var lang = DystellarCore.getInstance().getLang(user.language);

		if (user.privateMessagesMode == UserComponent.PMS_DISABLED ||
			(user.privateMessagesMode == UserComponent.PMS_ENABLED_FRIENDS_ONLY &&
				Utils.find(user.friends, map -> map.name().equalsIgnoreCase(target.getUsername())).isEmpty())
		) {
			p.sendMessage(lang.cantSendPmsDisabled.buildMessage());
			return;
		}

		final var targetUser = target.getHolder().getComponent(UserComponent.getComponentType());
		if (targetUser.privateMessagesMode == UserComponent.PMS_DISABLED ||
			(targetUser.privateMessagesMode == UserComponent.PMS_ENABLED_FRIENDS_ONLY &&
				Utils.find(targetUser.friends, map -> map.uuid().equals(p.getUuid())).isEmpty())
		) {
			p.sendMessage(lang.errorPlayerHasPmsDisabled.buildMessage());
			return;
		}

		if (Utils.find(targetUser.ignoreList, map -> map.uuid().equals(p.getUuid())).isPresent()) {
			p.sendMessage(lang.errorYouAreBlocked.buildMessage());
			return;
		}

		if (targetUser.isInRanked && targetUser.dnd) {
			p.sendMessage(lang.playerInDndMode.buildMessage().param("player", target.getUsername()));
			return;
		}

		final var targetLang = DystellarCore.getInstance().getLang(targetUser.language);
		final var message = ctx.get(messageArg);
		target.sendMessage(targetLang.msgReceiveFormat.buildMessage().param("sender", p.getUsername()).param("message", message));
		p.sendMessage(lang.msgSendFormat.buildMessage().param("receiver", target.getUsername()).param("message", message));
	}
}
