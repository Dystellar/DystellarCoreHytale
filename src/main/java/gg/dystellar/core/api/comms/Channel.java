package gg.dystellar.core.api.comms;

import java.io.OutputStream;

import gg.dystellar.core.utils.ByteBufferStreams.ByteBufferOutputStream;

public final class Channel {
	final String name;
	final WsClient handle;
	final Receiver callback;

	Channel(String name, WsClient handle, Receiver callback) {
		this.name = name;
		this.handle = handle;
		this.callback = callback;
	}

	public ByteBufferOutputStream createMessageStream() {
		
	}
}
