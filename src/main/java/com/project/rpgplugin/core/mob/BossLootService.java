package com.project.rpgplugin.core.mob;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class BossLootService {

    private static final int BOTTLE_PER_TIER = 16;

    // Loot tiers by boss max HP
    public enum LootTier {
        COMMON(0, 250),
        UNCOMMON(251, 350),
        RARE(351, 500),
        EPIC(501, 9999);

        final int minHp;
        final int maxHp;
        LootTier(int minHp, int maxHp) { this.minHp = minHp; this.maxHp = maxHp; }

        static LootTier forHp(double hp) {
            for (LootTier t : values()) {
                if (hp >= t.minHp && hp <= t.maxHp) return t;
            }
            return EPIC;
        }
    }

    private final Random rng = ThreadLocalRandom.current();

    public List<ItemStack> generateLoot(double bossMaxHealth) {
        LootTier tier = LootTier.forHp(bossMaxHealth);
        List<ItemStack> loot = new ArrayList<>();

        // XP bottles — always
        int bottles = tier.ordinal() * BOTTLE_PER_TIER + BOTTLE_PER_TIER + rng.nextInt(BOTTLE_PER_TIER);
        loot.add(new ItemStack(Material.EXPERIENCE_BOTTLE, bottles));

        // Random enchanted weapon or tool
        if (rng.nextDouble() < 0.7) {
            loot.add(makeRandomEnchantedWeapon(tier));
        }

        // Random enchanted armor piece
        if (rng.nextDouble() < 0.5) {
            loot.add(makeRandomEnchantedArmor(tier));
        }

        // Random rare item pool
        int rareRolls = 1 + tier.ordinal() + rng.nextInt(2);
        for (int i = 0; i < rareRolls; i++) {
            loot.add(pickRareItem(tier));
        }

        // Bonus high-tier items
        if (tier == LootTier.EPIC && rng.nextDouble() < 0.6) {
            loot.add(new ItemStack(Material.NETHERITE_SCRAP, 1 + rng.nextInt(3)));
        }
        if (tier.ordinal() >= LootTier.RARE.ordinal() && rng.nextDouble() < 0.4) {
            loot.add(makeEnchantedBook(tier));
        }

        return loot;
    }

    private ItemStack makeRandomEnchantedWeapon(LootTier tier) {
        Material[] weapons = {
            Material.DIAMOND_SWORD, Material.DIAMOND_AXE,
            Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL,
            Material.IRON_SWORD, Material.IRON_AXE
        };
        ItemStack item = new ItemStack(weapons[rng.nextInt(weapons.length)]);
        int enchantLevel = Math.min(1 + tier.ordinal(), 4);
        addRandomEnchant(item, enchantLevel);
        maybeAddSecondEnchant(item, tier);
        return item;
    }

    private ItemStack makeRandomEnchantedArmor(LootTier tier) {
        Material[] armors = {
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE,
            Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
            Material.IRON_HELMET, Material.IRON_CHESTPLATE,
            Material.IRON_LEGGINGS, Material.IRON_BOOTS
        };
        ItemStack item = new ItemStack(armors[rng.nextInt(armors.length)]);
        int enchantLevel = Math.min(1 + tier.ordinal(), 3);
        addRandomEnchant(item, enchantLevel);
        maybeAddSecondEnchant(item, tier);
        return item;
    }

    private void addRandomEnchant(ItemStack item, int level) {
        Enchantment[] pool = {
            Enchantment.SHARPNESS, Enchantment.POWER, Enchantment.PROTECTION,
            Enchantment.EFFICIENCY, Enchantment.UNBREAKING, Enchantment.FIRE_ASPECT,
            Enchantment.SWEEPING_EDGE, Enchantment.KNOCKBACK, Enchantment.LOOTING,
            Enchantment.FORTUNE, Enchantment.FEATHER_FALLING, Enchantment.RESPIRATION,
            Enchantment.DEPTH_STRIDER
        };
        Enchantment ench = pool[rng.nextInt(pool.length)];
        if (ench.canEnchantItem(item)) {
            item.addUnsafeEnchantment(ench, Math.min(level, ench.getMaxLevel()));
        } else {
            // Fallback: try unbreaking
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, Math.min(level, 3));
        }
    }

    private void maybeAddSecondEnchant(ItemStack item, LootTier tier) {
        if (tier.ordinal() >= LootTier.RARE.ordinal() && rng.nextDouble() < 0.5) {
            addRandomEnchant(item, Math.max(1, tier.ordinal() - 1));
        }
    }

    private ItemStack pickRareItem(LootTier tier) {
        Material[][] pools = {
            // COMMON
            {
                Material.GOLDEN_APPLE, Material.DIAMOND, Material.ENDER_PEARL,
                Material.OBSIDIAN, Material.NAME_TAG, Material.SADDLE
            },
            // UNCOMMON
            {
                Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE,
                Material.DIAMOND, Material.ENDER_PEARL, Material.SADDLE,
                Material.MUSIC_DISC_CAT, Material.MUSIC_DISC_BLOCKS,
                Material.NAME_TAG
            },
            // RARE
            {
                Material.ENCHANTED_GOLDEN_APPLE, Material.DIAMOND,
                Material.NETHERITE_SCRAP, Material.SHULKER_SHELL,
                Material.MUSIC_DISC_WARD, Material.MUSIC_DISC_MALL,
                Material.SADDLE, Material.NAUTILUS_SHELL
            },
            // EPIC
            {
                Material.ENCHANTED_GOLDEN_APPLE, Material.NETHERITE_SCRAP,
                Material.SHULKER_SHELL, Material.NAUTILUS_SHELL,
                Material.TRIDENT, Material.MUSIC_DISC_PIGSTEP,
                Material.HEART_OF_THE_SEA, Material.ELYTRA
            }
        };
        Material[] pool = pools[Math.min(tier.ordinal(), pools.length - 1)];
        Material chosen = pool[rng.nextInt(pool.length)];
        int count = switch (chosen) {
            case DIAMOND -> 2 + rng.nextInt(5);
            case NETHERITE_SCRAP -> 1 + rng.nextInt(3);
            case ENDER_PEARL -> 2 + rng.nextInt(4);
            case OBSIDIAN -> 4 + rng.nextInt(8);
            default -> 1;
        };
        return new ItemStack(chosen, count);
    }

    private ItemStack makeEnchantedBook(LootTier tier) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        if (meta != null) {
            Enchantment[] pool = {
                Enchantment.SHARPNESS, Enchantment.PROTECTION, Enchantment.EFFICIENCY,
                Enchantment.UNBREAKING, Enchantment.FORTUNE, Enchantment.LOOTING,
                Enchantment.MENDING, Enchantment.SILK_TOUCH, Enchantment.POWER,
                Enchantment.FEATHER_FALLING, Enchantment.DEPTH_STRIDER,
                Enchantment.RESPIRATION, Enchantment.SWEEPING_EDGE
            };
            Enchantment ench = pool[rng.nextInt(pool.length)];
            int level = Math.min(1 + rng.nextInt(1 + tier.ordinal()), ench.getMaxLevel());
            meta.addStoredEnchant(ench, level, true);
            book.setItemMeta(meta);
        }
        return book;
    }
}
