package com.project.rpgplugin.core.skill.trigger;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;

import java.util.Set;
import java.util.function.Predicate;

public class CompositeTrigger implements SkillTrigger {

    private final Set<TriggerKind> kinds;
    private final Predicate<SkillContext> matcher;

    public CompositeTrigger(Set<TriggerKind> kinds, Predicate<SkillContext> matcher) {
        this.kinds = kinds;
        this.matcher = matcher;
    }

    @Override
    public Set<TriggerKind> kinds() {
        return kinds;
    }

    @Override
    public boolean matches(Skill skill, SkillContext ctx) {
        return matcher.test(ctx);
    }
}
