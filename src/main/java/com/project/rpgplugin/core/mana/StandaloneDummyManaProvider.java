package com.project.rpgplugin.core.mana;

import com.project.rpgplugin.util.SchedulerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StandaloneDummyManaProvider implements ManaProvider {

    private static final double BASE_MAX_MANA = 100.0;
    private static final double REGEN_PER_SECOND = 5.0;

    private final JavaPlugin plugin;
    private final Map<UUID, Double> manaMap = new ConcurrentHashMap<>();
    private final Map<UUID, Double> bonusMaxMana = new ConcurrentHashMap<>();

    public StandaloneDummyManaProvider(JavaPlugin plugin) {
        this.plugin = plugin;
        startRegenTask();
        plugin.getLogger().info("ManaProvider: StandaloneDummy ativo — mana em memoria com regeneracao de " + REGEN_PER_SECOND + "/s.");
    }

    private void startRegenTask() {
        SchedulerUtil.runTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                UUID uid = p.getUniqueId();
                double current = manaMap.getOrDefault(uid, getMaxMana(p));
                double regen = REGEN_PER_SECOND / 2.0;
                double newValue = Math.min(getMaxMana(p), current + regen);
                manaMap.put(uid, newValue);
            }
        }, 20L, 10L);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public double getMana(Player player) {
        return manaMap.getOrDefault(player.getUniqueId(), getMaxMana(player));
    }

    @Override
    public double getMaxMana(Player player) {
        return BASE_MAX_MANA + bonusMaxMana.getOrDefault(player.getUniqueId(), 0.0);
    }

    public void setBaseMaxMana(Player player, double bonus) {
        bonusMaxMana.put(player.getUniqueId(), bonus);
    }

    @Override
    public boolean hasEnoughMana(Player player, double amount) {
        return getMana(player) >= amount;
    }

    @Override
    public boolean consumeMana(Player player, double amount) {
        UUID uid = player.getUniqueId();
        double current = manaMap.getOrDefault(uid, getMaxMana(player));
        if (current < amount) return false;
        manaMap.put(uid, current - amount);
        return true;
    }

    @Override
    public void addMana(Player player, double amount) {
        UUID uid = player.getUniqueId();
        double current = manaMap.getOrDefault(uid, getMaxMana(player));
        manaMap.put(uid, Math.min(getMaxMana(player), current + amount));
    }

    @Override
    public void setMana(Player player, double amount) {
        manaMap.put(player.getUniqueId(), Math.max(0, Math.min(getMaxMana(player), amount)));
    }

    @Override
    public String name() {
        return "StandaloneDummy";
    }
}
