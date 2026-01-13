package gg.dystellar.core.serialization;

import net.zylesh.dystellarcore.core.punishments.Punishment;

import java.util.Set;
import java.util.UUID;

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
