package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;

public record MultiMultiplierEffect(double damageDealt, double damageTaken) implements AugmentEffect {
    @Override
    public void apply(Player p, RunState run, int stacks) {
        run.addMultiplier("damage_dealt_mult", damageDealt);
        run.addMultiplier("damage_taken_mult", damageTaken);
    }

    @Override
    public void unapply(Player p, RunState run, int stacks) {
        run.removeMultiplier("damage_dealt_mult", damageDealt);
        run.removeMultiplier("damage_taken_mult", damageTaken);
    }
}
