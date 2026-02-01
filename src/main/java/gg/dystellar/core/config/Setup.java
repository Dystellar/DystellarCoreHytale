package gg.dystellar.core.config;

/**
 * Plugin base configuration
 */
public class Setup {

	public boolean debug_mode = false;
	public boolean scoreboard = false;
	public boolean allow_banned_players = false;
	public boolean resourcepack = false;
	public boolean automated_messages = false;
	public int automated_messages_rate = 240; // In seconds
	public boolean prevent_weather = true;
	public String api = "http://localhost"; // Backend url
	public String api_key = "secretkey"; // Provided by the backend
}
