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
            p.sendMessage(Component.text("<red>Você não tem uma run ativa."));
            return true;
        }

        RunState run = runManager.getRun(p);
        if (run == null) return true;

        p.sendMessage(Text.mm("<gold><bold>=== Run Info ==="));
        p.sendMessage(Text.mm("<gray>Nível: <white>" + run.level()));
        p.sendMessage(Text.mm("<gray>Status: <white>" + run.outcome().name()));
        p.sendMessage(Text.mm("<gray>Cartas: <white>" + run.ownedCards().size()));
        p.sendMessage(Text.mm("<gray>Drafts pendentes: <white>" + run.pendingDrafts()));

        long elapsed = System.currentTimeMillis() - run.startedAt();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60;
        p.sendMessage(Text.mm("<gray>Tempo de run: <white>" + minutes + "m " + seconds + "s"));

        p.sendMessage(Text.mm("<gray>Milestones: <white>" + run.milestonesReached()));
        if (!run.activeModifiers().isEmpty()) {
            p.sendMessage(Text.mm("<red>Mayhem ativos: <white>" + String.join(", ", run.activeModifiers())));
        }

        p.sendMessage(Text.mm("<gray>Multiplicadores: "));
        run.multipliers().forEach((key, val) ->
            p.sendMessage(Text.mm("  <dark_gray>" + key + ": <white>" + String.format("%.2f", val)))
        );

        return true;
    }
}
