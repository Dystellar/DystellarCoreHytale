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
import gg.dystellar.core.common.User;

/**
 * Moderator command to mute a player
 */
public class MuteCommand extends AbstractAsyncCommand {

	private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "The player to receive the punishment", ArgTypes.PLAYER_REF);
	private final RequiredArg<String> reasonArg = this.withRequiredArg("reason", "Why punishing this player", ArgTypes.STRING);
	private final RequiredArg<String> timeArg = this.withRequiredArg("time", "Duration of the punishment e.g. 30m, 30d, 2y", ArgTypes.STRING);

    public MuteCommand() {
		super("mute", "Dystellar's custom mute command");
		this.requirePermission("dystellar.punish");
    }

	@Override
	protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
		final var sender = ctx.sender();

		final var player = ctx.get(playerArg);
		final var reason = ctx.get(reasonArg);
		final var time = ctx.get(timeArg);

		if (!time.matches("^[0-9]+[ydhm]$")) {
			sender.sendMessage(Message.raw("Time format incorrect, regex is '^[0-9]+[ydhm]$'").color(new Color(0xFF0000)));
			return CompletableFuture.completedFuture(null);
		}

		LocalDateTime expirationDate = LocalDateTime.now(ZoneId.of("UTC"));
		int integer = Integer.parseInt(time.substring(0, time.length() - 1));
		switch (time.charAt(time.length() - 1)) {
			case 'y': expirationDate = expirationDate.plusYears(integer); break;
			case 'd': expirationDate = expirationDate.plusDays(integer); break;
			case 'h': expirationDate = expirationDate.plusHours(integer); break;
			case 'm': expirationDate = expirationDate.plusMinutes(integer); break;
		}

		try {
			final var punishment = DystellarCore.getApi().punish(
				player.getUuid(), "YOU HAVE BEEN MUTED", "mute",
				LocalDateTime.now(ZoneId.of("UTC")), Optional.ofNullable(expirationDate),
				reason, false, false, false, false, false
			);

			punishment.ifPresentOrElse(p -> {
				User user = User.getUser(player).get();
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

