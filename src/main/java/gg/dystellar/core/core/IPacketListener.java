package net.zylesh.dystellarcore.core;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketListenerPlayIn;
import net.minecraft.network.protocol.game.PacketListenerPlayOut;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;

public interface IPacketListener {

    void onPacketReceive(Packet<PacketListenerPlayIn> packet, Player player, AtomicBoolean cancel);

    void onPacketSend(Packet<PacketListenerPlayOut> packet, Player player, AtomicBoolean cancel);
}
