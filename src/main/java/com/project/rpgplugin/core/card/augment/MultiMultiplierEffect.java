package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<String> description() {
        List<String> lines = new ArrayList<>();
        if (damageDealt != 0) {
            lines.add("<red>+" + (int) (damageDealt * 100) + "% dano causado");
        }
        if (damageTaken != 0) {
            lines.add("<dark_red>+" + (int) (damageTaken * 100) + "% dano recebido");
        }
        return lines;
    }
}
