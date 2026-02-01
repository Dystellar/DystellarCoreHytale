package gg.dystellar.core.listeners;

import java.time.LocalDateTime;

import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.common.punishments.Punishment;

import com.hypixel.hytale.server.core.event.events.player.PlayerSetupConnectEvent;

public final class JoinsListener {
	public static void register(JavaPlugin plugin) {
		plugin.getEventRegistry().register(PlayerConnectEvent.class, e -> onConnect(e));
	}

	private static void onConnect(PlayerConnectEvent e) {
		final var p = e.getPlayerRef();
		final var holder = e.getHolder();

		try {
			final var user = DystellarCore.getApi().playerConnected(p.getUuid().toString(), p.getUsername(), p.getPacketHandler().getAuth().getReferralSource().host);

			holder.addComponent(UserComponent.getComponentType(), user);
			final var now = LocalDateTime.now();
			for (Punishment pun : user.punishments) {
				if (!pun.allowJoinMinigames() && !DystellarCore.getInstance().getSetup().allow_banned_players && (!pun.getExpirationDate().isPresent() || pun.getExpirationDate().get().isBefore(now))) {
					p.getPacketHandler().disconnect("You are banned from this server");
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			p.getPacketHandler().disconnect(ex.getMessage());
		}
	}

	public void onJoin(PlayerJoinEvent event) {
		User user = User.get(event.getPlayer());
		user.initializeSettingsPanel(event.getPlayer());
		Bukkit.getScheduler().runTaskLater(DystellarCore.getInstance(), () -> {
			if (user.globalTabComplete) DystellarCore.getInstance().sendPluginMessage(event.getPlayer(), DystellarCore.GLOBAL_TAB_REGISTER);
			if (DystellarCore.PACK_ENABLED) {
				DystellarCore.getInstance().sendPluginMessage(event.getPlayer(), DystellarCore.SHOULD_SEND_PACK);
				if (DystellarCore.DEBUG_MODE) Bukkit.getLogger().info("[Debug] Resource pack request sent to proxy.");
			}
		}, 30L);
	}

	@EventHandler
	public void clicked(InventoryClickEvent event) {
		User u = User.get(event.getWhoClicked().getUniqueId());
		if (u == null) {
			event.setCancelled(true);
			return;
		}
		if (event.getClickedInventory().equals(u.configManager)) {
			event.setCancelled(true);
			ItemStack i = event.getCurrentItem();
			if (i == null || i.getType() == Material.AIR) return;
			if (i.equals(u.globalChatItem)) u.toggleGlobalChat();
			else if (i.equals(u.pmsItem)) u.togglePms();
			else if (i.equals(u.globalTabCompleteItem)) u.toggleGlobalTabComplete();
			else if (i.equals(u.scoreboardEnabledItem)) u.toggleScoreboard();
			Player p = (Player) event.getWhoClicked();
			p.playSound(p.getLocation(), Sound.CLICK, 1.8f, 1.8f);
		}
	}

	@EventHandler
	public void drag(InventoryDragEvent event) {
		User u = User.get(event.getWhoClicked().getUniqueId());
		if (event.getInventory().equals(u.configManager)) event.setCancelled(true);
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		DystellarCore.getAsyncManager().submit(() -> MariaDB.savePlayerToDatabase(users.get(event.getPlayer().getUniqueId())));
	}

	@EventHandler
	public void onKick(PlayerKickEvent event) {
		DystellarCore.getAsyncManager().submit(() -> MariaDB.savePlayerToDatabase(users.get(event.getPlayer().getUniqueId())));
	}
}
