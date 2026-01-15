package gg.dystellar.core.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import gg.dystellar.core.DystellarCore;

import static gg.dystellar.core.DystellarCore.*;

/**
 * Plugin messages, these are just values because gson will dump values from json on these.
 */
public class Messages {
	public static Messages fallback = new Messages();
	public static Optional<Messages> lang_en = Optional.empty();
	public static Optional<Messages> lang_es = Optional.empty();
	public static Optional<Messages> lang_fr = Optional.empty();
	public static Optional<Messages> lang_de = Optional.empty();

	private static void createDefaultIfNotPresent(final File file) throws IOException {
		if (!file.exists()) {
			String output = getGson().toJson(new Messages());
			FileWriter wt = new FileWriter(file);

			wt.write(output);
			wt.close();
		}
	}

	public static void initConfigs(DystellarCore plugin) throws JsonSyntaxException, JsonIOException, IOException {
		final var en = new File(plugin.getDataDirectory().toFile(), "lang_en.json");
		final var es = new File(plugin.getDataDirectory().toFile(), "lang_es.json");
		final var fr = new File(plugin.getDataDirectory().toFile(), "lang_fr.json");
		final var de = new File(plugin.getDataDirectory().toFile(), "lang_de.json");

		createDefaultIfNotPresent(en);
		createDefaultIfNotPresent(es);
		createDefaultIfNotPresent(fr);
		createDefaultIfNotPresent(de);

		try (final var rEn = new FileReader(new File(plugin.getDataDirectory().toFile(), "lang_en.json"));
				final var rEs = new FileReader(new File(plugin.getDataDirectory().toFile(), "lang_en.json"));
				final var rFr = new FileReader(new File(plugin.getDataDirectory().toFile(), "lang_en.json"));
				final var rDe = new FileReader(new File(plugin.getDataDirectory().toFile(), "lang_en.json"))) {
			lang_en = Optional.of(getGson().fromJson(rEn, Messages.class));
			lang_en = Optional.of(getGson().fromJson(rEs, Messages.class));
			lang_en = Optional.of(getGson().fromJson(rFr, Messages.class));
			lang_en = Optional.of(getGson().fromJson(rDe, Messages.class));
		}
	}

	/**
	 * @param lang a country code such as "en", or "es". A default fallback is returned if not found
	 */
	public static Messages get(String lang) {
		try {
			final var langfile = Messages.class.getField("lang_" + lang);

			if (langfile.get(null) instanceof Optional<?> opt && opt.isPresent())
				return (Messages) opt.get();
		} catch (Exception e) {
			getLog().atSevere().log(e.getMessage());
			getLog().atSevere().log("Failed to find field lang_" + lang + ", using fallback instance");
		}

		return fallback;
	}

	public final double config_version = 0;

	public final String pms_enabled = "§aYou are now accepting all private messages.";
	public final String pms_enabled_blocking = "§eYou are now blocking messages from unwanted players.";
	public final String pms_enabled_friends_only = "§6You are now only accepting messages from friends.";
	public final String pms_disabled = "§cYou are now blocking all messages.";
	public final String player_msg_disabled = "§c<player> has disabled private messages.";
	public final String msg_send_format = "§7(§dTo <receiver>§7) §b<message>";
	public final String msg_receive_format = "§7(§dFrom <sender>§7) §b<message>";
	public final String command_deny_ingame = "§cYou're not allowed to use this command in-game.";
	public final String no_permission = "§cYou don't have permission to perform this action.";
	public final String error_player_not_online = "§cThis player is not online.";
	public final String error_player_no_longer_online = "§cThis player is no longer online.";
	public final String error_player_does_not_exist = "§cThis player does not exist or is not online.";
	public final String error_player_not_found = "§cThis player does not exist in our database.";
	public final String error_input_not_number = "§cInput must be a number, try again.";
	public final String error_not_a_player = "§cYou must be a player to perform this action.";
	public final String server_connection_error = "§cError trying to connect to server.";
	public final String cooldown = "§cYou are on cooldown, wait <seconds> seconds before performing this action again.";
	public final String prefix_not_owned = "§cYou don't have this prefix.";
	public final String global_chat_enabled = "§aYou've enabled global chat.";
	public final String global_chat_disabled = "§3You've disabled global chat.";
	public final String broadcast_format = "[§cBroadcast§f] §e<message>";
	public final String mute_message = "§cYou are muted. Expires in <time>";
	public final String ranked_ban_message = "§cYou are banned from playing ranked. Contact staff for more information.";
	public final String kick_message = "§cYou have been kicked from the server. Reason: §f<reason>";
	public final String[] ban_message = {
		"§4§lYou are banned",
		"Reason: <reason>",
		"Expires: <time>",
		"You may purchase an unban at https://dystellar.gg/"
	};
	public final String[] blacklist_message = {
		"§4§lYou are blacklisted",
		"Reason: <reason>",
		"Expires: Never",
		"This type of punishment cannot be appealed."
	};
	public final String[] warn_message = {
		"§7§m----------------------------------",
		" ",
		"§cYou have been warned!",
		"Reason: <reason>",
		" ",
		"§4Be careful, getting multiple warnings will get you banned!",
		" ",
		"§7§m----------------------------------"
	};
	public final String fly_need_rank = "§cYou need §aplus §crank or higher to enable fly mode.";
	public final String fly_mode_enabled = "§aYou have enabled the fly mode.";
	public final String fly_mode_disabled = "§eYou have disabled the fly mode.";
	public final String fly_mode_enabled_by_admin = "§aAn admin has enabled flight for you.";
	public final String fly_mod_disabled_by_admin = "§cAn admin has disabled flight for you.";
	public final String admin_fly_mod_enabled_other = "§aYou have enabled the fly mode for §3<player>§a.";
	public final String admin_fly_mod_disabled_other = "§eYou have disabled the fly mode for §3<player>§e.";
	public final String[] freeze_message = {
		"--------------------------------",
		"&cYou have been frozen!",
		"--------------------------------",
	};
	public final String unfreeze_message = "§aYou have been unfreezed. Sorry and thanks for your time!";
	public final String staff_message_freeze = "§e<player> is now frozen.";
	public final String staff_message_unfreeze = "§a<player> is now free to go.";
}
