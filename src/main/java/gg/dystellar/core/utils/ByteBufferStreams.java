package gg.dystellar.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class ByteBufferStreams {

	public static ByteBufferInputStream newInputStream(ByteBuffer buf) {
		return new ByteBufferInputStream(buf);
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
		public int available() throws IOException {
			return this.buf.remaining();
		}
	}

	public static final class ByteBufferOutputStream {
		ByteBufferOutputStream() {

		}
	}
}
