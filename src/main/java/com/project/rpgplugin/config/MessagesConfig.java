package com.project.rpgplugin.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessagesConfig {

    private final JavaPlugin plugin;
    private YamlConfiguration messages;
    private final Map<String, String> cache = new HashMap<>();
    private String lang;

    public MessagesConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getConfig().getString("language", "pt");
        load();
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");
        if (!file.exists()) {
            plugin.saveResource("messages/messages_" + lang + ".yml", false);
            file = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");
        }
        messages = YamlConfiguration.loadConfiguration(file);
        cache.clear();
    }

    public String get(String key) {
        return cache.computeIfAbsent(key, k -> messages.getString(k, "<red>msg " + k + " nao encontrada"));
    }

    public String get(String key, String... args) {
        String msg = get(key);
        for (int i = 0; i < args.length; i++) {
            msg = msg.replace("{" + i + "}", args[i]);
        }
        return msg;
    }

    public void reload() {
        lang = plugin.getConfig().getString("language", "pt");
        load();
    }
}
