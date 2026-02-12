package gg.dystellar.core.commands;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;

/**
 * Blacklist command, more severe than ban, and only used for special cases,
 * like for problematic people that you really don't want them connecting to the server ever again.
 */
public class BlacklistCommand extends AbstractAsyncCommand {

	private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "The player to receive the punishment", ArgTypes.PLAYER_REF);
	private final RequiredArg<String> reasonArg = this.withRequiredArg("reason", "Why punishing this player", ArgTypes.STRING);

    public BlacklistCommand() {
		super("blacklist", "Permanently invalidate a player from joining the server in");
		this.requirePermission("dystellar.admin");
    }

	@Override
	protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
		final var sender = ctx.sender();

		final var player = ctx.get(playerArg);
		final var reason = ctx.get(reasonArg);

		try {
			final var punishment = DystellarCore.getApi().punish(
				player.getUuid(), "YOU HAVE BEEN BLACKLISTED", "blacklist",
				LocalDateTime.now(ZoneId.of("UTC")), Optional.empty(),
				reason, true, false, false, false, false
			);

			punishment.ifPresentOrElse(p -> {
				UserComponent user = player.getHolder().getComponent(UserComponent.getComponentType());
				if (user != null)
					user.punish(p);
			}, () -> sender.sendMessage(
				Message.raw("Failed to create punishment, this player probably doesn't exist. Check logs for further information").color(new Color(0xFF0000))
			));
		} catch (Exception e) {
			e.printStackTrace();
			sender.sendMessage(Message.raw("Failed to create punishment: " + e.getMessage()).color(new Color(0xFF0000)));
		}

		return CompletableFuture.completedFuture(null);
	}
}
