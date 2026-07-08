package com.project.rpgplugin.core.skill.trigger;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ConsumeTrigger implements SkillTrigger {

    private final Predicate<SkillContext> matcher;
    private final String description;

    private ConsumeTrigger(Predicate<SkillContext> matcher, String description) {
        this.matcher = matcher;
        this.description = description;
    }

    public static ConsumeTrigger of(PotionType potionType) {
        String potionName = switch (potionType) {
            case WATER -> "<blue>Agua";
            default -> "<light_purple>" + formatPotionType(potionType);
        };
        return new ConsumeTrigger(ctx -> {
            if (ctx.usedItem() == null) return false;
            ItemMeta meta = ctx.usedItem().getItemMeta();
            if (!(meta instanceof PotionMeta potMeta)) return false;
            return potMeta.getBasePotionType() == potionType;
        }, "Beba: " + potionName);
    }

    public static ConsumeTrigger custom(Predicate<SkillContext> predicate) {
        return new ConsumeTrigger(predicate, "Consuma um item");
    }

    @Override
    public Set<TriggerKind> kinds() {
        return Set.of(TriggerKind.CONSUME);
    }

    @Override
    public boolean matches(Skill skill, SkillContext ctx) {
        return matcher.test(ctx);
    }

    @Override
    public List<String> activationDescription() {
        return List.of(description);
    }

    private static String formatPotionType(PotionType type) {
        String[] parts = type.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }
}
