package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;

import java.util.List;

public record OnKillEffect(String effect, double value) implements AugmentEffect {
    @Override
    public void apply(Player p, RunState run, int stacks) {
        run.addOnKillEffect(this);
    }

    @Override
    public void unapply(Player p, RunState run, int stacks) {
        run.removeOnKillEffect(this);
    }

    @Override
    public List<String> description() {
        String desc = switch (effect) {
            case "gold_drop" -> "Dropa ouro ao matar mobs";
            case "heal_and_mana" -> "Cura e recupera mana ao matar";
            default -> effect;
        };
        return List.of("<dark_red>Ao matar: <red>" + desc);
    }
}
