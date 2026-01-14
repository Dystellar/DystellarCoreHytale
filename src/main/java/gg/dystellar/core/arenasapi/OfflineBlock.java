package gg.dystellar.core.arenasapi;

import net.zylesh.dystellarcore.serialization.InventorySerialization;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.material.*;

import static net.zylesh.dystellarcore.arenasapi.BlockGeometrySchemaUtilRepresentation.*;

/**
 * Its called offline block because it doesn't represent a world block, so it doesn't have a location
 * It's only the data of the block that needs to be serialized, like the type of block, the biome, and extensions
 */
public class OfflineBlock {

    private final Material material; // Type of block
    private final Biome biome; // Biome data of the block
    private final DataArray data; // Change this to something more dedicated

    public OfflineBlock(Material mat, Biome biome, DataArray data) {
        this.material = mat;
        this.biome = biome;
        this.data = data;
    }

    public Biome getBiome() {
        return biome;
    }

    public DataArray getData() {
        return data;
    }

    public Material getMaterial() {
        return material;
    }

    /**
     * This pastes the block in a specified location
     * @param world World where the block will be pasted
     * @param x x pos
     * @param y y pos
     * @param z z pos
     */
    public void paste(World world, int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(material);
        block.setBiome(biome);
        applyData(block, data);
    }

