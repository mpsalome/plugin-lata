package com.project.rpgplugin.core.progression;

import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.run.SpawnResolver;
import com.project.rpgplugin.util.Text;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RecallProgression {

    private final JavaPlugin plugin;
    private final RunManager runManager;
    private final SpawnResolver spawnResolver;

    public RecallProgression(JavaPlugin plugin, RunManager runManager, SpawnResolver spawnResolver) {
        this.plugin = plugin;
        this.runManager = runManager;
        this.spawnResolver = spawnResolver;
    }

    public double required(RunState run) {
        double base = getCfg().getDouble("recall.base_distance", 2000);
        double growth = getCfg().getDouble("recall.growth", 1.5);
        double cap = getCfg().getDouble("recall.cap", 0);

        double r = base * Math.pow(growth, run.recallUses());
        return cap > 0 ? Math.min(r, cap) : r;
    }

    public boolean ready(RunState run) {
        return run.hasCard("recall") && run.blocksSinceRecall() >= required(run);
    }

    public void use(Player p, RunState run) {
        if (!ready(run)) {
            p.sendActionBar(Text.mm("<red>Recall ainda não carregado!"));
            return;
        }
        p.teleport(spawnResolver.resolve(p));
        run.incrementRecallUses();
        run.resetBlocksSinceRecall();
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        p.sendActionBar(Text.mm("<light_purple>Recall usado! Próximo em " + (int) required(run) + " blocos."));
    }

    public String progress(RunState run) {
        if (!run.hasCard("recall")) return "";
        long current = run.blocksSinceRecall();
        long needed = (long) required(run);
        return "<light_purple>Recall: " + current + "/" + needed;
    }

    private FileConfiguration getCfg() {
        File file = new File(plugin.getDataFolder(), "skills.yml");
        if (!file.exists()) {
            plugin.saveResource("skills.yml", false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }
}
