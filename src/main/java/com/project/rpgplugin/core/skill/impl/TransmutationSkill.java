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
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.Set;

public class TransmutationSkill extends AbstractSkill {

    public TransmutationSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "transmutation"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.MINER; }

    @Override
    public Material icon() { return Material.DIAMOND; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.INTERACT), ctx -> {
            if (ctx.usedItem() == null) return false;
            Material type = ctx.usedItem().getType();
            return (type == Material.IRON_INGOT || type == Material.GOLD_INGOT)
                && ctx.usedItem().getAmount() >= 5;
        }, "Clique com: <white>5x Barra de Ferro</white> ou <gold>5x Barra de Ouro</gold>");
    }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        Material type = ctx.usedItem().getType();
        if (type == Material.IRON_INGOT) {
            consume(ctx, 5);
            p.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 1));
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
            feedback(ctx, "§eTransmutação: 5 Ferro -> 1 Ouro!", null);
        } else if (type == Material.GOLD_INGOT) {
            consume(ctx, 5);
            p.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.8f);
            feedback(ctx, "§bTransmutação: 5 Ouro -> 1 Diamante!", null);
        }
    }
}
