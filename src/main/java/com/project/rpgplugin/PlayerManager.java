package com.project.rpgplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    public static final String SKILL_NAMESPACE = "roguelata";

    private final RPGPlugin plugin;
    private final NamespacedKey skillsKey;
    private final NamespacedKey equippedKey;

    public PlayerManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.skillsKey = new NamespacedKey(plugin, "rpg_skills");
        this.equippedKey = new NamespacedKey(plugin, "rpg_equipped");
    }

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
            if (getSkillTier(skillKey).equalsIgnoreCase(tier)) {
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
            if (getSkillType(skillKey).equalsIgnoreCase(type)) {
                count++;
            }
        }
        return count;
    }

    public Material determineSkillMaterial(String skillKey) {
        switch (skillKey.toLowerCase()) {
            case "dash": return Material.NETHER_WART;
            case "hydration": return Material.POTION;
            case "step_assist": return Material.SUGAR;
            case "grapple": return Material.STRING;
            case "safe_fall": return Material.FEATHER;
            case "water_breathing": return Material.LAPIS_LAZULI;
            case "recall": return Material.DRAGON_BREATH;
            case "jump_boost": return Material.RABBIT_FOOT;
            case "thermal_resistance": return Material.MAGMA_CREAM;
            case "sonar": return Material.AMETHYST_SHARD;
            case "dim_shift": return Material.ENDER_PEARL;
            case "wind_burst": return Material.GUNPOWDER;
            case "diet": return Material.COAL;
            case "stone_smash": return Material.COBBLESTONE;
            case "torch_light": return Material.TORCH;
            case "ore_sonar": return Material.GLOWSTONE_DUST;
            case "sight": return Material.AMETHYST_SHARD;
            case "haste": return Material.GOLD_INGOT;
            case "ore_repair": return Material.IRON_INGOT;
            case "molten_touch": return Material.FLINT;
            case "transmutation": return Material.DIAMOND;
            case "gravity_shield": return Material.OBSIDIAN;
            case "core_overdrive": return Material.REDSTONE_BLOCK;
            case "feast": return Material.OAK_LEAVES;
            case "woodcutter": return Material.WHEAT;
            case "canopy_step": return Material.LEATHER_BOOTS;
            case "fertilize": return Material.BONE_MEAL;
            case "flora_shield": return Material.DANDELION;
            case "lumberjack": return Material.IRON_BLOCK;
            case "silk_touch": return Material.SHEARS;
            case "scaffold": return Material.DIRT;
            case "architect_focus": return Material.STONE_BRICKS;
            case "unbreakable_block": return Material.CLAY_BALL;
            case "grace": return Material.FEATHER;
            case "gravity_defiance": return Material.SLIME_BLOCK;
            default: return Material.BARRIER;
        }
    }

    public String getSkillTier(String skillKey) {
        switch (skillKey.toLowerCase()) {
            case "dash":
            case "hydration":
            case "diet":
            case "stone_smash":
            case "feast":
            case "woodcutter":
            case "step_assist":
            case "torch_light":
            case "silk_touch":
            case "scaffold":
            case "grapple":
                return "bronze";
            case "safe_fall":
            case "water_breathing":
            case "ore_sonar":
            case "haste":
            case "canopy_step":
            case "fertilize":
            case "jump_boost":
            case "thermal_resistance":
            case "architect_focus":
            case "flora_shield":
            case "gravity_defiance":
                return "silver";
            case "recall":
            case "sonar":
            case "dim_shift":
            case "wind_burst":
            case "sight":
            case "ore_repair":
            case "molten_touch":
            case "transmutation":
            case "gravity_shield":
            case "core_overdrive":
            case "unbreakable_block":
            case "lumberjack":
            case "grace":
                return "gold";
            default:
                return "bronze";
        }
    }

    public int getSkillCost(String skillKey) {
        String tier = getSkillTier(skillKey);
        switch (tier) {
            case "bronze": return 1;
            case "silver": return 3;
            case "gold": return 5;
            default: return 1;
        }
    }

    public String getSkillType(String skillKey) {
        switch (skillKey.toLowerCase()) {
            case "dash":
            case "hydration":
            case "step_assist":
            case "grapple":
            case "safe_fall":
            case "water_breathing":
            case "recall":
            case "jump_boost":
            case "thermal_resistance":
            case "sonar":
            case "dim_shift":
            case "wind_burst":
                return "explorer";
            case "diet":
            case "stone_smash":
            case "torch_light":
            case "ore_sonar":
            case "sight":
            case "haste":
            case "ore_repair":
            case "molten_touch":
            case "transmutation":
            case "gravity_shield":
            case "core_overdrive":
                return "miner";
            case "feast":
            case "woodcutter":
            case "canopy_step":
            case "fertilize":
            case "flora_shield":
            case "lumberjack":
            case "silk_touch":
            case "scaffold":
            case "architect_focus":
            case "unbreakable_block":
            case "grace":
            case "gravity_defiance":
                return "builder";
            default:
                return "explorer";
        }
    }

    public String getSkillDisplayName(String skillKey) {
        switch (skillKey.toLowerCase()) {
            case "dash": return "§dDash das Flores";
            case "hydration": return "§bHidratacao";
            case "step_assist": return "§ePasso Agil";
            case "grapple": return "§aSalto Escalador";
            case "safe_fall": return "§fEscudo Anti-Queda";
            case "water_breathing": return "§6Respiracao Aquatica";
            case "recall": return "§5Recall do Dragao";
            case "jump_boost": return "§cSuper Salto";
            case "thermal_resistance": return "§eEscudo de Lava";
            case "sonar": return "§dSonar de Eco";
            case "dim_shift": return "§9Mudanca Dimensional";
            case "wind_burst": return "§aExplosao de Vento";
            case "diet": return "§6Dieta de Carvao";
            case "stone_smash": return "§8Quebra-Pedra";
            case "torch_light": return "§fLuz de Tocha";
            case "ore_sonar": return "§eRadar de Minerio";
            case "sight": return "§8Visao Noturna";
            case "haste": return "§eFebre do Ouro";
            case "ore_repair": return "§7Reparo de Minerio";
            case "molten_touch": return "§cToque de Fusao";
            case "transmutation": return "§cTransmutacao";
            case "gravity_shield": return "§8Escudo Gravitacional";
            case "core_overdrive": return "§cSobrecarga do Nucleo";
            case "feast": return "§2Banquete de Folhas";
            case "woodcutter": return "§aLenhador Rapido";
            case "canopy_step": return "§2Paso da Canopia";
            case "fertilize": return "§2Adubo Verde";
            case "flora_shield": return "§aEscudo Floral";
            case "lumberjack": return "§2Golpe do Lenhador";
            case "silk_touch": return "§7Toque de Seda Manual";
            case "scaffold": return "§aSalto do Andaime";
            case "architect_focus": return "§aFoco do Arquiteto";
            case "unbreakable_block": return "§aBloco Reforcado";
            case "grace": return "§aGraca da Pena";
            case "gravity_defiance": return "§aDesafio Gravitacional";
            default: return "§7Habilidade Desconhecida";
        }
    }

    public String getSkillDescription(String skillKey) {
        switch (skillKey.toLowerCase()) {
            case "dash": return "Consuma flor para dashes com Speed II e Invisibilidade";
            case "hydration": return "Garrafas de agua enchem fome e saturacao";
            case "step_assist": return "Consuma acucar para Speed II por 15s";
            case "grapple": return "Consuma slimeball para salto frontal";
            case "safe_fall": return "Anula dano de queda com Slow Falling";
            case "water_breathing": return "Consuma Lapis Lazuli para respirar na agua";
            case "recall": return "Teleporta ao spawn do mundo";
            case "jump_boost": return "Salto passivo permanente nas botas";
            case "thermal_resistance": return "Magma cream da Fire Resistance por 15s";
            case "sonar": return "Revela entidades proximas com particulas";
            case "dim_shift": return "Teletransporte dimensional avancado";
            case "wind_burst": return "Ejetado ao ceu com explosao de vento";
            case "diet": return "Alimente-se de carvao para recuperar fome";
            case "stone_smash": return "Quebre pedra mais rapido segurando pedregulho";
            case "torch_light": return "Consuma tocha para Visao Noturna 30s";
            case "ore_sonar": return "Mapeia minerios proximos com particulas";
            case "sight": return "Visao noturna permanente em locais escuros";
            case "haste": return "Consuma ouro com picareta para Haste II";
            case "ore_repair": return "Use ferro para reparar 30% da picareta";
            case "molten_touch": return "Funde minerios automaticamente por 30s";
            case "transmutation": return "Transmute metais em itens nobres";
            case "gravity_shield": return "Resistencia III ao consumir obsidiana";
            case "core_overdrive": return "Haste III e Forca II com penalty de lentidao";
            case "feast": return "Alimente-se de folhas diretamente";
            case "woodcutter": return "Trigo com machado da Haste I por 10s";
            case "canopy_step": return "Speed II ao pisar em folhas ou grama";
            case "fertilize": return "Farinha de osso cresce plantas ao redor";
            case "flora_shield": return "Consuma flor para regenerar 8 de vida";
            case "lumberjack": return "Quebrearvore inteira com Haste IV";
            case "silk_touch": return "Colete blocos fragsis de mao vazia";
            case "scaffold": return "Pula alto gerandoblocos temporarios";
            case "architect_focus": return "Resistencia IV por 30s com penalty";
            case "unbreakable_block": return "Reforce bloco temporariamente";
            case "grace": return "Pule alto com queda lenta";
            case "gravity_defiance": return "Flutue no ar temporariamente";
            default: return "Descricao indisponivel";
        }
    }

    public boolean isSkillEquippable(String skillKey) {
        String lower = skillKey.toLowerCase();
        return !lower.equals("sight") && !lower.equals("jump_boost");
    }

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

    public List<String> getAllBronzeSkills() {
        return Arrays.asList("dash", "hydration", "diet", "stone_smash", "feast", "woodcutter", "step_assist", "torch_light", "silk_touch", "scaffold", "grapple");
    }

    public List<String> getAllSilverSkills() {
        return Arrays.asList("safe_fall", "water_breathing", "ore_sonar", "haste", "canopy_step", "fertilize", "jump_boost", "thermal_resistance", "architect_focus", "flora_shield", "gravity_defiance");
    }

    public List<String> getAllGoldSkills() {
        return Arrays.asList("recall", "sonar", "dim_shift", "wind_burst", "sight", "ore_repair", "molten_touch", "transmutation", "gravity_shield", "core_overdrive", "unbreakable_block", "lumberjack", "grace");
    }

    public List<String> getAllSkillKeys() {
        List<String> all = new ArrayList<>();
        all.addAll(getAllBronzeSkills());
        all.addAll(getAllSilverSkills());
        all.addAll(getAllGoldSkills());
        return all;
    }
}
