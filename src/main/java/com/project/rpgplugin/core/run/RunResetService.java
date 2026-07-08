package com.project.rpgplugin.core.run;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.StatService;
import com.project.rpgplugin.core.mana.ManaService;
import com.project.rpgplugin.core.mayhem.MayhemService;
import com.project.rpgplugin.util.ItemKeys;
import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RunResetService {

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
    private final MayhemService mayhemService;
    private final SpawnResolver spawnResolver;
    private final ManaService manaService;
    private final RunPersistenceService persistence;

    private final Set<Location> trackedBlocks = new HashSet<>();
    private final Set<UUID> trackedEntities = new HashSet<>();

    public RunResetService(RPGPlugin plugin, CardRegistry cardRegistry, StatService statService,
                           MayhemService mayhemService, SpawnResolver spawnResolver, ManaService manaService,
                           RunPersistenceService persistence) {
        this.plugin = plugin;
        this.cardRegistry = cardRegistry;
        this.statService = statService;
        this.mayhemService = mayhemService;
        this.spawnResolver = spawnResolver;
        this.manaService = manaService;
        this.persistence = persistence;
    }

    public void trackBlock(Location loc) {
        trackedBlocks.add(loc);
    }

    public void untrackBlock(Location loc) {
        trackedBlocks.remove(loc);
    }

    public void trackEntity(UUID entityId) {
        trackedEntities.add(entityId);
    }

    public void untrackEntity(UUID entityId) {
        trackedEntities.remove(entityId);
    }

    public void fullReset(Player p, RunState run) {
        // 1. Remove all cards — call onRemove for each
        for (String cardId : List.copyOf(run.ownedCards())) {
            Card c = cardRegistry.byId(cardId).orElse(null);
            if (c != null) c.onRemove(p, run);
        }

        // 2. Clean leaked state from SkillServices
        plugin.getSkillServices().reinforcedBlocks().clear();
        plugin.getSkillServices().clearPlayerCooldowns(p.getUniqueId());

        // 3. Remove ALL RogueLata attribute modifiers from ALL player attributes
        clearAllRogueLataAttributes(p);

        // 4. Clear Bukkit potion effects
        p.getActivePotionEffects().stream()
            .toList()
            .forEach(e -> p.removePotionEffect(e.getType()));

        // 5. Heal and reset food
        p.setHealth(p.getAttribute(Attribute.MAX_HEALTH) != null
            ? p.getAttribute(Attribute.MAX_HEALTH).getValue() : 20.0);
        p.setFoodLevel(20);
        p.setSaturation(5.0f);
        p.setFireTicks(0);
        p.setFallDistance(0);
        p.setRemainingAir(p.getMaximumAir());

        // 6. Reset mana to max
        if (manaService != null && manaService.isEnabled()) {
            double maxMana = manaService.getMaxMana(p, run);
            manaService.provider().setMana(p, maxMana);
        }

        // 7. Level / XP vanilla to 0
        p.setLevel(0);
        p.setExp(0f);

        // 8. Remove tagged skill items from inventory
        removeTaggedSkillItems(p);

        // 9. Mayhem — clear active modifiers
        mayhemService.clear(p, run);

        // 10. AuraSkills — reset + remove slot attachments
        if (plugin.getAuraSkillsIntegration().isEnabled()) {
            plugin.getAuraSkillsIntegration().resetAllAuraSkills(p);
            plugin.getAuraSkillsIntegration().removeSkillSlotAttachment(p);
        }

        // 11. Reset run state
        run.reset();

        // 12. Clean tracked blocks (Folia-aware: use region scheduler per chunk)
        cleanTrackedBlocks();

        // 13. Clean tracked entities (Folia-aware: use entity scheduler)
        cleanTrackedEntities();

        // 14. Teleport to spawn (use entity scheduler for Folia compatibility)
        Location spawn = spawnResolver.resolve(p);
        p.teleportAsync(spawn);

        // 15. Clear PDC persisted run data
        if (persistence != null) {
            persistence.clearRun(p);
        }

        // 16. Ensure RPG book
        ensureRpgBook(p);

        // 17. Feedback
        p.sendMessage(com.project.rpgplugin.util.Text.mm("<red><bold>RUN ENCERRADA"));
        p.sendMessage(com.project.rpgplugin.util.Text.mm("<gray>Todos os poderes foram perdidos. Uma nova run comeca!"));
    }

    public void softReset(Player p, RunState run) {
        run.ownedCards().clear();
        run.ownedAbilities().clear();
        cleanTrackedBlocks();
        cleanTrackedEntities();
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

    private void cleanTrackedBlocks() {
        List<Location> snapshot = List.copyOf(trackedBlocks);
        trackedBlocks.clear();
        for (Location loc : snapshot) {
            World world = loc.getWorld();
            if (world == null) continue;
            world.getBlockAt(loc).setType(org.bukkit.Material.AIR);
        }
    }

    private void cleanTrackedEntities() {
        List<UUID> snapshot = List.copyOf(trackedEntities);
        trackedEntities.clear();
        for (UUID uid : snapshot) {
            for (World world : plugin.getServer().getWorlds()) {
                Entity entity = world.getEntity(uid);
                if (entity != null) {
                    entity.remove();
                    break;
                }
            }
        }
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

    public Set<Location> trackedBlocks() {
        return trackedBlocks;
    }

    public Set<UUID> trackedEntities() {
        return trackedEntities;
    }
}
