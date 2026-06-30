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

import java.util.Random;

public class MirrorMobsModifier extends BaseModifier {

    private Listener listener;
    private final Random random = new Random();

    public MirrorMobsModifier() {
        super("mirror_mobs", ModifierSeverity.INSANE, ModifierTag.OFFENSE);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        listener = new Listener() {
            @EventHandler
            public void onSpawn(EntitySpawnEvent e) {
                if (e.getEntity() instanceof Monster m) {
                    if (random.nextDouble() < 0.2) {
                        var attr = m.getAttribute(Attribute.ATTACK_DAMAGE);
                        if (attr != null) attr.setBaseValue(attr.getBaseValue() * 1.5);
                        var health = m.getAttribute(Attribute.MAX_HEALTH);
                        if (health != null) health.setBaseValue(health.getBaseValue() * 1.3);
                    }
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
