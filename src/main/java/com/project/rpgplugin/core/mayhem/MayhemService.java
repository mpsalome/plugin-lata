package com.project.rpgplugin.core.mayhem;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MayhemService {

    private final RPGPlugin plugin;
    private final ModifierRegistry registry;
    private final MilestoneService milestoneService;
    private final MayhemConfig config;
    private final Map<UUID, Set<String>> playerModifiers = new ConcurrentHashMap<>();
    private final Set<String> globalModifiers = ConcurrentHashMap.newKeySet();

    public MayhemService(RPGPlugin plugin, ModifierRegistry registry, MilestoneService milestoneService, MayhemConfig config) {
        this.plugin = plugin;
        this.registry = registry;
        this.milestoneService = milestoneService;
        this.config = config;
    }

    public void rollAndApply(RunState run, World world) {
        if (run.activeModifiers().size() >= config.maxActive()) return;

        int idx = run.milestonesReached();
        Set<ModifierSeverity> allowed = Set.copyOf(milestoneService.allowedSeverities(idx));

        Modifier m = registry.rollOne(allowed, run.activeModifiers());
        if (m == null) return;

        run.addModifier(m.id());
        MayhemContext ctx = new MayhemContext(plugin, run, world);
        m.onActivate(ctx);

        if (scopeIsServer()) {
            globalModifiers.add(m.id());
            run.setSharedModifiers(Set.copyOf(globalModifiers));
        }

        if (config.announce()) {
            broadcast(m);
        }
    }

    public void clear(Player p, RunState run) {
        World world = p.getWorld();
        for (String modId : run.activeModifiers()) {
            registry.byId(modId).ifPresent(m -> {
                MayhemContext ctx = new MayhemContext(plugin, run, world);
                m.onDeactivate(ctx);
            });
        }
        run.activeModifiers().clear();
        run.setMilestonesReached(0);
        playerModifiers.remove(p.getUniqueId());
        if (scopeIsServer()) {
            globalModifiers.clear();
        }
    }

    public void tryRelieveMayhem(Player deadPlayer, RunState run) {
        World world = deadPlayer.getWorld();

        // Anti-cheese: only reduce mayhem if player died at level 10+
        if (run.level() < 10) {
            clear(deadPlayer, run);
            return;
        }

        boolean relieved = false;

        // Server scope: remove the LAST global modifier
        if (scopeIsServer() && !globalModifiers.isEmpty()) {
            String lastModId = globalModifiers.stream()
                .reduce((first, second) -> second)
                .orElse(null);
            if (lastModId != null) {
                registry.byId(lastModId).ifPresent(m -> {
                    MayhemContext ctx = new MayhemContext(plugin, run, world);
                    m.onDeactivate(ctx);
                });
                globalModifiers.remove(lastModId);
                Set<String> updatedShared = Set.copyOf(globalModifiers);
                for (RunState otherRun : plugin.getRunManager().getAllRuns()) {
                    otherRun.setSharedModifiers(updatedShared);
                }
                relieved = true;
            }
        }

        // Clear dead player's personal modifier state
        for (String modId : run.activeModifiers()) {
            registry.byId(modId).ifPresent(m -> {
                MayhemContext ctx = new MayhemContext(plugin, run, world);
                m.onDeactivate(ctx);
            });
        }
        run.activeModifiers().clear();
        run.setMilestonesReached(0);
        playerModifiers.remove(deadPlayer.getUniqueId());

        if (relieved) {
            int newLevel = globalModifiers.size();
            String msg = "<gradient:#00ff87:#60efff>⬇ O sacrifício de <white>" + deadPlayer.getName()
                + "</white> acalmou a tempestade!</gradient> <gray>Mayhem reduzido para</gray> <yellow>Lv " + newLevel + "</yellow>";
            Bukkit.broadcast(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(msg));
            world.playSound(deadPlayer.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.8f);
        }
    }

    public void reduceMayhemByOne(Player player, RunState run) {
        if (run.activeModifiers().isEmpty()) return;

        String lastModId = run.activeModifiers().stream()
            .reduce((first, second) -> second)
            .orElse(null);
        if (lastModId == null) return;

        registry.byId(lastModId).ifPresent(m -> {
            MayhemContext ctx = new MayhemContext(plugin, run, player.getWorld());
            m.onDeactivate(ctx);
        });

        run.activeModifiers().remove(lastModId);
        run.setMilestonesReached(Math.max(0, run.milestonesReached() - 1));

        if (scopeIsServer()) {
            globalModifiers.remove(lastModId);
            Set<String> updatedShared = Set.copyOf(globalModifiers);
            for (RunState otherRun : plugin.getRunManager().getAllRuns()) {
                otherRun.setSharedModifiers(updatedShared);
            }
        }

        int newLevel = run.activeModifiers().size();
        Bukkit.broadcast(Text.mm(
            "<gradient:#ffd700:#00ff87>🕊 O Caos foi dissipado pela Loja Pao em Lata!</gradient> <gray>Mayhem na regiao de <white>"
            + player.getName() + "</white> caiu para</gray> <yellow>Lv " + newLevel + "</yellow>"
        ));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 0.8f);
    }

    public void reapplyOnJoin(Player p, RunState run) {
        if (scopeIsServer() && !globalModifiers.isEmpty()) {
            // Server-scope: reapply all global modifiers to this player
            for (String modId : globalModifiers) {
                if (!run.activeModifiers().contains(modId)) {
                    run.addModifier(modId);
                    registry.byId(modId).ifPresent(m -> {
                        MayhemContext ctx = new MayhemContext(plugin, run, p.getWorld());
                        m.onActivate(ctx);
                    });
                }
            }
        }
    }

    public Set<String> getActiveModifiers() {
        if (scopeIsServer()) return Set.copyOf(globalModifiers);
        return Set.of();
    }

    private void broadcast(Modifier m) {
        String severityColor = switch (m.severity()) {
            case MILD -> "<green>";
            case WILD -> "<gold>";
            case INSANE -> "<red>";
        };
        String msg = "<dark_gray>[<red>MAYHEM<dark_gray>] <gray>"
            + severityColor + "<bold>" + m.id().replace("_", " ") + "</bold>"
            + " <gray>ativado!";
        Bukkit.broadcast(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(msg));
    }

    private boolean scopeIsServer() {
        return "server".equals(config.scope());
    }

    public void clearAll() {
        globalModifiers.clear();
        playerModifiers.clear();
    }
}
