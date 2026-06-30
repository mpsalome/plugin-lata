package com.project.rpgplugin.core.mayhem.impl;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.Modifier;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;

import java.util.Set;

public abstract class BaseModifier implements Modifier {

    private final String id;
    private final ModifierSeverity severity;
    private final Set<ModifierTag> tags;

    protected BaseModifier(String id, ModifierSeverity severity, ModifierTag... tags) {
        this.id = id;
        this.severity = severity;
        this.tags = Set.of(tags);
    }

    @Override
    public String id() { return id; }

    @Override
    public ModifierSeverity severity() { return severity; }

    @Override
    public Set<ModifierTag> tags() { return tags; }

    @Override
    public boolean compatibleWith(Set<String> activeModifiers) {
        return true;
    }

    @Override
    public abstract void onActivate(MayhemContext ctx);

    @Override
    public abstract void onDeactivate(MayhemContext ctx);
}
