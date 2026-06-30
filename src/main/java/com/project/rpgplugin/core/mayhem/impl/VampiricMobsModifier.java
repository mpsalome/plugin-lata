package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class VampiricMobsModifier extends BaseModifier {

    private Listener listener;

    public VampiricMobsModifier() {
        super("vampiric_mobs", ModifierSeverity.WILD, ModifierTag.OFFENSE);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        listener = new Listener() {
            @EventHandler
            public void onDamage(EntityDamageByEntityEvent e) {
                if (e.getDamager() instanceof Mob mob && e.getEntity() instanceof LivingEntity) {
                    double heal = e.getDamage() * 0.2;
                    mob.setHealth(Math.min(mob.getMaxHealth(), mob.getHealth() + heal));
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
