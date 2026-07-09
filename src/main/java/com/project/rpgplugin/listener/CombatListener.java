package com.project.rpgplugin.listener;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.util.CombatTracker;
import com.project.rpgplugin.util.ItemKeys;
import com.project.rpgplugin.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
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
        if (e.getEntity() instanceof Player victim) {
            CombatTracker.recordDamage(victim.getUniqueId());
        }

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
        if (thornsReflect > 0 && e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
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
        if (!(e.getEntity().getKiller() instanceof Player killer)) return;

        RunState run = runManager.getRun(killer);
        if (run == null) return;

        double lifestealPct = run.getMultiplier("lifesteal");
        if (lifestealPct > 0) {
            double heal = target.getMaxHealth() * lifestealPct;
            killer.setHealth(Math.min(killer.getMaxHealth(), killer.getHealth() + heal));
        }

        boolean isBoss = target.getPersistentDataContainer().has(ItemKeys.isBoss(), PersistentDataType.BYTE);
        if (!isBoss) return;

        Location bossLoc = target.getLocation();

        int xpAmount = 15000;
        String bossName = target.getName();

        for (Player nearby : bossLoc.getNearbyPlayers(64)) {
            nearby.giveExp(xpAmount);
            nearby.sendMessage(Text.mm(
                "<gold><bold>⚔ " + bossName + " derrotado por " + killer.getName() + "!</bold></gold>"
            ));
            nearby.sendMessage(Text.mm(
                "<yellow>✨ +" + xpAmount + " XP pela vitoria!</yellow>"
            ));
        }

        bossLoc.getWorld().dropItemNaturally(bossLoc, new ItemStack(Material.DIAMOND, 8));
        bossLoc.getWorld().dropItemNaturally(bossLoc, new ItemStack(Material.NETHERITE_SCRAP, 2));
        bossLoc.getWorld().dropItemNaturally(bossLoc, new ItemStack(Material.EXPERIENCE_BOTTLE, 48));
        bossLoc.getWorld().dropItemNaturally(bossLoc, new ItemStack(Material.GOLDEN_APPLE, 4));
        bossLoc.getWorld().dropItemNaturally(bossLoc, new ItemStack(Material.ENDER_PEARL, 6));

        Bukkit.broadcast(Text.mm(
            "<gold><bold>🏆 " + killer.getName() + " e sua equipe derrotaram " + bossName + "!</bold></gold>"
        ));
    }
}
