package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Set;

public class GlassBodyModifier extends BaseModifier {

    public GlassBodyModifier() {
        super("glass_body", ModifierSeverity.INSANE, ModifierTag.DEFENSE, ModifierTag.CHAOS);
    }

    @Override
    public boolean compatibleWith(Set<String> activeModifiers) {
        return !activeModifiers.contains("glass_cannon_world");
    }

    @Override
    public void onActivate(MayhemContext ctx) {
        for (Player p : ctx.plugin().getServer().getOnlinePlayers()) {
            var attr = p.getAttribute(Attribute.MAX_HEALTH);
            if (attr != null) {
                double half = attr.getBaseValue() / 2.0;
                attr.setBaseValue(half);
            }
        }
    }

    @Override
    public void onDeactivate(MayhemContext ctx) {
        for (Player p : ctx.plugin().getServer().getOnlinePlayers()) {
            var attr = p.getAttribute(Attribute.MAX_HEALTH);
            if (attr != null) {
                attr.setBaseValue(attr.getDefaultValue());
            }
        }
    }
}
