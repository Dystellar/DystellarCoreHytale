package gg.dystellar.core.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;

import gg.dystellar.core.DystellarCore;
import gg.dystellar.core.api.comms.Channel.ByteBufferOutputStream;
import gg.dystellar.core.messaging.Subchannel;

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

	// TODO: Test this
    public static void resetEffects(Player p) {
		final var ref = p.getReference();
		final var effectStatus = ref.getStore().getComponent(ref, EffectControllerComponent.getComponentType());
		if (effectStatus != null)
			effectStatus.clearEffects(ref, ref.getStore());
		final var stats = ref.getStore().getComponent(ref, EntityStatMap.getComponentType());
		if (stats != null) {
			stats.setStatValue(DefaultEntityStatTypes.getHealth(), 100.0f);
			stats.setStatValue(DefaultEntityStatTypes.getMana(), 100.0f);
			stats.setStatValue(DefaultEntityStatTypes.getOxygen(), 100.0f);
			stats.setStatValue(DefaultEntityStatTypes.getStamina(), 10.0f);
			stats.setStatValue(DefaultEntityStatTypes.getSignatureEnergy(), 0.0f);
		}
    }

	public static void sendPropagatedOutputStream(Subchannel subchannel, int capacity, Consumer<ByteBufferOutputStream> func) {
		sendPropagatedOutputStream(subchannel.ordinal(), capacity, func);
	}

	public static void sendTargetedOutputStream(String target, Subchannel subchannel, int capacity, Consumer<ByteBufferOutputStream> func) {
		sendTargetedOutputStream(target, subchannel.ordinal(), capacity, func);
	}

	public static void sendPropagatedOutputStream(int id, int capacity, Consumer<ByteBufferOutputStream> func) {
		final var channel = DystellarCore.getChannel();
		try {
			final var out = channel.createPropagatedMessageStream(capacity);
			out.write(id);
			func.accept(out);
			channel.sendMessage(out.getBuffer());
		} catch (Exception ignored) {}
	}

	public static void sendTargetedOutputStream(String target, int id, int capacity, Consumer<ByteBufferOutputStream> func) {
		final var channel = DystellarCore.getChannel();
		try {
			final var out = channel.createTargetedMessageStream(target, capacity);
			out.write(id);
			func.accept(out);
			channel.sendMessage(out.getBuffer());
		} catch (Exception ignored) {}
	}
}
