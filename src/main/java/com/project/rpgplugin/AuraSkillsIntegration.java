package com.project.rpgplugin;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.event.skill.SkillLevelUpEvent;
import dev.aurelium.auraskills.api.item.ItemContext;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.registry.NamespacedRegistry;
import dev.aurelium.auraskills.api.skill.CustomSkill;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.api.skill.Skills;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class AuraSkillsIntegration implements Listener {

    public static final String NAMESPACE = "roguelata";
    public static final String SKILL_PREFIX = NAMESPACE + "/";

    private final RPGPlugin plugin;
    private final PlayerManager playerManager;
    private AuraSkillsApi auraSkills;
    private NamespacedRegistry registry;
    private boolean enabled;

    // All our custom skill keys registered in AuraSkills
    private final Set<String> registeredSkillKeys = new HashSet<>();

    // Map of AuraSkills default skill + level -> RogueLata skill to unlock
    // e.g., Skills.MINING at level 5 -> "stone_smash"
    private final Map<ProgressionGate, List<String>> progressionGates = new LinkedHashMap<>();

    public AuraSkillsIntegration(RPGPlugin plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.enabled = false;

        Plugin asPlugin = Bukkit.getPluginManager().getPlugin("AuraSkills");
        if (asPlugin == null || !asPlugin.isEnabled()) {
            plugin.getLogger().warning("AuraSkills nao encontrado! Rodando modo standalone.");
            return;
        }

        try {
            this.auraSkills = AuraSkillsApi.get();
            this.registry = auraSkills.useRegistry(NAMESPACE, plugin.getDataFolder());
            registerCustomSkills();
            setupProgressionGates();
            Bukkit.getPluginManager().registerEvents(this, plugin);
            this.enabled = true;
            plugin.getLogger().info("AuraSkills integrado com sucesso! " + registeredSkillKeys.size() + " skills registradas.");
        } catch (Exception e) {
            plugin.getLogger().warning("Falha ao integrar AuraSkills: " + e.getMessage());
        }
    }

    private void registerCustomSkills() {
        List<String> allKeys = playerManager.getAllSkillKeys();
        for (String key : allKeys) {
            String displayName = playerManager.getSkillDisplayName(key);
            String desc = playerManager.getSkillDescription(key);
            Material mat = playerManager.determineSkillMaterial(key);

            CustomSkill skill = CustomSkill.builder(NamespacedId.of(NAMESPACE, key))
                    .displayName(stripColorCodes(displayName))
                    .description(desc)
                    .item(ItemContext.builder().material(mat.name().toLowerCase()).build())
                    .build();

            registry.registerSkill(skill);
            registeredSkillKeys.add(key);
        }
    }

    private void setupProgressionGates() {
        // Explorer skills -> Agility levels
        progressionGates.put(new ProgressionGate(Skills.AGILITY, 2), List.of("dash"));
        progressionGates.put(new ProgressionGate(Skills.AGILITY, 4), List.of("step_assist"));
        progressionGates.put(new ProgressionGate(Skills.AGILITY, 6), List.of("grapple"));
        progressionGates.put(new ProgressionGate(Skills.AGILITY, 8), List.of("safe_fall"));
        progressionGates.put(new ProgressionGate(Skills.AGILITY, 10), List.of("jump_boost"));
        progressionGates.put(new ProgressionGate(Skills.AGILITY, 12), List.of("wind_burst"));
        progressionGates.put(new ProgressionGate(Skills.AGILITY, 14), List.of("dim_shift"));
        progressionGates.put(new ProgressionGate(Skills.AGILITY, 16), List.of("sonar"));
        progressionGates.put(new ProgressionGate(Skills.FIGHTING, 10), List.of("recall"));
        progressionGates.put(new ProgressionGate(Skills.FIGHTING, 5), List.of("thermal_resistance"));
        progressionGates.put(new ProgressionGate(Skills.FIGHTING, 8), List.of("water_breathing"));

        // Miner skills -> Mining levels
        progressionGates.put(new ProgressionGate(Skills.MINING, 2), List.of("stone_smash"));
        progressionGates.put(new ProgressionGate(Skills.MINING, 4), List.of("torch_light"));
        progressionGates.put(new ProgressionGate(Skills.MINING, 5), List.of("diet"));
        progressionGates.put(new ProgressionGate(Skills.MINING, 7), List.of("ore_sonar"));
        progressionGates.put(new ProgressionGate(Skills.MINING, 10), List.of("haste"));
        progressionGates.put(new ProgressionGate(Skills.MINING, 12), List.of("sight"));
        progressionGates.put(new ProgressionGate(Skills.MINING, 14), List.of("ore_repair"));
        progressionGates.put(new ProgressionGate(Skills.MINING, 16), List.of("molten_touch"));
        progressionGates.put(new ProgressionGate(Skills.MINING, 18), List.of("gravity_shield"));
        progressionGates.put(new ProgressionGate(Skills.MINING, 20), List.of("core_overdrive"));
        progressionGates.put(new ProgressionGate(Skills.ENCHANTING, 15), List.of("transmutation"));

        // Builder skills -> Foraging levels
        progressionGates.put(new ProgressionGate(Skills.FORAGING, 2), List.of("feast"));
        progressionGates.put(new ProgressionGate(Skills.FORAGING, 4), List.of("woodcutter"));
        progressionGates.put(new ProgressionGate(Skills.FORAGING, 5), List.of("canopy_step"));
        progressionGates.put(new ProgressionGate(Skills.FORAGING, 7), List.of("fertilize"));
        progressionGates.put(new ProgressionGate(Skills.FORAGING, 10), List.of("flora_shield"));
        progressionGates.put(new ProgressionGate(Skills.FORAGING, 12), List.of("scaffold"));
        progressionGates.put(new ProgressionGate(Skills.FORAGING, 14), List.of("silk_touch"));
        progressionGates.put(new ProgressionGate(Skills.FORAGING, 16), List.of("lumberjack"));
        progressionGates.put(new ProgressionGate(Skills.FORAGING, 18), List.of("architect_focus"));
        progressionGates.put(new ProgressionGate(Skills.FORAGING, 20), List.of("grace"));
        progressionGates.put(new ProgressionGate(Skills.FORAGING, 22), List.of("unbreakable_block"));
        progressionGates.put(new ProgressionGate(Skills.FORAGING, 24), List.of("gravity_defiance"));
        progressionGates.put(new ProgressionGate(Skills.FORAGING, 26), List.of("hydration"));
    }

    @EventHandler
    public void onSkillLevelUp(SkillLevelUpEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();
        if (player == null) return;

        Skill skill = event.getSkill();
        int level = event.getLevel();

        for (Map.Entry<ProgressionGate, List<String>> entry : progressionGates.entrySet()) {
            ProgressionGate gate = entry.getKey();
            if (gate.skill.equals(skill) && level >= gate.level) {
                for (String rogueKey : entry.getValue()) {
                    if (!playerManager.hasSkill(player, rogueKey)) {
                        playerManager.unlockSkill(player, rogueKey);
                        player.sendMessage("§a§l[RogueLata] §aNova habilidade desbloqueada: "
                                + playerManager.getSkillDisplayName(rogueKey));
                        syncAuraSkillLevel(player, rogueKey, 1);
                    }
                }
            }
        }
    }

    public void syncAuraSkillLevel(Player player, String rogueSkillKey, int level) {
        if (!enabled) return;
        try {
            SkillsUser user = auraSkills.getUser(player.getUniqueId());
            Skill skill = auraSkills.getGlobalRegistry().getSkill(NamespacedId.of(NAMESPACE, rogueSkillKey));
            if (skill != null) {
                user.setSkillLevel(skill, level);
            }
        } catch (Exception ignored) {}
    }

    public void resetPlayerSkills(Player player) {
        if (!enabled) return;
        try {
            SkillsUser user = auraSkills.getUser(player.getUniqueId());
            for (String key : registeredSkillKeys) {
                Skill skill = auraSkills.getGlobalRegistry().getSkill(NamespacedId.of(NAMESPACE, key));
                if (skill != null) {
                    user.setSkillLevel(skill, 0);
                }
            }
        } catch (Exception ignored) {}
    }

    public void resetAllAuraSkills(Player player) {
        if (!enabled) return;
        try {
            SkillsUser user = auraSkills.getUser(player.getUniqueId());
            for (Skills s : Skills.values()) {
                user.setSkillLevel(s, 0);
                user.setSkillXp(s, 0.0);
            }
            resetPlayerSkills(player);
        } catch (Exception ignored) {}
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static String marker(String skillKey) {
        return SKILL_PREFIX + skillKey;
    }

    public static boolean isRogueLataSkill(String skillId) {
        return skillId.startsWith(SKILL_PREFIX);
    }

    public static String stripRogueLataPrefix(String skillId) {
        if (isRogueLataSkill(skillId)) {
            return skillId.substring(SKILL_PREFIX.length());
        }
        return skillId;
    }

    private String stripColorCodes(String input) {
        return input.replaceAll("§[0-9a-fklmnor]", "").trim();
    }

    private static class ProgressionGate {
        final Skill skill;
        final int level;

        ProgressionGate(Skill skill, int level) {
            this.skill = skill;
            this.level = level;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProgressionGate that = (ProgressionGate) o;
            return level == that.level && Objects.equals(skill, that.skill);
        }

        @Override
        public int hashCode() {
            return Objects.hash(skill, level);
        }
    }
}
