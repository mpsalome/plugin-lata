package com.project.rpgplugin.ui;

import com.project.rpgplugin.core.PlayerState;
import com.project.rpgplugin.core.mana.ManaService;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.util.SchedulerUtil;
import com.project.rpgplugin.util.StringUtil;
import com.project.rpgplugin.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HudService extends BukkitRunnable {

    private final Plugin plugin;
    private final Map<Player, PlayerState> playerStates;
    private final ManaService manaService;
    private final RunManager runManager;
    private final Map<UUID, Map<String, Long>> cooldownDisplays = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> activeEffects = new ConcurrentHashMap<>();
    private final Map<UUID, BossBar> playerBossBars = new ConcurrentHashMap<>();
    private boolean bossBarEnabled = false;

    public HudService(Plugin plugin, Map<Player, PlayerState> playerStates) {
        this.plugin = plugin;
        this.playerStates = playerStates;
        this.manaService = null;
        this.runManager = null;
    }

    public HudService(JavaPlugin plugin, ManaService manaService, RunManager runManager) {
        this.plugin = plugin;
        this.manaService = manaService;
        this.runManager = runManager;
        this.playerStates = new ConcurrentHashMap<>();
        SchedulerUtil.runTimer(plugin, this, 20L, 4L);
    }

    public void registerCooldown(Player player, String skillId, String displayName, long durationMillis) {
        if (durationMillis <= 0) return;
        long expiry = System.currentTimeMillis() + durationMillis;
        cooldownDisplays.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
            .put(displayName, expiry);
        startPlayer(player);
    }

    public void setActiveEffect(Player player, String displayName, long durationMillis) {
        long expiry = durationMillis <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + durationMillis;
        activeEffects.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
            .put(displayName, expiry);
        startPlayer(player);
        PlayerState state = playerStates.get(player);
        if (state != null) {
            state.addActiveEffect(displayName, expiry);
        }
    }

    public void removeActiveEffect(Player player, String displayName) {
        Map<String, Long> effects = activeEffects.get(player.getUniqueId());
        if (effects != null) {
            effects.remove(displayName);
        }
        PlayerState state = playerStates.get(player);
        if (state != null) {
            state.removeActiveEffect(displayName);
        }
    }

    public void setBossBarEnabled(boolean enabled) {
        this.bossBarEnabled = enabled;
    }

    public static void setItemCooldown(Player player, org.bukkit.Material material, int durationTicks) {
        player.setCooldown(material, durationTicks);
    }

    public static void setItemCooldownDelayed(Player player, org.bukkit.Material material, int durationTicks, JavaPlugin plugin) {
        player.getScheduler().runDelayed(plugin, st -> player.setCooldown(material, durationTicks), null, 1L);
    }

    public void startPlayer(Player player) {
        PlayerState state = playerStates.computeIfAbsent(player, k -> new PlayerState());
        if (bossBarEnabled) {
            updateSkillBar(player, state);
        }
    }

    public void startPlayer(Player player, PlayerState state) {
        playerStates.put(player, state);
        if (bossBarEnabled) {
            updateSkillBar(player, state);
        }
    }

    public void stopPlayer(Player player) {
        playerStates.remove(player);
        BossBar bar = playerBossBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
        cooldownDisplays.remove(player.getUniqueId());
        activeEffects.remove(player.getUniqueId());
    }

    public void startAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            startPlayer(player);
        }
    }

    public void stopAll() {
        cancel();
        List.copyOf(playerStates.keySet()).forEach(this::stopPlayer);
        playerStates.clear();
        cooldownDisplays.clear();
        activeEffects.clear();
        playerBossBars.values().forEach(BossBar::removeAll);
        playerBossBars.clear();
    }

    @Override
    public void run() {
        for (Map.Entry<Player, PlayerState> entry : playerStates.entrySet()) {
            Player player = entry.getKey();
            PlayerState state = entry.getValue();

            if (!player.isOnline()) {
                continue;
            }

            tickState(player, state);

            composeActionbar(player, state);

            if (bossBarEnabled) {
                updateSkillBar(player, state);
            }
        }
    }

    private void tickState(Player player, PlayerState state) {
        if (manaService != null && runManager != null) {
            state.setCurrentMana(manaService.getMana(player));
            state.setMaxMana(manaService.getMaxMana(player, runManager.getRun(player)));
        }

        long now = System.currentTimeMillis();
        Map<String, Long> playerCooldowns = cooldownDisplays.get(player.getUniqueId());
        if (playerCooldowns != null) {
            playerCooldowns.values().removeIf(v -> v <= now);
        }
        Map<String, Long> playerEffects = activeEffects.get(player.getUniqueId());
        if (playerEffects != null) {
            playerEffects.values().removeIf(v -> v <= now && v != Long.MAX_VALUE);
        }

        state.setSkillBarTitle(buildSkillBarTitle(player));
    }

    private String buildSkillBarTitle(Player player) {
        long now = System.currentTimeMillis();
        List<String> parts = new ArrayList<>();

        Map<String, Long> effects = activeEffects.get(player.getUniqueId());
        if (effects != null) {
            for (Map.Entry<String, Long> entry : effects.entrySet()) {
                if (entry.getValue() <= now) continue;
                parts.add(entry.getKey());
            }
        }

        Map<String, Long> cooldowns = cooldownDisplays.get(player.getUniqueId());
        if (cooldowns != null) {
            for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
                long remaining = entry.getValue() - now;
                if (remaining <= 0) continue;
                double secs = remaining / 1000.0;
                parts.add(entry.getKey() + ": " + String.format("%.1f", secs) + "s");
            }
        }

        if (parts.isEmpty()) {
            return "";
        }

        return String.join(" | ", parts);
    }

    private void composeActionbar(Player player, PlayerState state) {
        StringBuilder actionBarText = new StringBuilder();

        actionBarText.append("Mana ")
                     .append(StringUtil.formatDouble(state.getCurrentMana()))
                     .append("/")
                     .append(StringUtil.formatDouble(state.getMaxMana()));

        actionBarText.append(" | HP ")
                     .append(StringUtil.formatDouble(player.getHealth()))
                     .append("/")
                     .append(StringUtil.formatDouble(player.getMaxHealth()));

        if (!state.getActiveEffects().isEmpty()) {
            actionBarText.append(" | ");
            List<String> effectParts = new ArrayList<>();
            state.getActiveEffects().forEach((effect, duration) -> {
                if (duration > 0) {
                    effectParts.add(effect.getName() + " " + StringUtil.formatDuration(duration));
                }
            });
            if (!effectParts.isEmpty()) {
                actionBarText.append(String.join(" | ", effectParts));
            }
        }

        player.sendActionBar(Text.mm(actionBarText.toString()));
    }

    private void updateSkillBar(Player player, PlayerState state) {
        if (!bossBarEnabled) {
            BossBar bar = playerBossBars.remove(player.getUniqueId());
            if (bar != null) {
                bar.removeAll();
            }
            return;
        }

        String title = state.getSkillBarTitle();

        if (title == null || title.trim().isEmpty()) {
            BossBar bar = playerBossBars.remove(player.getUniqueId());
            if (bar != null) {
                bar.removeAll();
            }
            return;
        }

        BossBar bar = playerBossBars.get(player.getUniqueId());
        if (bar == null) {
            bar = Bukkit.createBossBar(title, BarColor.WHITE, BarStyle.SOLID);
            bar.addPlayer(player);
            playerBossBars.put(player.getUniqueId(), bar);
        } else {
            bar.setTitle(title);
        }

        bar.setVisible(true);
    }
}
