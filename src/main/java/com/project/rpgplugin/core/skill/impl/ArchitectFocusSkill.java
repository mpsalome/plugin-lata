package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.core.skill.trigger.TriggerKind;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ArchitectFocusSkill extends AbstractSkill {

    private static final Map<UUID, LinkedList<Long>> PLACE_TIMES = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> COOLDOWN_UNTIL = new ConcurrentHashMap<>();

    public ArchitectFocusSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "architect_focus"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public Material icon() { return Material.STONE_BRICKS; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(5); }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.INTERACT), ctx -> {
            if (!(ctx.sourceEvent() instanceof BlockPlaceEvent)) return false;
            Player p = ctx.player();
            UUID uid = p.getUniqueId();
            long now = System.currentTimeMillis();
            if (now < COOLDOWN_UNTIL.getOrDefault(uid, 0L)) return false;
            LinkedList<Long> times = PLACE_TIMES.computeIfAbsent(uid, k -> new LinkedList<>());
            times.addLast(now);
            while (!times.isEmpty() && times.peekFirst() < now - 5000) {
                times.pollFirst();
            }
            return times.size() >= 8;
        }, "<gray>Coloque 8 blocos em menos de 5s");
    }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        UUID uid = p.getUniqueId();
        PLACE_TIMES.get(uid).clear();
        COOLDOWN_UNTIL.put(uid, System.currentTimeMillis() + 10000);
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 2, true, false, false));
        p.getWorld().spawnParticle(org.bukkit.Particle.CRIT, p.getLocation().add(0, 1, 0), 40, 1.0, 0.5, 1.0, 0.1);
        p.playSound(p.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
        feedback(ctx, "<green>Foco do Arquiteto: Resistencia III 10s!", null);
        SchedulerUtil.runLater(services.plugin(), () -> {
            COOLDOWN_UNTIL.remove(uid);
        }, 200L);
    }

    public static void clearAll() {
        PLACE_TIMES.clear();
        COOLDOWN_UNTIL.clear();
    }
}
