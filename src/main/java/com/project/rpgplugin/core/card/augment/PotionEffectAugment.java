package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public record PotionEffectAugment(String potionType, int amplifier) implements AugmentEffect {
    @Override
    public void apply(Player p, RunState run, int stacks) {
        run.addPotionType(potionType);
        PotionEffectType type = PotionEffectType.getByName(potionType);
        if (type != null) {
            p.addPotionEffect(new PotionEffect(type, PotionEffect.INFINITE_DURATION, amplifier, false, false, true));
        }
    }

    @Override
    public void unapply(Player p, RunState run, int stacks) {
        run.removePotionType(potionType);
        PotionEffectType type = PotionEffectType.getByName(potionType);
        if (type != null) p.removePotionEffect(type);
    }
}
