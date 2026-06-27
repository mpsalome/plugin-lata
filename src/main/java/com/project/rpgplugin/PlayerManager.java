package com.project.rpgplugin;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerManager {

    private final RPGPlugin plugin;
    private final NamespacedKey classKey;
    private final NamespacedKey skillsKey;
    private final NamespacedKey equippedItemsKey;

    public PlayerManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.classKey = new NamespacedKey(plugin, "rpg_class");
        this.skillsKey = new NamespacedKey(plugin, "rpg_skills");
        this.equippedItemsKey = new NamespacedKey(plugin, "rpg_equipped_items");
    }

    /**
     * Set a player's RPG class using PersistentDataContainer.
     * @param player The Minecraft Player
     * @param className The class name (EXPLORER, MINER, BUILDER, NONE)
     */
    public void setPlayerClass(Player player, String className) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if (className == null || className.equalsIgnoreCase("NONE")) {
            data.remove(classKey);
            data.remove(skillsKey);
        } else {
            data.set(classKey, PersistentDataType.STRING, className.toUpperCase());
        }
    }

    /**
     * Get the player's current RPG class from PersistentDataContainer.
     * @param player The Minecraft Player
     * @return The class name (defaulting to "NONE")
     */
    public String getPlayerClass(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if (!data.has(classKey, PersistentDataType.STRING)) {
            return "NONE";
        }
        String rpgClass = data.get(classKey, PersistentDataType.STRING);
        return rpgClass != null ? rpgClass : "NONE";
    }

    /**
     * Checks if a player has a specific RPG class.
     */
    public boolean hasClass(Player player, String className) {
        return getPlayerClass(player).equalsIgnoreCase(className);
    }

    /**
     * Gets a list of unlocked skill keys from PersistentDataContainer.
     */
    public List<String> getUnlockedSkills(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if (!data.has(skillsKey, PersistentDataType.STRING)) {
            return new ArrayList<>();
        }
        String skillsStr = data.get(skillsKey, PersistentDataType.STRING);
        if (skillsStr == null || skillsStr.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(skillsStr.split(",")));
    }

    /**
     * Unlocks a skill persistently for a player.
     */
    public void unlockSkill(Player player, String skillKey) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        List<String> skills = getUnlockedSkills(player);
        if (!skills.contains(skillKey)) {
            skills.add(skillKey);
            String skillsStr = String.join(",", skills);
            data.set(skillsKey, PersistentDataType.STRING, skillsStr);
        }
    }

    /**
     * Checks if a player has a skill unlocked persistently.
     */
    public boolean hasSkill(Player player, String skillKey) {
        return getUnlockedSkills(player).contains(skillKey.toLowerCase());
    }

    /**
     * Counts how many unlocked skills of a specific specialization/subtype the player has.
     */
    public int getSubtypeCount(Player player, String subtype) {
        List<String> unlocked = getUnlockedSkills(player);
        int count = 0;
        for (String skill : unlocked) {
            if (getSkillSubtype(skill).equalsIgnoreCase(subtype)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the specialization/subtype name of a given skill key.
     */
    public String getSkillSubtype(String skillKey) {
        switch (skillKey.toLowerCase()) {
            // Explorer
            case "dash":
            case "hydration":
            case "step_assist":
            case "grapple":
            case "safe_fall":
            case "water_breathing":
            case "recall":
                return "Aventureiro";
            case "jump_boost":
            case "thermal_resistance":
            case "sonar":
            case "dim_shift":
            case "wind_burst":
                return "Combatente";

            // Miner
            case "diet":
            case "stone_smash":
            case "torch_light":
            case "ore_sonar":
            case "double_ore":
            case "sight":
                return "Escavador";
            case "haste":
            case "ore_repair":
            case "molten_touch":
            case "transmutation":
            case "gravity_shield":
            case "core_overdrive":
                return "Alquimista";

            // Builder
            case "feast":
            case "woodcutter":
            case "canopy_step":
            case "fertilize":
            case "flora_shield":
            case "lumberjack":
                return "Silvicultor";
            case "silk_touch":
            case "scaffold":
            case "architect_focus":
            case "unbreakable_block":
            case "grace":
            case "gravity_defiance":
                return "Arquiteto";

            default:
                return "";
        }
    }
}
