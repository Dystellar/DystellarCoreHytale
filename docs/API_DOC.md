# DystellarCore API - Websocket & HTTP Guide

This guide focuses on the DystellarCore communication APIs: HTTP requests and the websocket protocol for plugin-to-plugin communication across Hytale servers.

---

## 1. HTTP API

Use the core API instance to perform all HTTP requests. This centralizes token management and ensures consistency.

### Methods

- `getJson(String path)` – perform GET requests.
- `requestJson(String path, String method, String json)` – perform POST, PUT, DELETE, or other requests with a JSON body.

### Usage Example:

```java
var api = DystellarCore.getApi();

// GET request
Response res = api.getJson("/players/stats");

// POST request with JSON body
String body = "{\"score\": 42}";
Response postRes = api.requestJson("/players/update", "POST", body);
```

> Responses include status code and JSON body. Failures are automatically logged.

---

## 2. Websocket Protocol (WsClient)

The websocket protocol allows plugins to communicate with their instances running on other Hytale servers.

### Concepts

- **WsClient:** The main websocket connection object, accessed via `DystellarCore.getApi().wsClient`.
- **Channel:** A named communication endpoint registered by a plugin. Only servers running the same plugin with the same channel name will receive messages.
- **Receiver:** Callback for handling messages received on a channel.
- **CacheReadReceiver:** Callback specifically for handling cache read responses.
- **Message Types:** `TARGETED`, `PROPAGATED`, `CACHE_WRITE`, `CACHE_READ`.

### Creating a Client & Channel

```java
WsClient client = DystellarCore.getApi().wsClient;

Channel lobbyChannel = client.registerChannel("LobbyChannel",
    (source, input) -> {
        // Handle incoming messages from other servers running this plugin
        String playerName = input.readPrefixedUTF8();
        int level = input.readInt();
        System.out.println(source + " sent player " + playerName + " with level " + level);
    },
    (source, cacheId, found, input) -> {
        // Handle cache read responses from other servers
        if (found) {
            String cachedData = input.readPrefixedUTF8();
            System.out.println("Cache " + cacheId + " from " + source + ": " + cachedData);
        }
    }
);
```

### Sending Messages

**1. Targeted Message** – Sends to a specific server only.

```java
ByteBufferOutputStream out = lobbyChannel.createTargetedMessageStream("GameServer1", 256);
out.writePrefixedUTF8("Player joined");
out.writeInt(5);
lobbyChannel.sendMessage(out.getBuffer());
```

**2. Propagated Message** – Broadcast to all servers running this plugin except the sender.

```java
ByteBufferOutputStream out = lobbyChannel.createPropagatedMessageStream(256);
out.writePrefixedUTF8("Global announcement");
lobbyChannel.sendMessage(out.getBuffer());
```

**3. Cache Write** – Store temporary data in the backend so other servers can read it.

The backend will save the cache, identified by an integer `cacheId`. You can optionally set an expiration time (in milliseconds) or use `Optional.empty()` to create a cache that never expires. **Note:** The cache will automatically be deleted if the sender's websocket connection closes.

```java
ByteBufferOutputStream cacheOut = lobbyChannel.createCacheWriteMessageStream(256, 101, Optional.of(30000L));
cacheOut.writePrefixedUTF8("Temporary lobby state");
lobbyChannel.sendMessage(cacheOut.getBuffer());

// Optional.empty() for never-expiring cache
ByteBufferOutputStream permanentCache = lobbyChannel.createCacheWriteMessageStream(256, 102, Optional.empty());
permanentCache.writePrefixedUTF8("Persistent data");
lobbyChannel.sendMessage(permanentCache.getBuffer());
```

**4. Cache Read** – Request previously stored cache data from other servers.

```java
lobbyChannel.readCacheRequest(101); // triggers cache callback when data arrives
```

> The receiving callback indicates whether the cache exists (`found` boolean) and provides the payload in `input` if available.

### Practical Use Case: Minigame Lobby

#### Scenario: A player joins the lobby

1. **Notify all game servers running the plugin:**
```java
ByteBufferOutputStream msg = lobbyChannel.createPropagatedMessageStream(128);
msg.writePrefixedUTF8("Steve"); // player name
msg.writeInt(0); // initial score
lobbyChannel.sendMessage(msg.getBuffer());
```

2. **Store temporary player stats in cache:**
```java
ByteBufferOutputStream cacheOut = lobbyChannel.createCacheWriteMessageStream(256, 42, Optional.of(60000L));
cacheOut.writePrefixedUTF8("Score: 0");
lobbyChannel.sendMessage(cacheOut.getBuffer());
```

3. **Other servers read cache when needed:**
```java
lobbyChannel.readCacheRequest(42); // triggers cache callback when data arrives
```

> Use cache reads to share temporary player state between servers running the same plugin, propagate cache IDs to ensure synchronization. The backend handles expiration and cleanup automatically.

### Notes & Best Practices

- All message handlers are asynchronous; avoid assuming thread safety.
- Capacity of `ByteBufferOutputStream` is adjustable; it auto-expands if needed.
- Each plugin should register its own channel to avoid conflicts.
- Message type choice depends on intent: targeted for specific server, propagated for broadcast, cache for temporary shared state.
- Expiration management is handled by the backend; remember caches are deleted if the sender disconnects.

---

This guide explains how to leverage DystellarCore's HTTP and websocket APIs to allow plugins to communicate with instances of themselves running on other Hytale servers, with full support for targeted, propagated, and cached messages, including expiration and cleanup mechanics.

