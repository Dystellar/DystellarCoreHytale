package gg.dystellar.core.serialization;

import com.hypixel.hytale.math.vector.Location;

/**
 * Simple location serialization, efficient, readable and simple
 * The serialized form follows this format: world;x;y;z;yaw;pitch;roll.
 */
public class LocationSerialization {

    public static String locationToString(Location loc) {
        return loc.getWorld() + ";"
			+ loc.getPosition().x + ";"
			+ loc.getPosition().y + ";"
			+ loc.getPosition().z + ";"
			+ (double)loc.getRotation().getYaw() + ";"
			+ (double)loc.getRotation().getPitch() + ";"
			+ (double)loc.getRotation().getRoll();
    }

    public static Location stringToLocation(String str) {
        String[] strings = str.split(";");

        return new Location(
			strings[0],
			Double.parseDouble(strings[1]),
			Double.parseDouble(strings[2]),
			Double.parseDouble(strings[3]),
			(float)Double.parseDouble(strings[4]),
			(float)Double.parseDouble(strings[5]),
			(float)Double.parseDouble(strings[6]));
    }
}
