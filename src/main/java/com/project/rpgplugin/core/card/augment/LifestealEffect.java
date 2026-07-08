package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;

import java.util.List;

public record LifestealEffect(double healPct) implements AugmentEffect {
    @Override
    public void apply(Player p, RunState run, int stacks) {
        run.addMultiplier("lifesteal", healPct);
    }

    @Override
    public void unapply(Player p, RunState run, int stacks) {
        run.removeMultiplier("lifesteal", healPct);
    }

    @Override
    public List<String> description() {
        return List.of("<red>Rouba " + formatPct(healPct) + "% do dano causado como vida");
    }

    public double healPct() { return healPct; }

    private static String formatPct(double v) {
        return String.valueOf((int) (v * 100));
    }
}
