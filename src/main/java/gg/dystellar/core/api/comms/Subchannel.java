package gg.dystellar.core.api.comms;

import java.net.http.WebSocket;

public final class Subchannel {
	private final String name;
	private final WebSocket handle;
	private final Receiver callback;

	Subchannel(String name, WebSocket handle, Receiver callback) {
		this.name = name;
		this.handle = handle;
		this.callback = callback;
	}
}
