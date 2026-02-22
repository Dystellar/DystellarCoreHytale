package gg.dystellar.core.commands;

import java.awt.Color;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.packets.player.SetMovementStates;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;

// TODO: Test this

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
		final var arg = ctx.get(playerArg);

		if (arg != null && arg.isValid()) {
			final var langEn = DystellarCore.getInstance().lang_en.get();
			if (!ctx.sender().hasPermission("dystellar.fly.other")) {
				ctx.sender().sendMessage(langEn.noPermission);
				return;
			}

			final var ref = arg.getReference();
			if (!ref.isValid()) {
				ctx.sender().sendMessage(langEn.errorPlayerNotInAWorld);
				return;
			}

			final var res = toggleFly(ref);
			if (res == null) {
				ctx.sendMessage(Message.raw("Something went wrong...").color(new Color(0xFF0000)));
				return;
			}
			final var user = arg.getHolder().getComponent(UserComponent.getComponentType());
			final var lang = DystellarCore.getInstance().getLang(user.language);
			arg.sendMessage(res ? lang.flyModeEnabledByAdmin : lang.flyModeDisabledByAdmin);
			ctx.sendMessage(res ? langEn.adminFlyModEnabledOther : langEn.adminFlyModDisabledOther);
		} else {
			if (!(ctx.sender() instanceof Player)) {
				ctx.sender().sendMessage(DystellarCore.getInstance().lang_en.get().errorNotAPlayer);
				return;
			}
			final var target = (Player)ctx.sender();
			final var ref = target.getReference();
			
			if (toggleFly(ref) == null)
				ctx.sendMessage(Message.raw("Something went wrong...").color(new Color(0xFF0000)));
		}
	}

	/**
	 * Toggle fly little helper
	 */
	private static Boolean toggleFly(Ref<EntityStore> ref) {
		final var playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
		final var move = ref.getStore().getComponent(ref, MovementStatesComponent.getComponentType());
		final var mngr = ref.getStore().getComponent(ref, MovementManager.getComponentType());

		if (move != null && mngr != null && playerRef != null) {
			final var user = playerRef.getHolder().getComponent(UserComponent.getComponentType());
			if (user.isInGame) {
				final var lang = DystellarCore.getInstance().getLang(user.language);
				playerRef.sendMessage(lang.commandDenyIngame);

				return null;
			}
			final var lang = DystellarCore.getInstance().getLang(user.language);
			final var targetFly = !mngr.getSettings().canFly;
			
			mngr.getSettings().canFly = targetFly;
			if (mngr.getDefaultSettings() != null)
				mngr.getDefaultSettings().canFly = targetFly;

			if (!targetFly) {
				move.getMovementStates().flying = false;
				playerRef.getPacketHandler().writeNoCache(new SetMovementStates(new SavedMovementStates(false)));
			}

			mngr.update(playerRef.getPacketHandler());
			playerRef.sendMessage(targetFly ? lang.flyModeEnabled : lang.flyModeDisabled);

			return targetFly;
		}
		return null;
	}
}
