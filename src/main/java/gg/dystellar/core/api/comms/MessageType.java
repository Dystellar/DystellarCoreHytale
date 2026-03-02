package gg.dystellar.core.api.comms;

public enum MessageType {
	PROPAGATE((byte)0),
	TARGET((byte)1),
	CACHE_READ((byte)2),
	CACHE_WRITE((byte)3);

	final int id;

	MessageType(int id) {
		this.id = id;
	}
}
