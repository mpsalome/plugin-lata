package com.project.rpgplugin.core.skill.trigger;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;
import org.bukkit.Tag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InteractTrigger implements SkillTrigger {

    private final Predicate<SkillContext> matcher;

    private InteractTrigger(Predicate<SkillContext> matcher) {
        this.matcher = matcher;
    }

    public static InteractTrigger of(Material material) {
        return new InteractTrigger(ctx ->
            ctx.usedItem() != null && ctx.usedItem().getType() == material);
    }

    public static InteractTrigger of(Material... materials) {
        Set<Material> set = Arrays.stream(materials).collect(Collectors.toSet());
        return new InteractTrigger(ctx ->
            ctx.usedItem() != null && set.contains(ctx.usedItem().getType()));
    }

    public static InteractTrigger of(Tag<Material> tag) {
        return new InteractTrigger(ctx ->
            ctx.usedItem() != null && tag.isTagged(ctx.usedItem().getType()));
    }

    public static InteractTrigger of(Material material, String heldToolSuffix) {
        return new InteractTrigger(ctx -> {
            if (ctx.usedItem() == null || ctx.usedItem().getType() != material) return false;
            ItemStack mainHand = ctx.player().getInventory().getItemInMainHand();
            return mainHand.getType().name().endsWith("_" + heldToolSuffix);
        });
    }

    public static InteractTrigger withAmount(Material material, int minAmount) {
        return new InteractTrigger(ctx ->
            ctx.usedItem() != null &&
            ctx.usedItem().getType() == material &&
            ctx.usedItem().getAmount() >= minAmount);
    }

    public static InteractTrigger custom(Predicate<SkillContext> predicate) {
        return new InteractTrigger(predicate);
    }

    @Override
    public Set<TriggerKind> kinds() {
        return Set.of(TriggerKind.INTERACT);
    }

    @Override
    public boolean matches(Skill skill, SkillContext ctx) {
        return matcher.test(ctx);
    }
}
