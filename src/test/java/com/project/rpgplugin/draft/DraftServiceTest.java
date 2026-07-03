package com.project.rpgplugin.draft;

import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.draft.DraftSession;
import com.project.rpgplugin.core.run.RunState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DraftServiceTest {

    private RunState run;

    @BeforeEach
    void setUp() {
        run = new RunState(UUID.randomUUID(), null);
    }

    @Test
    void stackingDoesNotDuplicateCardId() {
        run.addCard("swift");
        run.addCard("swift");
        assertEquals(1, run.ownedCards().size());
        assertEquals(2, run.cardCount("swift"));
    }

    @Test
    void stackIdempotentAfterReset() {
        run.addCard("swift");
        run.addCard("swift");
        run.reset();
        run.addCard("swift");
        assertEquals(1, run.cardCount("swift"));
    }

    @Test
    void sessionOptionsHaveNoDuplicates() {
        List<Card> cards = makeCards("a", "b", "c");
        DraftSession session = new DraftSession(UUID.randomUUID(), cards);
        assertEquals(3, session.options().size());
        Set<String> ids = new HashSet<>();
        for (Card c : session.options()) {
            assertTrue(ids.add(c.id()));
        }
    }

    @Test
    void sessionDecidePreventsFurtherChoice() {
        List<Card> cards = makeCards("a", "b");
        DraftSession session = new DraftSession(UUID.randomUUID(), cards);
        assertFalse(session.isDecided());
        session.choose(0);
        assertTrue(session.isDecided());
        assertNotNull(session.chosen());
        assertEquals("a", session.chosen().id());
    }

    @Test
    void sessionSkipMarksDecided() {
        List<Card> cards = makeCards("a");
        DraftSession session = new DraftSession(UUID.randomUUID(), cards);
        session.skip();
        assertTrue(session.isDecided());
        assertTrue(session.isSkipped());
    }

    private static List<Card> makeCards(String... ids) {
        List<Card> result = new ArrayList<>();
        for (String id : ids) {
            result.add(new Card() {
                @Override
                public String id() { return id; }
                @Override
                public com.project.rpgplugin.core.card.CardTier tier() { return com.project.rpgplugin.core.card.CardTier.BRONZE; }
                @Override
                public Set<com.project.rpgplugin.core.card.CardTag> tags() { return Set.of(); }
                @Override
                public com.project.rpgplugin.core.card.CardKind kind() { return com.project.rpgplugin.core.card.CardKind.AUGMENT; }
                @Override
                public org.bukkit.Material icon() { return org.bukkit.Material.STONE; }
                @Override
                public int maxStacks() { return 5; }
                @Override
                public boolean offerable(com.project.rpgplugin.core.run.RunState run) { return true; }
                @Override
                public void onAcquire(org.bukkit.entity.Player p, com.project.rpgplugin.core.run.RunState run) {}
                @Override
                public void onRemove(org.bukkit.entity.Player p, com.project.rpgplugin.core.run.RunState run) {}
            });
        }
        return result;
    }
}
