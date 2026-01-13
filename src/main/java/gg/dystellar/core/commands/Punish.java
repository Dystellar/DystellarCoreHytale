package net.zylesh.dystellarcore.commands;

import me.clip.placeholderapi.PlaceholderAPI;
import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Msgs;
import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.punishments.*;
import net.zylesh.dystellarcore.utils.Utils;
import net.zylesh.practice.PParty;
import net.zylesh.practice.PUser;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import gg.zylesh.skywars.SkywarsAPI;
import gg.zylesh.skywars.common.Team;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static net.zylesh.practice.PUser.ENABLED;
import static net.zylesh.practice.PUser.ENABLED_CHAT_ONLY;

public class Punish implements CommandExecutor, Listener {

    // Blacklists
    public static final Blacklist BLACKLIST_10_WARNS = new Blacklist("Don't say we didn't warn you. LOL");
    private static final Blacklist BLACKLIST_INMEDIATE = new Blacklist("Hope we don't see you again.");
    private static final Blacklist THREATENING = new Blacklist("Threatening");

    // Bans
    public static final Callable<Ban> BAN_3_WARNS = () -> new Ban(LocalDateTime.now().plusDays(7L), "Receiving 3 Warnings");
    public static final Callable<Ban> BAN_5_WARNS = () -> new Ban(LocalDateTime.now().plusDays(30L), "Receiving 5 Warnings");
    public static final Callable<Ban> BAN_7_WARNS = () -> new Ban(LocalDateTime.now().plusDays(90L), "Receiving 7 Warnings");
    private static final Callable<Ban> CHEATING = () -> new Ban(LocalDateTime.now().plusDays(60L), "Cheating");
    private static final Callable<Ban> CHEATING_ADMIT = () -> new Ban(LocalDateTime.now().plusDays(30L), "Cheating (Admit)");

    // Ranked bans
    private static final RankedBan RANKED_CHEATING = new RankedBan(null, "Cheating in Ranked");
    private static final Callable<RankedBan> RANKED_ANNOYING_PLAYSTYLE = () -> new RankedBan(LocalDateTime.now().plusDays(30L), "Annoying Playstyle in Ranked");
    private static final Callable<RankedBan> RANKED_TOXIC_BEHAVIOR = () -> new RankedBan(LocalDateTime.now().plusDays(7L), "Toxic Behavior in Ranked");

    // Mutes
    private static final Callable<Mute> MUTE_LESS_TOXIC_BEHAVIOR = () -> new Mute(LocalDateTime.now().plusMinutes(5L), "Toxic Behavior");
    private static final Callable<Mute> MUTE_TOXIC_BEHAVIOR = () -> new Mute(LocalDateTime.now().plusMinutes(30L), "Toxic Behavior");
    private static final Callable<Mute> MUTE_SEVER_TOXIC_BEHAVIOR = () -> new Mute(LocalDateTime.now().plusHours(12L), "Toxic Behavior");
    private static final Callable<Mute> MUTE_FLOOD_SPAM = () -> new Mute(LocalDateTime.now().plusHours(1L), "Excessive Flood/Spam");

    // Warns
    private static final Callable<Warn> WARN_LESS_TOXIC_BEHAVIOR = () -> new Warn(LocalDateTime.now().plusDays(20L), "Toxic Behavior");
    private static final Callable<Warn> WARN_TOXIC_BEHAVIOR = () -> new Warn(LocalDateTime.now().plusDays(30L), "Toxic Behavior");
    private static final Callable<Warn> WARN_SEVER_TOXIC_BEHAVIOR = () -> new Warn(LocalDateTime.now().plusDays(60L), "Toxic Behavior");
    private static final Callable<Warn> WARN_CROSSTEAMING = () -> new Warn(LocalDateTime.now().plusDays(25L), "Teaming / Cross Teaming");
    private static final Callable<Warn> WARN_BUGEXPLOITING = () -> new Warn(LocalDateTime.now().plusDays(25L), "Bug Exploiting");
    private static final Callable<Warn> WARN_TEAMKILL = () -> new Warn(LocalDateTime.now().plusDays(25L), "Killing/Trying to kill teammates");
    private static final Callable<Warn> WARN_ANNOYING_PLAYSTYLE = () -> new Warn(LocalDateTime.now().plusDays(25L), "Annoying Playstyle (excessive camping/running/other)");