    /**
     * Helper function that applies extensions to a physical block if possible
     * @param block
     * @param data
     */
    @SuppressWarnings("deprecation")
    public static void applyData(Block block, DataArray data) {
        byte extensions = data.readByte();
        for (byte e = 0; e < extensions; e++) {
            byte type = data.readByte();
            switch (type) {
                case BED: {
                    if (block.getType() != Material.BED) block.setType(Material.BED);
                    boolean flag = data.readBoolean();
                    Bed bed = (Bed) block.getState().getData();
                    bed.setHeadOfBed(flag);
                    break;
                }
                case REDSTONE: {
                    boolean flag = data.readBoolean();
                    Redstone redstone = (Redstone) block.getState().getData();
                    if (redstone instanceof Button) ((Button) redstone).setPowered(flag);
                    else if (redstone instanceof Command) ((Command) redstone).setPowered(flag);
                    else if (redstone instanceof Lever) ((Lever) redstone).setPowered(flag);
                    else if (redstone instanceof PistonBaseMaterial) ((PistonBaseMaterial) redstone).setPowered(flag);
                    else if (redstone instanceof PoweredRail) ((PoweredRail) redstone).setPowered(flag);
                    else if (redstone instanceof RedstoneTorch) {
                        if (flag) block.setType(Material.REDSTONE_TORCH_ON);
                        else block.setType(Material.REDSTONE_TORCH_OFF);
                    } else if (redstone instanceof RedstoneWire) {
                        RedstoneWire wire = (RedstoneWire) redstone;
                        if (flag) wire.setData((byte) 1);
                        else wire.setData((byte) 0);
                    } else if (redstone instanceof TripwireHook) ((TripwireHook) redstone).setActivated(flag);
                    break;
                }
                case PRESSURE_SENSOR: {
                    boolean flag = data.readBoolean();
                    PressureSensor sensor = (PressureSensor) block.getState().getData();
                    if (sensor instanceof DetectorRail) ((DetectorRail) sensor).setPressed(flag);
                    if (sensor instanceof PressurePlate) {
                        if (flag) ((PressurePlate) sensor).setData((byte) 1);
                        else ((PressurePlate) sensor).setData((byte) 0);
                    }
                    break;
                }
                case OPENABLE: {
                    boolean flag = data.readBoolean();
                    Openable openable = (Openable) block.getState().getData();
                    openable.setOpen(flag);
                    break;
                }
                case MUSHROOM: {
                    boolean flag = data.readBoolean();
                    Mushroom mushroom = (Mushroom) block.getState().getData();
                    if (flag) mushroom.setStem();
                    break;
                }
                case STAIRS: {
                    boolean flag = data.readBoolean();
                    Stairs stairs = (Stairs) block.getState().getData();
                    stairs.setInverted(flag);
                    break;
                }
                case STEP: {
                    boolean flag = data.readBoolean();
                    Step step = (Step) block.getState().getData();
                    step.setInverted(flag);
                    break;
                }
                case DIRECTIONAL: {
                    String s = data.readString();
                    Directional directional = (Directional) block.getState().getData();
                    directional.setFacingDirection(BlockFace.valueOf(s));
                    break;
                }
                case ATTACHABLE: {
                    String s = data.readString();
                    Attachable attachable = (Attachable) block.getState().getData();
                    attachable.setFacingDirection(BlockFace.valueOf(s));
                    break;
                }
                case COLORABLE: {
                    String s = data.readString();
                    Colorable colorable = (Colorable) block.getState().getData();
                    colorable.setColor(DyeColor.valueOf(s));
                    break;
                }
                case CHEST: {
                    String s = data.readString();
                    Chest chest = (Chest) block.getState();
                    chest.getBlockInventory().setContents(InventorySerialization.stringToInventory(s));
                    break;
                }
                case PISTON_EXTENSION: {
                    String s = data.readString();
                    boolean flag = data.readBoolean();
                    PistonExtensionMaterial extension = (PistonExtensionMaterial) block.getState().getData();
                    extension.setFacingDirection(BlockFace.valueOf(s));
                    extension.setSticky(flag);
                    break;
                }
                case NETHERWARTS: {
                    String s = data.readString();
                    NetherWarts warts = (NetherWarts) block.getState().getData();
                    warts.setState(NetherWartsState.valueOf(s));
                    break;
                }
                case LONG_GRASS: {
                    String s = data.readString();
                    LongGrass grass = (LongGrass) block.getState().getData();
                    grass.setSpecies(GrassSpecies.valueOf(s));
                    break;
                }
                case SANDSTONE: {
                    String s = data.readString();
                    Sandstone sandstone = (Sandstone) block.getState().getData();
                    sandstone.setType(SandstoneType.valueOf(s));
                    break;
                }
                case TREE: {
                    String s = data.readString();
                    String s1 = data.readString();
                    Tree tree = (Tree) block.getState().getData();
                    tree.setSpecies(TreeSpecies.valueOf(s));
                    tree.setDirection(BlockFace.valueOf(s1));
                    break;
                }
                case MONSTER_EGGS: {
                    String s = data.readString();
                    MonsterEggs eggs = (MonsterEggs) block.getState().getData();
                    eggs.setMaterial(Material.valueOf(s));
                    break;
                }
                case SMOOTH_BRICK: {
                    String s = data.readString();
                    SmoothBrick brick = (SmoothBrick) block.getState().getData();
                    brick.setMaterial(Material.valueOf(s));
                    break;
                }
                case CAKE: {
                    int i = data.readInt();
                    int i1 = data.readInt();
                    Cake cake = (Cake) block.getState().getData();
                    cake.setSlicesEaten(i);
                    cake.setSlicesRemaining(i1);
                    break;
                }
                case FLOWER_POT: {
                    int i = data.readInt();
                    FlowerPot pot = (FlowerPot) block.getState().getData();
                    pot.setContents(new MaterialData(i, pot.getData()));
                    break;
                }
                case SIGN: {
                    int lenght = data.readInt();
                    Sign sign = (Sign) block.getState();
                    for (int i = 0; i < lenght; i++) {
                        sign.setLine(i, data.readString());
                    }
                    break;
                }
                case TRAPDOOR: {
                    boolean flag = data.readBoolean();
                    boolean flag1 = data.readBoolean();

                    TrapDoor door = (TrapDoor) block.getState().getData();

                    door.setInverted(flag);
                    door.setOpen(flag1);
                    break;
                }
                case WOODEN_STEP: {
                    boolean flag = data.readBoolean();
                    String s = data.readString();

                    WoodenStep step = (WoodenStep) block.getState().getData();

                    step.setInverted(flag);
                    step.setSpecies(TreeSpecies.valueOf(s));
                    break;
                }
            }
        }
    }
}
