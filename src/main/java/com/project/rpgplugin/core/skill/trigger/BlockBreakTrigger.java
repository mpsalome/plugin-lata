package com.project.rpgplugin.core.skill.trigger;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BlockBreakTrigger implements SkillTrigger {

    private final Predicate<SkillContext> matcher;
    private final String description;

    private BlockBreakTrigger(Predicate<SkillContext> matcher, String description) {
        this.matcher = matcher;
        this.description = description;
    }

    public static BlockBreakTrigger always() {
        return new BlockBreakTrigger(ctx -> true, "Quebre qualquer bloco");
    }

    public static BlockBreakTrigger whenHolding(Material material) {
        return new BlockBreakTrigger(ctx -> {
            if (ctx.usedItem() == null) return false;
            return ctx.usedItem().getType() == material;
        }, "Quebre blocos segurando: " + formatMaterial(material));
    }

    public static BlockBreakTrigger whenHolding(Material... materials) {
        Set<Material> set = Arrays.stream(materials).collect(Collectors.toSet());
        String desc = Arrays.stream(materials)
            .map(BlockBreakTrigger::formatMaterial)
            .collect(Collectors.joining(" ou "));
        return new BlockBreakTrigger(ctx -> {
            if (ctx.usedItem() == null) return false;
            return set.contains(ctx.usedItem().getType());
        }, "Quebre blocos segurando: " + desc);
    }

    public static BlockBreakTrigger custom(Predicate<SkillContext> predicate) {
        return new BlockBreakTrigger(predicate, "Quebre blocos especificos");
    }

    public static BlockBreakTrigger custom(Predicate<SkillContext> predicate, String description) {
        return new BlockBreakTrigger(predicate, description);
    }

    @Override
    public Set<TriggerKind> kinds() {
        return Set.of(TriggerKind.BLOCK_BREAK);
    }

    @Override
    public boolean matches(Skill skill, SkillContext ctx) {
        return matcher.test(ctx);
    }

    @Override
    public List<String> activationDescription() {
        return List.of(description);
    }

    private static String formatMaterial(Material m) {
        return switch (m) {
            case STONE -> "<gray>Pedra";
            case COBBLESTONE -> "<dark_gray>Pedregulho";
            default -> "<white>" + formatName(m);
        };
    }

    private static String formatName(Material m) {
        String[] parts = m.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }
}
