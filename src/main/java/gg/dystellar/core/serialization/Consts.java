package net.zylesh.dystellarcore.serialization;

import org.bukkit.ChatColor;

public class Consts {

    public static final byte BYTE_FALSE = 0;
    public static final byte BYTE_TRUE = 1;

    /*
     * Format ->
     * byte: Position within array
     * String: Tip message
     */

    public static final byte FIRST_FRIEND_TIP_POS = 0;
    public static final String[] FIRST_FRIEND_TIP_MSG = {
            " ",
            ChatColor.DARK_GREEN + "Tip " + ChatColor.WHITE + "-> " + ChatColor.GRAY + "Use \"" + ChatColor.YELLOW + "/f find <friend>" + ChatColor.GRAY + "\" to know where your friend is playing within the network.",
            " "
    };

    public static final byte EXTRA_OPTION_FRIEND_REQUESTS_ENABLED_POS = 1;

    public static final byte EXTRA_OPTION_RESOURCEPACK_PROMPT_POS = 2;
}
