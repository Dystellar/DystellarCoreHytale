package gg.dystellar.core.commands;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.Argument;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.provider.HytalePermissionsProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;

/**
 * Custom ban command, generally the same as the builtin ban command but this also notifies the backend and updates the player's profile.
 */
public class BanCommand extends AbstractAsyncCommand {

	private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "The player to receive the punishment", ArgTypes.PLAYER_REF);
	private final RequiredArg<String> reasonArg = this.withRequiredArg("reason", "Why punishing this player", ArgTypes.STRING);
	private final OptionalArg<String> timeArg = this.withOptionalArg("time", "Duration of the punishment e.g. 30m, 30d, 2y", ArgTypes.STRING);
	private final FlagArg ipbanArg = this.withFlagArg("ipban", "If the punishment also applies to the player's IP address");

    public BanCommand(String name, String description) {
		super(name, description);
		this.requirePermission("dystellar.punish");
    }

	@Override
	protected CompletableFuture<Void> executeAsync(CommandContext ctx) {
		final var sender = ctx.sender();

		final var player = ctx.get(playerArg);
		final var reason = ctx.get(reasonArg);
		final var ipban = ctx.get(ipbanArg);
		final var time = ctx.get(timeArg);
		LocalDateTime expirationDate = null;

		if (time != null) {
			if (!time.matches("^[0-9]+[ydhm]$")) {
				sender.sendMessage(Message.raw("Time format incorrect, regex is '^[0-9]+[ydhm]$'").color(new Color(0xFF0000)));
				return CompletableFuture.completedFuture(null);
			}

			expirationDate = LocalDateTime.now(ZoneId.of("UTC"));
			int integer = Integer.parseInt(time.substring(0, time.length() - 1));
			switch (time.charAt(time.length() - 1)) {
				case 'y': expirationDate = expirationDate.plusYears(integer); break;
				case 'd': expirationDate = expirationDate.plusDays(integer); break;
				case 'h': expirationDate = expirationDate.plusHours(integer); break;
				case 'm': expirationDate = expirationDate.plusMinutes(integer); break;
			}
		}

		try {
			final var punishment = DystellarCore.getApi().punish(
				player.getUuid(), "YOU HAVE BEEN BANNED", "ban",
				LocalDateTime.now(ZoneId.of("UTC")), Optional.ofNullable(expirationDate),
				reason, ipban, false, false, false, false
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

