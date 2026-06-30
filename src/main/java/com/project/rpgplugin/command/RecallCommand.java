package com.project.rpgplugin.command;

import com.project.rpgplugin.core.progression.RecallProgression;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RecallCommand implements CommandExecutor {

    private final RunManager runManager;
    private final RecallProgression recallProgression;

    public RecallCommand(RunManager runManager, RecallProgression recallProgression) {
        this.runManager = runManager;
        this.recallProgression = recallProgression;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Apenas jogadores podem usar /recall.");
            return true;
        }

        if (!runManager.hasActiveRun(p)) {
            p.sendMessage(Component.text("§cVoce nao tem uma run ativa! Use /run para comecar."));
            return true;
        }

        RunState run = runManager.getRun(p);
        if (!run.hasCard("recall")) {
            p.sendMessage(Component.text("§cVoce ainda nao desbloqueou o Recall! (FIGHTING 10)"));
            return true;
        }

        recallProgression.use(p, run);
        return true;
    }
}
