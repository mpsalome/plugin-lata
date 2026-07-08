package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardKind;
import com.project.rpgplugin.core.card.CardTag;
import com.project.rpgplugin.core.card.CardTier;
import com.project.rpgplugin.core.run.RunState;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AugmentCard implements Card {

    private final String id;
    private final CardTier tier;
    private final Set<CardTag> tags;
    private final Material icon;
    private final int maxStacks;
    private final AugmentEffect effect;
    private final String requiredPlugin;

    public AugmentCard(String id, CardTier tier, List<CardTag> tags, Material icon, int maxStacks, AugmentEffect effect, String requiredPlugin) {
        this.id = id;
        this.tier = tier;
        this.tags = Set.copyOf(tags);
        this.icon = icon;
        this.maxStacks = maxStacks;
        this.effect = effect;
        this.requiredPlugin = requiredPlugin;
    }

    @Override
    public String id() { return id; }

    @Override
    public CardTier tier() { return tier; }

    @Override
    public Set<CardTag> tags() { return tags; }

    @Override
    public CardKind kind() { return CardKind.AUGMENT; }

    @Override
    public Material icon() { return icon; }

    @Override
    public int maxStacks() { return maxStacks; }

    @Override
    public boolean offerable(RunState run) {
        return run.cardCount(id()) < maxStacks;
    }

    @Override
    public void onAcquire(Player p, RunState run) {
        run.addCard(id());
        if (effect != null) {
            effect.apply(p, run, run.cardCount(id()));
        }
    }

    @Override
    public void onRemove(Player p, RunState run) {
        if (effect != null) {
            effect.unapply(p, run, 0);
        }
    }

    public AugmentEffect effect() { return effect; }

    @Override
    public String requiredPlugin() { return requiredPlugin; }

    @Override
    public List<String> lore(RunState run) {
        List<String> lines = new ArrayList<>();
        lines.add("");
        if (effect != null) {
            lines.addAll(effect.description());
        }
        return lines;
    }

    public static AugmentCard fromConfig(String id, ConfigurationSection section) {
        CardTier tier = CardTier.valueOf(section.getString("tier", "BRONZE").toUpperCase());
        List<CardTag> tags = section.getStringList("tags").stream()
            .map(t -> CardTag.valueOf(t.toUpperCase()))
            .toList();
        Material icon = Material.getMaterial(section.getString("icon", "BARRIER"));
        if (icon == null) icon = Material.BARRIER;
        int maxStacks = section.getInt("max_stacks", 1);
        String requiredPlugin = section.getString("require_plugin", null);

        AugmentEffect effect = parseEffect(id, section.getConfigurationSection("effect"));

        return new AugmentCard(id, tier, tags, icon, maxStacks, effect, requiredPlugin);
    }

    private static AugmentEffect parseEffect(String id, ConfigurationSection eff) {
        if (eff == null) return null;
        String type = eff.getString("type", "");
        return switch (type.toUpperCase()) {
            case "ATTRIBUTE" -> {
                String attr = eff.getString("attribute", "MAX_HEALTH");
                double add = eff.getDouble("add_per_stack", 0);
                yield new AttributeEffect(attr, add);
            }
            case "MULT" -> {
                String key = eff.getString("key", "");
                double add = eff.getDouble("add", 0);
                yield new MultiplierEffect(key, add);
            }
            case "ON_DAMAGE_DEALT" -> {
                double healPct = eff.getDouble("heal_pct", 0);
                yield new LifestealEffect(healPct);
            }
            case "MULT_MULTI" -> {
                double damageDealt = eff.getDouble("damage_dealt", 0);
                double damageTaken = eff.getDouble("damage_taken", 0);
                yield new MultiMultiplierEffect(damageDealt, damageTaken);
            }
            case "ON_KILL" -> {
                String killEffect = eff.getString("effect", "");
                double value = eff.getDouble("value", 0);
                yield new OnKillEffect(killEffect, value);
            }
            case "POTION" -> {
                String potionType = eff.getString("potion", "");
                int amplifier = eff.getInt("amplifier", 0);
                yield new PotionEffectAugment(potionType, amplifier);
            }
            case "GIANT" -> {
                double health = eff.getDouble("health_per_stack", 20);
                double damage = eff.getDouble("damage_per_stack", 0.1);
                double slow = eff.getDouble("slow_amplifier", 1);
                yield new GiantEffect(health, damage, slow);
            }
            default -> null;
        };
    }
}
