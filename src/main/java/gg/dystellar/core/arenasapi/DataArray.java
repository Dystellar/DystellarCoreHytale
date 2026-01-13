package net.zylesh.dystellarcore.arenasapi;

import java.util.ArrayList;
import java.util.List;

public class DataArray {

    int pos = 0;
    private final List<Object> array = new ArrayList<>();

    public void writeByte(byte value) {
        array.add(value);
    }

    public void writeString(String value) {
        array.add(value);
    }

    public int writeInt(int value) {
        array.add(value);
        return value;
    }

    public void writeBoolean(boolean value) {
        array.add(value);
    }

    public boolean ready() {
        return pos < array.size();
    }

    private void increment() {
        this.pos++;
    }

    public byte readByte() throws ClassCastException {
        byte a = (byte) this.array.get(pos);
        this.increment();
        return a;
    }

    public String readString() throws ClassCastException {
        String a = (String) this.array.get(pos);
        this.increment();
        return a;
    }

    public int readInt() throws ClassCastException {
        int a = (int) this.array.get(pos);
        this.increment();
        return a;
    }

    public boolean readBoolean() throws ClassCastException {
        boolean a = (boolean) this.array.get(pos);
        this.increment();
        return a;
    }
}
