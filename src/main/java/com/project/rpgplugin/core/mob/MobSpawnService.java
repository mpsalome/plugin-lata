package com.project.rpgplugin.core.mob;

import com.project.rpgplugin.integration.ModelEngineBridge;
import com.project.rpgplugin.integration.MythicMobsBridge;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MobSpawnService {

    private final JavaPlugin plugin;
    private final EliteFactory eliteFactory;
    private final RunManager runManager;
    private final Map<String, EliteFactory.BossDef> bossDefs = new ConcurrentHashMap<>();
    private final Map<String, EliteFactory.MobDef> mobDefs = new ConcurrentHashMap<>();

    public MobSpawnService(JavaPlugin plugin, RunManager runManager, MythicMobsBridge mythicMobs, ModelEngineBridge modelEngine) {
        this.plugin = plugin;
        this.eliteFactory = new EliteFactory(plugin, mythicMobs, modelEngine);
        plugin.getServer().getPluginManager().registerEvents(eliteFactory, plugin);
        this.runManager = runManager;
        loadConfigs();
    }

    public void loadConfigs() {
        loadBosses();
        loadMobs();
    }

    private void loadBosses() {
        bossDefs.clear();
        File file = new File(plugin.getDataFolder(), "bosses.yml");
        if (!file.exists()) {
            plugin.saveResource("bosses.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = config.getConfigurationSection("bosses");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            ConfigurationSection b = sec.getConfigurationSection(key);
            if (b == null) continue;
            String baseTypeStr = b.getString("base_type", "ZOMBIE").toUpperCase();
            EntityType type;
            try {
                type = EntityType.valueOf(baseTypeStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("MobSpawnService: EntityType invalido '" + baseTypeStr + "' no boss '" + key + "'. Usando ZOMBIE.");
                type = EntityType.ZOMBIE;
            }
            String name = b.getString("display_name", key);
            double hp = b.getDouble("health", 100);
            double dmg = b.getDouble("damage", 10);
            double speed = b.getDouble("speed", 0.25);
            double scale = b.getDouble("scale", 1.0);
            double kb = b.getDouble("knockback_resist", 0.5);
            boolean victory = b.getBoolean("victory", true);
            Map<String, ItemStack> eq = parseEquipment(b.getConfigurationSection("equipment"));
            bossDefs.put(key, new EliteFactory.BossDef(key, type, name, hp, dmg, speed, scale, kb, victory, eq));
        }
    }

    private void loadMobs() {
        mobDefs.clear();
        File file = new File(plugin.getDataFolder(), "mobs.yml");
        if (!file.exists()) {
            plugin.saveResource("mobs.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = config.getConfigurationSection("elites");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            ConfigurationSection m = sec.getConfigurationSection(key);
            if (m == null) continue;
            String baseTypeStr = m.getString("base_type", "ZOMBIE").toUpperCase();
            EntityType type;
            try {
                type = EntityType.valueOf(baseTypeStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("MobSpawnService: EntityType invalido '" + baseTypeStr + "' no mob '" + key + "'. Usando ZOMBIE.");
                type = EntityType.ZOMBIE;
            }
            String name = m.getString("display_name", key);
            double hp = m.getDouble("health", 40);
            double dmg = m.getDouble("damage", 6);
            double scale = m.getDouble("scale", 1.2);
            Map<String, ItemStack> eq = parseEquipment(m.getConfigurationSection("equipment"));
            mobDefs.put(key, new EliteFactory.MobDef(key, type, name, hp, dmg, scale, eq));
        }
    }

    public EliteFactory getEliteFactory() { return eliteFactory; }

    public EliteFactory.BossDef getBossDef(String id) {
        return bossDefs.get(id);
    }

    public EliteFactory.MobDef getMobDef(String id) {
        return mobDefs.get(id);
    }

    public void spawnElite(String id, Location loc) {
        EliteFactory.MobDef def = mobDefs.get(id);
        if (def != null) {
            eliteFactory.spawnElite(loc, def);
        }
    }

    public void spawnBoss(String id, Location loc) {
        EliteFactory.BossDef def = bossDefs.get(id);
        if (def != null) {
            eliteFactory.spawnBoss(loc, def);
        }
    }

    public void trySpawnBossForRun(Player p) {
        RunState run = runManager.getRun(p);
        if (run == null) return;
        if (run.milestonesReached() >= 5) {
            spawnBoss("frostmaw", p.getLocation().add(5, 0, 5));
        }
    }

    private Map<String, ItemStack> parseEquipment(ConfigurationSection sec) {
        if (sec == null) return null;
        Map<String, ItemStack> eq = new java.util.HashMap<>();
        for (String slot : sec.getKeys(false)) {
            String matName = sec.getString(slot, "AIR").toUpperCase();
            Material mat = Material.getMaterial(matName);
            if (mat != null && mat != Material.AIR) {
                eq.put(slot, new ItemStack(mat));
            }
        }
        return eq.isEmpty() ? null : eq;
    }
}
