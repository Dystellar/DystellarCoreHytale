package gg.dystellar.core.common;

import net.zylesh.dystellarcore.DystellarCore;

/**
 * Should be removed. It's bad design and probably doesn't work with hytale's json configs.
 */
public final class Msgs {

    public static void init() {
        DystellarCore i = DystellarCore.getInstance();
        FLY_NEED_PLUS_RANK = i.getLang().getString("fly-need-plus-rank");
        COMMAND_DENY_INGAME = i.getLang().getString("command-deny-ingame");
        FLY_MODE_ENABLED = i.getLang().getString("fly-mode-enabled");
        FLY_MODE_DISABLED = i.getLang().getString("fly-mode-disabled");
        NO_PERMISSION = i.getLang().getString("no-permission");
        ERROR_PLAYER_NOT_ONLINE = i.getLang().getString("error-player-not-online");
        ADMIN_FLY_MODE_ENABLED_OTHER = i.getLang().getString("admin-fly-mode-enabled-other");
        ADMIN_FLY_MODE_DISABLED_OTHER = i.getLang().getString("admin-fly-mode-disabled-other");
        FLY_MODE_ENABLED_BY_ADMIN = i.getLang().getString("fly-mode-enabled-by-admin");
        FLY_MODE_DISABLED_BY_ADMIN = i.getLang().getString("fly-mode-disabled-by-admin");
        STAFF_FREEZE = i.getLang().getString("staff-message-freeze");
        STAFF_UNFREEZE = i.getLang().getString("staff-message-unfree");
        FRIEND_REQUEST_ACCEPTED_SENDER = i.getLang().getString("friend-request-accepted-sender");
        FRIEND_REQUEST_REJECTED_SENDER = i.getLang().getString("friend-request-rejected-sender");
        ON_COOLDOWN = i.getLang().getString("on-cooldown-message");
        PLAYER_NOT_ON_FRIENDS_LIST = i.getLang().getString("player-not-on-friends-list");
        FRIEND_REMOVED_SENDER = i.getLang().getString("friend-removed-sender");
        FRIEND_REMOVED_RECEIVER = i.getLang().getString("friend-removed-receiver");
        ERROR_PLAYER_NOT_FOUND = i.getLang().getString("error-player-not-found");
        FIND_SAME_SERVER_AS_SENDER = i.getLang().getString("find-same-server-as-sender");
        FRIEND_REQUEST_EXPIRED = i.getLang().getString("friend-request-expired");
        FRIEND_REQUEST_ACCEPTED_RECEIVER = i.getLang().getString("friend-request-accepted-receiver");
        FRIEND_REQUEST_REJECTED_RECEIVER = i.getLang().getString("friend-request-rejected-receiver");
        FRIEND_REQUESTS_ENABLED = i.getLang().getString("friend-requests-enabled");
        FRIEND_REQUESTS_DISABLED = i.getLang().getString("friend-requests-disabled");
        ERROR_PLAYER_DOES_NOT_EXIST = i.getLang().getString("error-player-does-not-exist");
        PLAYER_BLOCKED = i.getLang().getString("player-blocked");
        ERROR_PLAYER_ALREADY_BLOCKED = i.getLang().getString("error-player-already-blocked");
        BLACKLIST_EMPTY = i.getLang().getString("blacklist-empty");
        BLACKLIST_PLAYER_REMOVED = i.getLang().getString("blacklist-player-removed");
        ERROR_NOT_ON_BLACKLIST = i.getLang().getString("error-player-not-on-blacklist");
        ERROR_INPUT_NOT_NUMBER = i.getLang().getString("error-input-not-number");
        ERROR_NOT_A_PLAYER = i.getLang().getString("error-not-a-player");
        SERVER_CONNECTION_ERROR = i.getLang().getString("server-connection-error");
        CANT_SEND_PMS_DISABLED = i.getLang().getString("cant-send-pms-disabled");
        PLAYER_IN_DND = i.getLang().getString("player-in-dnd-mode");
        PLAYER_HAS_BLOCKED_YOU = i.getLang().getString("error-you-are-blocked");
        ERROR_PLAYER_NO_LONGER_ONLINE = i.getLang().getString("error-player-no-longer-online");
        ERROR_NO_REPLY_CACHE = i.getLang().getString("error-no-reply-cache");
        ERROR_PREFIX_NOT_OWNED = i.getLang().getString("prefix-not-owned");
        GLOBAL_CHAT_ENABLED = i.getLang().getString("global-chat-enabled");
        GLOBAL_CHAT_DISABLED = i.getLang().getString("global-chat-disabled");
        GLOBAL_TAB_ENABLED = i.getLang().getString("global-tab-completion-enabled");
        GLOBAL_TAB_DISABLED = i.getLang().getString("global-tab-completion-disabled");
        PMS_ENABLED = i.getLang().getString("pms-enabled");
        PMS_ENABLED_WITH_BLACK_LIST = i.getLang().getString("pms-enabled-with-black-list");
        PMS_ENABLED_FRIENDS_ONLY = i.getLang().getString("pms-enabled-friends-only");
        PMS_DISABLED = i.getLang().getString("pms-disabled");
        BLOCKING_POINTLESS_HINT = i.getLang().getString("blocking-pointless-hint");
    }

