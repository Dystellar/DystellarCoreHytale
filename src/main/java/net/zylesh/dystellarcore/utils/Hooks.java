package net.zylesh.dystellarcore.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Hooks {

    public static boolean SKYWARS_HOOK = false;
    public static boolean PRACTICE_HOOK = false;

    private static void registerSkywarsHook() {
        SKYWARS_HOOK = true;
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Dystellar] Hooked into SkyWars plugin.");
    }

    private static void registerPracticeHook() {
        PRACTICE_HOOK = true;
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Dystellar] Hooked into Practice plugin.");
    }

    public static void registerHooks() {
        if (Bukkit.getPluginManager().getPlugin("SkyWars-Core") != null) registerSkywarsHook();
        if (Bukkit.getPluginManager().getPlugin("Practice-Core") != null) registerPracticeHook();
    }
}
