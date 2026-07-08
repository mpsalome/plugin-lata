package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.BlockBreakTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;

public class StoneSmashSkill extends AbstractSkill {

    public StoneSmashSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "stone_smash"; }

    @Override
    public SkillTier tier() { return SkillTier.BRONZE; }

    @Override
    public SkillType type() { return SkillType.MINER; }

    @Override
    public Material icon() { return Material.COBBLESTONE; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() {
        return BlockBreakTrigger.custom(ctx -> {
            if (ctx.usedItem() == null) return false;
            Material hand = ctx.usedItem().getType();
            if (hand != Material.STONE && hand != Material.COBBLESTONE) return false;
            Block block = ctx.targetBlock();
            if (block == null) return false;
            return block.getType() == Material.STONE || block.getType() == Material.COBBLESTONE || block.getType() == Material.DEEPSLATE;
        }, "Quebre pedra segurando <gray>Pedra</gray> ou <dark_gray>Pedregulho");
    }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 0, true, false, false));
    }
}
