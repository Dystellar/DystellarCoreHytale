package gg.dystellar.core.services.messaging;

import com.google.common.io.ByteArrayDataInput;

@FunctionalInterface
public interface PluginMessageCallback {
	void handle(Player p, ByteArrayDataInput in);
}
