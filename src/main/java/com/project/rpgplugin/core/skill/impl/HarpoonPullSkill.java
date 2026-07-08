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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;

import java.time.Duration;
import java.util.Set;

public class HarpoonPullSkill extends AbstractSkill {

    public HarpoonPullSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "harpoon_pull"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.EXPLORER; }

    @Override
    public Material icon() { return Material.FISHING_ROD; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(10); }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.INTERACT), ctx -> {
            if (!(ctx.sourceEvent() instanceof PlayerFishEvent e)) return false;
            return e.getState() == PlayerFishEvent.State.CAUGHT_ENTITY
                && e.getCaught() instanceof LivingEntity
                && !(e.getCaught() instanceof Player);
        }, "<gray>Fisgue um inimigo com a <white>Vara de Pesca</white>");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "<red>Arpao em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        if (!(ctx.sourceEvent() instanceof PlayerFishEvent e)) return;
        if (!(e.getCaught() instanceof LivingEntity target)) return;
        Player p = ctx.player();
        startCooldown(ctx);
        target.teleport(p.getLocation().add(p.getLocation().getDirection().normalize().multiply(1.5)));
        target.damage(5.0, p);
        target.setVelocity(p.getLocation().getDirection().normalize().multiply(0.3).setY(0.2));
        p.getWorld().spawnParticle(org.bukkit.Particle.WITCH, target.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.1);
        p.playSound(p.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, 1.0f, 1.0f);
        feedback(ctx, "<aqua><bold>Arpao! Inimigo puxado!</bold></aqua>", Sound.ENTITY_FISHING_BOBBER_RETRIEVE);
    }
}
