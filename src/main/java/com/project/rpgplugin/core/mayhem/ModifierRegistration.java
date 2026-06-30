package com.project.rpgplugin.core.mayhem;

import com.project.rpgplugin.core.mayhem.impl.*;

public final class ModifierRegistration {

    private ModifierRegistration() {}

    public static void registerAll(ModifierRegistry registry) {
        // MILD
        registry.register(new EternalNightModifier());
        registry.register(new GlassWorldModifier());
        registry.register(new DoubleXpModifier());
        registry.register(new IronRainModifier());
        registry.register(new CaffeinatedModifier());
        registry.register(new FragileBlocksModifier());

        // WILD
        registry.register(new MobsOnFireModifier());
        registry.register(new VampiricMobsModifier());
        registry.register(new LowGravityModifier());
        registry.register(new GlassCannonWorldModifier());
        registry.register(new SwarmModifier());
        registry.register(new HealingInvertedModifier());
        registry.register(new ExplosiveDeathModifier());
        registry.register(new MagneticStormModifier());

        // INSANE
        registry.register(new BloodMoonModifier());
        registry.register(new GlassBodyModifier());
        registry.register(new TimeWarpModifier());
        registry.register(new MirrorMobsModifier());
        registry.register(new ManaDroughtModifier());
        registry.register(new GravityChaosModifier());
        registry.register(new ApexPredatorModifier());
    }
}
