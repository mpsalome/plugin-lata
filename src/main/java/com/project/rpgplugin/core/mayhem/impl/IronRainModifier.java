package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class IronRainModifier extends BaseModifier {

    private Listener listener;
    private final Random random = new Random();

    public IronRainModifier() {
        super("iron_rain", ModifierSeverity.MILD, ModifierTag.ECONOMY);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        listener = new Listener() {
            @EventHandler
            public void onDeath(EntityDeathEvent e) {
                if (e.getEntity() instanceof LivingEntity && !(e.getEntity() instanceof org.bukkit.entity.Player)) {
                    if (random.nextDouble() < 0.3) {
                        e.getDrops().add(new ItemStack(Material.IRON_INGOT, 1));
                    }
                    if (random.nextDouble() < 0.1) {
                        e.getDrops().add(new ItemStack(Material.COPPER_INGOT, 2));
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
