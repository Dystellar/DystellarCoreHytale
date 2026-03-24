package gg.dystellar.core.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.hypixel.hytale.server.core.Message;

import gg.dystellar.core.utils.Pair;
import gg.dystellar.core.utils.Utils;

/**
 * Plugin messages, these are just values because gson will dump values from json.
 */
public class Messages {
	private final static ColorDeclaration DEFAULT_COLOR = new ColorDeclaration("Default", "#FFFFFF");
	
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
		new ColorDeclaration("StrongGreen", "#55FF55", true, false, false, false),
		new ColorDeclaration("StrongRed", "#FF5555", true, false, false, false),
		new ColorDeclaration("Test", "#000000", true, true, true, true)
	};

	private String command_hint = "<Gray>-> <MaterialEmerald>Hint<White>: <Blue>Use command <Orange>{command}";
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
	private String error_player_not_in_a_world = "<Red>This player is not in an actual world";
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
	private String[] automated_messages = {
		"Test",
		"Test",
		"Test",
		"Test",
		"Test"
	};

	private String friend_request_sent = "<Green>Friend request sent!";
	private String friend_request_received = "<DarkAqua>Incoming friend request from <Gold>{player}";
	private String friend_request_accepted_sender = "<DarkAqua>{player} <Green>has accepted your friend request!";
	private String friend_request_rejected_sender = "<DarkAqua>{player} <Red>has rejected your friend request.";
	private String friend_request_accepted_receiver = "<Green>Friend request accepted!";
	private String friend_request_rejected_receiver = "<Red>Friend request rejected.";
	private String player_not_on_friends_list = "<Red>This player is not in your friends list.";
	private String player_already_on_friends_list = "<Red>This player is already in your friends list.";
	private String target_requests_disabled = "<Red>This player has their friend requests disabled.";
	private String friend_removed_sender = "<Red>{player} has been removed from your friends list.";
	private String friend_removed_receiver = "<Red>{player} has removed you from their friends list. (They removed you)";
	private String friend_request_expired = "<Red>You don't have a friend request from this player. (Or it has expired)";
	private String friend_requests_enabled = "<DarkAqua>You are now able to receive friend requests.";
	private String friend_requests_disabled = "<Red>You are no longer able to receive friend requests.";
	private String friend_accept_button = "<StrongGreen>ACCEPT"; // TODO: Not available within the hytale api yet
	private String friend_reject_button = "<StrongRed>REJECT"; // TODO: Not available within the hytale api yet
	private String error_friend_add_yourself = "<Red>You can't send a friend request to yourself.";
	private String list_friends_title = "<DarkGreen>Friends list:";
	private String list_friends_entry = "<White> - <DarkAqua>{player}";

	private String find_same_server_as_sender = "<DarkAqua>{player} <Green>is in your same server!";
	private String find_not_found = "<Red>{player} is not within the network.";
	private String find_found = "<Aqua>{player} <White>is playing at <Green>{server}";

	private String player_blocked = "<DarkAqua>You blocked <White>{player}<DarkAqua>.";
	private String error_player_already_blocked = "<Red>This player is already in your blocked players list.";
	private String blacklist_empty = "<DarkAqua>Your blocked players list is empty.";
	private String blacklist_player_removed = "<Green>You've removed <DarkAqua>{player} <Green>from your blocked players list.";
	private String error_player_not_on_blocklist = "<Red>This player is not in your blocked players list.";
	private String blocked_players_list_title = "<DarkGreen>Blocked players:";
	private String blocked_players_list_entry = "<White> - <DarkAqua>{player}";

	private String cant_send_pms_disabled = "<Red>You can't send messages while blocking private messages. Enable them with <Yellow>/pms<Red>.";
	private String player_in_dnd_mode = "<Red>{player} is playing ranked and thus not receiving private messages due to Do Not Disturb mode.";
	private String error_you_are_blocked = "<Red>This player has blocked you.";
	private String error_no_reply_cache = "<Red>You don't have anyone to reply to.";
	private String error_player_has_pms_disabled = "<Red>This player has disabled their private messages.";

	private CompiledMessage compileMsg(String msg) {
		StringBuilder builder = new StringBuilder(msg);
		final var compiled = new CompiledMessage();
		int idx = 0;
		int from = 0;
		Optional<ColorDeclaration> opt = Optional.empty();

		while ((idx = builder.indexOf("<", from)) != -1) {
			if (idx > 0) {
				final var col = opt.orElse(DEFAULT_COLOR);

				compiled.parts.add(new Pair<>(builder.substring(from, idx), col));
			}
			from = idx + 1;
			int end = builder.indexOf(">", from);
			if (end == -1)
				break;

			String key = builder.substring(from, end);
			opt = Utils.findArr(this.color_declarations, declaration -> declaration.name == key);
		}
		if (idx > 0) {
			final var col = opt.orElse(DEFAULT_COLOR);
			compiled.parts.add(new Pair<>(builder.substring(from, idx), col));
		}

		return compiled;
	}

	public void compile() {
		this.commandHint = compileMsg(command_hint);
		this.pmsEnabled = compileMsg(pms_enabled);
		this.pmsEnabledBlocking = compileMsg(pms_enabled_blocking);
		this.pmsEnabledFriendsOnly = compileMsg(pms_enabled_friends_only);
		this.pmsDisabled = compileMsg(pms_disabled);
		this.playerMsgDisabled = compileMsg(player_msg_disabled);
		this.msgSendFormat = compileMsg(msg_send_format);
		this.msgReceiveFormat = compileMsg(msg_receive_format);
		this.commandDenyIngame = compileMsg(command_deny_ingame);
		this.noPermission = compileMsg(no_permission);
		this.errorPlayerNotOnline = compileMsg(error_player_not_online);
		this.errorPlayerNoLongerOnline = compileMsg(error_player_no_longer_online);
		this.errorPlayerDoesNotExist = compileMsg(error_player_does_not_exist);
		this.errorPlayerNotFound = compileMsg(error_player_not_found);
		this.errorPlayerNotInAWorld = compileMsg(error_player_not_in_a_world);
		this.errorInputNotNumber = compileMsg(error_input_not_number);
		this.errorNotAPlayer = compileMsg(error_not_a_player);
		this.serverConnectionError = compileMsg(server_connection_error);
		this.mCooldown = compileMsg(cooldown);
		this.prefixNotOwned = compileMsg(prefix_not_owned);
		this.globalChatEnabled = compileMsg(global_chat_enabled);
		this.globalChatDisabled = compileMsg(global_chat_disabled);
		this.broadcastFormat = compileMsg(broadcast_format);
		this.muteMessage = compileMsg(mute_message);
		this.rankedBanMessage = compileMsg(ranked_ban_message);
		this.kickMessage = compileMsg(kick_message);
		this.punishMessage = Arrays.stream(punish_message).map(s -> compileMsg(s)).toArray(CompiledMessage[]::new);
		this.flyNeedRank = compileMsg(fly_need_rank);
		this.flyModeEnabled = compileMsg(fly_mode_enabled);
		this.flyModeDisabled = compileMsg(fly_mode_disabled);
		this.flyModeEnabledByAdmin = compileMsg(fly_mode_enabled_by_admin);
		this.flyModeDisabledByAdmin = compileMsg(fly_mode_disabled_by_admin);
		this.adminFlyModEnabledOther = compileMsg(admin_fly_mod_enabled_other);
		this.adminFlyModDisabledOther = compileMsg(admin_fly_mod_disabled_other);
		this.freezeMessage = Arrays.stream(freeze_message).map(s -> compileMsg(s)).toArray(CompiledMessage[]::new);
		this.unfreezeMessage = compileMsg(unfreeze_message);
		this.staffMessageFreeze = compileMsg(staff_message_freeze);
		this.staffMessageUnfreeze = compileMsg(staff_message_unfreeze);
		this.automatedMessages = Arrays.stream(automated_messages).map(s -> compileMsg(s)).toArray(CompiledMessage[]::new);
		this.friendRequestSent = compileMsg(friend_request_sent);
		this.friendRequestReceived = compileMsg(friend_request_received);
		this.friendRequestAcceptedSender = compileMsg(friend_request_accepted_sender);
		this.friendRequestRejectedSender = compileMsg(friend_request_rejected_sender);
		this.friendRequestAcceptedReceiver = compileMsg(friend_request_accepted_receiver);
		this.friendRequestRejectedReceiver = compileMsg(friend_request_rejected_receiver);
		this.targetRequestsDisabled = compileMsg(target_requests_disabled);
		this.playerNotOnFriendsList = compileMsg(player_not_on_friends_list);
		this.playerAlreadyOnFriendsList = compileMsg(player_already_on_friends_list);
		this.friendRemovedSender = compileMsg(friend_removed_sender);
		this.friendRemovedReceiver = compileMsg(friend_removed_receiver);
		this.findSameServerAsSender = compileMsg(find_same_server_as_sender);
		this.friendRequestExpired = compileMsg(friend_request_expired);
		this.friendRequestsEnabled = compileMsg(friend_requests_enabled);
		this.friendRequestsDisabled = compileMsg(friend_requests_disabled);
		this.friendAcceptButton = compileMsg(friend_accept_button);
		this.friendRejectButton = compileMsg(friend_reject_button);
		this.errorFriendAddYourself = compileMsg(error_friend_add_yourself);
		this.findNotFound = compileMsg(find_not_found);
		this.findFound = compileMsg(find_found);
		this.listFriendsTitle = compileMsg(list_friends_title);
		this.listFriendsEntry = compileMsg(list_friends_entry);
		this.playerBlocked = compileMsg(player_blocked);
		this.errorPlayerAlreadyBlocked = compileMsg(error_player_already_blocked);
		this.blacklistEmpty = compileMsg(blacklist_empty);
		this.blacklistPlayerRemoved = compileMsg(blacklist_player_removed);
		this.errorPlayerNotOnBlocklist = compileMsg(error_player_not_on_blocklist);
		this.blockedPlayersListTitle = compileMsg(blocked_players_list_title);
		this.blockedPlayersListEntry = compileMsg(blocked_players_list_entry);
		this.cantSendPmsDisabled = compileMsg(cant_send_pms_disabled);
		this.playerInDndMode = compileMsg(player_in_dnd_mode);
		this.errorYouAreBlocked = compileMsg(error_you_are_blocked);
		this.errorNoReplyCache = compileMsg(error_no_reply_cache);
		this.errorPlayerHasPmsDisabled = compileMsg(error_player_has_pms_disabled);
	}

	public transient CompiledMessage commandHint;
	public transient CompiledMessage pmsEnabled;
	public transient CompiledMessage pmsEnabledBlocking;
	public transient CompiledMessage pmsEnabledFriendsOnly;
	public transient CompiledMessage pmsDisabled;
	public transient CompiledMessage playerMsgDisabled;
	public transient CompiledMessage msgSendFormat;
	public transient CompiledMessage msgReceiveFormat;
	public transient CompiledMessage commandDenyIngame;
	public transient CompiledMessage noPermission;
	public transient CompiledMessage errorPlayerNotOnline;
	public transient CompiledMessage errorPlayerNoLongerOnline;
	public transient CompiledMessage errorPlayerDoesNotExist;
	public transient CompiledMessage errorPlayerNotFound;
	public transient CompiledMessage errorPlayerNotInAWorld;
	public transient CompiledMessage errorInputNotNumber;
	public transient CompiledMessage errorNotAPlayer;
	public transient CompiledMessage serverConnectionError;
	public transient CompiledMessage mCooldown;
	public transient CompiledMessage prefixNotOwned;
	public transient CompiledMessage globalChatEnabled;
	public transient CompiledMessage globalChatDisabled;
	public transient CompiledMessage broadcastFormat;
	public transient CompiledMessage muteMessage;
	public transient CompiledMessage rankedBanMessage;
	public transient CompiledMessage kickMessage;
	public transient CompiledMessage[] punishMessage;
	public transient CompiledMessage flyNeedRank;
	public transient CompiledMessage flyModeEnabled;
	public transient CompiledMessage flyModeDisabled;
	public transient CompiledMessage flyModeEnabledByAdmin;
	public transient CompiledMessage flyModeDisabledByAdmin;
	public transient CompiledMessage adminFlyModEnabledOther;
	public transient CompiledMessage adminFlyModDisabledOther;
	public transient CompiledMessage[] freezeMessage;
	public transient CompiledMessage unfreezeMessage;
	public transient CompiledMessage staffMessageFreeze;
	public transient CompiledMessage staffMessageUnfreeze;
	public transient CompiledMessage[] automatedMessages;
	public transient CompiledMessage friendRequestSent;
	public transient CompiledMessage friendRequestReceived;
	public transient CompiledMessage friendRequestAcceptedSender;
	public transient CompiledMessage friendRequestRejectedSender;
	public transient CompiledMessage friendRequestAcceptedReceiver;
	public transient CompiledMessage friendRequestRejectedReceiver;
	public transient CompiledMessage targetRequestsDisabled;
	public transient CompiledMessage playerNotOnFriendsList;
	public transient CompiledMessage playerAlreadyOnFriendsList;
	public transient CompiledMessage friendRemovedSender;
	public transient CompiledMessage friendRemovedReceiver;
	public transient CompiledMessage findSameServerAsSender;
	public transient CompiledMessage friendRequestExpired;
	public transient CompiledMessage friendRequestsEnabled;
	public transient CompiledMessage friendRequestsDisabled;
	public transient CompiledMessage friendAcceptButton;
	public transient CompiledMessage friendRejectButton;
	public transient CompiledMessage errorFriendAddYourself;
	public transient CompiledMessage listFriendsTitle;
	public transient CompiledMessage listFriendsEntry;
	public transient CompiledMessage findNotFound;
	public transient CompiledMessage findFound;

	public transient CompiledMessage playerBlocked;
	public transient CompiledMessage errorPlayerAlreadyBlocked;
	public transient CompiledMessage blacklistEmpty;
	public transient CompiledMessage blacklistPlayerRemoved;
	public transient CompiledMessage errorPlayerNotOnBlocklist;
	public transient CompiledMessage blockedPlayersListTitle;
	public transient CompiledMessage blockedPlayersListEntry;

	public transient CompiledMessage cantSendPmsDisabled;
	public transient CompiledMessage playerInDndMode;
	public transient CompiledMessage errorYouAreBlocked;
	public transient CompiledMessage errorNoReplyCache;
	public transient CompiledMessage errorPlayerHasPmsDisabled;

	private static class ColorDeclaration {
		private String name;
		private String hex_color;
		private boolean bold = false;
		private boolean italic = false;
		private boolean monospace = false;
		private boolean underlined = false; // TODO: Not available...yet?

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

	public static class CompiledMessage {
		List<Pair<String, ColorDeclaration>> parts = new ArrayList<>();

		public Message buildMessage() {
			final var message = Message.empty();

			for (final var part : this.parts) {
				final var child = Message.raw(part.first)
					.color(part.second.hex_color)
					.bold(part.second.bold)
					.italic(part.second.italic)
					.monospace(part.second.monospace);

				message.insert(child);
			}

			return message;
		}
	}
}
