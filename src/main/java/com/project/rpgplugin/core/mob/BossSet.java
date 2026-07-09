package com.project.rpgplugin.core.mob;

import com.project.rpgplugin.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class BossSet {

    private static final Random RNG = ThreadLocalRandom.current();

    private BossSet() {}

    public record Item(
        String slot,
        Material material,
        String baseName,
        Map<Enchantment, Integer> enchantments,
        List<String> loreLines,
        int rgb,
        int minLevel,
        Map<Enchantment, Integer> bonusEnchants
    ) {
        public Item {
            enchantments = Map.copyOf(enchantments);
            loreLines = loreLines == null ? List.of() : List.copyOf(loreLines);
            bonusEnchants = bonusEnchants == null ? Map.of() : Map.copyOf(bonusEnchants);
        }
    }

    public record SetDef(
        String bossId,
        String displayName,
        List<Item> items,
        String color1,
        String color2
    ) {}

    private static final Map<String, SetDef> SETS = new LinkedHashMap<>();

    private static final Map<Material, Material[]> UPGRADES = new HashMap<>();

    // --- convenience item factories ---

    private static Item item(String slot, Material mat, String name, Map<Enchantment, Integer> ench, String... lore) {
        return new Item(slot, mat, name, ench, List.of(lore), -1, 1, Map.of());
    }

    private static Item item(String slot, Material mat, String name, Map<Enchantment, Integer> ench,
                             int minLevel, Map<Enchantment, Integer> bonus, String... lore) {
        return new Item(slot, mat, name, ench, List.of(lore), -1, minLevel, bonus);
    }

    private static Item coloredItem(String slot, Material mat, String name, Map<Enchantment, Integer> ench,
                                    int rgb, String... lore) {
        return new Item(slot, mat, name, ench, List.of(lore), rgb, 1, Map.of());
    }

    // --- upgrade paths ---

    static {
        // Helmets
        UPGRADES.put(Material.IRON_HELMET, new Material[]{Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET});
        UPGRADES.put(Material.DIAMOND_HELMET, new Material[]{Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET});
        UPGRADES.put(Material.NETHERITE_HELMET, new Material[]{Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET});
        // Chestplates
        UPGRADES.put(Material.IRON_CHESTPLATE, new Material[]{Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE});
        UPGRADES.put(Material.DIAMOND_CHESTPLATE, new Material[]{Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE});
        UPGRADES.put(Material.NETHERITE_CHESTPLATE, new Material[]{Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE});
        // Leggings
        UPGRADES.put(Material.IRON_LEGGINGS, new Material[]{Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS});
        UPGRADES.put(Material.DIAMOND_LEGGINGS, new Material[]{Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS});
        UPGRADES.put(Material.NETHERITE_LEGGINGS, new Material[]{Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS});
        // Boots
        UPGRADES.put(Material.IRON_BOOTS, new Material[]{Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS});
        UPGRADES.put(Material.DIAMOND_BOOTS, new Material[]{Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS});
        UPGRADES.put(Material.NETHERITE_BOOTS, new Material[]{Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS});
        // Swords
        UPGRADES.put(Material.IRON_SWORD, new Material[]{Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD});
        UPGRADES.put(Material.DIAMOND_SWORD, new Material[]{Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD});
        UPGRADES.put(Material.NETHERITE_SWORD, new Material[]{Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD});
        // Axes
        UPGRADES.put(Material.IRON_AXE, new Material[]{Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE});
        UPGRADES.put(Material.DIAMOND_AXE, new Material[]{Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE});
        UPGRADES.put(Material.NETHERITE_AXE, new Material[]{Material.IRON_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE});
    }

    // --- boss definitions ---

    static {
        // 1. frostmaw — ice/frost theme
        SETS.put("frostmaw", new SetDef("frostmaw", "Frostmaw", List.of(
            item("feet", Material.DIAMOND_BOOTS, "Botas de Gelo Eterno", Map.of(
                Enchantment.FROST_WALKER, 2, Enchantment.DEPTH_STRIDER, 2
            )),
            item("chest", Material.DIAMOND_CHESTPLATE, "Peitoral da Nevasca", Map.of(
                Enchantment.PROTECTION, 3
            )),
            item("hand", Material.DIAMOND_SWORD, "Lâmina Glacial", Map.of(
                Enchantment.SHARPNESS, 3
            ), 30, Map.of(),
                "<gray>Congela inimigos ao acertar</gray>"),
            item("head", Material.DIAMOND_HELMET, "Elmo do Inverno", Map.of(
                Enchantment.RESPIRATION, 3
            )),
            item("legs", Material.DIAMOND_LEGGINGS, "Calças da Geada", Map.of(
                Enchantment.PROTECTION, 2
            ))
        ), "#4FC3F7", "#E0F7FA"));

        // 2. magma_tyrant — fire/magma theme
        SETS.put("magma_tyrant", new SetDef("magma_tyrant", "Tyrant", List.of(
            coloredItem("chest", Material.LEATHER_CHESTPLATE, "Manto do Inferno", Map.of(
                Enchantment.FIRE_PROTECTION, 4
            ), 0xFF4500),
            item("hand", Material.DIAMOND_AXE, "Forja Vulcânica", Map.of(
                Enchantment.FIRE_ASPECT, 2, Enchantment.SHARPNESS, 3
            )),
            item("feet", Material.IRON_BOOTS, "Grevas de Lava", Map.of(
                Enchantment.FIRE_PROTECTION, 3, Enchantment.DEPTH_STRIDER, 2
            )),
            item("head", Material.GOLDEN_HELMET, "Coroa de Chamas", Map.of(
                Enchantment.FIRE_PROTECTION, 3
            ))
        ), "#FF6F00", "#FFAB00"));

        // 3. storm_wyvern — lightning/storm theme
        SETS.put("storm_wyvern", new SetDef("storm_wyvern", "Storm", List.of(
            item("chest", Material.DIAMOND_CHESTPLATE, "Capa da Tempestade", Map.of(
                Enchantment.PROTECTION, 3, Enchantment.THORNS, 2
            )),
            item("hand", Material.TRIDENT, "Tridente Celeste", Map.of(
                Enchantment.CHANNELING, 1, Enchantment.RIPTIDE, 3, Enchantment.IMPALING, 3
            )),
            item("feet", Material.DIAMOND_BOOTS, "Asas Elétricas", Map.of(
                Enchantment.FEATHER_FALLING, 4, Enchantment.DEPTH_STRIDER, 3
            )),
            item("head", Material.DIAMOND_HELMET, "Elmo do Ciclone", Map.of(
                Enchantment.RESPIRATION, 2, Enchantment.AQUA_AFFINITY, 1
            ))
        ), "#7C4DFF", "#00E5FF"));

        // 4. void_lich — void/wither/darkness theme
        SETS.put("void_lich", new SetDef("void_lich", "Void Lich", List.of(
            item("chest", Material.NETHERITE_CHESTPLATE, "Armadura do Vazio", Map.of(
                Enchantment.PROTECTION, 4, Enchantment.BLAST_PROTECTION, 3
            )),
            item("hand", Material.DIAMOND_SWORD, "Cajado das Sombras", Map.of(
                Enchantment.SHARPNESS, 4
            ), 20, Map.of(),
                "<dark_purple>Aplica Wither II nos inimigos</dark_purple>"),
            item("head", Material.NETHERITE_HELMET, "Máscara do Vazio", Map.of(
                Enchantment.RESPIRATION, 3, Enchantment.PROTECTION, 3
            )),
            item("legs", Material.NETHERITE_LEGGINGS, "Calças do Esquecimento", Map.of(
                Enchantment.PROTECTION, 3
            )),
            item("feet", Material.NETHERITE_BOOTS, "Botas do Abismo", Map.of(
                Enchantment.FEATHER_FALLING, 4, Enchantment.DEPTH_STRIDER, 2
            ))
        ), "#4A148C", "#B71C1C"));

        // 5. sir_creeper_alot — explosion/entertainment theme
        SETS.put("sir_creeper_alot", new SetDef("sir_creeper_alot", "Sir Creeper", List.of(
            item("chest", Material.CHAINMAIL_CHESTPLATE, "Armadura do Fimoso", Map.of(
                Enchantment.BLAST_PROTECTION, 4
            )),
            item("feet", Material.CHAINMAIL_BOOTS, "Botas do Sucesso", Map.of(
                Enchantment.FEATHER_FALLING, 3, Enchantment.DEPTH_STRIDER, 1
            )),
            item("hand", Material.IRON_SWORD, "Placa do Sucesso", Map.of(
                Enchantment.SHARPNESS, 2
            )),
            item("head", Material.CHAINMAIL_HELMET, "Elmo do Sucesso", Map.of(
                Enchantment.PROTECTION, 2
            ))
        ), "#2E7D32", "#66BB6A"));

        // 6. slime_shady — slime/bouncy theme
        SETS.put("slime_shady", new SetDef("slime_shady", "Slime", List.of(
            coloredItem("chest", Material.LEATHER_CHESTPLATE, "Túnica Pegajosa", Map.of(
                Enchantment.PROTECTION, 2
            ), 0x7CB342),
            item("feet", Material.IRON_BOOTS, "Botas Saltitantes", Map.of(
                Enchantment.FEATHER_FALLING, 6, Enchantment.DEPTH_STRIDER, 4
            )),
            item("hand", Material.IRON_SWORD, "Cajado de Gosma", Map.of(
                Enchantment.KNOCKBACK, 3, Enchantment.BANE_OF_ARTHROPODS, 2
            )),
            coloredItem("head", Material.LEATHER_HELMET, "Capuz Escorregadio", Map.of(
                Enchantment.RESPIRATION, 1
            ), 0x7CB342)
        ), "#7CB342", "#CDDC39"));

        // 7. the_beheader — execution/axe theme
        SETS.put("the_beheader", new SetDef("the_beheader", "Beheader", List.of(
            item("hand", Material.DIAMOND_AXE, "Machado do Carrasco", Map.of(
                Enchantment.SHARPNESS, 4
            ), 15, Map.of(Enchantment.FIRE_ASPECT, 1)),
            item("chest", Material.IRON_CHESTPLATE, "Armadura do Cadafalso", Map.of(
                Enchantment.PROTECTION, 3, Enchantment.THORNS, 1
            )),
            item("feet", Material.IRON_BOOTS, "Botas da Execução", Map.of(
                Enchantment.FEATHER_FALLING, 3, Enchantment.DEPTH_STRIDER, 1
            )),
            item("head", Material.IRON_HELMET, "Elmo do Julgamento", Map.of(
                Enchantment.PROTECTION, 3
            ))
        ), "#B71C1C", "#E53935"));

        // 8. ancient_guardian — ocean/guardian theme
        SETS.put("ancient_guardian", new SetDef("ancient_guardian", "Guardian", List.of(
            item("chest", Material.DIAMOND_CHESTPLATE, "Couraça das Profundezas", Map.of(
                Enchantment.PROTECTION, 3, Enchantment.RESPIRATION, 3
            )),
            item("hand", Material.TRIDENT, "Tridente do Abismo", Map.of(
                Enchantment.IMPALING, 4, Enchantment.LOYALTY, 3, Enchantment.RIPTIDE, 1
            )),
            item("feet", Material.DIAMOND_BOOTS, "Nadadeiras do Guardião", Map.of(
                Enchantment.DEPTH_STRIDER, 4, Enchantment.FEATHER_FALLING, 2
            )),
            item("head", Material.DIAMOND_HELMET, "Elmo Coral", Map.of(
                Enchantment.AQUA_AFFINITY, 1, Enchantment.RESPIRATION, 3
            )),
            item("offhand", Material.SHIELD, "Escudo do Recife", Map.of(
                Enchantment.UNBREAKING, 3
            ))
        ), "#00695C", "#26C6DA"));

        // 9. piglin_warlord — gold/piglin theme
        SETS.put("piglin_warlord", new SetDef("piglin_warlord", "Warlord", List.of(
            item("chest", Material.GOLDEN_CHESTPLATE, "Armadura Dourada", Map.of(
                Enchantment.PROTECTION, 3, Enchantment.FIRE_PROTECTION, 3
            )),
            item("hand", Material.GOLDEN_AXE, "Machado do General", Map.of(
                Enchantment.SHARPNESS, 3, Enchantment.FIRE_ASPECT, 2
            )),
            item("feet", Material.GOLDEN_BOOTS, "Bota de Ouro", Map.of(
                Enchantment.FEATHER_FALLING, 3, Enchantment.DEPTH_STRIDER, 1
            )),
            item("head", Material.GOLDEN_HELMET, "Coroa do Nether", Map.of(
                Enchantment.PROTECTION, 3, Enchantment.RESPIRATION, 1
            )),
            item("offhand", Material.SHIELD, "Escudo do Bruto", Map.of(
                Enchantment.UNBREAKING, 2
            ))
        ), "#FF8F00", "#FFD54F"));

        // 10. phantom_king — phantom/sky/elytra theme
        SETS.put("phantom_king", new SetDef("phantom_king", "Phantom King", List.of(
            item("chest", Material.ELYTRA, "Asas do Rei", Map.of(
                Enchantment.UNBREAKING, 3
            )),
            item("hand", Material.DIAMOND_SWORD, "Garras Fantasmas", Map.of(
                Enchantment.SHARPNESS, 3
            ), 20, Map.of(Enchantment.SWEEPING_EDGE, 3)),
            item("feet", Material.DIAMOND_BOOTS, "Botas Etéreas", Map.of(
                Enchantment.FEATHER_FALLING, 5, Enchantment.DEPTH_STRIDER, 2
            )),
            item("head", Material.DIAMOND_HELMET, "Máscara Alada", Map.of(
                Enchantment.PROTECTION, 2, Enchantment.RESPIRATION, 2
            ))
        ), "#7E57C2", "#B39DDB"));
    }

    // --- public static accessors ---

    public static SetDef getSet(String bossId) {
        return SETS.get(bossId);
    }

    public static List<Item> getItems(String bossId) {
        SetDef def = SETS.get(bossId);
        return def == null ? List.of() : def.items();
    }

    // --- Item creation ---

    public static ItemStack createItem(String bossId, Item item, int bossLevel) {
        SetDef def = SETS.get(bossId);
        if (def == null) return null;

        bossLevel = Math.max(1, bossLevel);

        // Upgrade material by level tier
        Material finalMat = resolveMaterial(item.material(), bossLevel);

        ItemStack stack = new ItemStack(finalMat);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        // Leather armor color
        if (item.rgb() >= 0 && meta instanceof LeatherArmorMeta leather) {
            leather.setColor(org.bukkit.Color.fromRGB(item.rgb()));
        }

        // Display name
        String nameFormat = "<gradient:" + def.color1() + ":" + def.color2() + "><bold>"
            + def.displayName() + " " + item.baseName() + "</bold></gradient>";
        meta.displayName(Text.mm(nameFormat));

        // Enchantments — base
        for (Map.Entry<Enchantment, Integer> entry : item.enchantments().entrySet()) {
            int level = scaleEnchantLevel(entry.getValue(), bossLevel);
            if (level > 0) {
                meta.addEnchant(entry.getKey(), level, true);
            }
        }

        // Enchantments — bonus (level-gated)
        if (bossLevel >= item.minLevel()) {
            for (Map.Entry<Enchantment, Integer> entry : item.bonusEnchants().entrySet()) {
                int level = scaleEnchantLevel(entry.getValue(), bossLevel);
                if (level > 0) {
                    meta.addEnchant(entry.getKey(), level, true);
                }
            }
        }

        // Lore
        if (!item.loreLines().isEmpty()) {
            List<Component> lore = item.loreLines().stream()
                .map(Text::mm)
                .collect(Collectors.toList());
            meta.lore(lore);
        }

        stack.setItemMeta(meta);
        return stack;
    }

    // --- Loot generation ---

    public static List<ItemStack> generateSetLoot(String bossId, int bossLevel) {
        SetDef def = SETS.get(bossId);
        if (def == null) return List.of();

        bossLevel = Math.max(1, bossLevel);
        List<Item> items = new ArrayList<>(def.items());

        int count = switch (bossLevel) {
            case 1, 2, 3, 4, 5 -> 1;
            case 6, 7, 8, 9, 10 -> 1 + RNG.nextInt(2);  // 1–2
            case 11, 12, 13, 14, 15 -> 2 + RNG.nextInt(2); // 2–3
            default -> 3 + RNG.nextInt(2);                // 3–4
        };
        count = Math.min(count, items.size());

        // Shuffle and pick
        Collections.shuffle(items, RNG);
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ItemStack stack = createItem(bossId, items.get(i), bossLevel);
            if (stack != null) {
                result.add(stack);
            }
        }
        return result;
    }

    // --- tier & scaling helpers ---

    private static int getTier(int bossLevel) {
        if (bossLevel <= 10) return 0;
        if (bossLevel <= 20) return 1;
        return 2;
    }

    private static Material resolveMaterial(Material base, int bossLevel) {
        Material[] path = UPGRADES.get(base);
        if (path == null) return base; // not upgradable (leather, gold, chainmail, elytra, trident, shield, etc.)
        int tier = getTier(bossLevel);
        return path[Math.min(tier, path.length - 1)];
    }

    private static int scaleEnchantLevel(int baseLevel, int bossLevel) {
        if (bossLevel <= 10) return Math.max(1, baseLevel - 1);
        if (bossLevel <= 20) return baseLevel;
        return baseLevel + 1;
    }
}
