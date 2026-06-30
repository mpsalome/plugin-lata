package com.project.rpgplugin.core.run;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Random;

public class SpawnResolver {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public SpawnResolver(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Location resolve(Player p) {
        String mode = getConfig().getString("spawn_resolver.mode", "world_spawn");
        return switch (mode) {
            case "random_far" -> resolveRandomFar(p.getWorld());
            default -> resolveWorldSpawn(p);
        };
    }

    private Location resolveWorldSpawn(Player p) {
        return p.getWorld().getSpawnLocation();
    }

    private Location resolveRandomFar(World w) {
        int minRadius = getConfig().getInt("spawn_resolver.random_far.min_radius", 5000);
        int maxRadius = getConfig().getInt("spawn_resolver.random_far.max_radius", 20000);

        double angle = random.nextDouble() * 2 * Math.PI;
        int distance = minRadius + random.nextInt(maxRadius - minRadius + 1);
        int x = (int) (Math.cos(angle) * distance);
        int z = (int) (Math.sin(angle) * distance);

        int y = w.getHighestBlockYAt(x, z);
        if (y < 1) y = 64;
        return new Location(w, x + 0.5, y + 1, z + 0.5);
    }

    private FileConfiguration getConfig() {
        File file = new File(plugin.getDataFolder(), "run.yml");
        if (!file.exists()) {
            plugin.saveResource("run.yml", false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }
}
