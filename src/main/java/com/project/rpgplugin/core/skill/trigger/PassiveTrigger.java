package com.project.rpgplugin.core.skill.trigger;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;

import java.util.Set;

public class PassiveTrigger implements SkillTrigger {

    private static final PassiveTrigger INSTANCE = new PassiveTrigger();

    public static PassiveTrigger instance() {
        return INSTANCE;
    }

    private PassiveTrigger() {}

    @Override
    public Set<TriggerKind> kinds() {
        return Set.of(TriggerKind.PASSIVE);
    }

    @Override
    public boolean matches(Skill skill, SkillContext ctx) {
        return true;
    }
}
