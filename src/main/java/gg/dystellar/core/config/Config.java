package gg.dystellar.core.config;

/**
 * Plugin configuration, these are just values because after investigating the hytale API,
 * I think what it does is load a json file into a java object, and uses reflection
 * to forcefully modify a class's attributes, so the json config in theory would be dumped
 * into this values.
 *
 * This should be tested though, as it's just my conclusion after reading the source code, and it's pure speculation.
 */
public class Config {

	public final boolean debug_mode = false;
	public final boolean scoreboard = false;
	public final boolean allow_banned_players = false;
	public final boolean resourcepack = false;
	public final String resourcepack_url = "https://example.com";
	public final String resourcepack_sha1 = "4f4257210bd4019cf085338fbc90d9d3128960a5";
	public final boolean automated_messages = false;
	public final int automated_messages_rate = 240; // In seconds
	public final boolean prevent_weather = true;
	public final String api = "http://localhost"; // Backend url
	public final String api_key = "secretkey"; // Provided by the backend
}
