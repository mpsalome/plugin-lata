package com.project.rpgplugin.ui;

import com.project.rpgplugin.core.progression.RecallProgression;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;

public class HudService {

    private final JavaPlugin plugin;
    private final RunManager runManager;
    private final RecallProgression recallProgression;
    private BukkitTask task;

    public HudService(JavaPlugin plugin, RunManager runManager, RecallProgression recallProgression) {
        this.plugin = plugin;
        this.runManager = runManager;
        this.recallProgression = recallProgression;
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 40L, 40L);
    }

    public void stop() {
        if (task != null) task.cancel();
    }

    private void tick() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            RunState run = runManager.getRun(p);
            if (run == null) continue;

            StringBuilder actionBar = new StringBuilder();
            actionBar.append("<gold>RogueLata <gray>|");

            if (run.ownedCards().size() > 0) {
                actionBar.append(" <yellow>").append(run.ownedCards().size()).append(" cartas <gray>|");
            }

            if (recallProgression != null && run.hasCard("recall")) {
                double req = recallProgression.required(run);
                long prog = run.blocksSinceRecall();
                actionBar.append(" <light_purple>Recall: ").append(prog).append("/").append((long) req).append(" <gray>|");
            }

            Set<String> mods = run.activeModifiers();
            if (!mods.isEmpty()) {
                actionBar.append(" <red>");
                mods.forEach(m -> actionBar.append("[").append(m.replace("_", " ")).append("] "));
            }

            p.sendActionBar(Text.mm(actionBar.toString()));
        }
    }
}
