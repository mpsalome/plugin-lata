package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;

public class ManaDroughtModifier extends BaseModifier {

    public ManaDroughtModifier() {
        super("mana_drought", ModifierSeverity.INSANE, ModifierTag.DEFENSE);
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        ctx.run().addMultiplier("mana_regen_mult", -0.7);
    }

    @Override
    public void onDeactivate(MayhemContext ctx) {
        ctx.run().removeMultiplier("mana_regen_mult", -0.7);
    }
}
