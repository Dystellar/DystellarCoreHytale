package gg.dystellar.core.serialization;

import net.zylesh.dystellarcore.core.punishments.Punishment;

import java.util.Set;
import java.util.UUID;

/**
 * Whatever weird hack this is coming from me not knowing how to properly use a database.
 * This is a mapping of ip, name and uuid to a set of punishments, used for some kind of IP banning implementation?
 *
 * Anything else would be better than this.
 */
public class Mapping {

    private final UUID uuid;
    private final String IP;
    private final String name;
    private final Set<Punishment> punishments;

    public Mapping(UUID uuid, String IP, String name, Set<Punishment> punishments) {
        this.uuid = uuid;
        this.name = name;
        this.IP = IP;
        this.punishments = punishments;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getIP() {
        return IP;
    }

    public Set<Punishment> getPunishments() {
        return punishments;
    }
}
