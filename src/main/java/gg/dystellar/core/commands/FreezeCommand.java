package gg.dystellar.core.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.utils.Pair;

/**
 * Moderator command that lets you freeze a player to prevent them from moving.
 * This is useful for moderators that want to redirect the player somewhere and ask them a few questions.
 * It interrups whatever game the player was in, as it's usually used for cheaters.
 */
public class FreezeCommand extends CommandBase {

	public static void register(JavaPlugin plugin) {
		plugin.getEntityStoreRegistry().registerSystem(new MoveSystem());
		plugin.getEventRegistry().register(PlayerDisconnectEvent.class, FreezeCommand::onQuit);
	}

    public static void onQuit(PlayerDisconnectEvent event) {
        frozenPlayers.remove(event.getPlayerRef().getUuid());
    }

	private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("target", "Target player to freeze", ArgTypes.PLAYER_REF);

    public FreezeCommand() {
		super("freeze", "Freeze a player");
		this.addAliases("ss");
		this.requirePermission("dystellar.freeze");
    }

    private static final Map<UUID, Pair<Vector3d, Vector3f>> frozenPlayers = new HashMap<>();

	@Override
	protected void executeSync(CommandContext ctx) {
		final var player = ctx.get(playerArg);
		if (!player.isValid()) {
			ctx.sender().sendMessage(DystellarCore.getInstance().lang_en.get().errorPlayerNotOnline);
			return;
		}

		final var ref = player.getReference();
		if (!ref.isValid()) {
			ctx.sender().sendMessage(DystellarCore.getInstance().lang_en.get().errorPlayerNotInAWorld);
			return;
		}

		final var comp = ref.getStore().getComponent(ref, TransformComponent.getComponentType());
		final var pos = comp.getPosition().clone();
		final var rot = comp.getRotation().clone();

		if (frozenPlayers.remove(player.getUuid()) != null)
			frozenPlayers.put(player.getUuid(), new Pair<>(pos, rot));
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
