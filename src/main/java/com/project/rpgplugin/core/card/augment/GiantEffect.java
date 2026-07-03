package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public record GiantEffect(double healthPerStack, double damagePerStack, double slowAmplifier) implements AugmentEffect {

    @Override
    public void apply(Player p, RunState run, int stacks) {
        var attr = p.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(attr.getBaseValue() + healthPerStack * stacks);
        }
        run.addMultiplier("damage_dealt", damagePerStack);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, (int) slowAmplifier, false, false, true));
    }

    @Override
    public void unapply(Player p, RunState run, int stacks) {
        var attr = p.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            attr.setBaseValue(attr.getBaseValue() - healthPerStack * stacks);
        }
        run.removeMultiplier("damage_dealt", damagePerStack);
        p.removePotionEffect(PotionEffectType.SLOWNESS);
    }
}
