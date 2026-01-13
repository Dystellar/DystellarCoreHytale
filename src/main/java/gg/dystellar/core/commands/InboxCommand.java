package gg.dystellar.core.commands;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Msgs;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.inbox.Inbox;
import net.zylesh.dystellarcore.core.inbox.senders.CoinsReward;
import net.zylesh.dystellarcore.core.inbox.senders.EloGainNotifier;
import net.zylesh.dystellarcore.core.inbox.senders.Message;
import net.zylesh.dystellarcore.core.inbox.senders.prewards.PKillEffectReward;
import net.zylesh.dystellarcore.core.punishments.SenderContainer;
import net.zylesh.dystellarcore.serialization.InboxSerialization;
import net.zylesh.dystellarcore.serialization.MariaDB;
import net.zylesh.practice.PKillEffect;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class InboxCommand implements CommandExecutor, Listener {

    private static final ItemStack SEND = new ItemStack(Material.WOOL, 1, (short) 5);
    private static final ItemStack DELETE = new ItemStack(Material.WOOL, 1, (short) 14);
    private static final ItemStack BACK = new ItemStack(Material.WOOL, 1, (short) 15);
    private static final ItemStack CREATE = new ItemStack(Material.WOOL, 1, (short) 5);
    static {
        ItemMeta metaSend = SEND.getItemMeta();
        metaSend.setDisplayName(ChatColor.GREEN + "Send");
        SEND.setItemMeta(metaSend);
        ItemMeta metaDelete = DELETE.getItemMeta();
        metaDelete.setDisplayName(ChatColor.RED + "Delete");
        DELETE.setItemMeta(metaDelete);
        ItemMeta metaBack = BACK.getItemMeta();
        metaBack.setDisplayName(ChatColor.RED + "Go Back");
        BACK.setItemMeta(metaBack);
        ItemMeta metaCreate = CREATE.getItemMeta();
        metaCreate.setDisplayName(ChatColor.GREEN + "Create New");
        CREATE.setItemMeta(metaCreate);
    }

    private static InboxCommand INSTANCE;

    public static InboxCommand get() {
        return INSTANCE;
    }

    private SenderContainer selected = null;

    private final Inventory menu = Bukkit.createInventory(null, 9, ChatColor.DARK_AQUA + "Menu");
    private final Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_AQUA + "Inbox Senders");
    private volatile SenderContainer[] containers;

    public InboxCommand() {
        Bukkit.getPluginCommand("inbox").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
        INSTANCE = this;
        inv.setContents(Inbox.LAYOUT);
        for (int i = 0; i < menu.getSize(); i++) {
            switch (i) {
                case 3: menu.setItem(i, SEND); break;
                case 5: menu.setItem(i, DELETE); break;
                case 8: menu.setItem(i, BACK); break;
                default: menu.setItem(i, DystellarCore.NULL_GLASS); break;
            }
        }
        init();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return true;
        Player p = (Player) commandSender;
        if (commandSender.hasPermission("dystellar.admin.inbox") && strings.length > 0 && strings[0].equalsIgnoreCase("manage")) {
            p.openInventory(inv);
        } else {
            User user = User.get(p);
            user.getInbox().open();
            return true;
        }
        return true;
    }

    @EventHandler
    public void onInv(InventoryClickEvent e) {
        if (e.getClickedInventory().equals(inv)) {
            if (selected != null) {
                ((Player) e.getWhoClicked()).sendMessage(ChatColor.RED + "Someone else is performing actions. Please wait.");
                return;
            }
            e.setCancelled(true);
            ItemStack i = e.getCurrentItem();
            if (i == null || i.getType().equals(Material.AIR) || i.equals(DystellarCore.NULL_GLASS)) return;
            if (i.equals(CREATE)) {
                if (creating != null) {
                    ((Player) e.getWhoClicked()).sendMessage(ChatColor.RED + "Someone else is creating a sender. Please wait.");
                    return;
                }
                this.creating = e.getWhoClicked().getUniqueId();
                this.selectingType = true;
                ((Player) e.getWhoClicked()).sendMessage(selectTypeMessage);
                e.getWhoClicked().closeInventory();
            } else {
                selected = containers[e.getSlot() - 9];
                e.getWhoClicked().openInventory(menu);
            }
        } else if (e.getClickedInventory().equals(menu)) {
            e.setCancelled(true);
            ItemStack i = e.getCurrentItem();
            Player p = (Player) e.getWhoClicked();
            if (i == null || i.getType().equals(Material.AIR) || i.equals(DystellarCore.NULL_GLASS)) return;
            if (selected == null) {
                p.sendMessage(ChatColor.RED + "An error ocurred trying to perform this action... Seems like you don't have a sender selected.");
                p.closeInventory();
                return;
            }
            if (i.equals(SEND)) {
                sending = true;
                p.sendMessage(ChatColor.DARK_AQUA + "Type the player's name you want to send this mail to. Write whitespaces on the message if you want to cancel.");
            } else if (i.equals(DELETE)) {
                // TODO
            } else if (i.equals(BACK)) {
                p.closeInventory();
                deselect();
                p.openInventory(inv);
            }
        }
    }

    private static final String[] selectTypeMessage = new String[] {
            ChatColor.AQUA + "Type in the chat the type of the sender",
            ChatColor.DARK_AQUA + "Available types (input must be a number):",
            PKillEffectReward.ID + " - Practice kill effect reward.",
            CoinsReward.ID + " - Global coins reward.",
            EloGainNotifier.ID + " - Elo compensation, compatibility type must be specified later.",
            Message.ID + " - Send just an inbox message."
    };

    private volatile UUID creating;
    private volatile boolean selectingType = false;
    private volatile byte type;
    private volatile StringBuffer message = new StringBuffer();
    private volatile byte compatibility;
    private volatile boolean settingCompatibility = false;
    private volatile boolean writing = false;
    private volatile String from;
    private volatile String title;
    private volatile int elo;
    private volatile boolean settingElo = false;
    private volatile int coins;
    private volatile boolean settingCoins = false;
    private volatile PKillEffect killEffect;
    private volatile String ladder;
    private volatile boolean sending;

    private void deselect() {
        creating = null;
        selectingType = false;
        type = 0;
        message = new StringBuffer();
        compatibility = 0;
        settingCompatibility = false;
        writing = false;
        from = null;
        title = null;
        elo = 0;
        settingElo = false;
        coins = 0;
        settingCoins = false;
        killEffect = null;
        ladder = null;
        sending = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void chat(AsyncPlayerChatEvent e) {
        if (e.getPlayer().getUniqueId().equals(creating)) {
            e.setCancelled(true);
            if (selectingType) {
                if (!e.getMessage().matches("[0-9]+")) {
                    e.getPlayer().sendMessage(Msgs.ERROR_INPUT_NOT_NUMBER);
                    return;
                }
                byte type = Byte.parseByte(e.getMessage());
                switch (type) {
                    case PKillEffectReward.ID: {
                        this.type = type;
                        e.getPlayer().sendMessage(ChatColor.GREEN + "Practice kill effect reward selected!");
                        e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Now specify also in chat the sender's source. You can put your name or someone/something you are representing (e. g. Administration Team).");
                        selectingType = false;
                        break;
                    }
                    case CoinsReward.ID: {
                        this.type = type;
                        e.getPlayer().sendMessage(ChatColor.GREEN + "Global coins reward selected!");
                        e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Now specify also in chat the sender's source. You can put your name or someone/something you are representing (e. g. Administration Team).");
                        selectingType = false;
                        break;
                    }
                    case EloGainNotifier.ID: {
                        this.type = type;
                        e.getPlayer().sendMessage(ChatColor.GREEN + "Elo compensation reward selected!");
                        e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Now specify also in chat the sender's source. You can put your name or someone/something you are representing (e. g. Administration Team).");
                        selectingType = false;
                        break;
                    }
                    case Message.ID: {
                        this.type = type;
                        e.getPlayer().sendMessage(ChatColor.GREEN + "Just a message/announcement selected!");
                        e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Now specify also in chat the sender's source. You can put your name or someone/something you are representing (e. g. Administration Team).");
                        selectingType = false;
                        break;
                    }
                    default: {
                        e.getPlayer().sendMessage(ChatColor.RED + "The type specified does not exist, try again.");
                        break;
                    }
                }
            } else {
                if (from == null) {
                    from = e.getMessage();
                    e.getPlayer().sendMessage(ChatColor.GREEN + "Source specified!");
                    e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Now write the message ->");
                    e.getPlayer().sendMessage(ChatColor.YELLOW + "Pro Tip: if the message is large and the chat box does not allow to write that much, you can end the chat message with %cont% and continue writing in another chat message.");
                    writing = true;
                } else if (writing) {
                    message.append(e.getMessage().replaceAll("%cont%", ""));
                    if (e.getMessage().endsWith("%cont%")) {
                        e.getPlayer().sendMessage(ChatColor.GREEN + "Nice! You may continue your message.");
                        return;
                    }
                    e.getPlayer().sendMessage(ChatColor.GREEN + "Message specified");
                    writing = false;
                    switch (type) {
                        case PKillEffectReward.ID:
                        case CoinsReward.ID: {
                            e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Now specify the title of the reward.");
                            break;
                        }
                        case EloGainNotifier.ID: {
                            e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Now specify the compatibility type ->");
                            e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Compatibility types available (must be a number):");
                            e.getPlayer().sendMessage(EloGainNotifier.PRACTICE + " - Practice API compatibility.");
                            e.getPlayer().sendMessage(EloGainNotifier.SKYWARS + " - Skywars API compatibility.");
                            settingCompatibility = true;
                            break;
                        }
                        case Message.ID: {
                            Message message1 = new Message(null, from, delimitateLines(message.toString()));
                            SenderContainer container = new SenderContainer(message1);
                            MariaDB.saveSenderContainer(container);
                            init();
                            e.getPlayer().sendMessage(ChatColor.GREEN + "Sender successfully created!");
                            deselect();
                            break;
                        }
                    }
                } else {
                    switch (type) {
                        case PKillEffectReward.ID: {
                            if (title == null) {
                                title = e.getMessage();
                                e.getPlayer().sendMessage(ChatColor.GREEN + "Title set!");
                                e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Now you must set kill efect enum ->");
                                e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Available enums:");
                                e.getPlayer().sendMessage(Arrays.toString(PKillEffect.values()));
                            } else if (killEffect == null) {
                                if (Arrays.stream(PKillEffect.values()).noneMatch(pKillEffect -> pKillEffect.name().equals(e.getMessage().toUpperCase()))) {
                                    e.getPlayer().sendMessage(ChatColor.RED + "That enum does not exist.");
                                    e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Available enums:");
                                    break;
                                }
                                killEffect = PKillEffect.valueOf(e.getMessage().toUpperCase());
                                PKillEffectReward reward = new PKillEffectReward(null, title, from, killEffect, delimitateLines(message.toString()));
                                SenderContainer container = new SenderContainer(reward);
                                MariaDB.saveSenderContainer(container);
                                init();
                                e.getPlayer().sendMessage(ChatColor.GREEN + "Sender successfully created!");
                                deselect();
                            }
                            break;
                        }
                        case CoinsReward.ID: {
                            if (title == null) {
                                title = e.getMessage();
                                e.getPlayer().sendMessage(ChatColor.GREEN + "Title set!");
                                e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Now you must set the coins amount ->");
                                e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Type a number.");
                                settingCoins = true;
                            } else if (settingCoins) {
                                if (!e.getMessage().matches("[0-9]+")) {
                                    e.getPlayer().sendMessage(ChatColor.RED + "Input must be a number.");
                                    break;
                                }
                                coins = Integer.parseInt(e.getMessage());
                                CoinsReward reward = new CoinsReward(null, coins, title, from, delimitateLines(message.toString()));
                                SenderContainer container = new SenderContainer(reward);
                                MariaDB.saveSenderContainer(container);
                                init();
                                e.getPlayer().sendMessage(ChatColor.GREEN + "Sender successfully created!");
                                deselect();
                            }
                            break;
                        }
                        case EloGainNotifier.ID: {
                            if (settingCompatibility) {
                                if (!e.getMessage().matches("[0-9]+")) {
                                    e.getPlayer().sendMessage(ChatColor.RED + "Input must be a number.");
                                    break;
                                }
                                compatibility = Byte.parseByte(e.getMessage());
                                if (compatibility == EloGainNotifier.PRACTICE || compatibility == EloGainNotifier.SKYWARS) {
                                    e.getPlayer().sendMessage(ChatColor.GREEN + "Compatibility set!");
                                    e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Now you must set the elo amount to give (must be a number) ->");
                                    settingElo = true;
                                    settingCompatibility = false;
                                } else {
                                    e.getPlayer().sendMessage(ChatColor.RED + "This compatibility is not valid.");
                                    e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Compatibility types available (must be a number):");
                                    e.getPlayer().sendMessage(EloGainNotifier.PRACTICE + " - Practice API compatibility.");
                                    e.getPlayer().sendMessage(EloGainNotifier.SKYWARS + " - Skywars API compatibility.");
                                    break;
                                }
                            } else if (settingElo) {
                                if (!e.getMessage().matches("[0-9]+")) {
                                    e.getPlayer().sendMessage(ChatColor.RED + "Input must be a number.");
                                    break;
                                }
                                elo = Integer.parseInt(e.getMessage());
                                e.getPlayer().sendMessage(ChatColor.GREEN + "Elo set!");
                                if (compatibility == EloGainNotifier.PRACTICE) {
                                    e.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Since you have selected Practice compatibility, you must set the ladder the elo will be added on.");
                                    e.getPlayer().sendMessage(ChatColor.YELLOW + "Since this is not practice plugin there is no way to check if the information provided here is correct, so please check practice's server config for ladders list if unsure.");
                                    settingElo = false;
                                } else {
                                    EloGainNotifier reward = new EloGainNotifier(null, elo, compatibility, null, from, delimitateLines(message.toString()));
                                    SenderContainer container = new SenderContainer(reward);
                                    MariaDB.saveSenderContainer(container);
                                    init();
                                    e.getPlayer().sendMessage(ChatColor.GREEN + "Sender successfully created!");
                                    deselect();
                                }
                            } else if (compatibility == EloGainNotifier.PRACTICE && ladder == null) {
                                ladder = e.getMessage();
                                e.getPlayer().sendMessage(ChatColor.GREEN + "Ladder set!");
                                EloGainNotifier reward = new EloGainNotifier(null, elo, compatibility, ladder, from, delimitateLines(message.toString()));
                                SenderContainer container = new SenderContainer(reward);
                                MariaDB.saveSenderContainer(container);
                                init();
                                e.getPlayer().sendMessage(ChatColor.GREEN + "Sender successfully created!");
                                deselect();
                            }
                            break;
                        }
                        case Message.ID: {
                            Message message1 = new Message(null, from, delimitateLines(message.toString()));
                            SenderContainer container = new SenderContainer(message1);
                            MariaDB.saveSenderContainer(container);
                            init();
                            e.getPlayer().sendMessage(ChatColor.GREEN + "Sender successfully created!");
                            deselect();
                            break;
                        }
                    }
                }
            }
        } else if (sending) {
            e.setCancelled(true);
            if (e.getMessage().contains(" ")) {
                e.getPlayer().sendMessage(ChatColor.RED + "Cancelled.");
                return;
            }
            String name = e.getMessage();
            Player pInt = Bukkit.getPlayer(name);
            if (pInt != null && pInt.isOnline()) {
                User u = User.get(e.getPlayer());
                u.getInbox().addSender(selected.getSender().clone(u.getInbox()));
                e.getPlayer().sendMessage(ChatColor.GREEN + "Sender successfully sent!");
                deselect();
            } else {
                DystellarCore.getInstance().sendPluginMessage(e.getPlayer(), DystellarCore.INBOX_SEND, name, InboxSerialization.senderToString(selected.getSender(), selected.getSender().getSerialID()));
            }
        }
    }

    public static String[] delimitateLines(String s) {
        StringBuilder builder = new StringBuilder();
        boolean splitRequired = false;
        int x = 0;
        for (int i = 0; i < s.length(); i++) {
            if (x >= 20) splitRequired = true;
            char c = s.charAt(i);
            if (splitRequired && c == ' ') {
                builder.append("-;-");
                x = 0;
                splitRequired = false;
                continue;
            }
            builder.append(c);
            if (!splitRequired) x++;
        }
        return builder.toString().split("-;-");
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().equals(inv) || e.getInventory().equals(menu)) e.setCancelled(true);
    }

    public void init() {
        DystellarCore.getAsyncManager().submit(() -> {
            containers = MariaDB.loadSenderContainers();
            synchronized (inv) {
                int i = 0;
                for (; i < containers.length; i++) {
                    if (i + 9 >= inv.getSize() - 9) break;
                    inv.setItem(i + 9, containers[i].getIcon());
                }
                if ((i + 9) < (inv.getSize() - 9)) {
                    for (; (i + 9) < (inv.getSize() - 9); i++) {
                        inv.setItem(i + 9, null);
                    }
                }
                inv.setItem(53, CREATE);
            }
        });
    }
}
