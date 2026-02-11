package gg.dystellar.core.listeners;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.common.UserComponent;
import gg.dystellar.core.common.punishments.Punishment;


public final class JoinsListener {
	public static void register(JavaPlugin plugin) {
		plugin.getEventRegistry().register(EventPriority.FIRST, PlayerConnectEvent.class, e -> onConnect(e));
		plugin.getEventRegistry().register(EventPriority.LAST, PlayerDisconnectEvent.class, e -> onLeave(e));
	}

	private static void onLeave(PlayerDisconnectEvent e) {
		final var holder = e.getPlayerRef().getHolder();
		final var user = holder.getComponent(UserComponent.getComponentType());
		HytaleServer.SCHEDULED_EXECUTOR.execute(() -> {
			try {
				DystellarCore.getApi().saveUser(user);
				DystellarCore.getLog().atInfo().log("The player '" + user.name + "' with an uuid of '" + user.uuid + "' has been saved correctly!");
			} catch (Exception ex) {
				ex.printStackTrace();
				DystellarCore.getLog().atSevere().log("Failed to save " + user.name + "'s data: " + ex.getMessage());
			}
		});
	}

	private static void onConnect(PlayerConnectEvent e) {
		final var p = e.getPlayerRef();
		p.getReference().getStore().getComponent(null, Player.getComponentType()).hasPermission(id)
		final var holder = e.getHolder();

		CompletableFuture.supplyAsync(() -> {
			try {
				return DystellarCore.getApi().playerConnected(p.getUuid().toString(), p.getUsername(), p.getPacketHandler().getAuth().getReferralSource().host);
			} catch (Exception ex) {
				e.getWorld().execute(() -> {
					ex.printStackTrace();
					p.getPacketHandler().disconnect(ex.getMessage());
				});
			}
			return null;
		}, HytaleServer.SCHEDULED_EXECUTOR).thenAccept(user -> {
			e.getWorld().execute(() -> {
				user.init(p);

				holder.addComponent(UserComponent.getComponentType(), user);
				final var now = LocalDateTime.now();
				for (Punishment pun : user.punishments) {
					if (!pun.allowJoinMinigames() && !DystellarCore.getInstance().getSetup().allow_banned_players && (!pun.getExpirationDate().isPresent() || pun.getExpirationDate().get().isBefore(now))) {
						p.getPacketHandler().disconnect("You have been banned from Dystellar Network");
					}
				}
			});
		});
	}
	
	/* TODO:
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
	*/

	/* TODO:
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

	public void onLeave(PlayerQuitEvent event) {
		DystellarCore.getAsyncManager().submit(() -> MariaDB.savePlayerToDatabase(users.get(event.getPlayer().getUniqueId())));
	}

	public void onKick(PlayerKickEvent event) {
		DystellarCore.getAsyncManager().submit(() -> MariaDB.savePlayerToDatabase(users.get(event.getPlayer().getUniqueId())));
	}
	*/
}
