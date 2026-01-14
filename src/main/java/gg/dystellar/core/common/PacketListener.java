package gg.dystellar.core.common;

import net.zylesh.dystellarcore.DystellarCore;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import gg.minecraft.server.v1_7_R4.NetworkManager;
import gg.minecraft.server.v1_7_R4.Packet;
import gg.minecraft.util.io.netty.channel.*;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Listener for packet stuff
 */
public class PacketListener implements Listener {

    public PacketListener() {
        Bukkit.getPluginManager().registerEvents(this, DystellarCore.getInstance());
    }

    /**
     * Removes a player from the event loop, to stop handling packets for them.
     * Used when a player leaves the server as it should stop processing anything.
     */
    public static void removePlayer(Player player) {
        try {
            Field field = NetworkManager.class.getDeclaredField("m");
            field.setAccessible(true);
            Channel channel = (Channel)field.get((((CraftPlayer)player).getHandle()).playerConnection.networkManager);
            channel.eventLoop().submit(() -> {
                CHANNEL.pipeline().remove(player.getName());
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Injects a player into a pipeline and starts processing packet listeners.
     */
    public static void injectPlayer(final Player player) {
        try {
            ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
                public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                    AtomicBoolean b = new AtomicBoolean(false);
                    for (IPacketListener listener : packetListeners) listener.onPacketReceive((Packet) packet, player, b);
                    if (b.get()) return;
                    super.channelRead(channelHandlerContext, packet);
                }

                public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                    AtomicBoolean b = new AtomicBoolean(false);
                    for (IPacketListener listener : packetListeners) listener.onPacketSend((Packet) packet, player, b);
                    if (b.get()) return;
                    super.write(channelHandlerContext, packet, channelPromise);
                }
            };

            // Reflection stuff
            Field field = NetworkManager.class.getDeclaredField("m");
            field.setAccessible(true);
            Channel channel = (Channel)field.get((((CraftPlayer)player).getHandle()).playerConnection.networkManager);
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
            field.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final Set<IPacketListener> packetListeners = new HashSet<>();

    public static void registerPacketHandler(IPacketListener listener) {
        packetListeners.add(listener);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        injectPlayer(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removePlayer(event.getPlayer());
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        removePlayer(event.getPlayer());
    }
}
