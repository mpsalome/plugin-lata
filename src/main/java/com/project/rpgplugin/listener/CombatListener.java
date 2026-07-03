package com.project.rpgplugin.listener;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunOutcome;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.util.ItemKeys;
import com.project.rpgplugin.util.Text;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.persistence.PersistentDataType;

public class CombatListener implements Listener {

    private final RunManager runManager;
    private final RPGPlugin plugin;

    public CombatListener(RunManager runManager) {
        this.runManager = runManager;
        if (runManager.plugin() instanceof RPGPlugin rpg) {
            this.plugin = rpg;
        } else {
            this.plugin = null;
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player damager)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;

        RunState run = runManager.getRun(damager);
        if (run == null) return;

        double damage = e.getDamage();

        double critChance = run.getMultiplier("crit_chance");
        if (critChance > 0 && Math.random() < critChance) {
            damage *= 1.5;
        }

        double lowHpDmg = run.getMultiplier("low_hp_damage");
        if (lowHpDmg > 0 && damager.getHealth() < damager.getMaxHealth() * 0.5) {
            damage *= (1.0 + lowHpDmg);
        }

        double dmgDealtMult = run.getMultiplier("damage_dealt");
        if (dmgDealtMult > 0) {
            damage *= (1.0 + dmgDealtMult);
        }

        double earlyDmg = run.getMultiplier("early_combat_damage");
        if (earlyDmg > 0) {
            damage *= (1.0 + earlyDmg);
        }

        e.setDamage(damage);

        double thornsReflect = run.getMultiplier("thorns_reflect");
        if (thornsReflect > 0 && e.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            target.damage(damage * thornsReflect);
        }

        double executeThreshold = run.getMultiplier("execute_threshold");
        if (executeThreshold > 0 && target.getHealth() - damage <= target.getMaxHealth() * executeThreshold) {
            boolean isBoss = target.getPersistentDataContainer().has(ItemKeys.isBoss(), PersistentDataType.BYTE);
            if (!isBoss && target.getHealth() - damage <= 0) {
                e.setDamage(target.getHealth() + 1);
                damager.sendActionBar(net.kyori.adventure.text.Component.text("Executado!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            }
        }

        double lifestealPct = run.getMultiplier("lifesteal");
        if (lifestealPct > 0 && !(target instanceof Player)) {
            double heal = damage * lifestealPct;
            damager.setHealth(Math.min(damager.getMaxHealth(), damager.getHealth() + heal));
        }

        if (target instanceof Player) {
            double dmgTakenMult = run.getMultiplier("damage_taken");
            if (dmgTakenMult > 0) {
                e.setDamage(e.getDamage() * (1.0 + dmgTakenMult));
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof LivingEntity target)) return;
        if (e.getEntity().getKiller() instanceof Player killer) {
            RunState run = runManager.getRun(killer);
            if (run == null) return;

            double lifestealPct = run.getMultiplier("lifesteal");
            if (lifestealPct > 0) {
                double heal = e.getEntity().getMaxHealth() * lifestealPct;
                killer.setHealth(Math.min(killer.getMaxHealth(), killer.getHealth() + heal));
            }

            boolean isBoss = target.getPersistentDataContainer().has(ItemKeys.isBoss(), PersistentDataType.BYTE);
            if (isBoss) {
                run.setMilestonesReached(run.milestonesReached() + 1);
                if (plugin != null && plugin.getMayhemService() != null) {
                    plugin.getMayhemService().rollAndApply(run, target.getWorld());
                }
                runManager.endRun(killer, RunOutcome.VICTORY);
                killer.sendMessage(Text.mm("<gold><bold>Boss derrotado! Vitória!"));
            }
        }
    }
}
