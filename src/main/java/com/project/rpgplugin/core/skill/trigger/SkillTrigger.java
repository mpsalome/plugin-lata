package com.project.rpgplugin.core.skill.trigger;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;

import java.util.List;
import java.util.Set;

public interface SkillTrigger {
    Set<TriggerKind> kinds();
    boolean matches(Skill skill, SkillContext ctx);
    default List<String> activationDescription() { return List.of(); }
}
