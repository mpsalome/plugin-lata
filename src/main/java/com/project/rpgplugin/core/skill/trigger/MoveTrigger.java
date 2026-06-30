package com.project.rpgplugin.core.skill.trigger;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;
import org.bukkit.Tag;
import org.bukkit.Material;

import java.util.Set;
import java.util.function.Predicate;

public class MoveTrigger implements SkillTrigger {

    private final Predicate<SkillContext> matcher;

    private MoveTrigger(Predicate<SkillContext> matcher) {
        this.matcher = matcher;
    }

    public static MoveTrigger always() {
        return new MoveTrigger(ctx -> true);
    }

    public static MoveTrigger whenOn(Tag<Material> tag) {
        return new MoveTrigger(ctx -> {
            if (ctx.targetBlock() == null) return false;
            return tag.isTagged(ctx.targetBlock().getType());
        });
    }

    public static MoveTrigger whenOn(Material material) {
        return new MoveTrigger(ctx -> {
            if (ctx.targetBlock() == null) return false;
            return ctx.targetBlock().getType() == material;
        });
    }

    @Override
    public Set<TriggerKind> kinds() {
        return Set.of(TriggerKind.MOVE);
    }

    @Override
    public boolean matches(Skill skill, SkillContext ctx) {
        return matcher.test(ctx);
    }
}
