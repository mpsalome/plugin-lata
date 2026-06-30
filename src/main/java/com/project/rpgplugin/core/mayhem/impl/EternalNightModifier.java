package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.bukkit.World;

public class EternalNightModifier extends BaseModifier {

    private Boolean originalDaylightCycle;

    public EternalNightModifier() {
        super("eternal_night", ModifierSeverity.MILD, ModifierTag.ENVIRONMENT);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        World w = ctx.world();
        originalDaylightCycle = w.getGameRuleValue(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE);
        w.setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, false);
        w.setTime(18000);
    }

    @Override
    public void onDeactivate(MayhemContext ctx) {
        if (originalDaylightCycle != null) {
            ctx.world().setGameRule(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE, originalDaylightCycle);
        }
    }
}
