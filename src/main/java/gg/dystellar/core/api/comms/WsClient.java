package gg.dystellar.core.api.comms;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import gg.dystellar.core.DystellarCore;

/**
 * Abstraction layer of a websocket client.
 * It makes communicating with other servers straightforward.
 */
public final class WsClient {
	private final String url;
	private final String token;
	final WebSocket client;
	private final Map<String, Channel> channels = new HashMap<>();

	public WsClient(String url, String token, String name, HttpClient httpClient) throws InterruptedException, ExecutionException {
		this.url = url;
		this.token = token;

		this.client = httpClient.newWebSocketBuilder()
			.header("Authorization", this.token)
			.buildAsync(URI.create(this.url + "?name" + name), this.new WsListener())
			.get();
	}

	public Channel registerChannel(String channelId, Receiver callback, CacheReadReceiver cacheCallback) {
		final var channel = new Channel(channelId, this, callback, cacheCallback);

		channels.put(channelId, channel);
		return channel;
	}

	private static final byte REGULAR_MESSAGE = 0;
	private static final byte CACHE_RESPONSE = 1;

	private final class WsListener implements Listener {
		@Override
		public CompletionStage<?> onBinary(WebSocket socket, ByteBuffer data, boolean last) {
			final var inputStream = new Channel.ByteBufferInputStream(data);
			final byte type = inputStream.readByte();
			switch (type) {
				case CACHE_RESPONSE -> {
					final int cacheId = inputStream.readInt();
					final boolean found = inputStream.readBool();
					final var channelName = inputStream.readPrefixedUTF8();

					final var channel = channels.get(channelName);
					if (channel == null)
						DystellarCore.getLog().atWarning().log("Received a ws message for a channel that doesn't exist");
					else
						channel.cacheCallback.receive(cacheId, found, inputStream);
				}
				case REGULAR_MESSAGE -> {
					final var source = inputStream.readPrefixedUTF8();
					final var channelName = inputStream.readPrefixedUTF8();

					final var channel = channels.get(channelName);
					if (channel == null)
						DystellarCore.getLog().atWarning().log("Received a ws message for a channel that doesn't exist");
					else
						channel.callback.receive(source, inputStream);
				}
			}
			socket.request(1L);
			return CompletableFuture.completedFuture(null);
		}
	}
}