    public static String PMS_ENABLED;
    public static String PMS_ENABLED_WITH_BLACK_LIST;
    public static String PMS_ENABLED_FRIENDS_ONLY;
    public static String PMS_DISABLED;

    // General
    public static String COMMAND_DENY_INGAME;
    public static String NO_PERMISSION;
    public static String ERROR_PLAYER_NOT_ONLINE;
    public static String ERROR_PLAYER_NO_LONGER_ONLINE;
    public static String ERROR_PLAYER_NOT_FOUND;
    public static String ERROR_PLAYER_DOES_NOT_EXIST;
    public static String ON_COOLDOWN;
    public static String ERROR_INPUT_NOT_NUMBER;
    public static String ERROR_NOT_A_PLAYER;
    public static String SERVER_CONNECTION_ERROR;
    public static String ERROR_PREFIX_NOT_OWNED;
    public static String GLOBAL_CHAT_ENABLED;
    public static String GLOBAL_CHAT_DISABLED;
    public static String GLOBAL_TAB_ENABLED;
    public static String GLOBAL_TAB_DISABLED;

    // Fly command
    public static String FLY_NEED_PLUS_RANK;
    public static String FLY_MODE_ENABLED;
    public static String FLY_MODE_DISABLED;
    public static String FLY_MODE_ENABLED_BY_ADMIN;
    public static String FLY_MODE_DISABLED_BY_ADMIN;
    public static String ADMIN_FLY_MODE_ENABLED_OTHER;
    public static String ADMIN_FLY_MODE_DISABLED_OTHER;

    // Freeze command
    public static String STAFF_FREEZE;
    public static String STAFF_UNFREEZE;

    // Friend command
    public static String FRIEND_REQUEST_ACCEPTED_SENDER;
    public static String FRIEND_REQUEST_REJECTED_SENDER;
    public static String FRIEND_REQUEST_ACCEPTED_RECEIVER;
    public static String FRIEND_REQUEST_REJECTED_RECEIVER;
    public static String PLAYER_NOT_ON_FRIENDS_LIST;
    public static String FRIEND_REMOVED_SENDER;
    public static String FRIEND_REMOVED_RECEIVER;
    public static String FIND_SAME_SERVER_AS_SENDER;
    public static String FRIEND_REQUEST_EXPIRED;
    public static String FRIEND_REQUESTS_ENABLED;
    public static String FRIEND_REQUESTS_DISABLED;

    // Block/Ignore command
    public static String PLAYER_BLOCKED;
    public static String ERROR_PLAYER_ALREADY_BLOCKED;

    // Blacklist/Ignorelist command
    public static String BLACKLIST_EMPTY;
    public static String BLACKLIST_PLAYER_REMOVED;
    public static String ERROR_NOT_ON_BLACKLIST;
    public static String BLOCKING_POINTLESS_HINT;

    // Msg command
    public static String CANT_SEND_PMS_DISABLED;
    public static String PLAYER_IN_DND;
    public static String PLAYER_HAS_BLOCKED_YOU;
    public static String ERROR_NO_REPLY_CACHE;
}
