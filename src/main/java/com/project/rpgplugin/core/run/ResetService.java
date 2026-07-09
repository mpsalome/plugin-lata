package com.project.rpgplugin.core.run;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.StatService;
import com.project.rpgplugin.core.mana.ManaService;
import com.project.rpgplugin.core.mayhem.MayhemService;
import org.bukkit.entity.Player;

public class ResetService {

    private final RunResetService delegate;
    private final BuildResetService buildDelegate;

    public ResetService(RPGPlugin plugin, CardRegistry cardRegistry, StatService statService,
                        MayhemService mayhemService, SpawnResolver spawnResolver,
                        ManaService manaService, RunPersistenceService persistence) {
        this.delegate = new RunResetService(plugin, cardRegistry, statService,
            mayhemService, spawnResolver, manaService, persistence);
        this.buildDelegate = new BuildResetService(plugin, cardRegistry, statService,
            manaService, persistence);
    }

    public void fullReset(Player p, RunState run) {
        delegate.fullReset(p, run);
    }

    public void resetBuild(Player p, RunState run) {
        buildDelegate.resetBuild(p, run);
    }

    public RunResetService delegate() {
        return delegate;
    }

    public BuildResetService buildDelegate() {
        return buildDelegate;
    }
}
