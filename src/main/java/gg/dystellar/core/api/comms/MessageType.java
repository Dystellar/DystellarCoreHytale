package gg.dystellar.core.api.comms;

public enum MessageType {
	PROPAGATE((byte)0),
	TARGETED((byte)1);

	final byte id;

	MessageType(byte id) {
		this.id = id;
	}
}
