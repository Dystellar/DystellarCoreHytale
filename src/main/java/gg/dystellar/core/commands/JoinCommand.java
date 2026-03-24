package gg.dystellar.core.commands;

import java.awt.Color;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.messaging.Handler;
import gg.dystellar.core.messaging.Subchannel;
import gg.dystellar.core.utils.Utils;

/**
 * This command lets you switch between servers, it tries to send a plugin message and the proxy handles the rest.
 * Useful because with this you can join players in an automatized way, as /server can't be used from the servers.
 */
public class JoinCommand extends AbstractPlayerCommand {

	private static final String IP_REGEX = "((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]|[0-9])";
	private static final String PORT_REGEX = "(6553[0-5]|655[0-2][0-9]|65[0-4][0-9]{2}|6[0-4][0-9]{3}|[1-5][0-9]{4}|[1-9][0-9]{0,3}|0)";

	private final RequiredArg<String> serverArg = this.withRequiredArg("server", "The server to connect to", ArgTypes.STRING);

    public JoinCommand() {
		super("join", "Join another server");
		this.addAliases("j");
		this.requirePermission("dystellar.referral");
    }

	@Override
	protected void execute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, World w) {
		final var server = ctx.get(serverArg);

		if (server.matches(IP_REGEX + ':' + PORT_REGEX)) {
			final var parts = server.split(":");

			p.sendMessage(Message.raw("Connecting...").color(new Color(0x00FF00)));
			p.referToServer(parts[0], Integer.parseInt(parts[1]));
		} else if (server.matches(IP_REGEX)) {
			p.sendMessage(Message.raw("Connecting...").color(new Color(0x00FF00)));
			p.referToServer(server, 5520);
		} else if (server.matches(PORT_REGEX)) {
			final var config = DystellarCore.getInstance().config.get();

			p.sendMessage(Message.raw("Connecting...").color(new Color(0x00FF00)));
			p.referToServer(config.public_ip, Integer.parseInt(server));
		} else {
			p.sendMessage(Message.raw("Connecting...").color(new Color(0x00FF00)));
			int id = Handler.createMessageSession((s, in) -> {
				final var host = in.readPrefixedUTF8();
				final var port = in.readInt();

				p.referToServer(host, port);
			}, () -> p.sendMessage(Message.raw("Server not found").color(new Color(0xFF0000))));

			Utils.sendTargetedOutputStream(server, Subchannel.REQUEST_ADDRESS, 10, out -> {
				out.writeInt(id);
			});
		}
	}
}
