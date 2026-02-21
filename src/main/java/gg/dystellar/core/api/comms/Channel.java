package gg.dystellar.core.api.comms;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.hypixel.hytale.server.core.HytaleServer;

import gg.dystellar.core.utils.ByteBufferStreams;
import gg.dystellar.core.utils.IOUtils;
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

	public ByteBufferOutputStream createTargetedMessageStream(String targetServer, int capacity) throws IOException {
		final var out = ByteBufferStreams.newOutputStream(capacity);

		out.write(MessageType.TARGETED.id);
		IOUtils.writeNulTerminatedString(out, targetServer);
		IOUtils.writeNulTerminatedString(out, this.name);
		return out;
	}

	public ByteBufferOutputStream createPropagatedMessageStream(int capacity) throws IOException {
		final var out = ByteBufferStreams.newOutputStream(capacity);

		out.write(MessageType.PROPAGATE.id);
		IOUtils.writeNulTerminatedString(out, this.name);
		return out;
	}

	public void sendMessage(ByteBuffer buffer) {
		HytaleServer.SCHEDULED_EXECUTOR.execute(() -> {
			try {
				handle.client.sendBinary(buffer, true).get();
			} catch (Exception e) { e.printStackTrace(); }
		});
	}
}
