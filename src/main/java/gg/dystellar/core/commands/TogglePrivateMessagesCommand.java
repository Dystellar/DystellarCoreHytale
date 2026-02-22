package gg.dystellar.core.commands;

/**
 * This lets you toggle private messages mode.
 */
public class TogglePrivateMessagesCommand implements CommandExecutor {

    public TogglePrivateMessagesCommand() {
        Bukkit.getPluginCommand("toggleprivatemessages").setExecutor(this);
        Bukkit.getPluginCommand("togglemessages").setExecutor(this);
        Bukkit.getPluginCommand("tpm").setExecutor(this);
        Bukkit.getPluginCommand("pms").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            User playerUser = User.get(player);
            playerUser.togglePms();
            switch (playerUser.getPrivateMessagesMode()) {
                case PMS_ENABLED:
                    player.sendMessage(Msgs.PMS_ENABLED);
                    break;
                case PMS_ENABLED_WITH_IGNORELIST:
                    player.sendMessage(Msgs.PMS_ENABLED_WITH_BLACK_LIST);
                    break;
                case PMS_ENABLED_FRIENDS_ONLY:
                    player.sendMessage(Msgs.PMS_ENABLED_FRIENDS_ONLY);
                    break;
                case PMS_DISABLED:
                    player.sendMessage(Msgs.PMS_DISABLED);
                    break;
            }
        } else {
            commandSender.sendMessage(Msgs.ERROR_NOT_A_PLAYER);
        }
        return true;
    }
}
