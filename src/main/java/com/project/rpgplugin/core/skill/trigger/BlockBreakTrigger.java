package com.project.rpgplugin.core.skill.trigger;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BlockBreakTrigger implements SkillTrigger {

    private final Predicate<SkillContext> matcher;

    private BlockBreakTrigger(Predicate<SkillContext> matcher) {
        this.matcher = matcher;
    }

    public static BlockBreakTrigger always() {
        return new BlockBreakTrigger(ctx -> true);
    }

    public static BlockBreakTrigger whenHolding(Material material) {
        return new BlockBreakTrigger(ctx -> {
            if (ctx.usedItem() == null) return false;
            return ctx.usedItem().getType() == material;
        });
    }

    public static BlockBreakTrigger whenHolding(Material... materials) {
        Set<Material> set = Arrays.stream(materials).collect(Collectors.toSet());
        return new BlockBreakTrigger(ctx -> {
            if (ctx.usedItem() == null) return false;
            return set.contains(ctx.usedItem().getType());
        });
    }

    public static BlockBreakTrigger custom(Predicate<SkillContext> predicate) {
        return new BlockBreakTrigger(predicate);
    }

    @Override
    public Set<TriggerKind> kinds() {
        return Set.of(TriggerKind.BLOCK_BREAK);
    }

    @Override
    public boolean matches(Skill skill, SkillContext ctx) {
        return matcher.test(ctx);
    }
}
