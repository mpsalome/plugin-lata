package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;

import java.util.List;

public record AttributeEffect(String attribute, double addPerStack) implements AugmentEffect {
    @Override
    public void apply(Player p, RunState run, int stacks) {
        // StatService handles attribute recalculation globally
    }

    @Override
    public void unapply(Player p, RunState run, int stacks) {
        // StatService handles attribute recalculation globally
    }

    public double totalForStacks(int stacks) {
        return addPerStack * stacks;
    }

    @Override
    public List<String> description() {
        String attrName = switch (attribute.toUpperCase()) {
            case "MAX_HEALTH" -> "Vida Maxima";
            case "ATTACK_DAMAGE" -> "Dano de Ataque";
            case "MOVEMENT_SPEED" -> "Velocidade";
            case "ARMOR" -> "Armadura";
            default -> attribute;
        };
        return List.of("<green>+" + formatNum(addPerStack) + " " + attrName + " por pilha");
    }

    private static String formatNum(double v) {
        if (v == (long) v) return String.valueOf((long) v);
        return String.valueOf(v);
    }
}
