package gg.dystellar.core.arenasapi;

import net.zylesh.dystellarcore.serialization.InventorySerialization;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.*;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.material.*;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Class to serialize and deserialize region files.
 * Capable of storing block states and data (such as signs text which is a common bug in worldedit), beds, containers like chests and its items, etc
 */
public class BlockGeometrySchemaUtilRepresentation {

    // Simple protocols to identify type of blocks in the region, registered as extensions for the block
    static final byte BED = 0;
    static final byte DIRECTIONAL = 1;
    static final byte ATTACHABLE = 2;
    static final byte COLORABLE = 3;
    static final byte REDSTONE = 4;
    static final byte CAKE = 5;
    static final byte CHEST = 6;
    static final byte PRESSURE_SENSOR = 8;
    static final byte FLOWER_POT = 9;
    static final byte OPENABLE = 10;
    static final byte LONG_GRASS = 11;
    static final byte MUSHROOM = 12;
    static final byte NETHERWARTS = 13;
    static final byte PISTON_EXTENSION = 15;
    static final byte SANDSTONE = 17;
    static final byte SIGN = 18;
    static final byte STEP = 19;
    static final byte SMOOTH_BRICK = 20;
    static final byte MONSTER_EGGS = 21;
    static final byte STAIRS = 22;
    static final byte TRAPDOOR = 23;
    static final byte TREE = 24;
    static final byte WOODEN_STEP = 25;

    private Vector posVector1; //
    private Vector posVector2;
    private World world;
    private Block[][][] blockData; // 3 dimensional array that contains the entire region
    private double[] center; // 'center' of the region, it will be stored the position relative to this

    // Internal caches for saving safely (don't remember)
    private int x;
    private int y;
    private int z;

    /**
     * Creates a real representation of a region, providing a region selection that starts from posVector1 and ends on posVector2,
     * Provides utilities to save the region and load one from storage
     * @param world The world in which the region is in
     * @param posVector1 position1
     * @param posVector2 position2
     */
    public BlockGeometrySchemaUtilRepresentation(World world, Vector posVector1, Vector posVector2) {
        this.posVector1 = posVector1;
        this.posVector2 = posVector2;
        this.world = world;
    }

    public BlockGeometrySchemaUtilRepresentation() {}

