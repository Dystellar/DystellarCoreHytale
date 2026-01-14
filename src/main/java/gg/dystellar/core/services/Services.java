package gg.dystellar.core.services;

import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayIn;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import net.zylesh.dystellarcore.core.IPacketListener;
import net.zylesh.dystellarcore.core.PacketListener;
import net.zylesh.dystellarcore.utils.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Services {

    /**
     * This basically sends a message to all the server every few time, the messages it sends can be defined in the config.
     * Mostly announcements and stuff.
     */
    public static void startAutomatedMessagesService() {
        if (!AUTOMATED_MESSAGES.isEmpty()) {
            asyncManager.scheduleAtFixedRate(() -> {
                PacketPlayOutChat chat = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(AUTOMATED_MESSAGES.get(i.get())));
                synchronized (Bukkit.getOnlinePlayers()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendPacket(chat);
                    }
                }
                i.incrementAndGet();
                if (i.get() >= AUTOMATED_MESSAGES.size()) i.set(0);
            }, AUTOMATED_MESSAGES_RATE, AUTOMATED_MESSAGES_RATE, TimeUnit.SECONDS);
        }
    }
}
