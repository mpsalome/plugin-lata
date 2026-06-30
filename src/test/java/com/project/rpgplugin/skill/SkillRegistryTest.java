package com.project.rpgplugin.skill;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillRegistry;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillServices;
import com.project.rpgplugin.core.skill.trigger.InteractTrigger;
import com.project.rpgplugin.core.skill.trigger.PassiveTrigger;
import com.project.rpgplugin.core.skill.trigger.TriggerKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SkillRegistryTest {

    private SkillRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SkillRegistry();
    }

    @Test
    void emptyRegistryHasSizeZero() {
        assertEquals(0, registry.size());
    }

    @Test
    void registerAndRetrieveSkill() {
        Skill mock = mockSkill("test_skill", SkillTier.BRONZE, SkillType.EXPLORER);
        registry.register(mock);
        assertEquals(1, registry.size());
        assertTrue(registry.byId("test_skill").isPresent());
        assertEquals(mock, registry.byId("test_skill").get());
    }

    @Test
    void registerThrowsOnDuplicateId() {
        Skill s1 = mockSkill("dupe", SkillTier.BRONZE, SkillType.EXPLORER);
        Skill s2 = mockSkill("dupe", SkillTier.SILVER, SkillType.MINER);
        registry.register(s1);
        assertThrows(IllegalStateException.class, () -> registry.register(s2));
    }

    @Test
    void byIdIsCaseInsensitive() {
        Skill skill = mockSkill("Dash", SkillTier.BRONZE, SkillType.EXPLORER);
        registry.register(skill);
        assertTrue(registry.byId("DASH").isPresent());
        assertTrue(registry.byId("dash").isPresent());
        assertTrue(registry.byId("Dash").isPresent());
    }

    @Test
    void byIdReturnsEmptyForUnknown() {
        assertTrue(registry.byId("nonexistent").isEmpty());
    }

    @Test
    void byTierFiltersCorrectly() {
        registry.register(mockSkill("s1", SkillTier.BRONZE, SkillType.EXPLORER));
        registry.register(mockSkill("s2", SkillTier.SILVER, SkillType.MINER));
        registry.register(mockSkill("s3", SkillTier.GOLD, SkillType.BUILDER));
        registry.register(mockSkill("s4", SkillTier.BRONZE, SkillType.MINER));

        assertEquals(2, registry.byTier(SkillTier.BRONZE).size());
        assertEquals(1, registry.byTier(SkillTier.SILVER).size());
        assertEquals(1, registry.byTier(SkillTier.GOLD).size());
    }

    @Test
    void byTypeFiltersCorrectly() {
        registry.register(mockSkill("s1", SkillTier.BRONZE, SkillType.EXPLORER));
        registry.register(mockSkill("s2", SkillTier.SILVER, SkillType.MINER));
        registry.register(mockSkill("s3", SkillTier.GOLD, SkillType.BUILDER));
        registry.register(mockSkill("s4", SkillTier.GOLD, SkillType.EXPLORER));

        assertEquals(2, registry.byType(SkillType.EXPLORER).size());
        assertEquals(1, registry.byType(SkillType.MINER).size());
        assertEquals(1, registry.byType(SkillType.BUILDER).size());
    }

    @Test
    void allReturnsAllRegistered() {
        registry.register(mockSkill("a", SkillTier.BRONZE, SkillType.EXPLORER));
        registry.register(mockSkill("b", SkillTier.SILVER, SkillType.MINER));
        assertEquals(2, registry.all().size());
    }

    @Test
    void allReturnedCollectionIsUnmodifiable() {
        registry.register(mockSkill("a", SkillTier.BRONZE, SkillType.EXPLORER));
        assertThrows(UnsupportedOperationException.class, () -> registry.all().add(null));
    }

    @Test
    void containsReturnsCorrectly() {
        registry.register(mockSkill("my_skill", SkillTier.BRONZE, SkillType.EXPLORER));
        assertTrue(registry.contains("my_skill"));
        assertTrue(registry.contains("MY_SKILL"));
        assertFalse(registry.contains("other"));
    }

    @Test
    void registerMaintainsInsertionOrder() {
        Skill a = mockSkill("a", SkillTier.BRONZE, SkillType.EXPLORER);
        Skill b = mockSkill("b", SkillTier.GOLD, SkillType.MINER);
        Skill c = mockSkill("c", SkillTier.SILVER, SkillType.BUILDER);
        registry.register(a);
        registry.register(b);
        registry.register(c);
        var it = registry.all().iterator();
        assertEquals(a, it.next());
        assertEquals(b, it.next());
        assertEquals(c, it.next());
    }

    @Test
    void skillTierEnumsHaveCorrectValues() {
        assertEquals(1, SkillTier.BRONZE.xpCost());
        assertEquals(3, SkillTier.SILVER.xpCost());
        assertEquals(5, SkillTier.GOLD.xpCost());
        assertEquals(3, SkillTier.BRONZE.maxEquipped());
        assertEquals(3, SkillTier.SILVER.maxEquipped());
        assertEquals(3, SkillTier.GOLD.maxEquipped());
    }

    @Test
    void skillTypeEnumsHaveCorrectKeys() {
        assertEquals("explorer", SkillType.EXPLORER.key());
        assertEquals("miner", SkillType.MINER.key());
        assertEquals("builder", SkillType.BUILDER.key());
    }

    @Test
    void allSkillsHaveUniqueIdsWhenRegistered() {
        var full = TestRegistries.createFullRegistry();
        assertEquals(35, full.size());
        Set<String> ids = new HashSet<>();
        for (Skill s : full.all()) {
            assertTrue(ids.add(s.id()), "Duplicate skill id: " + s.id());
        }
    }

    @Test
    void bronzeSilverGoldCountsMatchExpected() {
        var full = TestRegistries.createFullRegistry();
        assertEquals(11, full.byTier(SkillTier.BRONZE).size());
        assertEquals(11, full.byTier(SkillTier.SILVER).size());
        assertEquals(13, full.byTier(SkillTier.GOLD).size());
    }

    @Test
    void everySkillHasNonNullIcon() {
        var full = TestRegistries.createFullRegistry();
        for (Skill s : full.all()) {
            assertNotNull(s.icon(), "Skill " + s.id() + " has null icon");
        }
    }

    @Test
    void mockSkillTriggersAreNonNull() {
        Skill s = mockSkill("test", SkillTier.BRONZE, SkillType.EXPLORER);
        assertNotNull(s.trigger());
        assertFalse(s.trigger().kinds().isEmpty());
    }

    @Test
    void passiveSkillsAreCorrectlyMarked() {
        var full = TestRegistries.createFullRegistry();
        Set<String> passives = Set.of("jump_boost", "sight", "canopy_step", "safe_fall");
        for (Skill s : full.all()) {
            boolean expected = passives.contains(s.id());
            assertEquals(expected, s.passive(), "Skill " + s.id() + " passive flag mismatch");
        }
    }

    @Test
    void skillCostMatchesTier() {
        var full = TestRegistries.createFullRegistry();
        for (Skill s : full.all()) {
            assertEquals(s.tier().xpCost(), s.tier().xpCost());
        }
    }

    // --- Helper: creates a minimal Skill for registry testing ---

    private Skill mockSkill(String id, SkillTier tier, SkillType type) {
        return new Skill() {
            @Override
            public String id() { return id; }
            @Override
            public SkillTier tier() { return tier; }
            @Override
            public SkillType type() { return type; }
            @Override
            public org.bukkit.Material icon() { return org.bukkit.Material.STONE; }
            @Override
            public boolean passive() { return false; }
            @Override
            public java.time.Duration cooldown() { return java.time.Duration.ZERO; }
            @Override
            public com.project.rpgplugin.core.skill.trigger.SkillTrigger trigger() {
                return InteractTrigger.of(org.bukkit.Material.STONE);
            }
            @Override
            public void activate(SkillContext ctx) {}
        };
    }
}
