package com.project.rpgplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    public static final String SKILL_NAMESPACE = "roguelata";

    private final RPGPlugin plugin;
    private final NamespacedKey skillsKey;
    private final NamespacedKey equippedKey;

    // Display data moved from switch statements to maps
    private static final Map<String, String> SKILL_DISPLAY_NAMES = new HashMap<>();
    private static final Map<String, String> SKILL_DESCRIPTIONS = new HashMap<>();
    private static final Map<String, Material> SKILL_MATERIALS = new HashMap<>();
    private static final Map<String, String> SKILL_TIERS = new HashMap<>();
    private static final Map<String, String> SKILL_TYPES = new HashMap<>();
    private static final List<String> BRONZE_KEYS = new ArrayList<>();
    private static final List<String> SILVER_KEYS = new ArrayList<>();
    private static final List<String> GOLD_KEYS = new ArrayList<>();
    private static final List<String> ALL_KEYS = new ArrayList<>();
    private static boolean dataInitialized = false;

    public PlayerManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.skillsKey = new NamespacedKey(plugin, "rpg_skills");
        this.equippedKey = new NamespacedKey(plugin, "rpg_equipped");
        initSkillData();
    }

    private static void initSkillData() {
        if (dataInitialized) return;
        dataInitialized = true;

        // Bronze - Explorer
        putSkill("dash", "<light_purple>Dash das Flores", "Consuma flor para dashes com Speed II e Invisibilidade", Material.NETHER_WART, "bronze", "explorer");
        putSkill("hydration", "<aqua>Hidratacao", "Garrafas de agua enchem fome e saturacao", Material.POTION, "bronze", "explorer");
        putSkill("step_assist", "<yellow>Passo Agil", "Consuma acucar para Speed II por 15s", Material.SUGAR, "bronze", "explorer");
        putSkill("grapple", "<green>Salto Escalador", "Consuma slimeball para salto frontal", Material.STRING, "bronze", "explorer");

        // Bronze - Miner
        putSkill("diet", "<gold>Dieta de Carvao", "Alimente-se de carvao para recuperar fome", Material.COAL, "bronze", "miner");
        putSkill("stone_smash", "<dark_gray>Quebra-Pedra", "Quebre pedra mais rapido segurando pedregulho", Material.COBBLESTONE, "bronze", "miner");
        putSkill("torch_light", "<white>Luz de Tocha", "Consuma tocha para Visao Noturna 30s", Material.TORCH, "bronze", "miner");

        // Bronze - Builder
        putSkill("feast", "<dark_green>Banquete de Folhas", "Alimente-se de folhas diretamente", Material.OAK_LEAVES, "bronze", "builder");
        putSkill("woodcutter", "<green>Lenhador Rapido", "Trigo com machado da Haste I por 10s", Material.WHEAT, "bronze", "builder");
        putSkill("silk_touch", "<gray>Toque de Seda Manual", "Colete blocos fragsis de mao vazia", Material.SHEARS, "bronze", "builder");
        putSkill("scaffold", "<green>Salto do Andaime", "Pula alto gerando blocos temporarios", Material.DIRT, "bronze", "builder");

        // Silver - Explorer
        putSkill("safe_fall", "<white>Escudo Anti-Queda", "Anula dano de queda com Slow Falling", Material.FEATHER, "silver", "explorer");
        putSkill("water_breathing", "<gold>Respiracao Aquatica", "Consuma Lapis Lazuli para respirar na agua", Material.LAPIS_LAZULI, "silver", "explorer");
        putSkill("jump_boost", "<red>Super Salto", "Salto passivo permanente nas botas", Material.RABBIT_FOOT, "silver", "explorer");
        putSkill("thermal_resistance", "<yellow>Escudo de Lava", "Magma cream da Fire Resistance por 15s", Material.MAGMA_CREAM, "silver", "explorer");

        // Silver - Miner
        putSkill("ore_sonar", "<yellow>Radar de Minerio", "Mapeia minerios proximos com particulas", Material.GLOWSTONE_DUST, "silver", "miner");
        putSkill("haste", "<yellow>Febre do Ouro", "Consuma ouro com picareta para Haste II", Material.GOLD_INGOT, "silver", "miner");

        // Silver - Builder
        putSkill("canopy_step", "<dark_green>Passo da Canopia", "Speed II ao pisar em folhas ou grama", Material.LEATHER_BOOTS, "silver", "builder");
        putSkill("fertilize", "<dark_green>Adubo Verde", "Farinha de osso cresce plantas ao redor", Material.BONE_MEAL, "silver", "builder");
        putSkill("flora_shield", "<green>Escudo Floral", "Consuma flor para regenerar 8 de vida", Material.DANDELION, "silver", "builder");
        putSkill("architect_focus", "<green>Foco do Arquiteto", "Resistencia IV por 30s com penalty", Material.STONE_BRICKS, "silver", "builder");
        putSkill("gravity_defiance", "<green>Desafio Gravitacional", "Flutue no ar temporariamente", Material.SLIME_BLOCK, "silver", "builder");

        // Gold - Explorer
        putSkill("recall", "<dark_purple>Recall do Dragao", "Teleporta ao spawn do mundo", Material.DRAGON_BREATH, "gold", "explorer");
        putSkill("sonar", "<light_purple>Sonar de Eco", "Revela entidades proximas com particulas", Material.AMETHYST_SHARD, "gold", "explorer");
        putSkill("dim_shift", "<blue>Mudanca Dimensional", "Teletransporte dimensional avancado", Material.ENDER_PEARL, "gold", "explorer");
        putSkill("wind_burst", "<green>Explosao de Vento", "Ejetado ao ceu com explosao de vento", Material.GUNPOWDER, "gold", "explorer");

        // Gold - Miner
        putSkill("sight", "<dark_gray>Visao Noturna", "Visao noturna permanente em locais escuros", Material.AMETHYST_SHARD, "gold", "miner");
        putSkill("ore_repair", "<gray>Reparo de Minerio", "Use ferro para reparar 30% da picareta", Material.IRON_INGOT, "gold", "miner");
        putSkill("molten_touch", "<red>Toque de Fusao", "Funde minerios automaticamente por 30s", Material.FLINT, "gold", "miner");
        putSkill("transmutation", "<red>Transmutacao", "Transmute metais em itens nobres", Material.DIAMOND, "gold", "miner");
        putSkill("gravity_shield", "<dark_gray>Escudo Gravitacional", "Resistencia III ao consumir obsidiana", Material.OBSIDIAN, "gold", "miner");
        putSkill("core_overdrive", "<red>Sobrecarga do Nucleo", "Haste III e Forca II com penalty de lentidao", Material.REDSTONE_BLOCK, "gold", "miner");

        // Gold - Builder
        putSkill("lumberjack", "<dark_green>Golpe do Lenhador", "Quebre arvore inteira com Haste IV", Material.IRON_BLOCK, "gold", "builder");
        putSkill("unbreakable_block", "<green>Bloco Reforcado", "Reforce bloco temporariamente", Material.CLAY_BALL, "gold", "builder");
        putSkill("grace", "<green>Graca da Pena", "Pule alto com queda lenta", Material.FEATHER, "gold", "builder");
    }

    private static void putSkill(String key, String displayName, String desc, Material mat, String tier, String type) {
        SKILL_DISPLAY_NAMES.put(key, displayName);
        SKILL_DESCRIPTIONS.put(key, desc);
        SKILL_MATERIALS.put(key, mat);
        SKILL_TIERS.put(key, tier);
        SKILL_TYPES.put(key, type);
        ALL_KEYS.add(key);
        switch (tier) {
            case "bronze" -> BRONZE_KEYS.add(key);
            case "silver" -> SILVER_KEYS.add(key);
            case "gold" -> GOLD_KEYS.add(key);
        }
    }

    // -- Data persistence --

    public static String namespacedKey(String skillKey) {
        return SKILL_NAMESPACE + "/" + skillKey.toLowerCase();
    }

    public static boolean isRogueLataKey(String fullKey) {
        return fullKey.startsWith(SKILL_NAMESPACE + "/");
    }

    public List<String> getUnlockedSkills(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if (!data.has(skillsKey, PersistentDataType.STRING)) {
            return new ArrayList<>();
        }
        String skillsStr = data.get(skillsKey, PersistentDataType.STRING);
        if (skillsStr == null || skillsStr.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> skills = new ArrayList<>(Arrays.asList(skillsStr.split(",")));
        skills.removeIf(String::isEmpty);
        return skills;
    }

    public void unlockSkill(Player player, String skillKey) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        List<String> skills = getUnlockedSkills(player);
        String lower = skillKey.toLowerCase();
        if (!skills.contains(lower)) {
            skills.add(lower);
            data.set(skillsKey, PersistentDataType.STRING, String.join(",", skills));
        }
    }

    public boolean hasSkill(Player player, String skillKey) {
        return getUnlockedSkills(player).contains(skillKey.toLowerCase());
    }

    public List<String> getEquippedSkills(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if (!data.has(equippedKey, PersistentDataType.STRING)) {
            return new ArrayList<>();
        }
        String str = data.get(equippedKey, PersistentDataType.STRING);
        if (str == null || str.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> eq = new ArrayList<>(Arrays.asList(str.split(",")));
        eq.removeIf(String::isEmpty);
        return eq;
    }

    public void equipSkill(Player player, String skillKey) {
        List<String> equipped = getEquippedSkills(player);
        String lower = skillKey.toLowerCase();
        if (!equipped.contains(lower)) {
            equipped.add(lower);
            saveEquipped(player, equipped);
        }
    }

    public void unequipSkill(Player player, String skillKey) {
        List<String> equipped = getEquippedSkills(player);
        if (equipped.remove(skillKey.toLowerCase())) {
            saveEquipped(player, equipped);
        }
    }

    private void saveEquipped(Player player, List<String> equipped) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if (equipped.isEmpty()) {
            data.remove(equippedKey);
        } else {
            data.set(equippedKey, PersistentDataType.STRING, String.join(",", equipped));
        }
    }

    public int countEquippedByTier(Player player, String tier) {
        int count = 0;
        for (String skillKey : getEquippedSkills(player)) {
            String t = SKILL_TIERS.get(skillKey);
            if (t != null && t.equalsIgnoreCase(tier)) {
                count++;
            }
        }
        return count;
    }

    public void clearAllSkills(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.remove(skillsKey);
        data.remove(equippedKey);
    }

    public int getTotalUnlockedCount(Player player) {
        return getUnlockedSkills(player).size();
    }

    // -- Difficulty --

    public double getDifficultyDamageMultiplier(Player player) {
        int count = getTotalUnlockedCount(player);
        return 1.0 + (count * 0.02);
    }

    public double getDifficultyHungerMultiplier(Player player) {
        int count = getTotalUnlockedCount(player);
        return 1.0 + (count * 0.015);
    }

    public int getSkillCountByType(Player player, String type) {
        int count = 0;
        for (String skillKey : getEquippedSkills(player)) {
            String t = SKILL_TYPES.get(skillKey);
            if (t != null && t.equalsIgnoreCase(type)) {
                count++;
            }
        }
        return count;
    }

    // -- Synergy --

    public void applySynergyEffects(Player player) {
        int explorerCount = getSkillCountByType(player, "explorer");
        int minerCount = getSkillCountByType(player, "miner");
        int builderCount = getSkillCountByType(player, "builder");

        if (explorerCount >= 4) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 0, true, false, false));
        }
        if (minerCount >= 4) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 0, true, false, false));
        }
        if (builderCount >= 4) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0, true, false, false));
        }
    }

    public void clearPlayerData(Player player) {
        clearAllSkills(player);
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    for (String line : meta.getLore()) {
                        if (line.contains("Skill Item:")) {
                            player.getInventory().remove(item);
                            break;
                        }
                    }
                }
            }
        }
        player.setLevel(0);
        player.setExp(0);
    }

    // -- Data lookups (no switches) --

    public Material determineSkillMaterial(String skillKey) {
        return SKILL_MATERIALS.getOrDefault(skillKey.toLowerCase(), Material.BARRIER);
    }

    public String getSkillTier(String skillKey) {
        return SKILL_TIERS.getOrDefault(skillKey.toLowerCase(), "bronze");
    }

    public int getSkillCost(String skillKey) {
        String tier = getSkillTier(skillKey);
        return switch (tier) {
            case "silver" -> 3;
            case "gold" -> 5;
            default -> 1;
        };
    }

    public String getSkillType(String skillKey) {
        return SKILL_TYPES.getOrDefault(skillKey.toLowerCase(), "explorer");
    }

    public String getSkillDisplayName(String skillKey) {
        return SKILL_DISPLAY_NAMES.getOrDefault(skillKey.toLowerCase(), "<gray>Habilidade Desconhecida");
    }

    public String getSkillDescription(String skillKey) {
        return SKILL_DESCRIPTIONS.getOrDefault(skillKey.toLowerCase(), "Descricao indisponivel");
    }

    public boolean isSkillEquippable(String skillKey) {
        String lower = skillKey.toLowerCase();
        return !lower.equals("sight") && !lower.equals("jump_boost");
    }

    public List<String> getAllBronzeSkills() {
        return new ArrayList<>(BRONZE_KEYS);
    }

    public List<String> getAllSilverSkills() {
        return new ArrayList<>(SILVER_KEYS);
    }

    public List<String> getAllGoldSkills() {
        return new ArrayList<>(GOLD_KEYS);
    }

    public List<String> getAllSkillKeys() {
        return new ArrayList<>(ALL_KEYS);
    }
}
