package com.project.rpgplugin;

import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.CardKind;
import com.project.rpgplugin.core.progression.GateRegistry;
import com.project.rpgplugin.core.skill.SkillRegistry;
import com.project.rpgplugin.util.Text;
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
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import com.project.rpgplugin.core.mana.ManaService;
import com.project.rpgplugin.core.card.CardTag;
import java.util.*;
import java.util.stream.Collectors;

public class AuraSkillsIntegration implements Listener {

    public static final String NAMESPACE = "roguelata";
    public static final String SKILL_PREFIX = NAMESPACE + "/";

    private final RPGPlugin plugin;
    private final GateRegistry gateRegistry;
    private AuraSkillsApi auraSkills;
    private NamespacedRegistry registry;
    private boolean enabled;
    private ManaService manaService;

    // All our custom skill keys registered in AuraSkills
    private final Set<String> registeredSkillKeys = new HashSet<>();

    private final Map<UUID, PermissionAttachment> slotAttachments = new HashMap<>();

    public AuraSkillsIntegration(RPGPlugin plugin, GateRegistry gateRegistry) {
        this.plugin = plugin;
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
            plugin.getLogger().warning("=== AVISO: Para o HUD do RogueLata funcionar corretamente, desative a action-bar do AuraSkills! ===");
            plugin.getLogger().warning("Edite plugins/AuraSkills/config.yml e defina 'action-bar: false' na secao de habilidades.");
            plugin.getLogger().warning("Caso contrario, a action-bar nativa do AuraSkills colidira com o RogueLata HUDService.");
        } catch (Exception e) {
            plugin.getLogger().warning("Falha ao integrar AuraSkills: " + e.getMessage());
        }
    }

    private void registerCustomSkills() {
        CardRegistry cardRegistry = plugin.getCardRegistry();
        for (Card card : cardRegistry.all()) {
            if (card.kind() != CardKind.ABILITY) continue;
            String key = card.id();
            String displayName = card.id().replace("_", " ");
            Material mat = card.icon();

            String desc = "";
            if (manaService != null && manaService.hasCost(key)) {
                desc = "Mana: " + (int) manaService.getManaCost(key);
            }

            CustomSkill skill = CustomSkill.builder(NamespacedId.of(NAMESPACE, key))
                    .displayName(displayName)
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

    public Map<CardTag, Double> getClassWeights(Player player) {
        if (!enabled) {
            return defaultWeights();
        }
        try {
            SkillsUser user = auraSkills.getUser(player.getUniqueId());
            double explorer = 0;
            explorer += user.getSkillLevel(Skills.AGILITY);
            explorer += user.getSkillLevel(Skills.ARCHERY);
            explorer += user.getSkillLevel(Skills.DEFENSE);
            explorer += user.getSkillLevel(Skills.FIGHTING);

            double miner = 0;
            miner += user.getSkillLevel(Skills.MINING);
            miner += user.getSkillLevel(Skills.EXCAVATION);
            miner += user.getSkillLevel(Skills.ENCHANTING);
            miner += user.getSkillLevel(Skills.FORAGING);

            double builder = 0;
            builder += user.getSkillLevel(Skills.FARMING);
            builder += user.getSkillLevel(Skills.FISHING);
            builder += user.getSkillLevel(Skills.ALCHEMY);
            builder += user.getSkillLevel(Skills.FORAGING);

            double base = 30.0;
            Map<CardTag, Double> weights = new HashMap<>();
            weights.put(CardTag.EXPLORER, base + explorer);
            weights.put(CardTag.MINER, base + miner);
            weights.put(CardTag.BUILDER, base + builder);
            return weights;
        } catch (Exception e) {
            return defaultWeights();
        }
    }

    private Map<CardTag, Double> defaultWeights() {
        return Map.of(
            CardTag.EXPLORER, 100.0,
            CardTag.MINER, 100.0,
            CardTag.BUILDER, 100.0
        );
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void syncSkillSlots(Player player, int totalSlots) {
        if (!enabled) return;
        UUID uuid = player.getUniqueId();
        PermissionAttachment old = slotAttachments.remove(uuid);
        if (old != null) {
            old.remove();
        }
        if (totalSlots <= 0) return;
        PermissionAttachment att = player.addAttachment(plugin);
        for (int i = 1; i <= totalSlots; i++) {
            att.setPermission("auraskills.slot." + i, true);
        }
        slotAttachments.put(uuid, att);
    }

    public void removeSkillSlotAttachment(Player player) {
        UUID uuid = player.getUniqueId();
        PermissionAttachment old = slotAttachments.remove(uuid);
        if (old != null) {
            old.remove();
        }
    }

    public void setManaService(ManaService manaService) {
        this.manaService = manaService;
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

}
