package com.project.rpgplugin.command;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.mob.MobSpawnService;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.util.CombatTracker;
import com.project.rpgplugin.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RogueCommand implements CommandExecutor {

    private static final long TP_COOLDOWN_MS = 180_000;

    private final RPGPlugin plugin;
    private final RunManager runManager;
    private final MobSpawnService mobSpawnService;
    private final Map<UUID, Long> lastTpTime = new ConcurrentHashMap<>();

    public RogueCommand(RPGPlugin plugin, RunManager runManager, MobSpawnService mobSpawnService) {
        this.plugin = plugin;
        this.runManager = runManager;
        this.mobSpawnService = mobSpawnService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Apenas jogadores podem usar /rogue.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "tp" -> handleTeleport(player, args);
            case "boss" -> handleBossSpawn(player, args);
            default -> sendHelp(player);
        }

        return true;
    }

    private void handleTeleport(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<red>Uso: /rogue tp <jogador>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Jogador nao encontrado ou offline."));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(Text.mm("<red>Voce nao pode teleportar para si mesmo."));
            return;
        }

        if (CombatTracker.isInCombat(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Voce esta em combate! Aguarde 10s sem tomar dano."));
            return;
        }

        boolean fullHealth = player.getHealth() >= player.getMaxHealth();

        if (!fullHealth) {
            Long last = lastTpTime.get(player.getUniqueId());
            long remaining = last != null ? (TP_COOLDOWN_MS - (System.currentTimeMillis() - last)) : 0;
            if (remaining > 0) {
                player.sendMessage(Text.mm("<red>Teleporte em recarga. Aguarde " + (remaining / 1000) + "s ou recupere a vida total."));
                return;
            }
        }

        player.teleportAsync(target.getLocation());
        lastTpTime.put(player.getUniqueId(), System.currentTimeMillis());

        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.sendMessage(Text.mm("<green>Teleportado para <white>" + target.getName() + "</white>!"));
        target.sendMessage(Text.mm("<gray><white>" + player.getName() + "</white> se teleportou ate voce.</gray>"));
    }

    private void handleBossSpawn(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<red>Uso: /rogue boss spawn <frostmaw|tyrant>"));
            return;
        }

        if (!args[1].equalsIgnoreCase("spawn")) {
            player.sendMessage(Text.mm("<red>Uso: /rogue boss spawn <frostmaw|tyrant>"));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(Text.mm("<red>Uso: /rogue boss spawn <frostmaw|tyrant>"));
            return;
        }

        String bossId = args[2].toLowerCase();
        if (!bossId.equals("frostmaw") && !bossId.equals("magma_tyrant")) {
            player.sendMessage(Text.mm("<red>Boss desconhecido. Opcoes: frostmaw, magma_tyrant"));
            return;
        }

        String bossName = bossId.equals("frostmaw") ? "Frostmaw" : "Tirano Magmatico";
        Bukkit.broadcast(Text.mm(
            "<gold><bold>⚠ " + bossName + " sera invocado por " + player.getName() + " em 5 segundos!</bold></gold>"
        ));
        player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.0f, 0.5f);

        player.getScheduler().runDelayed(plugin, st -> {
            if (!player.isOnline()) return;
            mobSpawnService.spawnBoss(bossId, player.getLocation().add(5, 0, 5));
            Bukkit.broadcast(Text.mm(
                "<red><bold>" + bossName + " surgiu!</bold></red>"
            ));
        }, null, 100L);
    }

    private void sendHelp(Player player) {
        player.sendMessage(Text.mm("<gold>=== RogueLata Comandos ==="));
        player.sendMessage(Text.mm("<yellow>/rogue tp <jogador></yellow> <gray>- Teleporta ate um amigo (vida cheia = sem recarga)</gray>"));
        player.sendMessage(Text.mm("<yellow>/rogue boss spawn <frostmaw|tyrant></yellow> <gray>- Invoca um boss com 5s de delay</gray>"));
        player.sendMessage(Text.mm("<yellow>/run</yellow> <gray>- Mostra informacoes da sua run</gray>"));
        player.sendMessage(Text.mm("<yellow>/skills</yellow> <gray>- Abre o menu de habilidades</gray>"));
    }
}
