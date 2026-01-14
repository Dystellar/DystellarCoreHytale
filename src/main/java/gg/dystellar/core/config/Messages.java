package gg.dystellar.core.config;

/**
 * Plugin messages, these are just values because after investigating the hytale API,
 * I think what it does is load a json file into a java object, and uses reflection
 * to forcefully modify a class's attributes, so the json config in theory would be dumped
 * into this values.
 *
 * This should be tested though, as it's just my conclusion after reading the source code, and it's pure speculation.
 */
public class Messages {
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
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
	public final String _ = ;
}
