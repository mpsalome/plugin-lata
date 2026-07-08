package com.project.rpgplugin.skill;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillRegistry;
import com.project.rpgplugin.core.skill.SkillServices;

/**
 * Test helper that creates a fully populated SkillRegistry
 * using lightweight skill constructors for metadata verification.
 * <p>
 * Skill activation is NOT tested — only metadata (id, tier, type, icon, trigger, passive).
 */
public final class TestRegistries {

    private TestRegistries() {}

    /**
     * Creates a SkillRegistry with all 35 skills registered.
     * Uses a SkillServices that may have null plugin reference;
     * only metadata methods (id, tier, type, icon, trigger, passive) are safe to call.
     */
    public static SkillRegistry createFullRegistry() {
        // We register each skill with a null-safe approach.
        // The skill constructors accept SkillServices; for metadata-only tests
        // we use a minimal wrapper. Cooldown() calls that use cfg() may NPE.
        SkillRegistry registry = new SkillRegistry();
        SkillServices services = null; // metadata methods don't need services

        // Register all skills — they store ref to services but metadata is self-contained
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.DashSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.GrappleSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.WaterBreathingSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.RecallSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.ThermalResistanceSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.SonarSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.WindBurstSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.DimShiftSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.HarpoonPullSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.BladeDanceSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.DietSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.StoneSmashSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.TorchLightSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.OreSonarSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.HasteSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.OreRepairSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.TransmutationSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.GravityShieldSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.CoreOverdriveSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.SeismicSlamSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.GoldRushShieldSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.UnstableCoreSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.FeastSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.WoodcutterSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.FloraShieldSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.SilkTouchSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.ScaffoldSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.ArchitectFocusSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.UnbreakableBlockSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.GravityDefianceSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.SentryTurretSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.EntanglingRootsSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.FortressProtocolSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.HydrationSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.JumpBoostSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.SightSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.CanopyStepSkill(services));
        registerSkill(registry, new com.project.rpgplugin.core.skill.impl.SafeFallSkill(services));

        return registry;
    }

    private static void registerSkill(SkillRegistry registry, Skill skill) {
        try {
            registry.register(skill);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register skill " + skill.id() + ": " + e.getMessage(), e);
        }
    }
}
