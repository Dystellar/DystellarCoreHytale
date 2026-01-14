package gg.dystellar.core.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Simple location serialization, efficient, simple and the serialized form follows this format: world;x;y;z;yaw;pitch.
 */
public class LocationSerialization {

    public static String locationToString(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + (double) loc.getYaw() + ";" + (double) loc.getPitch();
    }

    public static Location stringToLocation(String str) {
        String[] strings = str.split(";");
        return new Location(Bukkit.getWorld(strings[0]), Double.parseDouble(strings[1]), Double.parseDouble(strings[2]), Double.parseDouble(strings[3]), (float) Double.parseDouble(strings[4]), (float) Double.parseDouble(strings[5]));
    }
}
