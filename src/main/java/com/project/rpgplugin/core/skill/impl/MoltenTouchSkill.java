package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.CompositeTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import com.project.rpgplugin.core.skill.trigger.TriggerKind;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.Set;

public class MoltenTouchSkill extends AbstractSkill {

    public MoltenTouchSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "molten_touch"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.MINER; }

    @Override
    public Material icon() { return Material.FLINT; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ofSeconds(45); }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.INTERACT, TriggerKind.BLOCK_BREAK), ctx -> {
            if (ctx.sourceEvent() instanceof BlockBreakEvent) {
                Block block = ctx.targetBlock();
                if (block == null) return false;
                return switch (block.getType()) {
                    case IRON_ORE, DEEPSLATE_IRON_ORE, GOLD_ORE, DEEPSLATE_GOLD_ORE,
                         COPPER_ORE, DEEPSLATE_COPPER_ORE -> true;
                    default -> false;
                };
            }
            return ctx.usedItem() != null && ctx.usedItem().getType() == Material.FLINT;
        }, "Clique em <dark_gray>Pedra</dark_gray> para ativar (30s), depois quebre minerios");
    }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        if (ctx.sourceEvent() instanceof BlockBreakEvent event) {
            if (!services.isMoltenTouchActive(p.getUniqueId())) return;
            Block block = ctx.targetBlock();
            if (block == null) return;
            Material smelted = switch (block.getType()) {
                case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
                case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.GOLD_INGOT;
                case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
                default -> null;
            };
            if (smelted != null) {
                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smelted, 1));
                block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.1, 0.1, 0.1, 0.05);
            }
            return;
        }
        if (onCooldown(ctx)) {
            feedback(ctx, "§cToque de Fusão em cooldown! " + cooldownRemaining(ctx) / 1000 + "s", Sound.BLOCK_NOTE_BLOCK_BASS);
            return;
        }
        consume(ctx, 1);
        services.activateMoltenTouch(p.getUniqueId(), 30000);
        startCooldown(ctx);
        feedback(ctx, "§cToque de Fusão: Minérios fundem por 30s!", Sound.ITEM_FLINTANDSTEEL_USE);
    }
}
