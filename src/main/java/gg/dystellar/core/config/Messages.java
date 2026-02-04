package gg.dystellar.core.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.hypixel.hytale.server.core.Message;

import gg.dystellar.core.utils.Utils;

/**
 * Plugin messages, these are just values because gson will dump values from json.
 */
public class Messages {
	private ColorDeclaration[] color_declarations = {
		new ColorDeclaration("Black", "#000000"),
		new ColorDeclaration("DarkBlue", "#0000AA"),
		new ColorDeclaration("DarkGreen", "#00AA00"),
		new ColorDeclaration("DarkAqua", "#00AAAA"),
		new ColorDeclaration("DarkRed", "#AA0000"),
		new ColorDeclaration("Gold", "#FFAA00"),
		new ColorDeclaration("Gray", "#AAAAAA"),
		new ColorDeclaration("DarkGray", "#555555"),
		new ColorDeclaration("Blue", "#5555FF"),
		new ColorDeclaration("Green", "#55FF55"),
		new ColorDeclaration("Aqua", "#55FFFF"),
		new ColorDeclaration("Red", "#FF5555"),
		new ColorDeclaration("Orange", "#EB7114"),
		new ColorDeclaration("Yellow", "#FFFF55"),
		new ColorDeclaration("White", "#FFFFFF"),
		new ColorDeclaration("LightPurple", "#FF55FF"),
		new ColorDeclaration("MaterialGold", "#DEB12D"),
		new ColorDeclaration("MaterialQuartz", "#E3D4D1"),
		new ColorDeclaration("MaterialIron", "#CECACA"),
		new ColorDeclaration("MaterialCopper", "#B4684D"),
		new ColorDeclaration("MaterialEmerald", "#47A036"),
		new ColorDeclaration("DarkerRed", "#971607"),
		new ColorDeclaration("Danger", "#AA0000", true, false, false, false),
	};

	private String pms_enabled = "<Green>You are now accepting all private messages.";
	private String pms_enabled_blocking = "<Yellow>You are now blocking messages from unwanted players.";
	private String pms_enabled_friends_only = "<Gold>You are now only accepting messages from friends.";
	private String pms_disabled = "<Red>You are now blocking all messages.";
	private String player_msg_disabled = "<Red>{player} has disabled private messages.";
	private String msg_send_format = "<Gray>(To <LightPurple>{receiver}<Gray>) <Aqua>{message}";
	private String msg_receive_format = "<Gray>(From <LightPurple>{sender}<Gray>) <Aqua>{message}";
	private String command_deny_ingame = "<Red>You're not allowed to use this command in-game.";
	private String no_permission = "<Red>You don't have permission to perform this action.";
	private String error_player_not_online = "<Red>This player is not online.";
	private String error_player_no_longer_online = "<Red>This player is no longer online.";
	private String error_player_does_not_exist = "<Red>This player does not exist or is not online.";
	private String error_player_not_found = "<Red>This player does not exist in our database.";
	private String error_input_not_number = "<Red>Input must be a number, try again.";
	private String error_not_a_player = "<Red>You must be a player to perform this action.";
	private String server_connection_error = "<Red>Error trying to connect to server.";
	private String cooldown = "<Red>You are on cooldown, wait {seconds} seconds before performing this action again.";
	private String prefix_not_owned = "<Red>You don't have this prefix.";
	private String global_chat_enabled = "<Green>You've enabled global chat.";
	private String global_chat_disabled = "<DarkAqua>You've disabled global chat.";
	private String broadcast_format = "<Gray>[<Red>Broadcast<Gray>] <Yellow>{message}";
	private String mute_message = "<Red>You are muted. Expires in {time}";
	private String ranked_ban_message = "<Red>You are banned from playing ranked. Contact staff for more information.";
	private String kick_message = "<Red>You have been kicked from the server. Reason: <White>{reason}";
	private String[] punish_message = {
		"<Gray>----------------------------------",
		" ",
		"<Red>{title}",
		"<Red>Reason<White>: {reason}",
		"<Red>Expires<White>: {expiration}",
		" ",
		"<Gray>----------------------------------"
	};
	private String fly_need_rank = "<Red>You need {rank_name} <Red>rank or higher to enable fly mode.";
	private String fly_mode_enabled = "<Green>You have enabled the fly mode.";
	private String fly_mode_disabled = "<Yellow>You have disabled the fly mode.";
	private String fly_mode_enabled_by_admin = "<Green>An admin has enabled flight for you.";
	private String fly_mode_disabled_by_admin = "<Red>An admin has disabled flight for you.";
	private String admin_fly_mod_enabled_other = "<Green>You have enabled the fly mode for <DarkAqua>{player}<Green>.";
	private String admin_fly_mod_disabled_other = "<Yellow>You have disabled the fly mode for <DarkAqua>{player}<Yellow>.";
	private String[] freeze_message = {
		"--------------------------------",
		"<Danger>You have been frozen!",
		"--------------------------------",
	};
	private String unfreeze_message = "<Green>You have been unfreezed. Sorry and thanks for your time!";
	private String staff_message_freeze = "<Yellow>{player} is now frozen.";
	private String staff_message_unfreeze = "<Green>{player} is now free to go.";

