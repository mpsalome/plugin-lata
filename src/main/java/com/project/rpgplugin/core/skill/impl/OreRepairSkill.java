package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.BlockBreakTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.time.Duration;

public class OreRepairSkill extends AbstractSkill {

    public OreRepairSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "ore_repair"; }

    @Override
    public SkillTier tier() { return SkillTier.GOLD; }

    @Override
    public SkillType type() { return SkillType.MINER; }

    @Override
    public Material icon() { return Material.IRON_INGOT; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() {
        return BlockBreakTrigger.custom(ctx -> {
            Block block = ctx.targetBlock();
            if (block == null) return false;
            return switch (block.getType()) {
                case IRON_ORE, DEEPSLATE_IRON_ORE, GOLD_ORE, DEEPSLATE_GOLD_ORE,
                     DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> true;
                default -> false;
            };
        }, "Quebre <gold>minerios</gold> de Fe/Au/Dia para reparar equipamentos");
    }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        boolean repaired = false;
        for (ItemStack item : p.getInventory().getArmorContents()) {
            if (item != null && item.getItemMeta() instanceof Damageable dmg && dmg.getDamage() > 0) {
                int rep = (int) (item.getType().getMaxDurability() * 0.05);
                dmg.setDamage(Math.max(0, dmg.getDamage() - rep));
                item.setItemMeta(dmg);
                repaired = true;
            }
        }
        ItemStack mainHand = p.getInventory().getItemInMainHand();
        if (mainHand.getItemMeta() instanceof Damageable dmg && dmg.getDamage() > 0) {
            int rep = (int) (mainHand.getType().getMaxDurability() * 0.05);
            dmg.setDamage(Math.max(0, dmg.getDamage() - rep));
            mainHand.setItemMeta(dmg);
            repaired = true;
        }
        if (repaired) {
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.5f);
        }
    }
}
