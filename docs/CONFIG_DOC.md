# Config API - Java Plugin Configuration

## Overview
`Config<T>` is a generic configuration utility for Hytale plugins using Gson.
It allows reading and writing JSON files in a type-safe way.

- `T` is your configuration class.
- Gson is used under the hood (`Config.getGson()` for access).
- `Config#load()` creates a new instance of `T` loaded from the file provided. If the file doesn't exist, it tries to generate defaults using the no-args constructor, it will also create the file and fill it with these defaults.
- Fields not meant to be saved can be marked `transient`.

## Creating a Config
```java
Config<Messages> config = new Config<>(this, "messages.json", Messages.class);
```
- `filename` must be JSON.
- `plugin` is the JavaPlugin instance.
- `Messages.class` is your config type.

## Loading and Saving
```java
config.load(); // load or create defaults
Messages messages = config.get(); // access safely
messages.welcomeMessage = "You are now accepting messages.";
config.save(); // save changes
```
Note: Calling `get()` before `load()` throws `IllegalStateException`.

## Example Config Class
```java
public class Messages {
    public String welcomeMessage = "Welcome!"; // saved
    public transient int runtimeCounter; // not saved

    public Messages() {
        this.runtimeCounter = 0;
    }

    public void incrementCounter() {
        runtimeCounter++;
    }
}
```
- The empty constructor sets default values.
- Extra methods and logic are allowed.
- Use `transient` for fields not to be serialized.

## Tips
- Always have a no-args constructor for `T`.
- Gson serializes all non-transient fields, no matter the visibility.
- Keep defaults initialized in the constructor.
