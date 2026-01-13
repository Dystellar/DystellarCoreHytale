package gg.dystellar.core.serialization;

import gg.dystellar.core.core.punishments.*;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Punishments {

    public static String serializePunishments(Set<Punishment> punishments) {
        StringBuilder sb = new StringBuilder();
        for (Punishment p : punishments) {
            String pun = Punishments.serialize(p);
            sb.append(pun).append(":\\|");
        }
        return sb.toString();
    }

    public static Set<Punishment> deserializePunishments(String s, Set<Punishment> punishmentSet) {
        for (String s1 : s.split(":\\|")) punishmentSet.add(Punishments.deserialize(s1));
        return punishmentSet;
    }

    public static String serialize(Punishment p) {
        StringBuilder builder = new StringBuilder();
        builder.append(p.hashCode()).append(";") // 0
                .append(p.getCreationDate().format(DateTimeFormatter.ISO_DATE_TIME)).append(";") // 1
                .append(p.getExpirationDate() == null ? "null" : p.getExpirationDate().format(DateTimeFormatter.ISO_DATE_TIME)).append(";") // 2
                .append(p.getSerializedId()).append(";") // 3
                .append(p.getReason()); // 4
        if (p instanceof Ban) builder.append(((Ban) p).isAlsoIP()); // 5
        return builder.toString();
    }

    @Nullable
    public static Punishment deserialize(String p) {
        String[] ss = p.split(";");
        int id = Integer.parseInt(ss[0]);
        LocalDateTime creationDate = LocalDateTime.parse(ss[1], DateTimeFormatter.ISO_DATE_TIME);
        LocalDateTime expirationDate = ss[2].equals("null") ? null : LocalDateTime.parse(ss[2], DateTimeFormatter.ISO_DATE_TIME);
        byte serializedId = Byte.parseByte(ss[3]);
        String reason = ss[4];

        switch (serializedId) {
            case Ban.SERIALIZATION_ID: {
                boolean b = Boolean.parseBoolean(ss[5]);
                return new Ban(id, creationDate, expirationDate, reason, b);
            }
            case Blacklist.SERIALIZATION_ID: {
                return new Blacklist(id, creationDate, reason);
            }
            case Mute.SERIALIZATION_ID: {
                return new Mute(id, creationDate, expirationDate, reason);
            }
            case RankedBan.SERIALIZATION_ID: {
                return new RankedBan(id, creationDate, expirationDate, reason);
            }
            case Warn.SERIALIZATION_ID: {
                return new Warn(id, creationDate, expirationDate, reason);
            }
        }
        return null;
    }

    public static String serializeNotes(Set<String> notes) {
        return String.join(";", notes);
    }

    public static Set<String> deserializeNotes(String s) {
        Set<String> notes = new HashSet<>();
        Collections.addAll(notes, s.split(";"));
        return notes;
    }
}
