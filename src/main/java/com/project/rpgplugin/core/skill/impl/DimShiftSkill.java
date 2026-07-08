package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.core.skill.trigger.TriggerKind;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.Set;

public class DimShiftSkill extends AbstractSkill {

    public DimShiftSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "dim_shift"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.EXPLORER; }

    @Override
    public Material icon() { return Material.ENDER_PEARL; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(15); }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.INTERACT), ctx -> {
            if (!(ctx.sourceEvent() instanceof PlayerInteractEvent e)) return false;
            if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
            return ctx.usedItem() != null && ctx.usedItem().getType() == Material.ENDER_PEARL;
        }, "<gray>Clique direito com <white>Ender Pearl</white> (gasto de mana: 15)");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "<red>Dim Shift em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        Player p = ctx.player();
        Vector dir = p.getLocation().getDirection().normalize().multiply(8);
        p.teleport(p.getLocation().add(dir));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 3, true, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0, true, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, true, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 2, true, false, false));
        startCooldown(ctx);
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.0f);
        feedback(ctx, "<light_purple>Mudanca Dimensional! Speed IV + Drawbacks.", null);
    }
}
