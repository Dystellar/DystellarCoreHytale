package gg.dystellar.core.serialization;

public final class Protocol {
	private Protocol() {}

	public static final class PunishParams {
		public String user_uuid;
		public String title;
		public String type;
		public long creation_date;
		public Long expiration_date;
		public String reason;
		public boolean alsoip;
		public boolean allow_chat;
		public boolean allow_ranked;
		public boolean allow_unranked;
		public boolean allow_join_minigames;
	}
}
