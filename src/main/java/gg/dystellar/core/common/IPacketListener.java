package gg.dystellar.core.common;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayIn;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Interface to facilitate working with packets.
 * It's part of my own packet manipulation API for any other plugin to use.
 */
public interface IPacketListener {

    void onPacketReceive(Packet<PacketListenerPlayIn> packet, Player player, AtomicBoolean cancel);

    void onPacketSend(Packet<PacketListenerPlayOut> packet, Player player, AtomicBoolean cancel);
}