	private Message parseMsg(String msg) {
		StringBuilder builder = new StringBuilder(msg);
		List<Message> parts = new ArrayList<>();
		int idx = 0;
		int from = 0;
		Optional<ColorDeclaration> opt = Optional.empty();

		while ((idx = builder.indexOf("<", from)) != -1) {
			if (idx > 0) {
				if (opt.isPresent()) {
					var col = opt.get();
					parts.add(Message.raw(builder.substring(from, idx))
						.color(col.hex_color)
						.bold(col.bold)
						.italic(col.italic)
						.monospace(col.monospace));
				} else {
					parts.add(Message.raw(builder.substring(from, idx)));
				}
			}
			from = idx + 1;
			int end = builder.indexOf(">", from);
			if (end == -1)
				break;

			String key = builder.substring(from, end);
			opt = Utils.findArr(this.color_declarations, declaration -> declaration.name == key);
		}
		if (idx > 0) {
			if (opt.isPresent()) {
				var col = opt.get();
				parts.add(Message.raw(builder.substring(from, idx))
					.color(col.hex_color)
					.bold(col.bold)
					.italic(col.italic)
					.monospace(col.monospace));
			} else {
				parts.add(Message.raw(builder.substring(from, idx)));
			}
		}

		return Message.join(parts.toArray(new Message[parts.size()]));
	}

	public void compile() {
		this.pmsEnabled = parseMsg(pms_enabled);
		this.pmsEnabledBlocking = parseMsg(pms_enabled_blocking);
		this.pmsEnabledFriendsOnly = parseMsg(pms_enabled_friends_only);
		this.pmsDisabled = parseMsg(pms_disabled);
		this.playerMsgDisabled = parseMsg(player_msg_disabled);
		this.msgSendFormat = parseMsg(msg_send_format);
		this.msgReceiveFormat = parseMsg(msg_receive_format);
		this.commandDenyIngame = parseMsg(command_deny_ingame);
		this.noPermission = parseMsg(no_permission);
		this.errorPlayerNotOnline = parseMsg(error_player_not_online);
		this.errorPlayerNoLongerOnline = parseMsg(error_player_no_longer_online);
		this.errorPlayerDoesNotExist = parseMsg(error_player_does_not_exist);
		this.errorPlayerNotFound = parseMsg(error_player_not_found);
		this.errorInputNotNumber = parseMsg(error_input_not_number);
		this.errorNotAPlayer = parseMsg(error_not_a_player);
		this.serverConnectionError = parseMsg(server_connection_error);
		this.mCooldown = parseMsg(cooldown);
		this.prefixNotOwned = parseMsg(prefix_not_owned);
		this.globalChatEnabled = parseMsg(global_chat_enabled);
		this.globalChatDisabled = parseMsg(global_chat_disabled);
		this.broadcastFormat = parseMsg(broadcast_format);
		this.muteMessage = parseMsg(mute_message);
		this.rankedBanMessage = parseMsg(ranked_ban_message);
		this.kickMessage = parseMsg(kick_message);
		this.punishMessage = Arrays.stream(punish_message).map(s -> parseMsg(s)).toArray(Message[]::new);
		this.flyNeedRank = parseMsg(fly_need_rank);
		this.flyModeEnabled = parseMsg(fly_mode_enabled);
		this.flyModeDisabled = parseMsg(fly_mode_disabled);
		this.flyModeEnabledByAdmin = parseMsg(fly_mode_enabled_by_admin);
		this.flyModeDisabledByAdmin = parseMsg(fly_mode_disabled_by_admin);
		this.adminFlyModEnabledOther = parseMsg(admin_fly_mod_enabled_other);
		this.adminFlyModDisabledOther = parseMsg(admin_fly_mod_disabled_other);
		this.freezeMessage = Arrays.stream(freeze_message).map(s -> parseMsg(s)).toArray(Message[]::new);
		this.unfreezeMessage = parseMsg(unfreeze_message);
		this.staffMessageFreeze = parseMsg(staff_message_freeze);
		this.staffMessageUnfreeze = parseMsg(staff_message_unfreeze);
	}

	public transient Message pmsEnabled;
	public transient Message pmsEnabledBlocking;
	public transient Message pmsEnabledFriendsOnly;
	public transient Message pmsDisabled;
	public transient Message playerMsgDisabled;
	public transient Message msgSendFormat;
	public transient Message msgReceiveFormat;
	public transient Message commandDenyIngame;
	public transient Message noPermission;
	public transient Message errorPlayerNotOnline;
	public transient Message errorPlayerNoLongerOnline;
	public transient Message errorPlayerDoesNotExist;
	public transient Message errorPlayerNotFound;
	public transient Message errorInputNotNumber;
	public transient Message errorNotAPlayer;
	public transient Message serverConnectionError;
	public transient Message mCooldown;
	public transient Message prefixNotOwned;
	public transient Message globalChatEnabled;
	public transient Message globalChatDisabled;
	public transient Message broadcastFormat;
	public transient Message muteMessage;
	public transient Message rankedBanMessage;
	public transient Message kickMessage;
	public transient Message[] punishMessage;
	public transient Message flyNeedRank;
	public transient Message flyModeEnabled;
	public transient Message flyModeDisabled;
	public transient Message flyModeEnabledByAdmin;
	public transient Message flyModeDisabledByAdmin;
	public transient Message adminFlyModEnabledOther;
	public transient Message adminFlyModDisabledOther;
	public transient Message[] freezeMessage;
	public transient Message unfreezeMessage;
	public transient Message staffMessageFreeze;
	public transient Message staffMessageUnfreeze;

	private static class ColorDeclaration {
		private String name;
		private String hex_color;
		private boolean bold = false;
		private boolean italic = false;
		private boolean monospace = false;
		private boolean underlined = false;

		public ColorDeclaration(final String name, final String hexColor) {
			this.name = name;
			this.hex_color = hexColor;
		}

		public ColorDeclaration(final String name, final String hexColor, boolean bold, boolean italic, boolean monospace, boolean underlined) {
			this.name = name;
			this.hex_color = hexColor;
			this.bold = bold;
			this.italic = italic;
			this.monospace = monospace;
			this.underlined = underlined;
		}
	}
}
