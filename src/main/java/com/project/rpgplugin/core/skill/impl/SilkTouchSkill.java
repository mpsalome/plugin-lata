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
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SilkTouchSkill extends AbstractSkill {

    public SilkTouchSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "silk_touch"; }

    @Override
    public SkillTier tier() { return SkillTier.BRONZE; }

    @Override
    public SkillType type() { return SkillType.BUILDER; }

    @Override
    public Material icon() { return Material.SHEARS; }

    @Override
    public boolean passive() { return false; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() {
        return new CompositeTrigger(Set.of(TriggerKind.INTERACT, TriggerKind.BLOCK_BREAK), ctx -> {
            if (ctx.sourceEvent() instanceof BlockBreakEvent) {
                return ctx.player().getInventory().getItemInMainHand().getType() == Material.AIR;
            }
            return ctx.usedItem() != null && ctx.usedItem().getType() == Material.SHEARS;
        }, "Quebre blocos com <white>maos vazias</white> para Toque de Seda");
    }

    @Override
    public void activate(SkillContext ctx) {
        if (ctx.sourceEvent() instanceof BlockBreakEvent event) {
            Player p = ctx.player();
            Block block = ctx.targetBlock();
            if (block == null) return;
            ItemStack silkTool = new ItemStack(Material.NETHERITE_PICKAXE);
            silkTool.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
            Collection<ItemStack> drops = block.getDrops(silkTool, p);
            if (!drops.isEmpty()) {
                event.setDropItems(false);
                for (ItemStack d : drops) {
                    Map<Integer, ItemStack> remaining = p.getInventory().addItem(d);
                    for (ItemStack leftover : remaining.values()) {
                        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), leftover);
                    }
                }
                feedback(ctx, "§aToque de Seda Manual!", null);
            }
            return;
        }
        feedback(ctx, "§aUse mãos vazias para Toque de Seda!", Sound.BLOCK_NOTE_BLOCK_BASS);
    }
}
