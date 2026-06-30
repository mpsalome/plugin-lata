package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class ExplosiveDeathModifier extends BaseModifier {

    private Listener listener;

    public ExplosiveDeathModifier() {
        super("explosive_death", ModifierSeverity.WILD, ModifierTag.CHAOS);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        listener = new Listener() {
            @EventHandler
            public void onDeath(EntityDeathEvent e) {
                if (e.getEntity() instanceof Monster m) {
                    m.getWorld().createExplosion(m.getLocation(), 1.5f, false, false);
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
