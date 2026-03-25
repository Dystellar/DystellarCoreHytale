package gg.dystellar.core.api.comms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.hypixel.hytale.server.core.HytaleServer;

import gg.dystellar.core.DystellarCore;

public final class Channel {
	public static final byte REGULAR = 0;
	public static final byte CACHE = 1;

	final String name;
	final WsClient handle;
	final Receiver callback;
	final CacheReadReceiver cacheCallback;

	Channel(String name, WsClient handle, Receiver callback, CacheReadReceiver cacheCallback) {
		this.name = name;
		this.handle = handle;
		this.callback = callback;
		this.cacheCallback = cacheCallback;
	}

	public ByteBufferOutputStream createTargetedMessageStream(String targetServer, int capacity) throws IOException {
		final var out = new ByteBufferOutputStream(capacity);
		out.write(MessageType.TARGET.id);
		out.writePrefixedUTF8(DystellarCore.getInstance().config.get().server_name);
		out.writePrefixedUTF8(targetServer);
		out.writePrefixedUTF8(this.name);

		return out;
	}

	public ByteBufferOutputStream createPropagatedMessageStream(int capacity) throws IOException {
		final var out = new ByteBufferOutputStream(capacity);
		out.write(MessageType.PROPAGATE.id);
		out.writePrefixedUTF8(DystellarCore.getInstance().config.get().server_name);
		out.writePrefixedUTF8(this.name);

		return out;
	}

	public void readCacheRequest(int cacheId) {
		final var buf = ByteBuffer.allocate(7 + this.name.length());
		buf.put((byte)MessageType.CACHE_READ.id);
		buf.putInt(cacheId);
		buf.putShort((short)this.name.length());
		buf.put(this.name.getBytes(StandardCharsets.UTF_8));

		this.sendMessage(buf);
	}

	public ByteBufferOutputStream createCacheWriteMessageStream(int capacity, int cacheId) throws IOException {
		return this.createCacheWriteMessageStream(capacity, cacheId, -1);
	}

	public ByteBufferOutputStream createCacheWriteMessageStream(int capacity, int cacheId, long expirationMillis) throws IOException {
		final var out = new ByteBufferOutputStream(capacity);
		out.write(MessageType.CACHE_WRITE.id);
		out.writePrefixedUTF8(DystellarCore.getInstance().config.get().server_name);
		out.writeInt(cacheId);
		out.writeLong(expirationMillis);
		out.writePrefixedUTF8(this.name);

		return out;
	}

	public void sendMessage(ByteBuffer buffer) {
		HytaleServer.SCHEDULED_EXECUTOR.execute(() -> {
			try {
				handle.client.sendBinary(buffer, true).get();
			} catch (Exception e) { e.printStackTrace(); }
		});
	}

	public static final class ByteBufferInputStream extends InputStream {
		private final ByteBuffer buf;

		ByteBufferInputStream(ByteBuffer buf) {
			this.buf = buf;
		}

		public String readPrefixedUTF8() {
			final var len = this.buf.getShort();
			final byte[] str = new byte[len];

			this.buf.get(str);
			return new String(str, StandardCharsets.UTF_8);
		}

		public int readInt() {
			return this.buf.getInt();
		}

		public long readLong() {
			return this.buf.getLong();
		}

		public float readFloat() {
			return this.buf.getFloat();
		}

		public double readDouble() {
			return this.buf.getDouble();
		}

		public boolean readBool() {
			return this.buf.get() != 0 ? true : false;
		}

		public byte readByte() {
			return this.buf.get();
		}

		@Override
		public int read() throws IOException {
			if (!buf.hasRemaining())
				return -1;
			return this.buf.get() & 0xFF;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			final var res = Math.min(len, buf.remaining());
			try {
				buf.get(b, off, res);
			} catch (Exception e) { throw new IOException(e); }
			return res;
		}

		@Override
		public byte[] readNBytes(int len) throws IOException {
			final byte[] buf = new byte[Math.min(len, this.buf.remaining())];
			this.buf.get(buf);

			return buf;
		}

		@Override
		public int readNBytes(byte[] b, int off, int len) throws IOException {
			return this.read(b, off, len);
		}

		@Override
		public byte[] readAllBytes() throws IOException {
			final byte[] buf = new byte[this.buf.remaining()];
		    
			this.buf.get(buf);
			return buf;
		}

		public ByteBuffer getBuffer() {
		    return buf;
		}

		@Override
		public int available() throws IOException {
			return this.buf.remaining();
		}
	}

	public static final class ByteBufferOutputStream extends OutputStream {
		private ByteBuffer buf;

		ByteBufferOutputStream() {
			buf = ByteBuffer.allocate(256);
		}

		ByteBufferOutputStream(int capacity) {
			buf = ByteBuffer.allocate(capacity);
		}

		private void ensureCapacity(int additional) {
			if (buf.remaining() >= additional) return;

			final int newCapacity = Math.max(buf.capacity() * 2, buf.capacity() + additional);
			final var newBuf = ByteBuffer.allocate(newCapacity);

			this.buf.flip();
			newBuf.put(buf);
			this.buf = newBuf;
		}

		public void writePrefixedUTF8(String s) {
			final byte[] utfBytes = s.getBytes(StandardCharsets.UTF_8);
			this.ensureCapacity(utfBytes.length + 2);

			this.buf.putShort((short)utfBytes.length);
			this.buf.put(utfBytes);
		}

		public void writeInt(int i) {
			this.ensureCapacity(4);
			this.buf.putInt(i);
		}

		public void writeLong(long l) {
			this.ensureCapacity(8);
			this.buf.putLong(l);
		}

		public void writeFloat(float f) {
			this.ensureCapacity(4);
			this.buf.putFloat(f);
		}

		public void writeDouble(double d) {
			this.ensureCapacity(8);
			this.buf.putDouble(d);
		}

		public void writeBool(boolean b) {
			this.ensureCapacity(1);
			this.buf.put(b ? (byte)1 : 0);
		}

		public void writeByte(byte b) {
			this.buf.put(b);
		}

		@Override
		public void write(byte[] src) throws IOException {
			this.ensureCapacity(src.length);
			this.buf.put(src);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			this.ensureCapacity(len);
			buf.put(b, off, len);
		}

		public ByteBuffer getBuffer() {
		    return buf;
		}

		@Override
		public void write(int b) throws IOException {
			this.buf.put((byte)b);
		}
	}
}
