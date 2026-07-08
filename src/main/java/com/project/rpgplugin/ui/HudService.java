package com.project.rpgplugin.ui;

import com.project.rpgplugin.core.progression.RecallProgression;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.skill.SkillRegistry;
import com.project.rpgplugin.core.skill.SkillServices;
import com.project.rpgplugin.util.SchedulerUtil;
import com.project.rpgplugin.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class HudService {

    private final JavaPlugin plugin;
    private final RunManager runManager;
    private final RecallProgression recallProgression;
    private final SkillServices skillServices;
    private final SkillRegistry skillRegistry;
    private BukkitTask task;

    public HudService(JavaPlugin plugin, RunManager runManager, RecallProgression recallProgression,
                      SkillServices skillServices, SkillRegistry skillRegistry) {
        this.plugin = plugin;
        this.runManager = runManager;
        this.recallProgression = recallProgression;
        this.skillServices = skillServices;
        this.skillRegistry = skillRegistry;
    }

    public void start() {
        task = SchedulerUtil.runTimer(plugin, this::tick, 20L, 20L);
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

            String cooldowns = formatCooldowns(p.getUniqueId(), run);
            if (!cooldowns.isEmpty()) {
                actionBar.append(" ").append(cooldowns).append("<gray>|");
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

    private String formatCooldowns(UUID playerId, RunState run) {
        List<String> entries = run.ownedAbilities().stream()
            .filter(id -> skillServices.isOnCooldown(playerId, id))
            .map(id -> {
                long rem = skillServices.cooldownRemaining(playerId, id) / 1000;
                String name = skillRegistry.byId(id)
                    .map(s -> s.id().replace("_", " "))
                    .orElse(id.replace("_", " "));
                return "<white>" + name + " <red>" + rem + "s";
            })
            .toList();
        if (entries.isEmpty()) return "";
        return String.join(" <gray>", entries);
    }
}
