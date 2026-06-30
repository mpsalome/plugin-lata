package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class BloodMoonModifier extends BaseModifier {

    private Listener listener;

    public BloodMoonModifier() {
        super("blood_moon", ModifierSeverity.INSANE, ModifierTag.CHAOS, ModifierTag.OFFENSE);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        listener = new Listener() {
            @EventHandler
            public void onSpawn(EntitySpawnEvent e) {
                if (e.getEntity() instanceof Monster m) {
                    var health = m.getAttribute(Attribute.MAX_HEALTH);
                    if (health != null) health.setBaseValue(health.getBaseValue() * 2.0);
                    var damage = m.getAttribute(Attribute.ATTACK_DAMAGE);
                    if (damage != null) damage.setBaseValue(damage.getBaseValue() * 1.5);
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
