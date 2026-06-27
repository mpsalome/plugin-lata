package com.project.rpgplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;

public class ClassListeners implements Listener {

    private final RPGPlugin plugin;
    private final PlayerManager playerManager;

    // Cooldown Maps (UUID -> Timestamp in milliseconds)
    private final Map<UUID, Long> explorerDashCooldown = new HashMap<>();
    private final Map<UUID, Long> explorerWaterBreathingCooldown = new HashMap<>();
    private final Map<UUID, Long> explorerRecallCooldown = new HashMap<>();
    private final Map<UUID, Long> minerHasteCooldown = new HashMap<>();
    private final Map<UUID, Long> builderGraceCooldown = new HashMap<>();

    private final String guiTitle = "§8Select Your RPG Class";

    public ClassListeners(RPGPlugin plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    /**
     * Opens the visual selection GUI (Virtual Chest - 9 Slots)
     */
    public void openSelectionGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, Component.text(guiTitle));

        // Slot 2: Explorer Class Item
        ItemStack explorerItem = new ItemStack(Material.COMPASS);
        ItemMeta explorerMeta = explorerItem.getItemMeta();
        explorerMeta.displayName(Component.text("Explorer Class").color(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        explorerMeta.lore(Arrays.asList(
                Component.text("§7Specialty: Mobility and Survival"),
                Component.text("§a- Reduces fall damage significantly"),
                Component.text("§a- Use Flowers to dash, gaining speed & invisibility"),
                Component.text("§a- Water bottles provide nourishment"),
                Component.text("§a- Auto-apply Jump Boost at XP Level 10"),
                Component.text("§a- Consume Lapis Lazuli for Water Breathing III"),
                Component.text("§a- Right-click Dragon's Breath to recall to spawn")
        ));
        explorerItem.setItemMeta(explorerMeta);
        gui.setItem(2, explorerItem);

        // Slot 4: Miner Class Item
        ItemStack minerItem = new ItemStack(Material.GOLDEN_PICKAXE);
        ItemMeta minerMeta = minerItem.getItemMeta();
        minerMeta.displayName(Component.text("Miner Class").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        minerMeta.lore(Arrays.asList(
                Component.text("§7Specialty: Deep Mining and Excavation"),
                Component.text("§a- Eat Coal/Charcoal directly for hunger"),
                Component.text("§a- Permanent Night Vision at high levels"),
                Component.text("§a- Consume gold ingot with pickaxe for Haste")
        ));
        minerItem.setItemMeta(minerMeta);
        gui.setItem(4, minerItem);

        // Slot 6: Builder Class Item
        ItemStack builderItem = new ItemStack(Material.BRICK);
        ItemMeta builderMeta = builderItem.getItemMeta();
        builderMeta.displayName(Component.text("Builder Class").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        builderMeta.lore(Arrays.asList(
                Component.text("§7Specialty: Architecture and Forestry"),
                Component.text("§a- Eat Leaves directly to recover energy"),
                Component.text("§a- Break blocks with empty hands simulating Silk Touch"),
                Component.text("§a- Right-click Feathers for Jump Boost and Slow Falling")
        ));
        builderItem.setItemMeta(builderMeta);
        gui.setItem(6, builderItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text(guiTitle))) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        String selectedClass = null;
        if (slot == 2) selectedClass = "EXPLORER";
        else if (slot == 4) selectedClass = "MINER";
        else if (slot == 6) selectedClass = "BUILDER";

        if (selectedClass != null) {
            playerManager.setPlayerClass(player, selectedClass);
            player.closeInventory();
            
            // Send Action Bar Message using modern Paper Component API
            player.sendActionBar(Component.text("Selected " + selectedClass + " class!")
                    .color(NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true));
            player.sendMessage(Component.text("[RPGPlugin] You are now a(n) " + selectedClass + "!")
                    .color(NamedTextColor.YELLOW));
        }
    }

    /* =========================================================================
       EXPLORER MECHANICS
       ========================================================================= */

    /**
     * Fall Reduction Mechanic: Explorer reduces damage by configured ratio.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (event.getCause() == DamageCause.FALL && playerManager.hasClass(player, "EXPLORER")) {
            double reduction = plugin.getConfig().getDouble("classes.explorer.fall_reduction", 0.5);
            event.setDamage(event.getDamage() * reduction);
        }
    }

    /**
     * Explorer Special Interactions (Flower Dash & Water Bottle Nourishment)
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // --- EXPLORER: Flower Dash ---
        if (playerManager.hasClass(player, "EXPLORER") && Tag.FLOWERS.isTagged(item.getType())) {
            event.setCancelled(true); // Prevent placing flower
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            long cooldownSec = plugin.getConfig().getLong("classes.explorer.dash.cooldown", 30);
            long cooldownMs = cooldownSec * 1000;

            if (explorerDashCooldown.containsKey(uuid)) {
                long lastUsed = explorerDashCooldown.get(uuid);
                if (now - lastUsed < cooldownMs) {
                    long remainingSec = (cooldownMs - (now - lastUsed)) / 1000;
                    player.sendActionBar(Component.text("Flower Dash is on cooldown! " + remainingSec + "s remaining.")
                            .color(NamedTextColor.RED));
                    return;
                }
            }

            // Consume 1 flower safely
            item.subtract(1);

            // Apply special effects
            int durationSec = plugin.getConfig().getInt("classes.explorer.dash.duration", 10);
            int speedAmp = plugin.getConfig().getInt("classes.explorer.dash.speed", 1);

            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationSec * 20, speedAmp));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationSec * 20, 0));

            explorerDashCooldown.put(uuid, now);
            player.sendActionBar(Component.text("Flower Dash Activated!").color(NamedTextColor.AQUA));
            return;
        }

        // --- EXPLORER: Lapis Water Breathing ---
        if (playerManager.hasClass(player, "EXPLORER") && item.getType() == Material.LAPIS_LAZULI) {
            event.setCancelled(true);
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            long cooldownSec = plugin.getConfig().getLong("classes.explorer.water_breathing.cooldown", 10);
            long cooldownMs = cooldownSec * 1000;

            if (explorerWaterBreathingCooldown.containsKey(uuid)) {
                long lastUsed = explorerWaterBreathingCooldown.get(uuid);
                if (now - lastUsed < cooldownMs) {
                    long remainingSec = (cooldownMs - (now - lastUsed)) / 1000;
                    player.sendActionBar(Component.text("Lapis Water Breathing is on cooldown! " + remainingSec + "s remaining.")
                            .color(NamedTextColor.RED));
                    return;
                }
            }

            // Consume 1 Lapis Lazuli safely
            item.subtract(1);

            int durationSec = plugin.getConfig().getInt("classes.explorer.water_breathing.duration", 15);
            player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, durationSec * 20, 2)); // Water Breathing III (amplifier 2)

            explorerWaterBreathingCooldown.put(uuid, now);
            player.sendActionBar(Component.text("Water Breathing III Activated!").color(NamedTextColor.BLUE));
            return;
        }

        // --- EXPLORER: Dragon's Breath Recall ---
        if (playerManager.hasClass(player, "EXPLORER") && item.getType() == Material.DRAGON_BREATH) {
            event.setCancelled(true);
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            long cooldownSec = plugin.getConfig().getLong("classes.explorer.recall.cooldown", 60);
            long cooldownMs = cooldownSec * 1000;

            if (explorerRecallCooldown.containsKey(uuid)) {
                long lastUsed = explorerRecallCooldown.get(uuid);
                if (now - lastUsed < cooldownMs) {
                    long remainingSec = (cooldownMs - (now - lastUsed)) / 1000;
                    player.sendActionBar(Component.text("Recall is on cooldown! " + remainingSec + "s remaining.")
                            .color(NamedTextColor.RED));
                    return;
                }
            }

            // Consume 1 Dragon's Breath safely
            item.subtract(1);

            // Teleport to spawn point
            player.teleport(player.getWorld().getSpawnLocation());

            explorerRecallCooldown.put(uuid, now);
            player.sendActionBar(Component.text("Teleported back to Spawn!").color(NamedTextColor.LIGHT_PURPLE));
            player.sendMessage(Component.text("[RPGPlugin] Recalled back to world spawn point using Dragon's Breath!").color(NamedTextColor.LIGHT_PURPLE));
            return;
        }

        // --- MINER: Coal Diet & Haste Ingot ---
        if (playerManager.hasClass(player, "MINER")) {
            if (item.getType() == Material.COAL || item.getType() == Material.CHARCOAL) {
                event.setCancelled(true);
                int hunger = plugin.getConfig().getInt("classes.miner.diet.hunger", 4);
                double saturation = plugin.getConfig().getDouble("classes.miner.diet.saturation", 2.0);

                if (player.getFoodLevel() < 20) {
                    item.subtract(1);
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + hunger));
                    player.setSaturation((float) Math.min(20, player.getSaturation() + saturation));
                    player.sendActionBar(Component.text("Consumed raw carbon! Food restored.").color(NamedTextColor.GOLD));
                }
                return;
            }

            if (item.getType() == Material.GOLD_INGOT) {
                // Check if holding pickaxe in the main hand
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (isPickaxe(mainHand.getType())) {
                    event.setCancelled(true);
                    UUID uuid = player.getUniqueId();
                    long now = System.currentTimeMillis();
                    long cooldownSec = plugin.getConfig().getLong("classes.miner.haste.cooldown", 20);
                    long cooldownMs = cooldownSec * 1000;

                    if (minerHasteCooldown.containsKey(uuid)) {
                        long lastUsed = minerHasteCooldown.get(uuid);
                        if (now - lastUsed < cooldownMs) {
                            long remainingSec = (cooldownMs - (now - lastUsed)) / 1000;
                            player.sendActionBar(Component.text("Gold Rush Haste is on cooldown! " + remainingSec + "s remaining.")
                                    .color(NamedTextColor.RED));
                            return;
                        }
                    }

                    // Consume ingot safely
                    item.subtract(1);

                    int durationSec = plugin.getConfig().getInt("classes.miner.haste.duration", 15);
                    int hasteAmp = plugin.getConfig().getInt("classes.miner.haste.amp", 1);

                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, durationSec * 20, hasteAmp));
                    minerHasteCooldown.put(uuid, now);
                    player.sendActionBar(Component.text("Gold Rush Haste Activated!").color(NamedTextColor.GOLD));
                }
                return;
            }
        }

        // --- BUILDER: Leaves diet & Grace Feather ---
        if (playerManager.hasClass(player, "BUILDER")) {
            if (Tag.LEAVES.isTagged(item.getType())) {
                event.setCancelled(true); // Prevent leaf placing
                int hunger = plugin.getConfig().getInt("classes.builder.feast.hunger", 2);
                double saturation = plugin.getConfig().getDouble("classes.builder.feast.saturation", 0.8);

                if (player.getFoodLevel() < 20) {
                    item.subtract(1);
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + hunger));
                    player.setSaturation((float) Math.min(20, player.getSaturation() + saturation));
                    player.sendActionBar(Component.text("Feasted on foliage!").color(NamedTextColor.GREEN));
                }
                return;
            }

            if (item.getType() == Material.FEATHER) {
                int reqLvl = plugin.getConfig().getInt("classes.builder.grace_lvl", 20);
                if (player.getLevel() < reqLvl) {
                    player.sendActionBar(Component.text("Requires XP Level " + reqLvl + " for Feather Grace!").color(NamedTextColor.RED));
                    return;
                }

                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                long cooldownSec = plugin.getConfig().getLong("classes.builder.grace.cooldown", 45);
                long cooldownMs = cooldownSec * 1000;

                if (builderGraceCooldown.containsKey(uuid)) {
                    long lastUsed = builderGraceCooldown.get(uuid);
                    if (now - lastUsed < cooldownMs) {
                        long remainingSec = (cooldownMs - (now - lastUsed)) / 1000;
                        player.sendActionBar(Component.text("Feather Grace is on cooldown! " + remainingSec + "s remaining.")
                                .color(NamedTextColor.RED));
                        return;
                    }
                }

                // Consume feather safely
                item.subtract(1);

                int durationSec = plugin.getConfig().getInt("classes.builder.grace.duration", 10);
                int jumpAmp = plugin.getConfig().getInt("classes.builder.grace.jump", 2);

                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, durationSec * 20, jumpAmp));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, durationSec * 20, 0));

                builderGraceCooldown.put(uuid, now);
                player.sendActionBar(Component.text("Feather Grace Activated!").color(NamedTextColor.GREEN));
            }
        }
    }

    /**
     * Hydration: Water bottle grants extra hunger/saturation to Explorers.
     */
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (playerManager.hasClass(player, "EXPLORER") && item.getType() == Material.POTION) {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof PotionMeta) {
                PotionMeta potionMeta = (PotionMeta) meta;
                if (potionMeta.getBasePotionType() == PotionType.WATER) {
                    int hunger = plugin.getConfig().getInt("classes.explorer.hydration.hunger", 1);
                    double saturation = plugin.getConfig().getDouble("classes.explorer.hydration.saturation", 0.5);

                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + hunger));
                    player.setSaturation((float) Math.min(20, player.getSaturation() + saturation));
                    player.sendMessage(Component.text("Hydrated! You feel energized.").color(NamedTextColor.AQUA));
                }
            }
        }
    }

    /* =========================================================================
       MINER SIGHT MECHANIC
       ========================================================================= */

    /**
     * Night vision & Jump Boost task: applies permanent effects to eligible players on move.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Miner Night Vision
        if (playerManager.hasClass(player, "MINER")) {
            int reqLvl = plugin.getConfig().getInt("classes.miner.sight_lvl", 30);
            if (player.getLevel() >= reqLvl) {
                // Apply night vision for 15s to keep it ongoing and ambient
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, true, false, false));
            }
        }
        
        // Explorer Jump Boost Lvl 10+
        if (playerManager.hasClass(player, "EXPLORER")) {
            int reqLvl = plugin.getConfig().getInt("classes.explorer.jump_boost_lvl", 10);
            if (player.getLevel() >= reqLvl) {
                // Apply jump boost II (amplifier 1) for 15s to keep it ongoing
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 300, 1, true, false, false));
            }
        }
    }

    /* =========================================================================
       BUILDER SILK TOUCH MECHANIC
       ========================================================================= */

    /**
     * Silk Touch breaking with empty hand for Builders.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!playerManager.hasClass(player, "BUILDER")) return;

        // Must break with empty hand
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() != Material.AIR) return;

        Block block = event.getBlock();
        
        // Simulating Netherite Pickaxe with Silk Touch I to extract full block drops
        ItemStack silkTool = new ItemStack(Material.NETHERITE_PICKAXE);
        silkTool.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);

        Collection<ItemStack> drops = block.getDrops(silkTool, player);
        if (!drops.isEmpty()) {
            event.setDropItems(false); // Cancel standard item drops to prevent duplicate
            for (ItemStack drop : drops) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
            player.sendActionBar(Component.text("Empty hand Silk Touch block drop!").color(NamedTextColor.GREEN));
        }
    }

    private boolean isPickaxe(Material material) {
        String name = material.name();
        return name.endsWith("_PICKAXE");
    }
}
