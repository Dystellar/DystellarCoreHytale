package gg.dystellar.core.commands;

import java.awt.Color;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.utils.Triple;

// TODO: Test this

/**
 * Moderator command that lets you freeze a player to prevent them from moving.
 * This is useful for moderators that want to redirect the player somewhere and ask them a few questions.
 * It interrups whatever game the player was in, as it's usually used for cheaters.
 */
public class FreezeCommand extends AbstractTargetPlayerCommand {

	public static void register(JavaPlugin plugin) {
		plugin.getEntityStoreRegistry().registerSystem(new MoveSystem());
		plugin.getEventRegistry().register(PlayerDisconnectEvent.class, FreezeCommand::onQuit);
	}

    public static void onQuit(PlayerDisconnectEvent event) {
        frozenPlayers.remove(event.getPlayerRef().getUuid());
    }

    public FreezeCommand() {
		super("freeze", "Freeze a player");
		this.addAliases("ss");
		this.requirePermission("dystellar.freeze");
    }

    private static final Map<UUID, Triple<Vector3d, Vector3f, ScheduledFuture<?>>> frozenPlayers = new ConcurrentHashMap<>();

	@Override
	protected void execute(CommandContext ctx, Ref<EntityStore> ref, Ref<EntityStore> src, PlayerRef target, World w, Store<EntityStore> store) {
		final var comp = ref.getStore().getComponent(ref, TransformComponent.getComponentType());
		final var pos = comp.getPosition().clone();
		final var rot = comp.getRotation().clone();
		final var removed = frozenPlayers.remove(target.getUuid());
		final var user = target.getHolder().getComponent(UserComponent.getComponentType());
		final var lang = DystellarCore.getInstance().getLang(user.language);

		if (removed == null) {
			final ScheduledFuture<?> task =  HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
				if (target.isValid()) {
					for (final var msg : lang.freezeMessage)
						target.sendMessage(msg.buildMessage());
				}
			}, 0, 2, TimeUnit.SECONDS);

			frozenPlayers.put(target.getUuid(), new Triple<>(pos, rot, task));
			ctx.sender().sendMessage(Message.raw("Player frozen!").color(Color.GREEN));
		} else {
			removed.third.cancel(false);
			target.sendMessage(lang.unfreezeMessage.buildMessage());
			ctx.sender().sendMessage(Message.raw("Player unfrozen!").color(Color.YELLOW));
		}
	}

	static class MoveSystem extends EntityTickingSystem<EntityStore> {

		private final Query<EntityStore> query = Archetype.of(Player.getComponentType());

		MoveSystem() {}

		@Override
		public Query<EntityStore> getQuery() {
			return query;
		}

		@Override
		public void tick(float dt, int idx, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buf) {
			final var player = chunk.getComponent(idx, PlayerRef.getComponentType());
			final var posData = frozenPlayers.get(player.getUuid());
			final var transform = chunk.getComponent(idx, TransformComponent.getComponentType());

			if (posData != null && (!transform.getPosition().equals(posData.first) || !transform.getRotation().equals(posData.second))) {
				transform.setPosition(posData.first);
				transform.setRotation(posData.second);
			}
		}
	}
}
