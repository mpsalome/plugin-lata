package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;

public record PotionEffectAugment(String potionType, int amplifier) implements AugmentEffect {
    @Override
    public void apply(Player p, RunState run, int stacks) {
        // Applied by passive tick task
    }

    @Override
    public void unapply(Player p, RunState run, int stacks) {
        // Removed by passive tick task
    }
}
