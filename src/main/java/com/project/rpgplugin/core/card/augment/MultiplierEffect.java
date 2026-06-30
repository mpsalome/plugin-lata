package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;

public record MultiplierEffect(String key, double addPerStack) implements AugmentEffect {
    @Override
    public void apply(Player p, RunState run, int stacks) {
        run.addMultiplier(key, addPerStack);
    }

    @Override
    public void unapply(Player p, RunState run, int stacks) {
        run.removeMultiplier(key, addPerStack);
    }
}
