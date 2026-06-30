package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

public class HealingInvertedModifier extends BaseModifier {

    private Listener listener;

    public HealingInvertedModifier() {
        super("healing_inverted", ModifierSeverity.WILD, ModifierTag.DEFENSE);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        listener = new Listener() {
            @EventHandler
            public void onFoodChange(FoodLevelChangeEvent e) {
                if (e.getEntity() instanceof Player p) {
                    int diff = e.getFoodLevel() - p.getFoodLevel();
                    if (diff > 0) {
                        e.setFoodLevel(p.getFoodLevel() + (int)(diff * 0.5));
                    }
                }
            }

            @EventHandler
            public void onDamage(EntityDamageByEntityEvent e) {
                if (e.getDamager() instanceof Player p && e.getEntity() instanceof LivingEntity target) {
                    double heal = e.getDamage() * 0.1;
                    p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + heal));
                }
            }
        };
        ctx.plugin().getServer().getPluginManager().registerEvents(listener, ctx.plugin());
    }

    @Override
    public void onDeactivate(MayhemContext ctx) {
        if (listener != null) {
            HandlerList.unregisterAll(listener);
            listener = null;
        }
    }
}
