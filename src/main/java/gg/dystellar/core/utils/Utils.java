package gg.dystellar.core.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.zylesh.dystellarcore.DystellarCore;
import net.zylesh.dystellarcore.core.Subchannel;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Array;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class Utils {

	public static <T> Optional<T> findArr(T[] arr, Predicate<T> predicate) {
		for (T t : arr) {
			if (predicate.test(t))
				return Optional.ofNullable(t);
		}

		return Optional.empty();
	}

    /**
     * Formats the time from an expiration date.
     */
    public static String getTimeFormat(LocalDateTime expirationDate) {
        if (expirationDate == null) return "Never";

        LocalDateTime now = LocalDateTime.now();
        long between = Duration.between(now, expirationDate).toDays();
        long betweenHours = Duration.between(now, expirationDate).toHours() - (between * 24);
        long betweenMinutes = Duration.between(now, expirationDate).toMinutes() - (betweenHours * 60);
        return (between > 0 ? between + " days, " : "") + (betweenHours > 0 ? betweenHours + " hours and " : "") + (betweenMinutes > 0 ? betweenMinutes + " minutes." : "");
    }

	public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
		player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public static void resetEffects(Player p) {
        p.setFireTicks(0);
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setSaturation(12.0f);
        p.getActivePotionEffects().forEach(potionEffect -> {
            if (!potionEffect.getType().equals(PotionEffectType.BLINDNESS)) p.removePotionEffect(potionEffect.getType());
        });
    }

    public static void removeArmor(Player p) {
        p.getInventory().setHelmet(null);
        p.getInventory().setChestplate(null);
        p.getInventory().setLeggings(null);
        p.getInventory().setBoots(null);
    }

    /**
     * TODO: Use null-terminates instead of this.
     */
    public static String bytesToString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        builder.append(bytes.length).append(":;:");
        for (byte b : bytes) {
            builder.append(b).append(";");
        }
        return builder.toString();
    }

    public static byte[] stringToBytes(String s, boolean compatibilityLayer) {
        if (compatibilityLayer) return new byte[50];
        String[] split = s.split(":;:");
        byte[] data = new byte[Integer.parseInt(split[0])];
        String[] split2 = split[1].split(";");
        for (int i = 0; i < split2.length; i++) {
            data[i] = Byte.parseByte(split2[i]);
        }
        return data;
    }

	public static void sendPluginMessage(Player player, Subchannel subchannel, Object...extraData) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(subchannel.id); // Subchannel
        if (extraData != null) {
            for (Object o : extraData) {
                if (o instanceof String) out.writeUTF((String) o);
                else if (o instanceof Byte) out.writeByte((byte) o);
                else if (o instanceof Integer) out.writeInt((int) o);
                else if (o instanceof Float) out.writeFloat((float) o);
                else if (o instanceof Double) out.writeDouble((double) o);
                else if (o instanceof Boolean) out.writeBoolean((boolean) o);
                else if (o instanceof Long) out.writeLong((long) o);
                else if (o instanceof Character) out.writeChar((char) o);
                else if (o instanceof Short) out.writeShort((short) o);
            }
        }
        player.sendPluginMessage(DystellarCore.getInstance(), DystellarCore.CHANNEL, out.toByteArray());
    }
}
