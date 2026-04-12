package gg.dystellar.core.config;

/**
 * Plugin base configuration
 */
public class Setup {
	public boolean debug_mode = false;
	public String server_name = "lobby";
	public boolean scoreboard = false;
	public boolean allow_banned_players = false;
	public boolean resourcepack = false;
	public boolean automated_messages = false;
	public int automated_messages_rate = 240; // In seconds
	public boolean prevent_weather = true;
	public String forced_weather = "Zone1_Sunny";
	public String public_ip = "53.63.213.73"; // IP that clients use to connect
	public String host = "localhost";
	public String api = "http://localhost"; // Backend url
	public String websocket_endpoint = "ws://localhost/api/core/create_ws";
	public String api_key = "secretkey"; // Provided by the backend
}
