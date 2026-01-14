package gg.dystellar.core.serialization;

import net.zylesh.dystellarcore.core.User;
import net.zylesh.dystellarcore.core.inbox.Inbox;
import net.zylesh.dystellarcore.core.inbox.Sendable;
import net.zylesh.dystellarcore.core.inbox.senders.CoinsReward;
import net.zylesh.dystellarcore.core.inbox.senders.EloGainNotifier;
import net.zylesh.dystellarcore.core.inbox.senders.Message;
import net.zylesh.dystellarcore.core.inbox.senders.prewards.PKillEffectReward;
import net.zylesh.practice.PKillEffect;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Serializes an entire inbox with mails.
 * This should probably be reimplemented.
 */
public class InboxSerialization {

    private static final String SEPARATOR_FIELDS = "-%;%+";
    private static final String SEPARATOR = "Il\\|lI";

    public static String inboxToString(Inbox inbox) {
        StringBuilder builder = new StringBuilder();
        for (Sendable sender : inbox.getSenders()) {
            builder.append(senderToString(sender, sender.getSerialID())).append(SEPARATOR);
        }
        return builder.toString();
    }

    public static Inbox stringToInbox(String s, User user) {
        String[] split = s.split(SEPARATOR);
        Inbox inbox = new Inbox(user);
        for (String string : split) {
            Sendable sender = stringToSender(string, inbox);
            inbox.getSenders().add(sender);
        }
        inbox.update();
        return inbox;
    }

    public static String senderToString(Sendable sender, byte serial) {
        StringBuilder builder = new StringBuilder();
        String submission = sender.getSubmissionDate().format(DateTimeFormatter.ISO_DATE_TIME);
        builder.append(serial)
                .append(SEPARATOR_FIELDS)
                .append(sender.getId())
                .append(SEPARATOR_FIELDS)
                .append(submission)
                .append(SEPARATOR_FIELDS);
        switch (serial) {
            case Message.ID: {
                if (!(sender instanceof Message)) throw new IllegalArgumentException("Invalid sender implementation.");
                break;
            }
            case EloGainNotifier.ID: {
                if (!(sender instanceof EloGainNotifier)) throw new IllegalArgumentException("Invalid sender implementation.");
                EloGainNotifier elo = (EloGainNotifier) sender;
                builder.append(elo.getElo())
                        .append(SEPARATOR_FIELDS)
                        .append(elo.isClaimed())
                        .append(SEPARATOR_FIELDS)
                        .append(elo.getCompatibilityType())
                        .append(SEPARATOR_FIELDS);
                if (elo.getCompatibilityType() == EloGainNotifier.PRACTICE)
                    builder.append(elo.getLadder())
                            .append(SEPARATOR_FIELDS);
                break;
            }
            case CoinsReward.ID: {
                if (!(sender instanceof CoinsReward)) throw new IllegalArgumentException("Invalid sender implementation.");
                CoinsReward reward = (CoinsReward) sender;
                builder.append(reward.getTitle())
                        .append(SEPARATOR_FIELDS)
                        .append(reward.isClaimed())
                        .append(SEPARATOR_FIELDS)
                        .append(reward.getCoins())
                        .append(SEPARATOR_FIELDS);
                break;
            }
            case PKillEffectReward.ID: {
                if (!(sender instanceof PKillEffectReward)) throw new IllegalArgumentException("Invalid sender implementation.");
                PKillEffectReward reward = (PKillEffectReward) sender;
                builder.append(reward.getTitle())
                        .append(SEPARATOR_FIELDS)
                        .append(reward.isClaimed())
                        .append(SEPARATOR_FIELDS)
                        .append(reward.getKillEffect().name())
                        .append(SEPARATOR_FIELDS);
                break;
            }
            default: throw new IllegalArgumentException("Invalid sender implementation.");
        }
        String from = sender.getFrom();
        boolean deleted = sender.isDeleted();
        builder.append(from)
                .append(SEPARATOR_FIELDS)
                .append(deleted)
                .append(SEPARATOR_FIELDS);
        builder.append(((Message) sender).getSerializedMessage());
        return builder.toString();
    }

    public static Sendable stringToSender(String s, @Nullable Inbox inbox) {
        String[] split = s.split(SEPARATOR_FIELDS);
        Sendable sender = null;
        byte serial = Byte.parseByte(split[0]);
        int id = Integer.parseInt(split[1]);
        LocalDateTime submission = LocalDateTime.parse(split[2], DateTimeFormatter.ISO_DATE_TIME);
        switch (serial) {
            case Message.ID: {
                String from = split[3];
                boolean deleted = Boolean.parseBoolean(split[4]);
                String[] message = split[5].split(":;");
                sender = new Message(inbox, id, from, message, submission, deleted);
                break;
            }
            case EloGainNotifier.ID: {
                int elo = Integer.parseInt(split[3]);
                boolean claimed = Boolean.parseBoolean(split[4]);
                byte compatibility = Byte.parseByte(split[5]);
                if (compatibility == EloGainNotifier.PRACTICE) {
                    String ladder = split[6];
                    String from = split[7];
                    boolean deleted = Boolean.parseBoolean(split[8]);
                    String[] message = split[9].split(":;");
                    sender = new EloGainNotifier(inbox, id, elo, compatibility, ladder, from, message, submission, deleted, claimed);
                } else if (compatibility == EloGainNotifier.SKYWARS) {
                    String from = split[6];
                    boolean deleted = Boolean.parseBoolean(split[7]);
                    String[] message = split[8].split(":;");
                    sender = new EloGainNotifier(inbox, id, elo, compatibility, null, from, message, submission, deleted, claimed);
                }
                break;
            }
            case CoinsReward.ID: {
                String title = split[3];
                boolean claimed = Boolean.parseBoolean(split[4]);
                int coins = Integer.parseInt(split[5]);
                String from = split[6];
                boolean deleted = Boolean.parseBoolean(split[7]);
                String[] message = split[8].split(":;");
                sender = new CoinsReward(inbox, id, from, message, submission, deleted, title, claimed, coins);
                break;
            }
            case PKillEffectReward.ID: {
                String title = split[3];
                boolean claimed = Boolean.parseBoolean(split[4]);
                PKillEffect effect = PKillEffect.valueOf(split[5]);
                String from = split[6];
                boolean deleted = Boolean.parseBoolean(split[7]);
                String[] message = split[8].split(":;");
                sender = new PKillEffectReward(inbox, id, from, message, submission, deleted, title, claimed, effect);
                break;
            }
            default: throw new IllegalArgumentException("Invalid sender implementation.");
        }
        return sender;
    }
}
