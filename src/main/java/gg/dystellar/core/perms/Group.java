package gg.dystellar.core.perms;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.hypixel.hytale.server.core.universe.Universe;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.serialization.Protocol.RawGroup;

/**
 * Group of permissions, you may understand better if you refer to it as ranks.
 */
public class Group {

    private static volatile Optional<Group> DEFAULT_GROUP = Optional.empty();
    public static volatile boolean PERMS_LOADED = true;

    public static final Map<String, Group> groups = new ConcurrentHashMap<>();

	public static boolean initGroups() throws IOException, InterruptedException {
		groups.clear();
		final var groupsOpt = DystellarCore.getApi().getGroupsData();
		if (!groupsOpt.isPresent())
			return false;

		final var rawGroups = groupsOpt.get();
		for (RawGroup g : rawGroups.groups)
			groups.put(g.name, g.toGroup());

		DEFAULT_GROUP = Optional.ofNullable(rawGroups.default_group != null ? groups.get(rawGroups.default_group) : null);
		return true;
	}

	public static void setDefaultGroup(Group group) {
		final var newOptional = Optional.ofNullable(group);
		final var old = DEFAULT_GROUP;
		for (final var p : Universe.get().getPlayers()) {
			final var user = p.getHolder().getComponent(UserComponent.getComponentType());
			if (user != null && user.group.equals(old))
				user.group = newOptional;
		}

		DEFAULT_GROUP = newOptional;
	}

	public static Optional<Group> getDefaultGroup() {
	    return DEFAULT_GROUP;
	}

    private final String name;
    private String prefix;
    private String suffix;
    private final Map<String, Permission> permissions = new HashMap<>();

    public Group(String name, String prefix, String suffix, List<Permission> permissions) {
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
        permissions.forEach(permission -> this.permissions.put(permission.getPerm(), permission));
    }

    public String getName() {
        return name;
    }

    public Map<String, Permission> getPermissions() {
        return permissions;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public static Optional<Group> getGroup(String name) {
        return Optional.ofNullable(groups.get(name));
    }

    public static void registerGroup(Group group) {
        groups.put(group.name, group);
    }
}
