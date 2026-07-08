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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.time.Duration;
import java.util.Set;

public class GoldRushShieldSkill extends AbstractSkill {

    public GoldRushShieldSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "gold_rush_shield"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.MINER; }

    @Override
    public Material icon() { return Material.GOLDEN_APPLE; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(60); }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.DAMAGE), ctx -> {
            if (!(ctx.sourceEvent() instanceof EntityDamageEvent e)) return false;
            if (!(e.getEntity() instanceof Player p)) return false;
            if (e.getFinalDamage() < p.getHealth() * 0.5) return false;
            PlayerInventory inv = p.getInventory();
            int goldValue = 0;
            for (ItemStack item : inv.getContents()) {
                if (item == null) continue;
                goldValue += goldWorth(item);
            }
            return goldValue >= (int) e.getFinalDamage();
        }, "<gray>Ao sofrer dano fatal (consome ouro)");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) {
            return;
        }
        if (!(ctx.sourceEvent() instanceof EntityDamageEvent e)) return;
        Player p = ctx.player();
        double damage = e.getFinalDamage();
        e.setDamage(0);
        int remaining = (int) damage;
        PlayerInventory inv = p.getInventory();
        for (int i = 0; i < inv.getSize() && remaining > 0; i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;
            int worth = goldWorth(item);
            if (worth <= 0) continue;
            int consumeStack = Math.min(remaining / worth, item.getAmount());
            if (consumeStack > 0) {
                item.subtract(consumeStack);
                remaining -= consumeStack * worth;
            }
        }
        startCooldown(ctx);
        p.getWorld().spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, p.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
        feedback(ctx, "<gold><bold>Blindagem de Midas!</bold></gold> Dano absorvido com ouro!", null);
    }

    private static int goldWorth(ItemStack item) {
        return switch (item.getType()) {
            case GOLD_NUGGET -> 1;
            case GOLD_INGOT -> 9;
            case GOLD_BLOCK -> 81;
            case RAW_GOLD -> 6;
            case RAW_GOLD_BLOCK -> 54;
            default -> 0;
        };
    }
}
