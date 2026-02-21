package gg.dystellar.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public final class IOUtils {
	public static void writeNulTerminatedString(OutputStream out, String s) throws IOException {
		byte[] utf8 = s.getBytes(StandardCharsets.UTF_8);
		out.write(utf8);
		out.write(0);
	}

	public static String readNulTerminatedString(InputStream in) throws IOException {
		ByteBuf buf = Unpooled.buffer(30);
		int b;

		while ((b = in.read()) != 0) {
			buf.writeByte(b);
		}
		return buf.toString(StandardCharsets.UTF_8);
	}
}
