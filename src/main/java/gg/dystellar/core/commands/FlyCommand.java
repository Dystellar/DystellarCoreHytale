package gg.dystellar.core.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.SavedMovementStates;
import com.hypixel.hytale.protocol.packets.player.SetMovementStates;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;

// TODO: Test this

/**
 * This command is for premium people, so they can fly in the lobbys, so they can explore the buildings with ease.
 * It shouldn't have any effect on actual games.
 */
public class FlyCommand extends AbstractPlayerCommand {

    public FlyCommand() {
		super("fly", "Fly command for the privileged");
		this.requirePermission("dystellar.fly");

		this.addUsageVariant(new FlyOtherCommand());
    }

	@Override
	protected void execute(CommandContext ctx, Store<EntityStore> arg1, Ref<EntityStore> arg2, PlayerRef arg3, World arg4) {
		toggleFly(arg2, arg3);
	}

	/**
	 * Toggle fly little helper
	 */
	private static Boolean toggleFly(Ref<EntityStore> ref, PlayerRef playerRef) {
		final var move = ref.getStore().getComponent(ref, MovementStatesComponent.getComponentType());
		final var mngr = ref.getStore().getComponent(ref, MovementManager.getComponentType());

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

	private static class FlyOtherCommand extends AbstractTargetPlayerCommand {
		FlyOtherCommand() {
			super("fly", "Toggle fly mode for someone else");
			this.requirePermission("dystellar.fly.other");
		}

		@Override
		protected void execute(CommandContext ctx, Ref<EntityStore> ref, Ref<EntityStore> arg2, PlayerRef target, World w, Store<EntityStore> store) {
			final var langEn = DystellarCore.getInstance().lang_en.get();
			if (!ctx.sender().hasPermission("dystellar.fly.other")) {
				ctx.sender().sendMessage(langEn.noPermission);
				return;
			}

			final var res = toggleFly(ref, target);
			if (res == null)
				return;

			final var user = target.getHolder().getComponent(UserComponent.getComponentType());
			final var lang = DystellarCore.getInstance().getLang(user.language);
			target.sendMessage(res ? lang.flyModeEnabledByAdmin : lang.flyModeDisabledByAdmin);
			ctx.sendMessage(res ? langEn.adminFlyModEnabledOther : langEn.adminFlyModDisabledOther);
		}

	}
}
