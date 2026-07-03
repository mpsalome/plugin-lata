package com.project.rpgplugin.build;

import com.project.rpgplugin.core.build.SynergyService;
import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardKind;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.CardTag;
import com.project.rpgplugin.core.card.CardTier;
import com.project.rpgplugin.core.run.RunState;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SynergyServiceTest {

    private CardRegistry registry;
    private SynergyService synergy;
    private RunState run;

    @BeforeEach
    void setUp() {
        registry = new CardRegistry();
        registerCard("swift", CardTag.MOBILITY, CardTag.LOOT);
        registerCard("double_jump", CardTag.MOBILITY, CardTag.LOOT);
        registerCard("xp_boost", CardTag.LOOT);
        synergy = new SynergyService(registry);
        run = new RunState(UUID.randomUUID(), registry);
    }

    private void registerCard(String id, CardTag... tags) {
        Card c = new Card() {
            @Override
            public String id() { return id; }
            @Override
            public CardTier tier() { return CardTier.BRONZE; }
            @Override
            public Set<CardTag> tags() { return Set.of(tags); }
            @Override
            public CardKind kind() { return CardKind.AUGMENT; }
            @Override
            public Material icon() { return Material.STONE; }
            @Override
            public int maxStacks() { return 5; }
            @Override
            public boolean offerable(RunState run) { return true; }
            @Override
            public void onAcquire(org.bukkit.entity.Player p, RunState run) {}
            @Override
            public void onRemove(org.bukkit.entity.Player p, RunState run) {}
        };
        registry.register(c);
    }

    @Test
    void countByTagEmptyRun() {
        var counts = synergy.countByTag(run);
        assertTrue(counts.isEmpty());
    }

    @Test
    void detectArchetypeUnknownForEmptyRun() {
        assertEquals(SynergyService.Archetype.UNKNOWN, synergy.detectArchetype(run));
    }

    @Test
    void countByTagAfterAddingCards() {
        run.addCard("swift");
        run.addCard("swift");
        run.addCard("xp_boost");

        var counts = synergy.countByTag(run);
        // countByTag iterates unique cards, not stacks
        assertEquals(1, counts.getOrDefault(CardTag.MOBILITY, 0));
        assertEquals(2, counts.getOrDefault(CardTag.LOOT, 0));
    }

    @Test
    void detectArchetypePicksDominantTag() {
        run.addCard("swift");
        run.addCard("double_jump");
        run.addCard("xp_boost");
        // LOOT = 3, MOBILITY = 2 => LOOT should be dominant
        assertEquals(SynergyService.Archetype.LOOT, synergy.detectArchetype(run));
    }

    @Test
    void synergyWorksWithRegisteredCards() {
        assertTrue(registry.byId("swift").isPresent());
        assertTrue(registry.byId("double_jump").isPresent());
        assertTrue(registry.byId("xp_boost").isPresent());
    }
}
