package com.project.rpgplugin.core.mayhem;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.run.RunState;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MayhemService {

    private final RPGPlugin plugin;
    private final ModifierRegistry registry;
    private final MilestoneService milestoneService;
    private final MayhemConfig config;
    private final Map<UUID, Set<String>> playerModifiers = new HashMap<>();
    private final Set<String> globalModifiers = new HashSet<>();

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
        Component msg = Component.text("")
            .append(Component.text("§8[§cMAYHEM§8] §f"))
            .append(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                .deserialize(severityColor + "<bold>" + m.id().replace("_", " ") + "</bold>"))
            .append(Component.text(" §7ativado!"));
        Bukkit.broadcast(msg);
    }

    private boolean scopeIsServer() {
        return "server".equals(config.scope());
    }
}
