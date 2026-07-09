package com.project.rpgplugin.listener;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.mob.BossLootService;
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

import java.util.List;

public class CombatListener implements Listener {

    private final RunManager runManager;
    private final RPGPlugin plugin;
    private final BossLootService lootService;

    public CombatListener(RunManager runManager) {
        this.runManager = runManager;
        if (runManager.plugin() instanceof RPGPlugin rpg) {
            this.plugin = rpg;
            this.lootService = new BossLootService();
        } else {
            this.plugin = null;
            this.lootService = null;
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

        // Clear natural drops — we handle our own loot
        e.getDrops().clear();

        Location bossLoc = target.getLocation();

        // XP reward for nearby players
        int xpAmount = (int) (target.getMaxHealth() * 40);
        String bossName = target.getName();

        for (Player nearby : bossLoc.getNearbyPlayers(64)) {
            nearby.giveExp(xpAmount);
            nearby.sendMessage(Text.mm(
                "<gold><bold>\u2694 " + bossName + " derrotado por " + killer.getName() + "!</bold></gold>"
            ));
            nearby.sendMessage(Text.mm(
                "<yellow>\u2728 +" + xpAmount + " XP pela vitoria!</yellow>"
            ));
        }

        // Gerar loot baseado na vida maxima do boss
        if (lootService != null) {
            List<ItemStack> loot = lootService.generateLoot(target.getMaxHealth());
            for (ItemStack item : loot) {
                bossLoc.getWorld().dropItemNaturally(bossLoc, item);
            }
        }

        // Base loot sempre cai
        bossLoc.getWorld().dropItemNaturally(bossLoc, new ItemStack(Material.DIAMOND, 4 + (int)(Math.random() * 5)));

        Bukkit.broadcast(Text.mm(
            "<gold><bold>\uD83C\uDFC6 " + killer.getName() + " e sua equipe derrotaram " + bossName + "!</bold></gold>"
        ));
    }
}
