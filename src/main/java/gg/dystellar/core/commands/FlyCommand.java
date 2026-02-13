package gg.dystellar.core.commands;

import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;

/**
 * This command is for premium people, so they can fly in the lobbys, so they can explore the buildings with ease.
 * It shouldn't have any effect on actual games.
 */
public class FlyCommand extends CommandBase {

	private final OptionalArg<PlayerRef> playerArg = this.withOptionalArg("target", "Player to apply this command to", ArgTypes.PLAYER_REF);

    public FlyCommand() {
		super("fly", "Fly command for the privileged");
		this.requirePermission("dystellar.fly");
    }

	@Override
	protected void executeSync(CommandContext ctx) {
		final Player target;
		final var arg = ctx.get(playerArg);

		if (arg != null && arg.isValid()) {
			if (!ctx.sender().hasPermission("dystellar.fly.other")) {
				ctx.sender().sendMessage(DystellarCore.getInstance().lang_en.get().noPermission);
				return;
			}

			final var ref = arg.getReference();
			if (!ref.isValid()) {
				ctx.sender().sendMessage(DystellarCore.getInstance().lang_en.get().errorPlayerNotInAWorld);
				return;
			}

			target = ref.getStore().getComponent(ref, Player.getComponentType());
		} else {
			if (!(ctx.sender() instanceof Player)) {
				ctx.sender().sendMessage(DystellarCore.getInstance().lang_en.get().errorNotAPlayer);
				return;
			}
			target = (Player)ctx.sender();
			final var ref = target.getReference();
			final var pref = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
			final var user = pref.getHolder().getComponent(UserComponent.getComponentType());

			if (user.isInGame) {
				final var lang = DystellarCore.getInstance().getLang(user.language);
				pref.sendMessage(lang.commandDenyIngame);

				return;
			}
		}


		final var ref = target.getReference();
		final var move = ref.getStore().getComponent(ref, MovementStatesComponent.getComponentType());
	}

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player player = (Player) commandSender;
        if (strings.length < 1) {
            if (!player.hasPermission("dystellar.plus")) {
                player.sendMessage(Msgs.FLY_NEED_PLUS_RANK);
                return true;
            }
            if ((DystellarCore.PRACTICE_HOOK && PUser.get(player).isInGame()) || (DystellarCore.SKYWARS_HOOK && SkywarsAPI.getPlayerUser(player).isInGame())) {
                player.sendMessage(Msgs.COMMAND_DENY_INGAME);
                return true;
            }
            player.setAllowFlight(!player.getAllowFlight());
            if (player.getAllowFlight()) {
                player.sendMessage(Msgs.FLY_MODE_ENABLED);
            } else {
                player.setFlying(false);
                player.sendMessage(Msgs.FLY_MODE_DISABLED);
            }
        } else {
            if (!player.getName().equalsIgnoreCase(strings[0]) && !player.hasPermission("dystellar.mod")) {
                player.sendMessage(Msgs.NO_PERMISSION);
                return true;
            }
            Player p = Bukkit.getPlayer(strings[0]);
            if (p == null || !p.isOnline()) {
                player.sendMessage(Msgs.ERROR_PLAYER_NOT_ONLINE);
                return true;
            }
            p.setAllowFlight(!p.getAllowFlight());
            if (p.getAllowFlight()) {
                player.sendMessage(Msgs.ADMIN_FLY_MODE_ENABLED_OTHER.replace("<player>", player.getName()));
                p.sendMessage(Msgs.FLY_MODE_ENABLED_BY_ADMIN);
            } else {
                p.setFlying(false);
                player.sendMessage(Msgs.ADMIN_FLY_MODE_DISABLED_OTHER.replace("<player>", player.getName()));
                p.sendMessage(Msgs.FLY_MODE_DISABLED_BY_ADMIN);
            }
        }
        return true;
    }
}
