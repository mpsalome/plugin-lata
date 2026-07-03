package com.project.rpgplugin.core.card.ability;

import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardKind;
import com.project.rpgplugin.core.card.CardTag;
import com.project.rpgplugin.core.card.CardTier;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.skill.Skill;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Set;

public class AbilityCard implements Card {

    private final Skill skill;
    private final Set<CardTag> tags;

    public AbilityCard(Skill skill, Set<CardTag> tags) {
        this.skill = skill;
        this.tags = tags;
    }

    @Override
    public String id() { return skill.id(); }

    @Override
    public CardTier tier() {
        return switch (skill.tier()) {
            case BRONZE -> CardTier.BRONZE;
            case SILVER -> CardTier.SILVER;
            case GOLD -> CardTier.GOLD;
        };
    }

    @Override
    public Set<CardTag> tags() { return tags; }

    @Override
    public CardKind kind() { return CardKind.ABILITY; }

    @Override
    public Material icon() { return skill.icon(); }

    @Override
    public int maxStacks() { return 1; }

    @Override
    public boolean offerable(RunState run) {
        return !run.hasCard(id());
    }

    @Override
    public void onAcquire(Player p, RunState run) {
        run.addCard(id());
        run.ownedAbilities().add(id());
    }

    @Override
    public void onRemove(Player p, RunState run) {
        run.removeCard(id());
        run.ownedAbilities().remove(id());
    }
}
