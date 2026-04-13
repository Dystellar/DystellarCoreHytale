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
import gg.dystellar.core.common.User;
import gg.dystellar.core.utils.Utils;

/**
 * Command to reply to the latest player that sent you a message.
 * Should be implemented in the proxy the same as msg command, not here.
 */
public class ReplyCommand extends AbstractPlayerCommand {
	private final RequiredArg<String> messageArg = this.withRequiredArg("message", "Message to send", ArgTypes.STRING);

    public ReplyCommand() {
		super("reply", "Reply to a previously messaged user");
		this.addAliases("r");
		this.requirePermission("dystellar.message");
    }

	@Override
	protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
		final var user = User.getUser(p).get();
		final var target = user.lastMessagedPlayer;
		final var lang = DystellarCore.getInstance().getLang(user.language);

		if (target == null || !target.isValid()) {
			p.sendMessage(lang.errorNoReplyCache.buildMessage());
			return;
		}

		if (user.privateMessagesMode == User.PMS_DISABLED ||
			(user.privateMessagesMode == User.PMS_ENABLED_FRIENDS_ONLY &&
				Utils.find(user.friends, map -> map.name().equalsIgnoreCase(target.getUsername())).isEmpty())
		) {
			p.sendMessage(lang.cantSendPmsDisabled.buildMessage());
			return;
		}

		final var targetUser = User.getUser(target).get();
		if (targetUser.privateMessagesMode == User.PMS_DISABLED ||
			(targetUser.privateMessagesMode == User.PMS_ENABLED_FRIENDS_ONLY &&
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
