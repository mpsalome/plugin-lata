package com.project.rpgplugin.core.skill.trigger;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class CompositeTrigger implements SkillTrigger {

    private final Set<TriggerKind> kinds;
    private final Predicate<SkillContext> matcher;
    private final String description;

    public CompositeTrigger(Set<TriggerKind> kinds, Predicate<SkillContext> matcher) {
        this(kinds, matcher, null);
    }

    public CompositeTrigger(Set<TriggerKind> kinds, Predicate<SkillContext> matcher, String description) {
        this.kinds = kinds;
        this.matcher = matcher;
        this.description = description;
    }

    @Override
    public Set<TriggerKind> kinds() {
        return kinds;
    }

    @Override
    public boolean matches(Skill skill, SkillContext ctx) {
        return matcher.test(ctx);
    }

    @Override
    public List<String> activationDescription() {
        if (description != null) return List.of(description);
        return kinds.stream()
            .map(k -> switch (k) {
                case INTERACT -> "Clique com o item certo";
                case CONSUME -> "Consuma algo";
                case BLOCK_BREAK -> "Quebre blocos";
                case MOVE -> "Ao se mover";
                case DAMAGE -> "Ao causar/receber dano";
                default -> k.name();
            })
            .map(s -> "<gray>" + s)
            .toList();
    }
}
