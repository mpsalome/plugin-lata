package com.project.rpgplugin.card;

import com.project.rpgplugin.core.card.*;
import com.project.rpgplugin.core.card.augment.*;
import com.project.rpgplugin.core.run.RunState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AugmentCardTest {

    private CardRegistry registry;
    private RunState run;

    @BeforeEach
    void setUp() {
        registry = new CardRegistry();
        run = new RunState(UUID.randomUUID(), registry);
    }

    @Test
    void augmentCardDefaults() {
        AugmentCard card = new AugmentCard(
            "test_aug", CardTier.BRONZE, List.of(CardTag.LOOT),
            org.bukkit.Material.STONE, 1, null, null
        );
        assertEquals("test_aug", card.id());
        assertEquals(CardTier.BRONZE, card.tier());
        assertEquals(CardKind.AUGMENT, card.kind());
        assertNull(card.requiredPlugin());
    }

    @Test
    void augmentCardWithPluginRequirement() {
        AugmentCard card = new AugmentCard(
            "mana_pool", CardTier.SILVER, List.of(CardTag.UTILITY),
            org.bukkit.Material.EXPERIENCE_BOTTLE, 3, null, "AuraSkills"
        );
        assertEquals("AuraSkills", card.requiredPlugin());
    }

    @Test
    void offerableRespectsMaxStacks() {
        AugmentCard card = new AugmentCard(
            "stackable", CardTier.BRONZE, List.of(CardTag.LOOT),
            org.bukkit.Material.STONE, 3, null, null
        );
        assertTrue(card.offerable(run));
        run.addCard("stackable");
        assertTrue(card.offerable(run));
        run.addCard("stackable");
        assertTrue(card.offerable(run));
        run.addCard("stackable");
        assertEquals(3, run.cardCount("stackable"));
        assertFalse(card.offerable(run));
    }

    @Test
    void augmentCardIsRegisteredInRegistry() {
        AugmentCard card = new AugmentCard(
            "test_aug", CardTier.GOLD, List.of(CardTag.DPS, CardTag.TANK),
            org.bukkit.Material.DIAMOND_SWORD, 1, null, null
        );
        registry.register(card);
        assertTrue(registry.byId("test_aug").isPresent());
        assertEquals(card, registry.byId("test_aug").get());
    }

    @Test
    void augmentEffectIsNullWhenNotProvided() {
        AugmentCard card = new AugmentCard(
            "no_effect", CardTier.BRONZE, List.of(),
            org.bukkit.Material.STONE, 1, null, null
        );
        assertNull(card.effect());
    }

    @Test
    void multiplierEffectAddsToRunMultipliers() {
        MultiplierEffect effect = new MultiplierEffect("test_mult", 0.5);
        AugmentCard card = new AugmentCard(
            "mult_test", CardTier.SILVER, List.of(CardTag.LOOT),
            org.bukkit.Material.GOLD_INGOT, 1, effect, null
        );
        assertEquals(0.0, run.getMultiplier("test_mult"), 0.001);
        card.onAcquire(null, run);
        assertEquals(0.5, run.getMultiplier("test_mult"), 0.001);
        card.onRemove(null, run);
        assertEquals(0.0, run.getMultiplier("test_mult"), 0.001);
    }

    @Test
    void onKillEffectIsTrackedInRun() {
        OnKillEffect effect = new OnKillEffect("gold_drop", 2);
        AugmentCard card = new AugmentCard(
            "midas", CardTier.GOLD, List.of(CardTag.LOOT, CardTag.RISK),
            org.bukkit.Material.GOLD_NUGGET, 1, effect, null
        );
        card.onAcquire(null, run);
        assertEquals(1, run.onKillEffects().size());
        assertEquals("gold_drop", run.onKillEffects().getFirst().effect());
        card.onRemove(null, run);
        assertTrue(run.onKillEffects().isEmpty());
    }
}
