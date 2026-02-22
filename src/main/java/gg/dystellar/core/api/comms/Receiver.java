package gg.dystellar.core.api.comms;

import gg.dystellar.core.api.comms.Channel.ByteBufferInputStream;

@FunctionalInterface
public interface Receiver {
	/**
	 * Warning! This will be called from async, do not assume thread safety!
	 * The input stream is merely a wrapper to a ByteBuffer underneath, no exception will be thrown and you can just silence IOException.
	 * There is no need to close the stream either.
	 *
	 * @param source From which server this message comes from
	 * @param input The data sent itself, can be wrapped in a DataInputStream or ObjectInputStream, or any other input stream really as long as the format is known.
	 */
	void receive(String source, ByteBufferInputStream input);
}
