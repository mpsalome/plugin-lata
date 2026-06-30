package com.project.rpgplugin.command;

import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunOutcome;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class RunCommand implements CommandExecutor {

    private final RunManager runManager;

    public RunCommand(RunManager runManager) {
        this.runManager = runManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Component.text("Apenas jogadores podem usar /run."));
            return true;
        }

        if (!runManager.hasActiveRun(p)) {
            p.sendMessage(Component.text("§cVocê não tem uma run ativa."));
            return true;
        }

        RunState run = runManager.getRun(p);
        if (run == null) return true;

        p.sendMessage(Component.text("§6§l=== Run Info ==="));
        p.sendMessage(Component.text("§7Nível: §f" + run.level()));
        p.sendMessage(Component.text("§7Status: §f" + run.outcome().name()));
        p.sendMessage(Component.text("§7Cartas: §f" + run.ownedCards().size()));
        p.sendMessage(Component.text("§7Drafts pendentes: §f" + run.pendingDrafts()));

        long elapsed = System.currentTimeMillis() - run.startedAt();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60;
        p.sendMessage(Component.text("§7Tempo de run: §f" + minutes + "m " + seconds + "s"));

        p.sendMessage(Component.text("§7Milestones: §f" + run.milestonesReached()));
        if (!run.activeModifiers().isEmpty()) {
            p.sendMessage(Component.text("§cMayhem ativos: §f" + String.join(", ", run.activeModifiers())));
        }

        p.sendMessage(Component.text("§7Multiplicadores: "));
        run.multipliers().forEach((key, val) ->
            p.sendMessage(Component.text("  §8" + key + ": §f" + String.format("%.2f", val)))
        );

        return true;
    }
}
