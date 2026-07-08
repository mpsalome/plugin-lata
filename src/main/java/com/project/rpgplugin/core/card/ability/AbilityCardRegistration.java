package com.project.rpgplugin.core.card.ability;

import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.CardTag;
import com.project.rpgplugin.core.skill.SkillRegistry;
import com.project.rpgplugin.core.skill.Skill;

import java.util.Map;
import java.util.Set;
import java.util.List;

public final class AbilityCardRegistration {

    private AbilityCardRegistration() {}

    private static final Map<String, List<CardTag>> SKILL_TAGS = Map.ofEntries(
        // Explorer
        Map.entry("dash", List.of(CardTag.EXPLORER, CardTag.MOBILITY)),
        Map.entry("hydration", List.of(CardTag.EXPLORER, CardTag.SUSTAIN)),
        Map.entry("grapple", List.of(CardTag.EXPLORER, CardTag.MOBILITY)),
        Map.entry("safe_fall", List.of(CardTag.EXPLORER, CardTag.TANK)),
        Map.entry("water_breathing", List.of(CardTag.EXPLORER, CardTag.UTILITY)),
        Map.entry("jump_boost", List.of(CardTag.EXPLORER, CardTag.MOBILITY)),
        Map.entry("thermal_resistance", List.of(CardTag.EXPLORER, CardTag.TANK)),
        Map.entry("recall", List.of(CardTag.EXPLORER, CardTag.UTILITY)),
        Map.entry("sonar", List.of(CardTag.EXPLORER, CardTag.UTILITY)),
        Map.entry("dim_shift", List.of(CardTag.EXPLORER, CardTag.MOBILITY, CardTag.RISK)),
        Map.entry("wind_burst", List.of(CardTag.EXPLORER, CardTag.MOBILITY)),
        Map.entry("harpoon_pull", List.of(CardTag.EXPLORER, CardTag.DPS, CardTag.MOBILITY)),
        Map.entry("blade_dance", List.of(CardTag.EXPLORER, CardTag.DPS, CardTag.MOBILITY)),
        // Miner
        Map.entry("diet", List.of(CardTag.MINER, CardTag.SUSTAIN)),
        Map.entry("stone_smash", List.of(CardTag.MINER, CardTag.DPS)),
        Map.entry("torch_light", List.of(CardTag.MINER, CardTag.UTILITY)),
        Map.entry("ore_sonar", List.of(CardTag.MINER, CardTag.UTILITY)),
        Map.entry("haste", List.of(CardTag.MINER, CardTag.DPS)),
        Map.entry("ore_repair", List.of(CardTag.MINER, CardTag.UTILITY)),
        Map.entry("transmutation", List.of(CardTag.MINER, CardTag.ECONOMY)),
        Map.entry("gravity_shield", List.of(CardTag.MINER, CardTag.TANK)),
        Map.entry("core_overdrive", List.of(CardTag.MINER, CardTag.RISK, CardTag.DPS)),
        Map.entry("sight", List.of(CardTag.MINER, CardTag.UTILITY)),
        Map.entry("seismic_slam", List.of(CardTag.MINER, CardTag.DPS)),
        Map.entry("gold_rush_shield", List.of(CardTag.MINER, CardTag.TANK, CardTag.ECONOMY)),
        Map.entry("unstable_core", List.of(CardTag.MINER, CardTag.DPS, CardTag.RISK)),
        // Builder
        Map.entry("feast", List.of(CardTag.BUILDER, CardTag.SUSTAIN)),
        Map.entry("woodcutter", List.of(CardTag.BUILDER, CardTag.DPS)),
        Map.entry("silk_touch", List.of(CardTag.BUILDER, CardTag.UTILITY)),
        Map.entry("scaffold", List.of(CardTag.BUILDER, CardTag.MOBILITY)),
        Map.entry("canopy_step", List.of(CardTag.BUILDER, CardTag.MOBILITY)),
        Map.entry("flora_shield", List.of(CardTag.BUILDER, CardTag.SUSTAIN)),
        Map.entry("architect_focus", List.of(CardTag.BUILDER, CardTag.TANK, CardTag.RISK)),
        Map.entry("gravity_defiance", List.of(CardTag.BUILDER, CardTag.MOBILITY, CardTag.TANK)),
        Map.entry("unbreakable_block", List.of(CardTag.BUILDER, CardTag.TANK, CardTag.UTILITY)),
        Map.entry("sentry_turret", List.of(CardTag.BUILDER, CardTag.DPS, CardTag.UTILITY)),
        Map.entry("entangling_roots", List.of(CardTag.BUILDER, CardTag.DPS, CardTag.TANK)),
        Map.entry("fortress_protocol", List.of(CardTag.BUILDER, CardTag.TANK, CardTag.SUSTAIN))
    );

    public static void registerAll(CardRegistry cardRegistry, SkillRegistry skillRegistry) {
        for (Skill skill : skillRegistry.all()) {
            Set<CardTag> tags = Set.copyOf(SKILL_TAGS.getOrDefault(skill.id(), List.of()));
            cardRegistry.register(new AbilityCard(skill, tags));
        }
    }
}