    /**
     * Warning! Big operation.
     */
    public void loadFromVectors() {
        if (posVector1 == null || posVector2 == null || world == null) {
            Bukkit.getLogger().warning("Warning! Tried to load region from vectors but they are null.");
            return;
        }
        int x; // X amount
        int y; // Y amount
        int z; // Z amount

        int xPos; // X minimum position
        int yPos; // Y minimum position
        int zPos; // Z minimum position

        if (posVector1.getBlockX() - posVector2.getBlockX() >= 0) {
            x = posVector1.getBlockX() - posVector2.getBlockX() + 1;
            xPos = posVector2.getBlockX();
        } else {
            x = posVector2.getBlockX() - posVector1.getBlockX() + 1;
            xPos = posVector1.getBlockX();
        }

        if (posVector1.getBlockY() - posVector2.getBlockY() >= 0) {
            y = posVector1.getBlockY() - posVector2.getBlockY() + 1;
            yPos = posVector2.getBlockY();
        } else {
            y = posVector2.getBlockY() - posVector1.getBlockY() + 1;
            yPos = posVector1.getBlockY();
        }

        if (posVector1.getBlockZ() - posVector2.getBlockZ() >= 0) {
            z = posVector1.getBlockZ() - posVector2.getBlockZ() + 1;
            zPos = posVector2.getBlockZ();
        } else {
            z = posVector2.getBlockZ() - posVector1.getBlockZ() + 1;
            zPos = posVector1.getBlockZ();
        }

        this.blockData = new Block[x][y][z];
        this.center = new double[] {(double) x / 2, (double) y / 2, (double) z / 2};

        // Init cache
        this.x = x;
        this.y = y;
        this.z = z;

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    this.blockData[i][j][k] = world.getBlockAt(xPos, yPos, zPos);
                    zPos++;
                }
                yPos++;
            }
            xPos++;
        }
    }

    @SuppressWarnings("deprecation")
    public OfflineRegion loadFromFile(DataInputStream in, boolean close) throws IOException {
        this.x = in.readInt();
        this.y = in.readInt();
        this.z = in.readInt();
        OfflineBlock[][][] blockData = new OfflineBlock[x][y][z];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    int typeId = in.readInt();
                    Biome biome = Biome.valueOf(in.readUTF());
                    DataArray extensions = BlocksSchemes.decodeExtensions(in);

                    blockData[i][j][k] = new OfflineBlock(Material.getMaterial(typeId), biome, extensions);
                }
            }
        }
        if (close) in.close();
        return new OfflineRegion(blockData, x, y, z);
    }

    @SuppressWarnings("deprecation")
    public void encode(DataOutputStream out) throws IOException {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    Block block = this.blockData[i][j][k];
                    out.writeInt(block.getTypeId());
                    out.writeUTF(block.getBiome().name());

                    byte extensions = getExtensions(block);

                    out.writeByte(extensions);

                    if (block.getState() instanceof Bed) {
                        Bed bed = (Bed) block.getState().getData();
                        out.writeByte(BED);
                        out.writeBoolean(bed.isHeadOfBed());
                    }
                    if (block.getState() instanceof Directional) {
                        Directional direction = (Directional) block.getState().getData();
                        out.writeByte(DIRECTIONAL);
                        out.writeUTF(direction.getFacing().name());
                    }
                    if (block.getState() instanceof Attachable) {
                        Attachable attachable = (Attachable) block.getState().getData();
                        out.writeByte(ATTACHABLE);
                        out.writeUTF(attachable.getAttachedFace().name());
                    }
                    if (block.getState() instanceof Colorable) {
                        Colorable colorable = (Colorable) block.getState().getData();
                        out.writeByte(COLORABLE);
                        out.writeUTF(colorable.getColor().name());
                    }
                    if (block.getState() instanceof Redstone) {
                        Redstone redstone = (Redstone) block.getState().getData();
                        out.writeByte(REDSTONE);
                        out.writeBoolean(redstone.isPowered());
                    }
                    if (block.getState() instanceof Cake) {
                        Cake cake = (Cake) block.getState().getData();
                        out.writeByte(CAKE);
                        out.writeInt(cake.getSlicesEaten());
                        out.writeInt(cake.getSlicesRemaining());
                    }
                    if (block.getState() instanceof Chest) {
                        Chest chest = (Chest) block.getState();
                        out.writeByte(CHEST);
                        out.writeUTF(InventorySerialization.inventoryToString(chest.getBlockInventory().getContents()));
                    }
                    if (block.getState() instanceof PressureSensor) {
                        PressureSensor sensor = (PressureSensor) block.getState().getData();
                        out.writeByte(PRESSURE_SENSOR);
                        out.writeBoolean(sensor.isPressed());
                    }
                    if (block.getState() instanceof FlowerPot) {
                        FlowerPot flowerpot = (FlowerPot) block.getState().getData();
                        out.writeByte(FLOWER_POT);
                        out.writeInt(flowerpot.getContents().getItemTypeId());
                    }
                    if (block.getState() instanceof Openable) {
                        Openable openable = (Openable) block.getState().getData();
                        out.writeByte(OPENABLE);
                        out.writeBoolean(openable.isOpen());
                    }
                    if (block.getState() instanceof LongGrass) {
                        LongGrass grass = (LongGrass) block.getState().getData();
                        out.writeByte(LONG_GRASS);
                        out.writeUTF(grass.getSpecies().name());
                    }
                    if (block.getState() instanceof Mushroom) {
                        Mushroom mushroom = (Mushroom) block.getState().getData();
                        out.writeByte(MUSHROOM);
                        out.writeBoolean(mushroom.isStem());
                    }
                    if (block.getState() instanceof NetherWarts) {
                        NetherWarts netherWarts = (NetherWarts) block.getState().getData();
                        out.writeByte(NETHERWARTS);
                        out.writeUTF(netherWarts.getState().name());
                    }
                    if (block.getState() instanceof PistonExtensionMaterial) {
                        PistonExtensionMaterial material = (PistonExtensionMaterial) block.getState().getData();
                        out.writeByte(PISTON_EXTENSION);
                        out.writeUTF(material.getAttachedFace().name());
                        out.writeBoolean(material.isSticky());
                    }
                    if (block.getState() instanceof Sandstone) {
                        Sandstone sandstone = (Sandstone) block.getState().getData();
                        out.writeByte(SANDSTONE);
                        out.writeUTF(sandstone.getType().name());
                    }
                    if (block.getState() instanceof Sign) {
                        Sign sign = (Sign) block.getState();
                        out.writeByte(SIGN);
                        out.writeInt(sign.getLines().length);
                        for (String line : sign.getLines()) out.writeUTF(line);
                    }
                    if (block.getState() instanceof Step) {
                        Step step = (Step) block.getState().getData();
                        out.writeByte(STEP);
                        out.writeBoolean(step.isInverted());
                    }
                    if (block.getState() instanceof SmoothBrick) {
                        SmoothBrick smoothBrick = (SmoothBrick) block.getState().getData();
                        out.writeByte(SMOOTH_BRICK);
                        out.writeUTF(smoothBrick.getMaterial().name());
                    }
                    if (block.getState() instanceof MonsterEggs) {
                        MonsterEggs eggs = (MonsterEggs) block.getState().getData();
                        out.writeByte(MONSTER_EGGS);
                        out.writeUTF(eggs.getMaterial().name());
                    }
                    if (block.getState() instanceof Stairs) {
                        Stairs stairs = (Stairs) block.getState().getData();
                        out.writeByte(STAIRS);
                        out.writeBoolean(stairs.isInverted());
                    }
                    if (block.getState() instanceof TrapDoor) {
                        TrapDoor trapDoor = (TrapDoor) block.getState().getData();
                        out.writeByte(TRAPDOOR);
                        out.writeBoolean(trapDoor.isInverted());
                        out.writeBoolean(trapDoor.isOpen());
                    }
                    if (block.getState() instanceof Tree) {
                        Tree tree = (Tree) block.getState().getData();
                        out.writeByte(TREE);
                        out.writeUTF(tree.getSpecies().name());
                        out.writeUTF(tree.getDirection().name());
                    }
                    if (block.getState() instanceof WoodenStep) {
                        WoodenStep step = (WoodenStep) block.getState().getData();
                        out.writeByte(WOODEN_STEP);
                        out.writeUTF(step.getSpecies().name());
                        out.writeBoolean(step.isInverted());
                    }
                }
            }
        }
    }

    private static byte getExtensions(Block block) {
        byte extensions = 0;
        if (block.getState().getData() instanceof Bed) extensions++;
        if (block.getState().getData() instanceof Directional) extensions++;
        if (block.getState().getData() instanceof Attachable) extensions++;
        if (block.getState().getData() instanceof Colorable) extensions++;
        if (block.getState().getData() instanceof Redstone) extensions++;
        if (block.getState().getData() instanceof Cake) extensions++;
        if (block.getState() instanceof Chest) extensions++;
        if (block.getState().getData() instanceof Cauldron) extensions++;
        if (block.getState().getData() instanceof PressureSensor) extensions++;
        if (block.getState().getData() instanceof FlowerPot) extensions++;
        if (block.getState().getData() instanceof Openable) extensions++;
        if (block.getState().getData() instanceof LongGrass) extensions++;
        if (block.getState().getData() instanceof Mushroom) extensions++;
        if (block.getState().getData() instanceof NetherWarts) extensions++;
        if (block.getState().getData() instanceof PistonExtensionMaterial) extensions++;
        if (block.getState().getData() instanceof Sandstone) extensions++;
        if (block.getState() instanceof Sign) extensions++;
        if (block.getState().getData() instanceof Step) extensions++;
        if (block.getState().getData() instanceof SmoothBrick) extensions++;
        if (block.getState().getData() instanceof MonsterEggs) extensions++;
        if (block.getState().getData() instanceof Stairs) extensions++;
        if (block.getState().getData() instanceof TrapDoor) extensions++;
        if (block.getState().getData() instanceof Tree) extensions++;
        if (block.getState().getData() instanceof WoodenStep) extensions++;
        return extensions;
    }

    public void save(File file) throws IOException {
        BlocksSchemes.save(this, file);
    }

    @Nullable
    public Block[][][] getBlockData() {
        return blockData;
    }

    @Nullable
    public double[] getCenter() {
        return center;
    }
}
