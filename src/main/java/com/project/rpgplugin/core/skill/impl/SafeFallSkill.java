package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.core.skill.trigger.TriggerKind;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.Set;

public class SafeFallSkill extends AbstractSkill {

    public SafeFallSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "safe_fall"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.EXPLORER; }

    @Override
    public Material icon() { return Material.FEATHER; }

    @Override
    public boolean passive() { return true; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.MOVE, TriggerKind.DAMAGE), ctx -> true);
    }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        if (ctx.sourceEvent() instanceof EntityDamageEvent event) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setDamage(event.getDamage() * 0.5);
            }
            return;
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, true, false, false));
    }
}
