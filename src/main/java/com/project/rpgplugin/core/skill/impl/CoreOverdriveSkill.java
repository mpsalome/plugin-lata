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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.Set;

public class CoreOverdriveSkill extends AbstractSkill {

    public CoreOverdriveSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "core_overdrive"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.MINER; }

    @Override
    public Material icon() { return Material.REDSTONE_BLOCK; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(60); }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.INTERACT), ctx -> {
            if (ctx.usedItem() == null) return false;
            Material type = ctx.usedItem().getType();
            return switch (type) {
                case DIAMOND_BLOCK, EMERALD_BLOCK, GOLD_BLOCK, IRON_BLOCK, NETHERITE_BLOCK -> true;
                default -> false;
            };
        }, "<gray>Clique direito com <white>Bloco de Minerio</white> (gasto de mana: 30)");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            feedback(ctx, "<red>Sobrecarga do Nucleo em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        Player p = ctx.player();
        consume(ctx, 1);
        int dur = cfg().getInt("duration", 15) * 20;
        p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, dur, 2, true, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, dur, 1, true, false, false));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur, 1, true, false, false));
        startCooldown(ctx);
        p.getWorld().spawnParticle(org.bukkit.Particle.SONIC_BOOM, p.getLocation().add(0, 1, 0), 5, 0.5, 0.5, 0.5, 0);
        p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 0.5f, 1.0f);
        feedback(ctx, "<red><bold>Sobrecarga do Nucleo!</bold> <gray>Haste III + Strength II + Speed II 15s (+25% dano recebido)", null);
    }
}
