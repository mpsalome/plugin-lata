package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Random;

public class FragileBlocksModifier extends BaseModifier {

    private Listener listener;
    private final Random random = new Random();

    public FragileBlocksModifier() {
        super("fragile_blocks", ModifierSeverity.MILD, ModifierTag.ENVIRONMENT);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        listener = new Listener() {
            @EventHandler
            public void onBlockBreak(BlockBreakEvent e) {
                if (random.nextDouble() < 0.1) {
                    Block b = e.getBlock();
                    b.getWorld().createExplosion(b.getLocation(), 1.0f, false, false);
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
