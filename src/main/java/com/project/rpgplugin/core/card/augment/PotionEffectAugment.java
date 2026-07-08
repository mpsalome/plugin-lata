package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

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

    @Override
    public List<String> description() {
        String name = switch (potionType.toUpperCase()) {
            case "NIGHT_VISION" -> "Visao Noturna";
            case "REGENERATION" -> "Regeneracao";
            case "ABSORPTION" -> "Absorcao";
            case "SPEED" -> "Velocidade";
            case "STRENGTH" -> "Forca";
            case "JUMP" -> "Super Pulo";
            case "INVISIBILITY" -> "Invisibilidade";
            case "FIRE_RESISTANCE" -> "Resistencia ao Fogo";
            case "WATER_BREATHING" -> "Respiracao Aquatica";
            default -> potionType;
        };
        String level = amplifier == 0 ? "" : " " + (amplifier + 1);
        return List.of("<aqua>Concede " + name + level + " permanentemente");
    }
}
