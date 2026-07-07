package com.project.rpgplugin.card;

import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardKind;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.CardTag;
import com.project.rpgplugin.core.card.CardTier;
import com.project.rpgplugin.core.run.RunState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CardRegistryTest {

    private CardRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CardRegistry();
    }

    @Test
    void emptyRegistryHasSizeZero() {
        assertEquals(0, registry.size());
    }

    @Test
    void registerAndRetrieveCard() {
        Card mock = mockCard("test_card", CardTier.BRONZE, CardKind.AUGMENT);
        registry.register(mock);
        assertEquals(1, registry.size());
        assertTrue(registry.byId("test_card").isPresent());
        assertEquals(mock, registry.byId("test_card").get());
    }

    @Test
    void registerThrowsOnDuplicateId() {
        registry.register(mockCard("dupe", CardTier.BRONZE, CardKind.AUGMENT));
        assertThrows(IllegalStateException.class, () ->
            registry.register(mockCard("dupe", CardTier.SILVER, CardKind.ABILITY)));
    }

    @Test
    void byIdIsCaseInsensitive() {
        registry.register(mockCard("Dash", CardTier.BRONZE, CardKind.ABILITY));
        assertTrue(registry.byId("DASH").isPresent());
        assertTrue(registry.byId("dash").isPresent());
    }

    @Test
    void byTierFiltersCorrectly() {
        registry.register(mockCard("c1", CardTier.BRONZE, CardKind.AUGMENT));
        registry.register(mockCard("c2", CardTier.SILVER, CardKind.AUGMENT));
        registry.register(mockCard("c3", CardTier.GOLD, CardKind.AUGMENT));
        registry.register(mockCard("c4", CardTier.BRONZE, CardKind.ABILITY));

        assertEquals(2, registry.byTier(CardTier.BRONZE).size());
        assertEquals(1, registry.byTier(CardTier.SILVER).size());
        assertEquals(1, registry.byTier(CardTier.GOLD).size());
    }

    @Test
    void byTagFiltersCorrectly() {
        registry.register(mockCardWithTag("c1", CardTag.EXPLORER));
        registry.register(mockCardWithTag("c2", CardTag.MINER));
        registry.register(mockCardWithTag("c3", CardTag.EXPLORER));

        assertEquals(2, registry.byTag(CardTag.EXPLORER).size());
        assertEquals(1, registry.byTag(CardTag.MINER).size());
    }

    @Test
    void allReturnsAllRegistered() {
        registry.register(mockCard("a", CardTier.BRONZE, CardKind.AUGMENT));
        registry.register(mockCard("b", CardTier.SILVER, CardKind.ABILITY));
        assertEquals(2, registry.all().size());
    }

    @Test
    void cardTierEnumsHaveCorrectValues() {
        assertEquals(1, CardTier.BRONZE.rank());
        assertEquals(2, CardTier.SILVER.rank());
        assertEquals(3, CardTier.GOLD.rank());
    }

    @Test
    void cardKindValuesAreCorrect() {
        assertEquals(CardKind.ABILITY, CardKind.valueOf("ABILITY"));
        assertEquals(CardKind.AUGMENT, CardKind.valueOf("AUGMENT"));
    }

    @Test
    void offerableFiltersByCardOfferable() {
        RunState run = new RunState(UUID.randomUUID(), registry);
        Card always = mockCard("always", CardTier.BRONZE, CardKind.AUGMENT);
        Card never = new Card() {
            public String id() { return "never"; }
            public CardTier tier() { return CardTier.BRONZE; }
            public Set<CardTag> tags() { return Set.of(); }
            public CardKind kind() { return CardKind.AUGMENT; }
            public Material icon() { return Material.STONE; }
            public int maxStacks() { return 1; }
            public boolean offerable(RunState r) { return false; }
            public void onAcquire(org.bukkit.entity.Player p, RunState r) {}
            public void onRemove(org.bukkit.entity.Player p, RunState r) {}
        };
        registry.register(always);
        registry.register(never);
        List<Card> offerable = registry.offerable(run);
        assertEquals(1, offerable.size());
        assertEquals("always", offerable.getFirst().id());
    }

    @Test
    void offerableByTierFiltersByBothTierAndOfferable() {
        RunState run = new RunState(UUID.randomUUID(), registry);
        registry.register(mockCard("a", CardTier.BRONZE, CardKind.AUGMENT));
        registry.register(mockCard("b", CardTier.GOLD, CardKind.AUGMENT));
        registry.register(new Card() {
            public String id() { return "c"; }
            public CardTier tier() { return CardTier.BRONZE; }
            public Set<CardTag> tags() { return Set.of(); }
            public CardKind kind() { return CardKind.AUGMENT; }
            public Material icon() { return Material.STONE; }
            public int maxStacks() { return 1; }
            public boolean offerable(RunState r) { return false; }
            public void onAcquire(org.bukkit.entity.Player p, RunState r) {}
            public void onRemove(org.bukkit.entity.Player p, RunState r) {}
        });
        assertEquals(1, registry.offerableByTier(run, CardTier.BRONZE).size());
        assertEquals(1, registry.offerableByTier(run, CardTier.GOLD).size());
    }

    private Card mockCard(String id, CardTier tier, CardKind kind) {
        return new Card() {
            @Override
            public String id() { return id; }
            @Override
            public CardTier tier() { return tier; }
            @Override
            public Set<CardTag> tags() { return Set.of(); }
            @Override
            public CardKind kind() { return kind; }
            @Override
            public Material icon() { return Material.STONE; }
            @Override
            public int maxStacks() { return 1; }
            @Override
            public boolean offerable(RunState run) { return true; }
            @Override
            public void onAcquire(Player p, RunState run) {}
            @Override
            public void onRemove(Player p, RunState run) {}
        };
    }

    private Card mockCardWithTag(String id, CardTag tag) {
        return new Card() {
            @Override
            public String id() { return id; }
            @Override
            public CardTier tier() { return CardTier.BRONZE; }
            @Override
            public Set<CardTag> tags() { return Set.of(tag); }
            @Override
            public CardKind kind() { return CardKind.AUGMENT; }
            @Override
            public Material icon() { return Material.STONE; }
            @Override
            public int maxStacks() { return 1; }
            @Override
            public boolean offerable(RunState run) { return true; }
            @Override
            public void onAcquire(Player p, RunState run) {}
            @Override
            public void onRemove(Player p, RunState run) {}
        };
    }
}
