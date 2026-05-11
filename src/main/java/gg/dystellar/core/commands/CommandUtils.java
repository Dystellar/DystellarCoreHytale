package gg.dystellar.core.commands;

import javax.annotation.Nonnull;

import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.suggestion.SuggestionResult;
import com.hypixel.hytale.server.core.universe.Universe;

import gg.dystellar.core.common.User;
import gg.dystellar.core.perms.Group;

public final class CommandUtils {
	public static void playerSuggestor(@Nonnull CommandSender sender, @Nonnull String text, int var3, @Nonnull SuggestionResult res) {
		for (final var p : Universe.get().getPlayers()) {
			if (p.getUsername().toLowerCase().startsWith(text.toLowerCase()))
				res.suggest(p.getUsername());
		}
	}

	public static void friendSuggestor(@Nonnull CommandSender sender, @Nonnull String text, int var3, @Nonnull SuggestionResult res) {
		final var u = User.getUser(sender.getUuid());
		u.ifPresent(user -> {
			for (final var f : user.friends) {
				if (f.name().toLowerCase().startsWith(text.toLowerCase()))
					res.suggest(f.name());
			}
		});
	}

	public static void ignorelistSuggestor(@Nonnull CommandSender sender, @Nonnull String text, int var3, @Nonnull SuggestionResult res) {
		final var u = User.getUser(sender.getUuid());
		u.ifPresent(user -> {
			for (final var f : user.ignoreList) {
				if (f.name().toLowerCase().startsWith(text.toLowerCase()))
					res.suggest(f.name());
			}
		});
	}

	public static void groupSuggestor(@Nonnull CommandSender sender, @Nonnull String text, int var3, @Nonnull SuggestionResult res) {
		for (final var group : Group.groups.keySet()) {
			if (group.toLowerCase().startsWith(text.toLowerCase()))
				res.suggest(group);
		}
	}
}
