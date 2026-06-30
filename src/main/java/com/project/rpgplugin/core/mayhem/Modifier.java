package com.project.rpgplugin.core.mayhem;

import java.util.Set;

public interface Modifier {
    String id();
    ModifierSeverity severity();
    Set<ModifierTag> tags();
    void onActivate(MayhemContext ctx);
    void onDeactivate(MayhemContext ctx);
    boolean compatibleWith(Set<String> activeModifiers);
}
