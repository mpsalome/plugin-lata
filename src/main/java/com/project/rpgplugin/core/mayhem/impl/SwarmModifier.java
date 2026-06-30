package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class SwarmModifier extends BaseModifier {

    private Listener listener;

    public SwarmModifier() {
        super("swarm", ModifierSeverity.WILD, ModifierTag.CHAOS);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        listener = new Listener() {
            @EventHandler
            public void onSpawn(EntitySpawnEvent e) {
                if (e.getEntity() instanceof Monster m) {
                    var attr = m.getAttribute(Attribute.MAX_HEALTH);
                    if (attr != null) attr.setBaseValue(attr.getBaseValue() * 0.5);
                    var dmg = m.getAttribute(Attribute.ATTACK_DAMAGE);
                    if (dmg != null) dmg.setBaseValue(dmg.getBaseValue() * 0.7);
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
