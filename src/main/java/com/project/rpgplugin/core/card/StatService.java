package com.project.rpgplugin.core.card;

import com.project.rpgplugin.core.card.augment.AttributeEffect;
import com.project.rpgplugin.core.card.augment.AugmentCard;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.util.ItemKeys;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class StatService {

    private static final String MODIFIER_PREFIX = "roguelata:";

    public void recompute(Player p, RunState run) {
        // 1. Remove all RogueLata attribute modifiers
        AttributeInstance maxHealth = p.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance attackDamage = p.getAttribute(Attribute.ATTACK_DAMAGE);
        AttributeInstance movementSpeed = p.getAttribute(Attribute.MOVEMENT_SPEED);
        AttributeInstance armor = p.getAttribute(Attribute.ARMOR);

        if (maxHealth != null) clearModifiers(maxHealth);
        if (attackDamage != null) clearModifiers(attackDamage);
        if (movementSpeed != null) clearModifiers(movementSpeed);
        if (armor != null) clearModifiers(armor);

        // 2. Sum contributions from all owned cards
        double healthBonus = 0;
        double damageBonus = 0;
        double speedBonus = 0;
        double armorBonus = 0;

        for (String cardId : run.ownedCards()) {
            Card card = run.cardRegistry().byId(cardId).orElse(null);
            if (card instanceof AugmentCard ac && ac.effect() instanceof AttributeEffect attr) {
                int stacks = run.cardCount(cardId);
                double total = attr.totalForStacks(stacks);
                switch (attr.attribute().toUpperCase()) {
                    case "MAX_HEALTH" -> healthBonus += total;
                    case "ATTACK_DAMAGE" -> damageBonus += total;
                    case "MOVEMENT_SPEED" -> speedBonus += total;
                    case "ARMOR" -> armorBonus += total;
                }
            }
        }

        // 3. Apply as AttributeModifier
        if (maxHealth != null && healthBonus != 0) {
            maxHealth.addModifier(new AttributeModifier(
                ItemKeys.withKey(MODIFIER_PREFIX + "max_health"),
                healthBonus, AttributeModifier.Operation.ADD_NUMBER
            ));
        }
        if (attackDamage != null && damageBonus != 0) {
            attackDamage.addModifier(new AttributeModifier(
                ItemKeys.withKey(MODIFIER_PREFIX + "attack_damage"),
                damageBonus, AttributeModifier.Operation.ADD_NUMBER
            ));
        }
        if (movementSpeed != null && speedBonus != 0) {
            movementSpeed.addModifier(new AttributeModifier(
                ItemKeys.withKey(MODIFIER_PREFIX + "movement_speed"),
                speedBonus, AttributeModifier.Operation.ADD_SCALAR
            ));
        }
        if (armor != null && armorBonus != 0) {
            armor.addModifier(new AttributeModifier(
                ItemKeys.withKey(MODIFIER_PREFIX + "armor"),
                armorBonus, AttributeModifier.Operation.ADD_NUMBER
            ));
        }

        // 4. Ensure health doesn't exceed new max
        double newMax = maxHealth != null ? maxHealth.getValue() : 20.0;
        if (p.getHealth() > newMax) {
            p.setHealth(newMax);
        }
    }

    public void resetToBaseline(Player p) {
        AttributeInstance maxHealth = p.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance attackDamage = p.getAttribute(Attribute.ATTACK_DAMAGE);
        AttributeInstance movementSpeed = p.getAttribute(Attribute.MOVEMENT_SPEED);
        AttributeInstance armor = p.getAttribute(Attribute.ARMOR);

        if (maxHealth != null) clearModifiers(maxHealth);
        if (attackDamage != null) clearModifiers(attackDamage);
        if (movementSpeed != null) clearModifiers(movementSpeed);
        if (armor != null) clearModifiers(armor);
    }

    private void clearModifiers(AttributeInstance attr) {
        Collection<AttributeModifier> mods = attr.getModifiers();
        if (mods.isEmpty()) return;
        attr.getModifiers().stream()
            .filter(m -> m.getName().startsWith(MODIFIER_PREFIX))
            .toList()
            .forEach(attr::removeModifier);
    }
}
