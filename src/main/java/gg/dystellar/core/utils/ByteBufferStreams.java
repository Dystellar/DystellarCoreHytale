package gg.dystellar.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public final class ByteBufferStreams {

	public static ByteBufferInputStream newInputStream(ByteBuffer buf) {
		return new ByteBufferInputStream(buf);
	}

	public static ByteBufferOutputStream newOutputStream() {
		return new ByteBufferOutputStream();
	}

	public static ByteBufferOutputStream newOutputStream(int capacity) {
		return new ByteBufferOutputStream(capacity);
	}

	public static final class ByteBufferInputStream extends InputStream {
		private final ByteBuffer buf;

		ByteBufferInputStream(ByteBuffer buf) {
			this.buf = buf;
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

		@Override
		public void write(int b) throws IOException {
			// TODO
		}
	}
}