    private static final Inventory inv = Bukkit.createInventory(null, 54, ChatColor.RED + "Punishments");

    // TODO: Separate command and listener into modules for readability

    public Punish() {
        Bukkit.getPluginCommand("punish").setExecutor(this);
        initInventory();
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
    }

    @Override
    public boolean onCommand(CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if (!commandSender.hasPermission("dystellar.staff") || !(commandSender instanceof Player)) return true;
        Player p = (Player) commandSender;
        if (strings.length < 1) {
            p.sendMessage(ChatColor.RED + "Usage: /punish <player>");
            return true;
        }
        Player pInt = Bukkit.getPlayer(strings[0]);
        if (pInt == null || !pInt.isOnline()) {
            p.sendMessage(Msgs.ERROR_PLAYER_NOT_ONLINE);
            return true;
        }
        commandCache.put(p.getUniqueId(), pInt);
        p.openInventory(inv);
        return true;
    }

    private static final Map<UUID, Player> commandCache = new HashMap<>();

    @EventHandler
    public void onInvClick(InventoryClickEvent event) {
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) return;
        if (event.getClickedInventory().equals(inv)) {
            event.setCancelled(true);
        } else return;
        ItemStack i = event.getCurrentItem();
        Player p = (Player) event.getWhoClicked();
        Player playerToPunish = commandCache.get(p.getUniqueId());
        if (playerToPunish == null) {
            p.sendMessage(Msgs.ERROR_PLAYER_NO_LONGER_ONLINE);
            return;
        }
        User userToPunish = User.get(playerToPunish);
        try {
            if (i.equals(crossTeaming)) {
                Punishment punishment = WARN_CROSSTEAMING.call();
                userToPunish.punish(punishment);
                commandCache.remove(p.getUniqueId());
                Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> playerToPunish.kickPlayer(ChatColor.RED + punishment.getReason()), 30L);
            } else if (i.equals(bugExploiting)) {
                Punishment punishment = WARN_BUGEXPLOITING.call();
                userToPunish.punish(punishment);
                commandCache.remove(p.getUniqueId());
                Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> playerToPunish.kickPlayer(ChatColor.RED + punishment.getReason()), 30L);
            } else if (i.equals(teamKill)) {
                Punishment punishment = WARN_TEAMKILL.call();
                userToPunish.punish(punishment);
                commandCache.remove(p.getUniqueId());
                Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> playerToPunish.kickPlayer(ChatColor.RED + punishment.getReason()), 30L);
            } else if (i.equals(annoyingPlaystyle)) {
                Punishment punishment = WARN_ANNOYING_PLAYSTYLE.call();
                userToPunish.punish(punishment);
                commandCache.remove(p.getUniqueId());
                Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> playerToPunish.kickPlayer(ChatColor.RED + punishment.getReason()), 30L);
            } else if (i.equals(customWarn)) {
                cache.put(event.getWhoClicked().getUniqueId(), customWarn);
                event.getWhoClicked().closeInventory();
                ((Player) event.getWhoClicked()).sendMessage(ChatColor.YELLOW + "Type the reason in chat, or type 'cancel' to cancel.");
            } else if (i.equals(lessToxic)) {
                Punishment punishment = MUTE_LESS_TOXIC_BEHAVIOR.call();
                userToPunish.punish(WARN_LESS_TOXIC_BEHAVIOR.call());
                userToPunish.punish(punishment);
                commandCache.remove(p.getUniqueId());
            } else if (i.equals(toxic)) {
                Punishment punishment = MUTE_TOXIC_BEHAVIOR.call();
                userToPunish.punish(WARN_TOXIC_BEHAVIOR.call());
                userToPunish.punish(punishment);
                commandCache.remove(p.getUniqueId());
            } else if (i.equals(severToxic)) {
                Punishment punishment = MUTE_SEVER_TOXIC_BEHAVIOR.call();
                userToPunish.punish(WARN_SEVER_TOXIC_BEHAVIOR.call());
                userToPunish.punish(punishment);
                commandCache.remove(p.getUniqueId());
            } else if (i.equals(floodSpam)) {
                Punishment punishment = MUTE_FLOOD_SPAM.call();
                userToPunish.punish(punishment);
                commandCache.remove(p.getUniqueId());
            } else if (i.equals(obvCheating) || i.equals(cheatingFoundInSS) || i.equals(refuseSS)) {
                Punishment punishment = CHEATING.call();
                userToPunish.punish(punishment);
                commandCache.remove(p.getUniqueId());
            } else if (i.equals(admitCheating)) {
                Punishment punishment = CHEATING_ADMIT.call();
                userToPunish.punish(punishment);
                commandCache.remove(p.getUniqueId());
            } else if (i.equals(rankedCheating)) {
                Punishment punishment = CHEATING.call();
                userToPunish.punish(RANKED_CHEATING);
                userToPunish.punish(punishment);
                commandCache.remove(p.getUniqueId());
            } else if (i.equals(rankedAnnoying)) {
                Punishment punishment = RANKED_ANNOYING_PLAYSTYLE.call();
                Punishment punishment1 = WARN_ANNOYING_PLAYSTYLE.call();
                userToPunish.punish(punishment);
                userToPunish.punish(punishment1);
                commandCache.remove(p.getUniqueId());
                Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> playerToPunish.kickPlayer(ChatColor.RED + punishment1.getReason()), 30L);
            } else if (i.equals(rankedToxic)) {
                Punishment punishment = RANKED_TOXIC_BEHAVIOR.call();
                userToPunish.punish(MUTE_TOXIC_BEHAVIOR.call());
                userToPunish.punish(WARN_TOXIC_BEHAVIOR.call());
                userToPunish.punish(punishment);
                commandCache.remove(p.getUniqueId());
            } else if (i.equals(emergencyBlacklist)) {
                userToPunish.punish(BLACKLIST_INMEDIATE);
                commandCache.remove(p.getUniqueId());
            } else if (i.equals(threatening)) {
                userToPunish.punish(THREATENING);
                commandCache.remove(p.getUniqueId());
            } else if (i.equals(blacklistCustom)) {
                cache.put(event.getWhoClicked().getUniqueId(), blacklistCustom);
                event.getWhoClicked().closeInventory();
                ((Player) event.getWhoClicked()).sendMessage(ChatColor.YELLOW + "Type the reason in chat, or type 'cancel' to cancel.");
            } else if (i.equals(note)) {
                cache.put(event.getWhoClicked().getUniqueId(), note);
                event.getWhoClicked().closeInventory();
                ((Player) event.getWhoClicked()).sendMessage(ChatColor.YELLOW + "Type the reason in chat, or type 'cancel' to cancel.");
            }
        } catch (Exception ignored) {}
    }

    private static final Map<UUID, ItemStack> cache = new HashMap<>();

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(inv)) event.setCancelled(true);
    }

    private static ItemStack crossTeaming;
    private static ItemStack bugExploiting;
    private static ItemStack teamKill;
    private static ItemStack annoyingPlaystyle;
    private static ItemStack customWarn;
    private static ItemStack lessToxic;
    private static ItemStack toxic;
    private static ItemStack severToxic;
    private static ItemStack floodSpam;
    private static ItemStack obvCheating;
    private static ItemStack cheatingFoundInSS;
    private static ItemStack admitCheating;
    private static ItemStack refuseSS;
    private static ItemStack rankedCheating;
    private static ItemStack rankedAnnoying;
    private static ItemStack rankedToxic;
    private static ItemStack emergencyBlacklist;
    private static ItemStack threatening;
    private static ItemStack blacklistCustom;
    private static ItemStack note;

    private void initInventory() {
        ItemStack warns = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 4);
        ItemMeta warnsMeta = warns.getItemMeta();
        warnsMeta.setDisplayName(ChatColor.RED + "Warns");
        warns.setItemMeta(warnsMeta);
        crossTeaming = new ItemStack(Material.WOOL, 1, (short) 4);
        ItemMeta crossMeta = crossTeaming.getItemMeta();
        crossMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Teaming/Cross Teaming");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_AQUA + "Teaming for non strategical playstyle.");
        lore.add(ChatColor.RED + "Warning! " + ChatColor.DARK_AQUA + "Teaming temporarily for strategical reasons");
        lore.add(ChatColor.DARK_AQUA + "without being too obvious or discriminatory is allowed!");
        lore.add(ChatColor.DARK_AQUA + "Also this rule doesn't apply for party events and friendly games.");
        crossMeta.setLore(lore);
        crossTeaming.setItemMeta(crossMeta);
        bugExploiting = new ItemStack(Material.WOOL, 1, (short) 4);
        ItemMeta bugMeta = bugExploiting.getItemMeta();
        bugMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Bug Exploiting");
        List<String> lore0 = new ArrayList<>();
        lore0.add(ChatColor.DARK_AQUA + "Abuse of server bugs for own profit.");
        bugMeta.setLore(lore0);
        bugExploiting.setItemMeta(bugMeta);
        teamKill = new ItemStack(Material.WOOL, 1, (short) 4);
        ItemMeta teamMeta = teamKill.getItemMeta();
        teamMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Team Members Killing");
        List<String> lore1 = new ArrayList<>();
        lore1.add(ChatColor.DARK_AQUA + "Player trying to kill their teammates (by breaking floor blocks,");
        lore1.add(ChatColor.DARK_AQUA + "using lava or making them die on purpose.");
        teamMeta.setLore(lore1);
        teamKill.setItemMeta(teamMeta);
        annoyingPlaystyle = new ItemStack(Material.WOOL, 1, (short) 4);
        ItemMeta annMeta = annoyingPlaystyle.getItemMeta();
        annMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Annoying Playstyle");
        List<String> lore2 = new ArrayList<>();
        lore2.add(ChatColor.DARK_AQUA + "Excessive running, camping and other types");
        lore2.add(ChatColor.DARK_AQUA + "of discouraged/exhaustive playstyle.");
        annMeta.setLore(lore2);
        annoyingPlaystyle.setItemMeta(annMeta);
        customWarn = new ItemStack(Material.WOOL, 1, (short) 4);
        ItemMeta cwMeta = customWarn.getItemMeta();
        cwMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Custom Warn");
        List<String> lore3 = new ArrayList<>();
        lore3.add(ChatColor.DARK_AQUA + "Warn for custom reason (specify in chat).");
        cwMeta.setLore(lore3);
        customWarn.setItemMeta(cwMeta);

        ItemStack mutes = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 1);
        ItemMeta mutesMeta = mutes.getItemMeta();
        mutesMeta.setDisplayName(ChatColor.RED + "Mutes");
        mutes.setItemMeta(mutesMeta);
        lessToxic = new ItemStack(Material.WOOL, 1, (short) 1);
        ItemMeta lessToxicMeta = lessToxic.getItemMeta();
        lessToxicMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Toxic Behavior (Less Severe)");
        List<String> lore4 = new ArrayList<>();
        lore4.add(ChatColor.DARK_AQUA + "Bad game modals expressions like 'L', 'eZ',...");
        lore4.add(ChatColor.DARK_AQUA + "This rule is not exhaustive.");
        lessToxicMeta.setLore(lore4);
        lessToxic.setItemMeta(lessToxicMeta);
        toxic = new ItemStack(Material.WOOL, 1, (short) 1);
        ItemMeta toxicMeta = toxic.getItemMeta();
        toxicMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Toxic Behavior");
        List<String> lore5 = new ArrayList<>();
        lore5.add(ChatColor.DARK_AQUA + "Being toxic to players using expressions like:");
        lore5.add(ChatColor.DARK_AQUA + "idiot, a**hole and other personal insults.");
        lore5.add(ChatColor.DARK_AQUA + "This rule is not exhaustive.");
        toxicMeta.setLore(lore5);
        toxic.setItemMeta(toxicMeta);
        severToxic = new ItemStack(Material.WOOL, 1, (short) 1);
        ItemMeta severMeta = severToxic.getItemMeta();
        severMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Toxic Behavior (Severe)");
        List<String> lore6 = new ArrayList<>();
        lore6.add(ChatColor.DARK_AQUA + "Extreme toxic behavior, using expressions like:");
        lore6.add(ChatColor.DARK_AQUA + "Kill yourself, I'm gonna dox you, I'm gonna kill you irl, I hope you die, etc.");
        lore6.add(ChatColor.DARK_AQUA + "This rule IS INDEED exhaustive.");
        severMeta.setLore(lore6);
        severToxic.setItemMeta(severMeta);
        floodSpam = new ItemStack(Material.WOOL, 1, (short) 1);
        ItemMeta floodMeta = floodSpam.getItemMeta();
        floodMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Flood/Excessive Spamming");
        List<String> lore7 = new ArrayList<>();
        lore7.add(ChatColor.DARK_AQUA + "Just spamming and being annoying.");
        floodMeta.setLore(lore7);
        floodSpam.setItemMeta(floodMeta);

        ItemStack bans = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta bansMeta = bans.getItemMeta();
        bansMeta.setDisplayName(ChatColor.RED + "Bans");
        bans.setItemMeta(bansMeta);
        obvCheating = new ItemStack(Material.WOOL, 1, (short) 14);
        ItemMeta obvCheatingMeta = obvCheating.getItemMeta();
        obvCheatingMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Obviously Cheating");
        List<String> lore8 = new ArrayList<>();
        lore8.add(ChatColor.DARK_AQUA + "Obvious cheating, flying/b-hopping/kill-auraing around the map.");
        obvCheatingMeta.setLore(lore8);
        obvCheating.setItemMeta(obvCheatingMeta);
        cheatingFoundInSS = new ItemStack(Material.WOOL, 1, (short) 14);
        ItemMeta cheatingFoundMeta = cheatingFoundInSS.getItemMeta();
        cheatingFoundMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Cheating (Found in SS)");
        List<String> lore9 = new ArrayList<>();
        lore9.add(ChatColor.DARK_AQUA + "Punishment after a successful screen share, if cheats are found.");
        cheatingFoundMeta.setLore(lore9);
        cheatingFoundInSS.setItemMeta(cheatingFoundMeta);
        admitCheating = new ItemStack(Material.WOOL, 1, (short) 14);
        ItemMeta admitCheatingMeta = admitCheating.getItemMeta();
        admitCheatingMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Cheating (Admit)");
        List<String> lore10 = new ArrayList<>();
        lore10.add(ChatColor.DARK_AQUA + "Admitting publicly or before a screen share to cheat.");
        admitCheatingMeta.setLore(lore10);
        admitCheating.setItemMeta(admitCheatingMeta);
        refuseSS = new ItemStack(Material.WOOL, 1, (short) 14);
        ItemMeta refuseSSMeta = refuseSS.getItemMeta();
        refuseSSMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Refuse SS");
        List<String> lore11 = new ArrayList<>();
        lore11.add(ChatColor.DARK_AQUA + "Refusing to accept a screen share for non-reasonable reason.");
        refuseSSMeta.setLore(lore11);
        refuseSS.setItemMeta(refuseSSMeta);

        ItemStack rankedBans = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 10);
        ItemMeta rankedMeta = rankedBans.getItemMeta();
        rankedMeta.setDisplayName(ChatColor.RED + "RankedBans");
        rankedBans.setItemMeta(warnsMeta);
        rankedCheating = new ItemStack(Material.WOOL, 1, (short) 10);
        ItemMeta rankedCheatingMeta = rankedCheating.getItemMeta();
        rankedCheatingMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Cheating in Ranked");
        List<String> lore12 = new ArrayList<>();
        lore12.add(ChatColor.DARK_AQUA + "Caught cheating on ranked.");
        rankedCheatingMeta.setLore(lore12);
        rankedCheating.setItemMeta(rankedCheatingMeta);
        rankedAnnoying = new ItemStack(Material.WOOL, 1, (short) 10);
        ItemMeta rankedAnnoyingMeta = rankedAnnoying.getItemMeta();
        rankedAnnoyingMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Annoying Playstyle in Ranked");
        List<String> lore13 = new ArrayList<>();
        lore13.add(ChatColor.DARK_AQUA + "Excessive running, camping, etc on ranked.");
        rankedAnnoyingMeta.setLore(lore13);
        rankedAnnoying.setItemMeta(rankedAnnoyingMeta);
        rankedToxic = new ItemStack(Material.WOOL, 1, (short) 10);
        ItemMeta rankedToxicMeta = rankedToxic.getItemMeta();
        rankedToxicMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Toxic Behavior in Ranked");
        List<String> lore14 = new ArrayList<>();
        lore14.add(ChatColor.DARK_AQUA + "Being toxic in ranked.");
        rankedToxicMeta.setLore(lore14);
        rankedToxic.setItemMeta(rankedToxicMeta);

        ItemStack other = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 9);
        ItemMeta otherMeta = other.getItemMeta();
        otherMeta.setDisplayName(ChatColor.RED + "Warns");
        other.setItemMeta(otherMeta);
        emergencyBlacklist = new ItemStack(Material.REDSTONE);
        ItemMeta emergencyMeta = emergencyBlacklist.getItemMeta();
        emergencyMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Emergency Blacklist");
        List<String> lore15 = new ArrayList<>();
        lore15.add(ChatColor.DARK_AQUA + "Fast blacklist if someone is raiding the server or other extreme cases.");
        emergencyMeta.setLore(lore15);
        emergencyBlacklist.setItemMeta(emergencyMeta);
        threatening = new ItemStack(Material.APPLE);
        ItemMeta threateningMeta = threatening.getItemMeta();
        threateningMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Threatening");
        List<String> lore16 = new ArrayList<>();
        lore16.add(ChatColor.DARK_AQUA + "Threatening other players with sever things like privacy, death, etc.");
        threateningMeta.setLore(lore16);
        threatening.setItemMeta(threateningMeta);
        blacklistCustom = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta blacklistCustomMeta = blacklistCustom.getItemMeta();
        blacklistCustomMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Custom Blacklist");
        List<String> lore17 = new ArrayList<>();
        lore17.add(ChatColor.DARK_AQUA + "Custom blacklist (specify the reason in chat).");
        blacklistCustomMeta.setLore(lore17);
        blacklistCustom.setItemMeta(blacklistCustomMeta);
        note = new ItemStack(Material.PAPER);
        ItemMeta noteMeta = note.getItemMeta();
        noteMeta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Annotate");
        List<String> lore18 = new ArrayList<>();
        lore18.add(ChatColor.DARK_AQUA + "Annotate something relevant for a player,");
        lore18.add(ChatColor.DARK_AQUA + "something that other staff members should know.");
        noteMeta.setLore(lore18);
        note.setItemMeta(noteMeta);

        inv.setItem(0, warns);
        inv.setItem(9, crossTeaming);
        inv.setItem(18, bugExploiting);
        inv.setItem(27, teamKill);
        inv.setItem(36, annoyingPlaystyle);
        inv.setItem(45, customWarn);
        inv.setItem(2, mutes);
        inv.setItem(11, lessToxic);
        inv.setItem(20, toxic);
        inv.setItem(29, severToxic);
        inv.setItem(38, floodSpam);
        inv.setItem(4, bans);
        inv.setItem(13, obvCheating);
        inv.setItem(22, cheatingFoundInSS);
        inv.setItem(31, admitCheating);
        inv.setItem(40, refuseSS);
        inv.setItem(6, rankedBans);
        inv.setItem(15, rankedCheating);
        inv.setItem(24, rankedAnnoying);
        inv.setItem(33, rankedToxic);
        inv.setItem(8, other);
        inv.setItem(17, emergencyBlacklist);
        inv.setItem(26, threatening);
        inv.setItem(35, blacklistCustom);
        inv.setItem(44, note);
    }

    private static final Set<UUID> chatCooldown = Collections.synchronizedSet(new HashSet<>());

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (cache.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            if (event.getMessage().equalsIgnoreCase("cancel")) {
                cache.remove(event.getPlayer().getUniqueId());
                event.getPlayer().sendMessage(ChatColor.RED + "Cancelled.");
                event.getPlayer().openInventory(inv);
            } else {
                Player playerToPunish = commandCache.get(event.getPlayer().getUniqueId());
                if (playerToPunish == null) {
                    event.getPlayer().sendMessage(ChatColor.RED + "This player is no longer online.");
                    return;
                }
                User userToPunish = User.get(playerToPunish);
                if (cache.get(event.getPlayer().getUniqueId()).equals(customWarn)) {
                    userToPunish.punish(new Warn(LocalDateTime.now().plusDays(25L), event.getMessage()));
                    commandCache.remove(event.getPlayer().getUniqueId());
                } else if (cache.get(event.getPlayer().getUniqueId()).equals(blacklistCustom)) {
                    userToPunish.punish(new Blacklist(event.getMessage()));
                    commandCache.remove(event.getPlayer().getUniqueId());
                } else if (cache.get(event.getPlayer().getUniqueId()).equals(note)) {
                    userToPunish.addNote(event.getMessage());
                    commandCache.remove(event.getPlayer().getUniqueId());
                }
                cache.remove(event.getPlayer().getUniqueId());
            }
            return;
        }
        User user = User.get(event.getPlayer());
        if (user == null) {
            event.setCancelled(true);
            Bukkit.getLogger().severe("User is null.");
            return;
        }
        if (user.getPunishments() != null && !user.getPunishments().isEmpty()) {
            for (Punishment punishment : user.getPunishments()) {
                if (!punishment.allowChat()) {
                    event.setCancelled(true);
                    String time = Utils.getTimeFormat(punishment.getExpirationDate());
                    event.getPlayer().sendMessage(punishment.getMessage().replaceAll("<time>", time));
                    return;
                }
            }
        }
        if (!user.isGlobalChatEnabled()) {
            event.setCancelled(true);
            return;
        }
        String playerName = event.getPlayer().getDisplayName();
        if (!event.getPlayer().hasPermission("dystellar.plus")) {
            if (chatCooldown.contains(event.getPlayer().getUniqueId())) {
                event.getPlayer().sendMessage(ChatColor.RED + "You're on chat cooldown, please wait before typing again. " + ChatColor.DARK_GREEN + "(Plus ranks and up bypass this!)");
                event.setCancelled(true);
                return;
            } else {
                chatCooldown.add(event.getPlayer().getUniqueId());
                DystellarCore.getAsyncManager().schedule(() -> chatCooldown.remove(event.getPlayer().getUniqueId()), 2800L, TimeUnit.MILLISECONDS);
            }
        }
        if (DystellarCore.PRACTICE_HOOK) {
            PUser player = PUser.get(event.getPlayer());
            if (player.isInParty() && player.isPartyChatActive()) {
                event.setCancelled(true);
                PParty party = player.getParty();
                for (PUser pl : player.getParty().getPlayers()) {
                    if (party.getLeader().equals(player)) {
                        pl.getPlayer().sendMessage(ChatColor.DARK_PURPLE + player.getPlayer().getDisplayName() + ChatColor.RESET + ": " + ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', event.getMessage()));
                    } else {
                        pl.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + player.getPlayer().getDisplayName() + ChatColor.RESET + ": " + ChatColor.AQUA + event.getMessage());
                    }
                }
            } else if (player.isInGame() && player.getLastGame().isRanked()) {
                switch (player.getDoNotDisturbMode()) {
                    case ENABLED_CHAT_ONLY:
                    case ENABLED:
                        event.setCancelled(true);
                        event.getPlayer().sendMessage(ChatColor.RED + "You have do not disturb mode enabled.");
                        break;
                }
            } else {
                for (PUser users : PUser.getUsers().values()) {
                    if (users.isInGame() && users.getLastGame().isRanked()) {
                        switch (users.getDoNotDisturbMode()) {
                            case ENABLED_CHAT_ONLY:
                            case ENABLED:
                                event.getRecipients().remove(users.getPlayer());
                                break;
                        }
                    }
                }
            }
        } else if (DystellarCore.SKYWARS_HOOK) {
            net.zylesh.skywars.common.PlayerUser playerUser = SkywarsAPI.getPlayerUser(event.getPlayer());
            if (playerUser.isInGame()) {
                Set<Player> recipients = event.getRecipients();
                recipients.clear();
                for (Team t : playerUser.getGame().players) {
                    for (net.zylesh.skywars.common.PlayerUser playerUser1 : t.getPlayers()) {
                        recipients.add(playerUser1.getBukkitPlayer());
                    }
                }

            }
        }
        if (event.getPlayer().hasPermission("dystellar.plus")) {
            event.setFormat(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(event.getPlayer(), "%luckperms_prefix%" + playerName + " " + user.getSuffix() + ChatColor.WHITE + ": " + event.getMessage())));
        } else {
            event.setFormat(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(event.getPlayer(), "%luckperms_prefix%" + playerName + " " + user.getSuffix() + ChatColor.WHITE + ": ")) + event.getMessage());
        }
    }
}
