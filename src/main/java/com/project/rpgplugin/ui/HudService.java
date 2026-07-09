package com.project.rpgplugin.ui;

import com.project.rpgplugin.core.mana.ManaService;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.util.Text;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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
    private boolean bossBarEnabled = true;

    // Cooldown display: player UUID -> skill display name -> expiry timestamp
    private final Map<UUID, Map<String, Long>> cooldownDisplays = new ConcurrentHashMap<>();

    // Active effects: player UUID -> effect display name (e.g. "Sonar")
    private final Map<UUID, Map<String, Long>> activeEffects = new ConcurrentHashMap<>();

    public HudService(JavaPlugin plugin, ManaService manaService, RunManager runManager) {
        this.plugin = plugin;
        this.manaService = manaService;
        this.runManager = runManager;
    }

    public void setCooldown(Player player, String displayName, int durationSeconds) {
        if (durationSeconds <= 0) return;
        long expiry = System.currentTimeMillis() + durationSeconds * 1000L;
        cooldownDisplays.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
            .put(displayName, expiry);
        startPlayer(player);
    }

    public void registerCooldown(Player player, String skillId, String displayName, long durationMillis) {
        if (durationMillis <= 0) return;
        long expiry = System.currentTimeMillis() + durationMillis;
        cooldownDisplays.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
            .put(displayName, expiry);
        startPlayer(player);
    }

    public static void setItemCooldown(Player player, org.bukkit.Material material, int durationTicks) {
        player.setCooldown(material, durationTicks);
    }

    public static void setItemCooldownDelayed(Player player, org.bukkit.Material material, int durationTicks, JavaPlugin plugin) {
        player.getScheduler().runDelayed(plugin, st -> player.setCooldown(material, durationTicks), null, 1L);
    }

    /**
     * Registra um efeito ativo (como Sonar). O efeito aparece na BossBar
     * de status ate ser removido via removeActiveEffect() ou expire.
     */
    public void setActiveEffect(Player player, String displayName, long durationMillis) {
        long expiry = durationMillis <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + durationMillis;
        activeEffects.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
            .put(displayName, expiry);
        startPlayer(player);
    }

    public void removeActiveEffect(Player player, String displayName) {
        Map<String, Long> effects = activeEffects.get(player.getUniqueId());
        if (effects != null) {
            effects.remove(displayName);
        }
    }

    public void setBossBarEnabled(boolean enabled) {
        this.bossBarEnabled = enabled;
        if (!enabled) {
            sessions.values().forEach(hud -> {
                if (hud.skillBar != null) {
                    hud.skillBar.removeAll();
                    hud.skillBar.setVisible(false);
                }
            });
        }
    }

    public void startPlayer(Player player) {
        sessions.computeIfAbsent(player.getUniqueId(), k -> {
            var hud = new PlayerHud();
            if (bossBarEnabled) {
                hud.skillBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);
                hud.skillBar.setVisible(false);
            }
            hud.task = player.getScheduler().runAtFixedRate(plugin, st -> tick(player), () -> {}, 1L, TICK_INTERVAL);
            return hud;
        });
    }

    public void stopPlayer(Player player) {
        PlayerHud hud = sessions.remove(player.getUniqueId());
        if (hud != null) {
            if (hud.task != null) hud.task.cancel();
            if (hud.skillBar != null) {
                hud.skillBar.removeAll();
                hud.skillBar.setVisible(false);
            }
        }
        cooldownDisplays.remove(player.getUniqueId());
        activeEffects.remove(player.getUniqueId());
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
        activeEffects.clear();
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
        Map<String, Long> playerEffects = activeEffects.get(player.getUniqueId());

        cleanup(playerCooldowns);
        cleanup(playerEffects);

        // --- Actionbar: mana + health only ---
        String actionbar = composeActionbar(player, hasRun);
        if (!actionbar.equals(hud.lastActionbar)) {
            if (actionbar.isEmpty()) {
                player.sendActionBar(Component.empty());
            } else {
                player.sendActionBar(Text.mm(actionbar));
            }
            hud.lastActionbar = actionbar;
        }

        // --- BossBar: cooldowns + active effects ---
        if (bossBarEnabled) {
            updateSkillBar(player, hud, playerCooldowns, playerEffects);
        }
    }

    private void cleanup(Map<String, Long> map) {
        if (map == null || map.isEmpty()) return;
        long now = System.currentTimeMillis();
        map.values().removeIf(v -> v <= now);
    }

    private String composeActionbar(Player player, boolean hasRun) {
        if (!hasRun) return "";

        StringBuilder sb = new StringBuilder();

        boolean showMana = manaService != null && manaService.isEnabled();
        if (showMana) {
            double mana = manaService.getMana(player);
            double maxMana = manaService.getMaxMana(player, runManager.getRun(player));
            sb.append("\u26a1 <aqua>").append((int) mana).append("/").append((int) maxMana).append("</aqua>");
            sb.append(" <dark_gray>|</dark_gray> ");
        }

        sb.append("<green>\u2764 ").append((int) player.getHealth())
            .append("/").append((int) player.getMaxHealth()).append("</green>");

        return sb.toString();
    }

    private void updateSkillBar(Player player, PlayerHud hud,
                                Map<String, Long> cooldowns, Map<String, Long> effects) {
        long now = System.currentTimeMillis();
        List<String> parts = new ArrayList<>();

        if (effects != null) {
            for (Map.Entry<String, Long> entry : effects.entrySet()) {
                if (entry.getValue() <= now) continue;
                parts.add("<green>" + entry.getKey() + "</green>");
            }
        }

        if (cooldowns != null) {
            int idx = 0;
            for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
                long remaining = entry.getValue() - now;
                if (remaining <= 0) continue;
                double secs = remaining / 1000.0;
                String color = (idx++ % 2 == 0) ? "yellow" : "gold";
                parts.add("<" + color + ">" + entry.getKey() + ": "
                    + String.format("%.1f", secs) + "s</" + color + ">");
            }
        }

        if (parts.isEmpty()) {
            if (hud.skillBar.isVisible()) {
                hud.skillBar.removeAll();
                hud.skillBar.setVisible(false);
            }
            return;
        }

        String title = String.join(" <dark_gray>|</dark_gray> ", parts);
        hud.skillBar.setTitle(title.replaceAll("<[^>]+>", ""));
        hud.skillBar.setProgress(1.0);

        if (!hud.skillBar.isVisible()) {
            hud.skillBar.addPlayer(player);
            hud.skillBar.setVisible(true);
        }
    }

    private static class PlayerHud {
        ScheduledTask task;
        BossBar skillBar;
        String lastActionbar = "";
    }
}
