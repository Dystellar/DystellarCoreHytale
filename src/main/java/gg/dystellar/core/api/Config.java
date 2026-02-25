package gg.dystellar.core.api;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;

/**
 * Common configuration utils.
 *
 * The contents of the underlying T instance can be freely modified,
 * when Config#save() is used it will update accordingly.
 */
public final class Config<T> implements Supplier<T> {

	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.registerTypeAdapter(Boolean.class, new TypeAdapter<Boolean>() {
			@Override
			public Boolean read(JsonReader arg0) throws IOException { return arg0.nextBoolean(); }

			@Override
			public void write(JsonWriter arg0, Boolean arg1) throws IOException {
				if (Boolean.TRUE.equals(arg1))
					arg0.value(true);
				else arg0.nullValue();
			}
		})
		.create();
	
	public static Gson getGson() { return GSON; }

	private T value;

	private final Class<T> clazz;
	public final File file;

	/**
	 * Create a managed configuration instance.
	 * The file must be a json file.
	 *
	 * @param plugin The plugin where this configuration is from
	 * @param filename Name of the file, only name, no path
	 * @param type configuration type class e.g. MyAwesomeConfig.class
	 */
	public Config(JavaPlugin plugin, String filename, Class<T> type) {
		this.file = new File(plugin.getDataDirectory().toFile(), filename);
		this.clazz = type;
	}

	/**
	 * Loads the configuration from the file.
	 * If the file doesn't exist, it tries to create a new file,
	 * and will be filled with the defaults.
	 *
	 * To get the defaults, it will try to instantiate a constructor of T with no parameters,
	 * and will fail if there is no such constructor.
	 */
	public void load() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException, IOException {
		if (!this.file.exists()) {
			file.mkdirs();
			this.value = this.clazz.getConstructor().newInstance();

			this.save();
		} else {
			FileReader r = new FileReader(this.file);

			this.value = GSON.fromJson(r, this.clazz);
			r.close();
		}
	}

	/**
	 * Save the contents of T value into the file as json
	 */
	public void save() throws IOException {
		if (this.value != null) {
			FileWriter w = new FileWriter(this.file);

			w.write(GSON.toJson(this.value));
			w.close();
		}
	}

	/**
	 * WARNING! If Config#load() is not called, this will throw IllegalStateException.
	 */
	@Override
	public T get() {
		if (this.value == null)
			throw new IllegalStateException("Called get but the config wasn't loaded yet. Call Config#load() first");
	    return this.value;
	}
}
