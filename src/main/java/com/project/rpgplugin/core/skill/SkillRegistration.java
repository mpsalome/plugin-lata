package com.project.rpgplugin.core.skill;

import com.project.rpgplugin.core.skill.impl.*;

public final class SkillRegistration {

    private SkillRegistration() {}

    public static void registerAll(SkillRegistry registry, SkillServices services) {
        // Explorer
        registry.register(new DashSkill(services));
        registry.register(new GrappleSkill(services));
        registry.register(new WaterBreathingSkill(services));
        registry.register(new RecallSkill(services));
        registry.register(new ThermalResistanceSkill(services));
        registry.register(new SonarSkill(services));
        registry.register(new WindBurstSkill(services));
        registry.register(new DimShiftSkill(services));
        registry.register(new HarpoonPullSkill(services));
        registry.register(new BladeDanceSkill(services));

        // Miner
        registry.register(new DietSkill(services));
        registry.register(new StoneSmashSkill(services));
        registry.register(new TorchLightSkill(services));
        registry.register(new OreSonarSkill(services));
        registry.register(new HasteSkill(services));
        registry.register(new OreRepairSkill(services));
        registry.register(new TransmutationSkill(services));
        registry.register(new GravityShieldSkill(services));
        registry.register(new CoreOverdriveSkill(services));
        registry.register(new SeismicSlamSkill(services));
        registry.register(new GoldRushShieldSkill(services));
        registry.register(new UnstableCoreSkill(services));

        // Builder
        registry.register(new FeastSkill(services));
        registry.register(new WoodcutterSkill(services));
        registry.register(new FloraShieldSkill(services));
        registry.register(new SilkTouchSkill(services));
        registry.register(new ScaffoldSkill(services));
        registry.register(new ArchitectFocusSkill(services));
        registry.register(new UnbreakableBlockSkill(services));
        registry.register(new GravityDefianceSkill(services));
        registry.register(new SentryTurretSkill(services));
        registry.register(new EntanglingRootsSkill(services));
        registry.register(new FortressProtocolSkill(services));

        // Passive / cross-type
        registry.register(new HydrationSkill(services));
        registry.register(new JumpBoostSkill(services));
        registry.register(new SightSkill(services));
        registry.register(new CanopyStepSkill(services));
        registry.register(new SafeFallSkill(services));
    }
}
