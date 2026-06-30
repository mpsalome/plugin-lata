package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Set;

public class GlassCannonWorldModifier extends BaseModifier {

    private Listener listener;

    public GlassCannonWorldModifier() {
        super("glass_cannon_world", ModifierSeverity.WILD, ModifierTag.OFFENSE);
    }

    @Override
    public boolean compatibleWith(Set<String> activeModifiers) {
        return !activeModifiers.contains("glass_body");
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        listener = new Listener() {
            @EventHandler
            public void onDamage(EntityDamageByEntityEvent e) {
                if (e.getDamager() instanceof Player || e.getEntity() instanceof Player) {
                    e.setDamage(e.getDamage() * 1.5);
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
