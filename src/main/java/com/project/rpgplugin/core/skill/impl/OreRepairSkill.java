package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.InteractTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Material;
import org.bukkit.Sound;
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
        return InteractTrigger.custom(ctx -> {
            if (ctx.usedItem() == null || ctx.usedItem().getType() != Material.IRON_INGOT) return false;
            ItemStack mainHand = ctx.player().getInventory().getItemInMainHand();
            return mainHand.getType() == Material.IRON_PICKAXE
                || mainHand.getType() == Material.DIAMOND_PICKAXE
                || mainHand.getType() == Material.NETHERITE_PICKAXE;
        }, "Clique com: <white>Barra de Ferro</white> (segurando uma Picareta)");
    }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        ItemStack mainHand = p.getInventory().getItemInMainHand();
        Damageable dmg = (Damageable) mainHand.getItemMeta();
        if (dmg != null && dmg.getDamage() > 0) {
            consume(ctx, 1);
            int rep = (int) (mainHand.getType().getMaxDurability() * 0.30);
            dmg.setDamage(Math.max(0, dmg.getDamage() - rep));
            mainHand.setItemMeta(dmg);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
            feedback(ctx, "§aPicareta reparada +30%!", null);
        }
    }
}
