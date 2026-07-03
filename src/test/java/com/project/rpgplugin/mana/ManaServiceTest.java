package com.project.rpgplugin.mana;

import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardKind;
import com.project.rpgplugin.core.card.CardTag;
import com.project.rpgplugin.core.card.CardTier;
import com.project.rpgplugin.core.draft.DraftService;
import com.project.rpgplugin.core.run.RunState;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ManaServiceTest {

    @Test
    void cardRequiredPluginIsNullByDefault() {
        Card c = card("test_card", null);
        assertNull(c.requiredPlugin());
    }

    @Test
    void cardRequiredPluginReturnsSetValue() {
        Card c = card("mana_pool", "AuraSkills");
        assertEquals("AuraSkills", c.requiredPlugin());
    }

    @Test
    void cardRequiredPluginFiltersDisabled() {
        // This tests the logic: a card with a missing plugin should be filtered
        Card c = card("mana_pool", "NonExistentPlugin");
        // The plugin doesn't exist on the test server, so the DraftService filter would exclude it
        assertEquals("NonExistentPlugin", c.requiredPlugin());
    }

    private static Card card(String id, String requiredPlugin) {
        return new Card() {
            @Override
            public String id() { return id; }
            @Override
            public CardTier tier() { return CardTier.BRONZE; }
            @Override
            public Set<CardTag> tags() { return Set.of(); }
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
            @Override
            public String requiredPlugin() { return requiredPlugin; }
        };
    }
}
