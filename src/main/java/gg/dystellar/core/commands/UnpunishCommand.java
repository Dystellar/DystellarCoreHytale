package gg.dystellar.core.commands;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.User;
import gg.dystellar.core.messaging.Subchannel;
import gg.dystellar.core.utils.Utils;

/**
 * This command lets admins remove punishments from a player.
 */
public class UnpunishCommand extends CommandBase {
	private final RequiredArg<String> nameArg = this.withRequiredArg("player", "The player to unpunish", ArgTypes.STRING);
	private final RequiredArg<Integer> idArg = this.withRequiredArg("id", "Punishment id, see with /punishments <player>", ArgTypes.INTEGER);

    public UnpunishCommand() {
		super("unpunish", "Remove a punishment from a player");
		this.requirePermission("dystellar.unpunish");
    }

	@Override
	protected void executeSync(CommandContext ctx) {
		final var name = ctx.get(nameArg);
		final var id = ctx.get(idArg);
		final var p = Universe.get().getPlayer(name, NameMatching.EXACT_IGNORE_CASE);

		if (p != null) {
			final var user = User.getUser(p).get();
			final var punishment = Utils.find(user.punishments, pun -> pun.getId() == id);
			if (punishment.isPresent()) {
				punishment.get().setExpirationDate(LocalDateTime.now(ZoneId.of("UTC")));
				ctx.sender().sendMessage(Message.raw("Punishment set to expired!").color(Color.GREEN));
			} else ctx.sender().sendMessage(Message.raw("No such punishment exists with the provided id").color(Color.RED));
		} else {
			ctx.sender().sendMessage(Message.raw("Syncing...").color(Color.YELLOW));
			Utils.sendPropagatedOutputStream(Subchannel.UNPUNISH, 38, out -> {
				out.writePrefixedUTF8(name);
				out.writeLong(id);
			});
			HytaleServer.SCHEDULED_EXECUTOR.execute(() -> {
				try {
					DystellarCore.getApi().unpunish(name, id)
						.ifOk(_ -> ctx.sender().sendMessage(Message.raw("Punishment set to expired!").color(Color.GREEN)))
						.ifErr(s -> ctx.sender().sendMessage(Message.raw("Failed: " + s).color(Color.RED)));
				} catch (Exception e) {
					e.printStackTrace();
					ctx.sender().sendMessage(Message.raw("Exception thrown: " + e.getMessage()).color(Color.RED));
				}
			});
		}
	}
}
