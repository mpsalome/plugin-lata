package com.project.rpgplugin.ui;

import com.project.rpgplugin.core.mana.ManaService;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.util.Text;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
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

    public HudService(JavaPlugin plugin, ManaService manaService, RunManager runManager) {
        this.plugin = plugin;
        this.manaService = manaService;
        this.runManager = runManager;
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
    }

    public void addTemporaryStatus(Player player, String rawText, int durationTicks) {
        String converted = rawText.contains("\u00a7") ? Text.legacyToMiniMessage(rawText) : rawText;
        PlayerHud hud = sessions.get(player.getUniqueId());
        if (hud == null) {
            startPlayer(player);
            hud = sessions.get(player.getUniqueId());
            if (hud == null) return;
        }
        hud.statuses.add(new TempStatus(converted, System.currentTimeMillis() + durationTicks * 50L));
    }

    public void addFeedback(Player player, String rawText) {
        addTemporaryStatus(player, rawText, 35);
    }

    private void tick(Player player) {
        if (!player.isOnline()) {
            stopPlayer(player);
            return;
        }
        PlayerHud hud = sessions.get(player.getUniqueId());
        if (hud == null) return;

        long now = System.currentTimeMillis();
        hud.statuses.removeIf(s -> s.expiryMillis <= now);

        if (runManager.getRun(player) == null && hud.statuses.isEmpty()) {
            if (!hud.lastSent.isEmpty()) {
                player.sendActionBar(net.kyori.adventure.text.Component.empty());
                hud.lastSent = "";
            }
            stopPlayer(player);
            return;
        }

        String composed = compose(player, hud);
        if (!composed.equals(hud.lastSent)) {
            player.sendActionBar(Text.mm(composed));
            hud.lastSent = composed;
        }
    }

    private String compose(Player player, PlayerHud hud) {
        StringBuilder sb = new StringBuilder();

        boolean showMana = manaService != null && manaService.isEnabled()
            && runManager.getRun(player) != null;

        if (showMana) {
            double mana = manaService.getMana(player);
            double maxMana = manaService.getMaxMana(player, runManager.getRun(player));
            sb.append("\u26a1 <aqua>").append((int) mana).append("/").append((int) maxMana);
            if (hud.statuses.isEmpty()) {
                sb.append(" Mana");
            }
            sb.append("</aqua> <dark_gray>|</dark_gray> ");
        }

        sb.append("<green>\u2764 ").append((int) player.getHealth())
            .append("/").append((int) player.getMaxHealth()).append("</green>");

        if (!hud.statuses.isEmpty()) {
            sb.append(" <dark_gray>|</dark_gray> ");
            List<String> texts = hud.statuses.stream()
                .map(s -> "<white>" + s.text + "</white>")
                .toList();
            sb.append(String.join(" <gray>\u00b7</gray> ", texts));
        }

        return sb.toString();
    }

    private static class PlayerHud {
        ScheduledTask task;
        String lastSent = "";
        List<TempStatus> statuses = new ArrayList<>();
    }

    private record TempStatus(String text, long expiryMillis) {}
}
