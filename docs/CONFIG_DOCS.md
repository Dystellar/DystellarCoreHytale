# Config API - Java Plugin Configuration

## Overview
`Config<T>` is a generic configuration utility for Hytale plugins using Gson.
It allows reading and writing JSON files in a type-safe way.

- `T` is your configuration class.
- Gson is used under the hood (`Config.getGson()` for access).
- `Config#load()` creates a new instance of `T` using the no-args constructor if the file is missing.
- Fields not meant to be saved can be marked `transient`.

## Creating a Config
```java
Config<Messages> lang_en = new Config<>(this, "lang_en.json", Messages.class);
```
- `filename` must be JSON.
- `plugin` is the JavaPlugin instance.
- `Messages.class` is your config type.

## Loading and Saving
```java
lang_en.load(); // load or create defaults
Messages messages = lang_en.get(); // access safely
messages.pms_enabled = "<Green>You are now accepting messages.";
lang_en.save(); // save changes
```
Note: Calling `get()` before `load()` throws `IllegalStateException`.

## Example Config Class
```java
public class Messages {
    public String welcomeMessage = "Welcome!"; // saved
    public transient int runtimeCounter; // not saved

    public Messages() {
        this.welcomeMessage = "Welcome!";
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
- Gson serializes public or non-transient fields.
- Keep defaults initialized in the constructor.

## Advanced
Access the internal Gson instance for custom serialization:
```java
Gson gson = Config.getGson();
```

