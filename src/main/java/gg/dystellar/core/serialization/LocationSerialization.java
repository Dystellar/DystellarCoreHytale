package net.zylesh.dystellarcore.serialization;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationSerialization {

    public static String locationToString(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + (double) loc.getYaw() + ";" + (double) loc.getPitch();
    }

    public static Location stringToLocation(String str) {
        String[] strings = str.split(";");
        return new Location(Bukkit.getWorld(strings[0]), Double.parseDouble(strings[1]), Double.parseDouble(strings[2]), Double.parseDouble(strings[3]), (float) Double.parseDouble(strings[4]), (float) Double.parseDouble(strings[5]));
    }
}
