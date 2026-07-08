package com.project.rpgplugin.core.skill.trigger;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;
import org.bukkit.Tag;
import org.bukkit.Material;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class MoveTrigger implements SkillTrigger {

    private final Predicate<SkillContext> matcher;
    private final String description;

    private MoveTrigger(Predicate<SkillContext> matcher, String description) {
        this.matcher = matcher;
        this.description = description;
    }

    public static MoveTrigger always() {
        return new MoveTrigger(ctx -> true, "Ao se mover (sempre ativo)");
    }

    public static MoveTrigger whenOn(Tag<Material> tag) {
        String blockName = tag == Tag.LEAVES ? "<green>Folhas</green>" : "<white>" + tag.getKey().getKey();
        return new MoveTrigger(ctx -> {
            if (ctx.targetBlock() == null) return false;
            return tag.isTagged(ctx.targetBlock().getType());
        }, "Ao andar sobre: " + blockName);
    }

    public static MoveTrigger whenOn(Material material) {
        return new MoveTrigger(ctx -> {
            if (ctx.targetBlock() == null) return false;
            return ctx.targetBlock().getType() == material;
        }, "Ao andar sobre: " + formatMaterial(material));
    }

    @Override
    public Set<TriggerKind> kinds() {
        return Set.of(TriggerKind.MOVE);
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
            case GRASS_BLOCK -> "<green>Grama";
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
