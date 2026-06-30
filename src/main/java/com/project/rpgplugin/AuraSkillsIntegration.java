package com.project.rpgplugin;

import com.project.rpgplugin.core.progression.GateRegistry;
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
    private final GateRegistry gateRegistry;
    private AuraSkillsApi auraSkills;
    private NamespacedRegistry registry;
    private boolean enabled;

    // All our custom skill keys registered in AuraSkills
    private final Set<String> registeredSkillKeys = new HashSet<>();

    public AuraSkillsIntegration(RPGPlugin plugin, PlayerManager playerManager, GateRegistry gateRegistry) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.gateRegistry = gateRegistry;
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

    @EventHandler
    public void onSkillLevelUp(SkillLevelUpEvent event) {
        if (!enabled) return;
        Player player = event.getPlayer();
        if (player == null) return;

        Skill skill = event.getSkill();
        int level = event.getLevel();

        gateRegistry.check(player, skill.name(), level);
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
}
