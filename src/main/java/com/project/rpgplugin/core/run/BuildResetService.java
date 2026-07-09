package com.project.rpgplugin.core.run;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.StatService;
import com.project.rpgplugin.core.mana.ManaService;
import com.project.rpgplugin.util.ItemKeys;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

public class BuildResetService {

    private static final String NAMESPACE = "roguelata";
    private static final String MODIFIER_PREFIX = "roguelata_";

    private static final Set<Attribute> PLAYER_ATTRIBUTES = Set.of(
        Attribute.MAX_HEALTH,
        Attribute.ATTACK_DAMAGE,
        Attribute.MOVEMENT_SPEED,
        Attribute.ARMOR,
        Attribute.ARMOR_TOUGHNESS,
        Attribute.KNOCKBACK_RESISTANCE,
        Attribute.ATTACK_SPEED,
        Attribute.LUCK,
        Attribute.BLOCK_INTERACTION_RANGE,
        Attribute.ENTITY_INTERACTION_RANGE,
        Attribute.SAFE_FALL_DISTANCE,
        Attribute.SUBMERGED_MINING_SPEED,
        Attribute.SWEEPING_DAMAGE_RATIO,
        Attribute.JUMP_STRENGTH,
        Attribute.SCALE,
        Attribute.STEP_HEIGHT
    );

    private final RPGPlugin plugin;
    private final CardRegistry cardRegistry;
    private final StatService statService;
    private final ManaService manaService;
    private final RunPersistenceService persistence;

    public BuildResetService(RPGPlugin plugin, CardRegistry cardRegistry,
                             StatService statService, ManaService manaService,
                             RunPersistenceService persistence) {
        this.plugin = plugin;
        this.cardRegistry = cardRegistry;
        this.statService = statService;
        this.manaService = manaService;
        this.persistence = persistence;
    }

    public void resetBuild(Player p, RunState run) {
        int previousLevel = run.level();

        for (String cardId : List.copyOf(run.ownedCards())) {
            Card c = cardRegistry.byId(cardId).orElse(null);
            if (c != null) c.onRemove(p, run);
        }

        plugin.getSkillServices().clearPlayerCooldowns(p.getUniqueId());

        clearAllRogueLataAttributes(p);

        p.getActivePotionEffects().stream()
            .toList()
            .forEach(e -> p.removePotionEffect(e.getType()));

        if (manaService != null && manaService.isEnabled()) {
            manaService.provider().setMana(p, 0);
        }

        p.setLevel(0);
        p.setExp(0f);

        removeTaggedSkillItems(p);

        if (plugin.getAuraSkillsIntegration().isEnabled()) {
            plugin.getAuraSkillsIntegration().resetAllAuraSkills(p);
            plugin.getAuraSkillsIntegration().removeSkillSlotAttachment(p);
        }

        run.reset();

        if (persistence != null) {
            persistence.clearRun(p);
        }

        ensureRpgBook(p);

        p.getScheduler().run(plugin, st -> {
            p.setHealth(p.getAttribute(Attribute.MAX_HEALTH) != null
                ? p.getAttribute(Attribute.MAX_HEALTH).getValue() : 20.0);
            p.setFoodLevel(20);
            p.setSaturation(5.0f);
            p.setFireTicks(0);
            p.setFallDistance(0);
            p.setRemainingAir(p.getMaximumAir());
        }, null);

        String msg = "<red>☠ <bold>A BUILD FOI DESTRUÍDA!</bold></red>"
            + " <gray><white>" + p.getName() + "</white> morreu no <yellow>Nível "
            + previousLevel + "</yellow> e perdeu todos os seus poderes...</gray>";
        Bukkit.broadcast(MiniMessage.miniMessage().deserialize(msg));
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.5f);
    }

    private void clearAllRogueLataAttributes(Player p) {
        for (Attribute attr : PLAYER_ATTRIBUTES) {
            AttributeInstance instance = p.getAttribute(attr);
            if (instance == null) continue;
            List<AttributeModifier> toRemove = instance.getModifiers().stream()
                .filter(this::isRogueLataModifier)
                .toList();
            for (AttributeModifier mod : toRemove) {
                instance.removeModifier(mod);
            }
        }
    }

    private boolean isRogueLataModifier(AttributeModifier mod) {
        NamespacedKey key = mod.getKey();
        return key != null
            && NAMESPACE.equals(key.getNamespace())
            && key.getKey().startsWith(MODIFIER_PREFIX);
    }

    private void removeTaggedSkillItems(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                var meta = item.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    for (var line : meta.getLore()) {
                        if (line.contains("Skill Item:")) {
                            p.getInventory().remove(item);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void ensureRpgBook(Player p) {
        boolean hasBook = false;
        for (ItemStack invItem : p.getInventory().getContents()) {
            if (ItemKeys.isRpgBook(invItem)) {
                hasBook = true;
                break;
            }
        }
        if (!hasBook) {
            p.getInventory().addItem(plugin.createRpgBook());
        }
    }
}
