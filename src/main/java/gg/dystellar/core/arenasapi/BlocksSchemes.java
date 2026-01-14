package gg.dystellar.core.arenasapi;

import net.zylesh.dystellarcore.DystellarCore;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;

import static net.zylesh.dystellarcore.arenasapi.BlockGeometrySchemaUtilRepresentation.*;

/**
 * Static utility functions for region operations
 */
public class BlocksSchemes {

    /**
     * Saves a real physical region into a file.
     * This should be changed, because getAsyncManager is unnecessary and hytale's async manager should be used instead.
     * @param schema Physical region
     * @param file File where the region will be saved
     * @throws IOException
     */
    public static void save(BlockGeometrySchemaUtilRepresentation schema, File file) throws IOException {
        if (file.createNewFile()) {
            DystellarCore.getAsyncManager().submit(() -> {
                try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
                    schema.encode(out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            throw new FileAlreadyExistsException("This file already exists.");
        }
    }

    /**
     * Loads an offline region from any input stream, usually a file.
     * @param in where to read from
     * @return The offline region saved in the file
     * @throws IOException
     */
    private static OfflineRegion load(DataInputStream in) throws IOException {
        BlockGeometrySchemaUtilRepresentation schema = new BlockGeometrySchemaUtilRepresentation();
        return schema.loadFromFile(in, true);
    }

    /**
     * Helper to handle extensions, like signs, chests filled with items, etc.
     * @param in Where to read from
     * @return The data in a DataArray, this should probably be changed as DataArray is just a list of objects that behaves like a stream but it's not.
     * @throws IOException
     */
    public static DataArray decodeExtensions(DataInputStream in) throws IOException {
        byte extensions = in.readByte();
        DataArray array = new DataArray();
        array.writeByte(extensions);
        for (byte e = 0; e < extensions; e++) {
            byte type = in.readByte();
            array.writeByte(type);
            switch (type) {
                case BED:
                case REDSTONE:
                case PRESSURE_SENSOR:
                case OPENABLE:
                case MUSHROOM:
                case STAIRS:
                case STEP: {
                    array.writeBoolean(in.readBoolean());
                    break;
                }
                case DIRECTIONAL:
                case ATTACHABLE:
                case COLORABLE:
                case CHEST:
                case NETHERWARTS:
                case LONG_GRASS:
                case SANDSTONE:
                case MONSTER_EGGS:
                case SMOOTH_BRICK: {
                    array.writeString(in.readUTF());
                    break;
                }
                case TREE: {
                    array.writeString(in.readUTF());
                    array.writeString(in.readUTF());
                    break;
                }
                case PISTON_EXTENSION:
                case WOODEN_STEP: {
                    array.writeString(in.readUTF());
                    array.writeBoolean(in.readBoolean());
                    break;
                }
                case CAKE: {
                    array.writeInt(in.readInt());
                    array.writeInt(in.readInt());
                    break;
                }
                case FLOWER_POT: {
                    array.writeInt(in.readInt());
                    break;
                }
                case SIGN: {
                    int lenght = array.writeInt(in.readInt());
                    for (int i = 0; i < lenght; i++) {
                        array.writeString(in.readUTF());
                    }
                    break;
                }
                case TRAPDOOR: {
                    array.writeBoolean(in.readBoolean());
                    array.writeBoolean(in.readBoolean());
                    break;
                }
            }
        }
        return array;
    }
}
