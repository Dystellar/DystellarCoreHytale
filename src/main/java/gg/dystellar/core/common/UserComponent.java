package gg.dystellar.core.common;

import java.util.*;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import gg.dystellar.core.common.inbox.Inbox;
import gg.dystellar.core.common.punishments.Punishment;
import gg.dystellar.core.perms.Group;
import gg.dystellar.core.perms.Permission;

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

	public final List<UUID> friends = new ArrayList<>();
    public final List<UUID> ignoreList = new ArrayList<>();
	public final Map<String, Permission> perms = new HashMap<>();

    public UserComponent(UUID id, String ip, String name) {
        this.uuid = id;
        this.ip = ip;
        this.name = name;
		this.inbox = new Inbox(this);
    }

	@Override
	public Component<EntityStore> clone() {
		System.out.println("UserComponent was cloned (resource loss)");
		return new UserComponent(uuid, ip, name);
	}

	// TODO: implement properly
    public void punish(Punishment punishment) {
        this.punishments.add(punishment);
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
}
