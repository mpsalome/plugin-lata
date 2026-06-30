package com.project.rpgplugin.core.skill.trigger;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;
import org.bukkit.Material;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionType;

import java.util.Set;
import java.util.function.Predicate;

public class ConsumeTrigger implements SkillTrigger {

    private final Predicate<SkillContext> matcher;

    private ConsumeTrigger(Predicate<SkillContext> matcher) {
        this.matcher = matcher;
    }

    public static ConsumeTrigger of(PotionType potionType) {
        return new ConsumeTrigger(ctx -> {
            if (ctx.usedItem() == null) return false;
            ItemMeta meta = ctx.usedItem().getItemMeta();
            if (!(meta instanceof PotionMeta potMeta)) return false;
            return potMeta.getBasePotionType() == potionType;
        });
    }

    public static ConsumeTrigger custom(Predicate<SkillContext> predicate) {
        return new ConsumeTrigger(predicate);
    }

    @Override
    public Set<TriggerKind> kinds() {
        return Set.of(TriggerKind.CONSUME);
    }

    @Override
    public boolean matches(Skill skill, SkillContext ctx) {
        return matcher.test(ctx);
    }
}
