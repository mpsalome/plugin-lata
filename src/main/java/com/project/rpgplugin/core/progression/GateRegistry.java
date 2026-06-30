package com.project.rpgplugin.core.progression;

import com.project.rpgplugin.AuraSkillsIntegration;
import com.project.rpgplugin.PlayerManager;
import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.run.RunState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GateRegistry {

    private final RPGPlugin plugin;
    private final List<Gate> gates = new ArrayList<>();

    public GateRegistry(RPGPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        gates.clear();
        File file = new File(plugin.getDataFolder(), "gates.yml");
        if (!file.exists()) {
            plugin.saveResource("gates.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<?> raw = config.getList("gates");
        if (raw == null) return;
        for (Object obj : raw) {
            if (!(obj instanceof java.util.LinkedHashMap<?,?> map)) continue;
            String skill = (String) map.get("skill");
            int level = (int) map.get("level");
            String card = (String) map.get("card");
            String mode = (String) map.get("mode");
            if (skill != null && card != null && mode != null) {
                gates.add(new Gate(skill, level, card, mode));
            }
        }
    }

    public void check(Player p, String auraSkillName, int auraLevel) {
        PlayerManager playerManager = plugin.getPlayerManager();
        AuraSkillsIntegration auraSkills = plugin.getAuraSkillsIntegration();
        if (playerManager == null || auraSkills == null) return;

        RunState run = null;
        if (plugin.getRunManager() != null) {
            run = plugin.getRunManager().getRun(p);
        }
        if (run == null) return;

        for (Gate gate : gates) {
            if (!gate.skill.equalsIgnoreCase(auraSkillName)) continue;
            if (auraLevel < gate.level) continue;

            if (!playerManager.hasSkill(p, gate.card)) {
                playerManager.unlockSkill(p, gate.card);
                p.sendMessage(com.project.rpgplugin.util.Text.mm("<green><bold>[RogueLata] <green>Nova habilidade desbloqueada: "
                        + playerManager.getSkillDisplayName(gate.card)));
                auraSkills.syncAuraSkillLevel(p, gate.card, 1);
            }

            if ("grant".equalsIgnoreCase(gate.mode) && !run.hasCard(gate.card)) {
                Card card = plugin.getCardRegistry().byId(gate.card).orElse(null);
                if (card != null) {
                    card.onAcquire(p, run);
                    p.sendMessage(com.project.rpgplugin.util.Text.mm("<green><bold>✦ Carta adicionada: " + gate.card.replace("_", " ")));
                }
            }
        }
    }

    private record Gate(String skill, int level, String card, String mode) {}
}
