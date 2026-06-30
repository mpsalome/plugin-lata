package com.project.rpgplugin.core.skill;

import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Material;

import java.time.Duration;

public interface Skill {
    String id();
    SkillTier tier();
    SkillType type();
    Material icon();
    boolean passive();
    Duration cooldown();
    SkillTrigger trigger();
    void activate(SkillContext ctx);
    default String displayKey() { return "skill." + id() + ".name"; }
    default String descKey() { return "skill." + id() + ".desc"; }
}
