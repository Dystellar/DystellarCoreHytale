package gg.dystellar.core.common;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.inbox.Inbox;
import gg.dystellar.core.common.punishments.Punishment;
import gg.dystellar.core.perms.Group;
import gg.dystellar.core.perms.Permission;
import gg.dystellar.core.utils.Utils;

public class UserComponent implements Component<EntityStore> {

	private static ComponentType<EntityStore, UserComponent> COMPONENT_TYPE;

	public static void init(JavaPlugin plugin) {
		COMPONENT_TYPE = plugin.getEntityStoreRegistry().registerComponent(UserComponent.class, () -> new UserComponent(null, null, null));
	}

	public static ComponentType<EntityStore, UserComponent> getComponentType() {
	    return COMPONENT_TYPE;
	}

    public static final byte PMS_ENABLED = 0;
    public static final byte PMS_ENABLED_FRIENDS_ONLY = 1;
    public static final byte PMS_DISABLED = 2;

	public PlayerRef player;
    public final UUID uuid;
    public Suffix suffix = Suffix.NONE;
    public final List<Punishment> punishments = new ArrayList<>();
    public String language = "en";
    public PlayerRef lastMessagedPlayer;
	public final String ip;
	public Optional<String> email = Optional.empty();
    public final String name;
    public final Inbox inbox;
    public final List<String> notes = new ArrayList<>();
    public long coins = 0;
    public boolean globalChatEnabled = true;
    public boolean scoreboardEnabled = true;
	public boolean friendRequests = true;
    public byte privateMessagesMode = PMS_ENABLED;
	public Optional<Group> group = Optional.ofNullable(Group.DEFAULT_GROUP);
	public LocalDateTime creationDate = LocalDateTime.now();

	public final List<UUID> friends = new ArrayList<>();
    public final List<UUID> ignoreList = new ArrayList<>();
	public final Map<String, Permission> perms = new HashMap<>();

    public UserComponent(UUID id, String ip, String name) {
        this.uuid = id;
        this.ip = ip;
        this.name = name;
		this.inbox = new Inbox(this);
    }

	public void init(PlayerRef player) {
		this.player = player;
	}

	@Override
	public Component<EntityStore> clone() {
		System.out.println("UserComponent was cloned (resource loss)");
		return new UserComponent(uuid, ip, name);
	}

    public void punish(Punishment punishment) {
        this.punishments.add(punishment);
		final var lang = DystellarCore.getInstance().getLang(language);
		for (Message msg : lang.punishMessage) {
			final var out = Message.join(msg)
				.param("title", punishment.getTitle())
				.param("reason", punishment.getReason())
				.param("expiration", Utils.getTimeFormat(punishment.getExpirationDate().orElse(null)));

			player.sendMessage(out);
		}
		if (!DystellarCore.getInstance().getSetup().allow_banned_players && !punishment.allowJoinMinigames()) {
			final var ref = player.getReference();
			if (ref.isValid()) {
				final var world = ref.getStore().getComponent(ref, Player.getComponentType()).getWorld();
				HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> {
					world.execute(() -> {
						player.getPacketHandler().disconnect("You have been banned");
					});
				}, 3, TimeUnit.SECONDS);
			} else {
				player.getPacketHandler().disconnect("You have been banned");
			}
		}
    }

    public void togglePms() {
        switch (privateMessagesMode) {
            case PMS_ENABLED:
            case PMS_ENABLED_FRIENDS_ONLY:
                privateMessagesMode++;
                break;
            case PMS_DISABLED:
                privateMessagesMode = PMS_ENABLED;
                break;
        }
    }

	public boolean hasPermission(String perm) {
		var permission = lookupPermission(perm, this.perms);
		if (permission != null)
			return permission.get();

		if (group.isPresent()) {
			permission = lookupPermission(perm, group.get().getPermissions());
			if (permission != null)
				return permission.get();
		}

		return false;
	}

	@Nullable
	private static Permission lookupPermission(String perm, Map<String, Permission> perms) {
		var permission = perms.get(perm);
		if (permission != null)
			return permission;

		permission = perms.get("*");
		if (permission != null)
			return permission;

		String[] split = perm.split("\\.");

		for (Permission p : perms.values()) {
			if (!p.getPerm().contains("*"))
				continue;
			String[] permSplit = p.getPerm().split("\\.");

			int min = Math.min(split.length, permSplit.length);

			for (int j = 0; j < min; j++) {
				if (permSplit[j].equals("*"))
					return p;
				if (!permSplit[j].equalsIgnoreCase(split[j]))
					break;
			}
		}

		return null;
	}
}
