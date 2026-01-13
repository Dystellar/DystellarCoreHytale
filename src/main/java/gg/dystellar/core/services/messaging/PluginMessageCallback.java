package net.zylesh.dystellarcore.services.messaging;

import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataInput;

@FunctionalInterface
public interface PluginMessageCallback {
	void handle(Player p, ByteArrayDataInput in);
}
