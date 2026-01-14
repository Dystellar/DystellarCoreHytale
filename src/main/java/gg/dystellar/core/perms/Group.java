package gg.dystellar.core.perms;

import com.google.common.io.ByteArrayDataInput;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import live.taketsu.config.ConfValues;
import live.taketsu.core.User;
import live.taketsu.messaging.Encoder.Perms;
import live.taketsu.serialization.MariaDB;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Group of permissions, you may understand better if you refer to it as ranks.
 */
public class Group {

    public static Group DEFAULT_GROUP;
    public static boolean PERMS_LOADED = true;

    private static final Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
    public static final Map<String, Group> groups = new HashMap<>();

    private final int id;
    private final String name;
    private String prefix;
    private String suffix;
    private final Map<String, Permission> permissions = new HashMap<>();
    private Team team;
	private LocalDateTime lastModification;

    public Group(int id, String name, String prefix, String suffix, List<Permission> permissions, LocalDateTime lastModification) {
        this.id = id;
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
		if (lastModification != null)
			this.lastModification = lastModification;
		else
			this.lastModification = LocalDateTime.now();
        permissions.forEach(permission -> this.permissions.put(permission.getPerm(), permission));
        initTeam();
    }

    private void initTeam() {
        team = sb.getTeam(this.name);

        if (team == null)
            team = sb.registerNewTeam(this.name);

        team.setPrefix(this.prefix);
        team.setSuffix(this.suffix);
        team.setAllowFriendlyFire(true);
        team.setNameTagVisibility(NameTagVisibility.ALWAYS);
    }

	public LocalDateTime getLastModification() {
		return lastModification;
	}

	public void setLastModification(LocalDateTime lastModification) {
		this.lastModification = lastModification;
	}

	public void applyTeam(String player) {
        this.team.addEntry(player);
    }

    public Team getTeam() {
        return team;
    }

    public int getId() {
        return id;
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
		this.team.setPrefix(prefix);
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
		this.team.setSuffix(suffix);
    }

    public static Optional<Group> getGroup(String name) {
        return Optional.ofNullable(groups.get(name));
    }

    public static Group getDefault() {
        return DEFAULT_GROUP;
    }

    public static void registerGroup(Group group) {
        groups.put(group.name, group);
    }

    public static void init() {
        if (!MariaDB.loadGroups(groups)) {
            Bukkit.getLogger().severe("Fatal error trying to load groups, aborting...");
            PERMS_LOADED = false;
            return;
        }

        String defaultGroupN = MariaDB.getDefaultGroup();
        DEFAULT_GROUP = groups.get(defaultGroupN);
        if (DEFAULT_GROUP == null)
            Bukkit.getLogger().warning("Default group is not set!");
    }

    public static void handleMsg(Player p, ByteArrayDataInput in) {
		byte type = in.readByte();

		switch (type) {
			case Perms.GROUP_FLUSH_PERMISSIONS: {
				String gName = in.readUTF();

				Group group = groups.get(gName);
				if (group == null) {
					Bukkit.getLogger().warning("Received a flush permissions message for a group that doesn't exist.");
                    return;
                }

				group.permissions.clear();
				List<Permission> perms = MariaDB.getGroupPermissions(group.id);
				if (perms == null) {
					Bukkit.getLogger().severe("Failed to flush group permissions, check logs.");
					return;
				}

				perms.forEach(permission -> group.permissions.put(permission.getPerm(), permission));
				break;
			}
			case Perms.GROUP_DEFAULT_UPDATED: {
				String name = in.readUTF();
				Group group = groups.get(name);
				if (group == null) {
					Bukkit.getLogger().warning("Received a default group update message with a name that doesn't exist.");
					return;
				}
				DEFAULT_GROUP = group;
				Bukkit.getLogger().info("Group updated successfully, notified from proxy!");
				break;
			}
			case Perms.USER_GROUP_ADD: {
				String name = in.readUTF();
				User user = User.get(p);
				if (user == null) {
					Bukkit.getLogger().warning("Received a group update for a player that is not online.");
					return;
				}
				Optional<Group> g = Group.getGroup(name);
				if (!g.isPresent()) {
					Bukkit.getLogger().info("Received a group update for " + name + " but the group '" + name + "' doesn't exist.");
					return;
				}
				user.setGroup(g.get());
				p.sendMessage(ChatColor.BLUE + "Your rank has been updated! New rank: " + user.getGroup().getName());
				break;
			}
			case Perms.GROUP_NEW: {
				int id = in.readInt();
				Group group = MariaDB.getGroupFromId(id);
				Group.registerGroup(group);
				Bukkit.getLogger().info("Registered group with id " + id + ".");
				break;
			}
			case Perms.GROUP_DESTROY: {
				String name = in.readUTF();
				Group group = groups.get(name);

				if (group == null) {
					Bukkit.getLogger().warning("Received a group destroy packet for a group that does not exist.");
					return;
				}

				// Set default group to players who were in this group
				for (User user : User.getUsers().values()) {
					if (user.getGroup() != null && user.getGroup().getName().equals(name))
						user.setGroup(getDefault());
				}
				
				groups.remove(name);
				if (ConfValues.DEBUG_MODE)
					Bukkit.getLogger().info("Group removed successfully.");
				break;
			}
			case Perms.GROUP_PERM_ADDED: {
				String name = in.readUTF();
				String perm = in.readUTF();
				boolean negate = in.readBoolean();

				Group group = groups.get(name);
				if (group == null) {
					Bukkit.getLogger().warning("Received a perm add to a group that does not exist.");
					return;
				}
				group.getPermissions().put(perm, new Permission(perm, negate));

				if (ConfValues.DEBUG_MODE)
					Bukkit.getLogger().info("Permission added correctly");
				break;
			}
			case Perms.GROUP_PERM_REMOVED: {
				String name = in.readUTF();
				String prm = in.readUTF();

				Group group = groups.get(name);
				if (group == null) {
					Bukkit.getLogger().warning("Received a perm remove packet of a group that does not exist.");
					return;
				}

				List<Permission> tmp = new LinkedList<>();

				for (Permission perm : group.getPermissions().values()) {
					if (perm.getPerm().equals(prm))
						tmp.add(perm);
				}
				tmp.forEach(perm -> group.getPermissions().remove(perm.getPerm()));

				if (ConfValues.DEBUG_MODE)
					Bukkit.getLogger().info("[Debug] Group perm removed packet.");
				break;
			}
			case Perms.GROUP_UPDATED: {
				String name = in.readUTF();

				Group group = groups.get(name);
				if (group == null) {
					Bukkit.getLogger().warning("Received a group update but the group specified does not exist.");
					return;
				}

				if (!MariaDB.getGroupUpdate(group)) {
					Bukkit.getLogger().warning("Fatal error getting group update! Check logs.");
					return;
				}

				if (ConfValues.DEBUG_MODE)
					Bukkit.getLogger().info("[Debug] Group updated.");
				break;
			}
		}
    }
}
