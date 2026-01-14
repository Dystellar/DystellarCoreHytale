package gg.dystellar.core.arenasapi;

import java.util.ArrayList;
import java.util.List;

/**
 * Whatever I was thinking back in the day, this is bad and should be changed.
 * It's just a list of objects that stores any data, this is obviously over-engineered and something more reasonable should be designed/used instead of this.
 *
 * This is used to store 'extensions' of a block, like the inventory if its a chest, the direction if its a bed, or the text if its a sign,
 * but there are clearly better ways to design this.
 */
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
