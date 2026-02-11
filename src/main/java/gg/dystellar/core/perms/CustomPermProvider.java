package gg.dystellar.core.perms;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.universe.Universe;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;

/**
 * PermissionProvider hook for Hytale
 */
public class CustomPermProvider implements PermissionProvider {

	@Override
	public void addGroupPermissions(String arg0, Set<String> arg1) {
		DystellarCore.getLog().atWarning().log(
			"Something tried to add group permissions using the permissions provider, that is not allowed."
		);
	}

	@Override
	public void addUserPermissions(UUID arg0, Set<String> arg1) {
		DystellarCore.getLog().atWarning().log(
			"Something tried to add user permissions using the permissions provider, that is not allowed."
		);
	}

	@Override
	public void addUserToGroup(UUID arg0, String arg1) {
		DystellarCore.getLog().atWarning().log(
			"Something tried to add a group to a player, that is not allowed."
		);
	}

	@Override
	public Set<String> getGroupPermissions(String g) {
		Optional<Group> gr = Group.getGroup(g);
		if (gr.isPresent()) {
			Group group = gr.get();

			return group.getPermissions().entrySet().stream()
				.map(p -> (!p.getValue().get() ? "-" : "") + p.getKey())
				.collect(Collectors.toUnmodifiableSet());
		}

		return Set.of();
	}

	@Override
	public Set<String> getGroupsForUser(UUID uuid) {
		final var player = Universe.get().getPlayer(uuid);
		if (player != null && player.isValid()) {
			final var user = player.getHolder().getComponent(UserComponent.getComponentType());

			return user.group.map(g -> Set.of(g.getName())).orElse(Set.of());
		}

		return Set.of();
	}

	@Override
	public String getName() {
		return "dystellar-perms";
	}

	@Override
	public Set<String> getUserPermissions(UUID uuid) {
		final var player = Universe.get().getPlayer(uuid);
		if (player != null && player.isValid()) {
			final var user = player.getHolder().getComponent(UserComponent.getComponentType());
			final var group = user.group;
			final Set<String> result = new HashSet<>(user.perms.size() + group.map(g -> g.getPermissions().size()).orElse(0));

			user.perms.forEach((s, p) -> result.add((!p.get() ? "-" : "") + s));
			group.ifPresent(g -> g.getPermissions().forEach((s, p) -> {
				if (user.perms.get(s) == null) {
					result.add((!p.get() ? "-" : "") + s);
				}
			}));

			return result;
		}

		return Set.of();
	}

	@Override
	public void removeGroupPermissions(String arg0, Set<String> arg1) {
		DystellarCore.getLog().atWarning().log(
			"Something tried to remove group permissions using provider, that is not allowed."
		);
	}

	@Override
	public void removeUserFromGroup(UUID arg0, String arg1) {
		DystellarCore.getLog().atWarning().log(
			"Something tried to remove group permissions using provider, that is not allowed."
		);
	}

	@Override
	public void removeUserPermissions(UUID arg0, Set<String> arg1) {
		DystellarCore.getLog().atWarning().log(
			"Something tried to remove user permissions using provider, that is not allowed."
		);
	}
}
