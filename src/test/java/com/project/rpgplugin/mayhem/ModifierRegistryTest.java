package com.project.rpgplugin.mayhem;

import com.project.rpgplugin.core.mayhem.MayhemContext;
import com.project.rpgplugin.core.mayhem.Modifier;
import com.project.rpgplugin.core.mayhem.ModifierRegistry;
import com.project.rpgplugin.core.mayhem.ModifierSeverity;
import com.project.rpgplugin.core.mayhem.ModifierTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ModifierRegistryTest {

    private ModifierRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ModifierRegistry();
    }

    @Test
    void emptyRegistryHasSizeZero() {
        assertEquals(0, registry.size());
    }

    @Test
    void registerAndRetrieve() {
        registry.register(mockModifier("test_mod", ModifierSeverity.MILD));
        assertTrue(registry.byId("test_mod").isPresent());
    }

    @Test
    void registerThrowsOnDuplicate() {
        registry.register(mockModifier("dup", ModifierSeverity.MILD));
        assertThrows(IllegalStateException.class, () ->
            registry.register(mockModifier("dup", ModifierSeverity.WILD)));
    }

    @Test
    void bySeverityFiltersCorrectly() {
        registry.register(mockModifier("m1", ModifierSeverity.MILD));
        registry.register(mockModifier("m2", ModifierSeverity.MILD));
        registry.register(mockModifier("w1", ModifierSeverity.WILD));
        registry.register(mockModifier("i1", ModifierSeverity.INSANE));

        assertEquals(2, registry.bySeverity(ModifierSeverity.MILD).size());
        assertEquals(1, registry.bySeverity(ModifierSeverity.WILD).size());
        assertEquals(1, registry.bySeverity(ModifierSeverity.INSANE).size());
    }

    @Test
    void rollOneReturnsOnlyFromAllowedSeverities() {
        registry.register(mockModifier("m1", ModifierSeverity.MILD));
        registry.register(mockModifier("m2", ModifierSeverity.MILD));
        registry.register(mockModifier("w1", ModifierSeverity.WILD));

        for (int i = 0; i < 100; i++) {
            Modifier m = registry.rollOne(Set.of(ModifierSeverity.MILD), new HashSet<>());
            assertNotNull(m);
            assertEquals(ModifierSeverity.MILD, m.severity());
        }
    }

    @Test
    void rollOneExcludesAlreadyActive() {
        registry.register(mockModifier("m1", ModifierSeverity.MILD));
        registry.register(mockModifier("m2", ModifierSeverity.MILD));

        Set<String> active = new HashSet<>();
        active.add("m1");

        for (int i = 0; i < 100; i++) {
            Modifier m = registry.rollOne(Set.of(ModifierSeverity.MILD), active);
            assertNotNull(m);
            assertNotEquals("m1", m.id());
        }
    }

    @Test
    void rollOneReturnsNullWhenPoolEmpty() {
        assertNull(registry.rollOne(Set.of(ModifierSeverity.INSANE), new HashSet<>()));
    }

    @Test
    void rollOneRespectsCompatibility() {
        registry.register(new Modifier() {
            public String id() { return "incompatible"; }
            public ModifierSeverity severity() { return ModifierSeverity.MILD; }
            public Set<ModifierTag> tags() { return Set.of(); }
            public void onActivate(MayhemContext ctx) {}
            public void onDeactivate(MayhemContext ctx) {}
            public boolean compatibleWith(Set<String> active) { return !active.contains("blocker"); }
        });
        registry.register(mockModifier("other", ModifierSeverity.MILD));

        Set<String> active = Set.of("blocker");
        for (int i = 0; i < 100; i++) {
            Modifier m = registry.rollOne(Set.of(ModifierSeverity.MILD), active);
            assertNotNull(m);
            assertNotEquals("incompatible", m.id());
        }
    }

    private Modifier mockModifier(String id, ModifierSeverity severity) {
        return new Modifier() {
            @Override
            public String id() { return id; }
            @Override
            public ModifierSeverity severity() { return severity; }
            @Override
            public Set<ModifierTag> tags() { return Set.of(); }
            @Override
            public void onActivate(MayhemContext ctx) {}
            @Override
            public void onDeactivate(MayhemContext ctx) {}
            @Override
            public boolean compatibleWith(Set<String> active) { return true; }
        };
    }
}
