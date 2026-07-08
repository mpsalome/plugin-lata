package com.project.rpgplugin.ui;

import com.project.rpgplugin.core.mana.ManaService;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.util.Text;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HudService {

    private static final long TICK_INTERVAL = 4L;

    private final JavaPlugin plugin;
    private final ManaService manaService;
    private final RunManager runManager;
    private final Map<UUID, PlayerHud> sessions = new ConcurrentHashMap<>();

    // Visual cooldown display registry: player UUID -> skill display name -> expiry timestamp
    private final Map<UUID, Map<String, Long>> cooldownDisplays = new ConcurrentHashMap<>();

    public HudService(JavaPlugin plugin, ManaService manaService, RunManager runManager) {
        this.plugin = plugin;
        this.manaService = manaService;
        this.runManager = runManager;
    }

    /**
     * Registers a visual cooldown for display on the HUD actionbar.
     * The actionbar will show "⏳ SkillName: X.Xs" until the cooldown expires.
     * <p>
     * For item-based skills (e.g. pickaxe, fishing rod), prefer using
     * {@link Player#setCooldown(Material, int)} for the native hotbar
     * cooldown overlay instead. See {@link #setItemCooldown(Player, Material, int)}.
     *
     * @param player          the player
     * @param displayName     human-readable skill name (e.g. "Dash", "Sonar")
     * @param durationSeconds cooldown duration in seconds
     */
    public void setCooldown(Player player, String displayName, int durationSeconds) {
        if (durationSeconds <= 0) return;
        long expiry = System.currentTimeMillis() + durationSeconds * 1000L;
        cooldownDisplays.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
            .put(displayName, expiry);
        startPlayer(player);
    }

    /**
     * Registers a visual cooldown from the backend skill system.
     * Called automatically by {@link com.project.rpgplugin.core.skill.AbstractSkill#startCooldown}.
     * Uses millisecond precision for the duration.
     */
    public void registerCooldown(Player player, String skillId, String displayName, long durationMillis) {
        if (durationMillis <= 0) return;
        long expiry = System.currentTimeMillis() + durationMillis;
        cooldownDisplays.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
            .put(displayName, expiry);
        startPlayer(player);
    }

    /**
     * Convenience helper for item-based skills.
     * Uses the native Minecraft hotbar cooldown overlay instead of actionbar text.
     * Call this in the skill's activate() method when the skill is triggered by a
     * physical item (e.g. pickaxe for SeismicSlam, fishing rod for HarpoonPull).
     *
     * @param player        the player
     * @param material      the item material to put on cooldown
     * @param durationTicks cooldown duration in ticks (20 ticks = 1 second)
     */
    public static void setItemCooldown(Player player, Material material, int durationTicks) {
        player.setCooldown(material, durationTicks);
    }

    /**
     * Folia-aware 1-tick delayed item cooldown.
     * When a skill cancels a PlayerInteractEvent/BlockPlaceEvent the client inventory
     * sync can wipe the hotbar cooldown visual if setCooldown is called in the same tick.
     * Scheduling it 1 tick later avoids this desync.
     */
    public static void setItemCooldownDelayed(Player player, Material material, int durationTicks, JavaPlugin plugin) {
        player.getScheduler().runDelayed(plugin, st -> player.setCooldown(material, durationTicks), null, 1L);
    }

    public void startPlayer(Player player) {
        sessions.computeIfAbsent(player.getUniqueId(), k -> {
            var hud = new PlayerHud();
            hud.task = player.getScheduler().runAtFixedRate(plugin, st -> tick(player), () -> {}, 1L, TICK_INTERVAL);
            return hud;
        });
    }

    public void stopPlayer(Player player) {
        PlayerHud hud = sessions.remove(player.getUniqueId());
        if (hud != null && hud.task != null) {
            hud.task.cancel();
        }
        cooldownDisplays.remove(player.getUniqueId());
    }

    public void startAll() {
        plugin.getServer().getOnlinePlayers().forEach(this::startPlayer);
    }

    public void stopAll() {
        sessions.keySet().forEach(uuid -> {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) stopPlayer(player);
        });
        sessions.clear();
        cooldownDisplays.clear();
    }

    private void tick(Player player) {
        if (!player.isOnline()) {
            stopPlayer(player);
            return;
        }
        PlayerHud hud = sessions.get(player.getUniqueId());
        if (hud == null) return;

        boolean hasRun = runManager.getRun(player) != null;
        Map<String, Long> playerCooldowns = cooldownDisplays.get(player.getUniqueId());

        cleanup(playerCooldowns);

        boolean hasCooldowns = playerCooldowns != null && !playerCooldowns.isEmpty();
        if (!hasRun && !hasCooldowns) {
            if (!hud.lastSent.isEmpty()) {
                player.sendActionBar(Component.empty());
                hud.lastSent = "";
            }
            stopPlayer(player);
            return;
        }

        String composed = compose(player, hasRun, playerCooldowns);
        if (!composed.equals(hud.lastSent)) {
            player.sendActionBar(Text.mm(composed));
            hud.lastSent = composed;
        }
    }

    private void cleanup(Map<String, Long> cooldowns) {
        if (cooldowns == null || cooldowns.isEmpty()) return;
        long now = System.currentTimeMillis();
        cooldowns.values().removeIf(v -> v <= now);
    }

    private String compose(Player player, boolean hasRun, Map<String, Long> playerCooldowns) {
        StringBuilder sb = new StringBuilder();

        boolean showMana = hasRun && manaService != null && manaService.isEnabled();
        if (showMana) {
            double mana = manaService.getMana(player);
            double maxMana = manaService.getMaxMana(player, runManager.getRun(player));
            sb.append("\u26a1 <aqua>").append((int) mana).append("/").append((int) maxMana).append("</aqua>");
            sb.append(" <dark_gray>|</dark_gray> ");
        }

        sb.append("<green>\u2764 ").append((int) player.getHealth())
            .append("/").append((int) player.getMaxHealth()).append("</green>");

        if (playerCooldowns != null && !playerCooldowns.isEmpty()) {
            long now = System.currentTimeMillis();
            List<String> parts = new ArrayList<>();
            int idx = 0;

            for (Map.Entry<String, Long> entry : playerCooldowns.entrySet()) {
                long remaining = entry.getValue() - now;
                if (remaining <= 0) continue;
                double secs = remaining / 1000.0;
                String color = (idx++ % 2 == 0) ? "yellow" : "gold";
                parts.add("<" + color + ">\u231b " + entry.getKey() + ": "
                    + String.format("%.1f", secs) + "s</" + color + ">");
            }

            if (!parts.isEmpty()) {
                sb.append(" <dark_gray>|</dark_gray> ");
                sb.append(String.join(" <dark_gray>|</dark_gray> ", parts));
            }
        }

        return sb.toString();
    }

    private static class PlayerHud {
        ScheduledTask task;
        String lastSent = "";
    }
}
