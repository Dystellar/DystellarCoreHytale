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

/**
 * Abstraction layer of a websocket client.
 * It makes communicating with other servers straightforward.
 */
public final class WsClient {
	private final String url;
	private final String token;
	private final WebSocket client;
	private final Map<String, Subchannel> subchannels = new HashMap<>();

	public WsClient(String url, String token, HttpClient httpClient) throws InterruptedException, ExecutionException {
		this.url = url;
		this.token = token;

		this.client = httpClient.newWebSocketBuilder()
			.header("Authorization", this.token)
			.buildAsync(URI.create(this.url), new WsListener())
			.get();
	}

	private static final class WsListener implements Listener {
		@Override
		public CompletionStage<?> onBinary(WebSocket socket, ByteBuffer data, boolean last) {
			
			socket.request(1L);
			return CompletableFuture.completedFuture(null);
		}
	}
}
