package com.project.rpgplugin.core.run;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.CardTier;
import com.project.rpgplugin.core.card.StatService;
import com.project.rpgplugin.core.mayhem.MayhemService;
import com.project.rpgplugin.util.ItemKeys;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RunManager {

    private final RPGPlugin plugin;
    private final CardRegistry cardRegistry;
    private final StatService statService;
    private final ResetService resetService;
    private final MayhemService mayhemService;
    private final Map<UUID, RunState> activeRuns = new HashMap<>();
    private final Random random = new Random();
    private boolean resetting = false;

    public RunManager(RPGPlugin plugin, CardRegistry cardRegistry, StatService statService, ResetService resetService, MayhemService mayhemService) {
        this.plugin = plugin;
        this.cardRegistry = cardRegistry;
        this.statService = statService;
        this.resetService = resetService;
        this.mayhemService = mayhemService;
    }

    public void startRun(Player p) {
        RunState run = new RunState(p.getUniqueId(), cardRegistry);
        activeRuns.put(p.getUniqueId(), run);

        rollInitialKit(p, run);
        giveBook(p);

        statService.recompute(p, run);
        p.sendMessage(Component.text("Nova Run Iniciada!").color(net.kyori.adventure.text.format.NamedTextColor.GREEN).decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true));
    }

    private void giveBook(Player p) {
        boolean hasBook = false;
        boolean hasShop = false;
        for (var item : p.getInventory().getContents()) {
            if (item != null) {
                if (ItemKeys.isRpgBook(item)) hasBook = true;
                if (ItemKeys.isShopItem(item)) hasShop = true;
            }
        }
        if (!hasBook) {
            p.getInventory().addItem(plugin.createRpgBook());
        }
        if (!hasShop) {
            p.getInventory().addItem(plugin.createShopItem());
        }
    }

    public void endRun(Player p, RunOutcome outcome) {
        if (resetting) return;
        resetting = true;

        RunState run = activeRuns.get(p.getUniqueId());
        if (run == null) {
            resetting = false;
            return;
        }

        run.setOutcome(outcome);

        if (outcome == RunOutcome.VICTORY) {
            p.sendMessage(Component.text("VITORIA!").color(net.kyori.adventure.text.format.NamedTextColor.GOLD).decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true));
        }

        resetService.fullReset(p, run);
        activeRuns.remove(p.getUniqueId());

        // Start new clean run
        startRun(p);

        resetting = false;
    }

    private void rollInitialKit(Player p, RunState run) {
        // Optional: grant 1 random bronze card at start (configurable later)
        List<Card> bronzeCards = cardRegistry.byTier(CardTier.BRONZE).stream()
            .filter(c -> c.offerable(run))
            .toList();
        if (!bronzeCards.isEmpty()) {
            Card starter = bronzeCards.get(random.nextInt(bronzeCards.size()));
            starter.onAcquire(p, run);
            statService.recompute(p, run);
            p.sendActionBar(Component.text("Kit inicial: " + starter.id().replace("_", " ")).color(net.kyori.adventure.text.format.NamedTextColor.GREEN));
        }
    }

    public RunState getRun(Player p) {
        return activeRuns.get(p.getUniqueId());
    }

    public boolean hasActiveRun(Player p) {
        return activeRuns.containsKey(p.getUniqueId());
    }

    public void restoreRun(Player p, RunState run) {
        activeRuns.put(p.getUniqueId(), run);
        statService.recompute(p, run);
        mayhemService.reapplyOnJoin(p, run);
        p.sendMessage(Component.text("Run restaurada!").color(net.kyori.adventure.text.format.NamedTextColor.GREEN).decoration(net.kyori.adventure.text.format.TextDecoration.BOLD, true));
    }

    public void removeRun(Player p) {
        activeRuns.remove(p.getUniqueId());
    }

    public RPGPlugin plugin() { return plugin; }
    public CardRegistry cardRegistry() { return cardRegistry; }
    public StatService statService() { return statService; }
    public Collection<RunState> getAllRuns() { return activeRuns.values(); }
}
