package com.project.rpgplugin.listener;

import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.util.Text;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;

public class AugmentListener implements Listener {

    private final RunManager runManager;

    public AugmentListener(RunManager runManager) {
        this.runManager = runManager;
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent e) {
        Player p = (Player) e.getPlayer();
        RunState run = runManager.getRun(p);
        if (run == null) return;
        double xpMult = run.getMultiplier("xp_gain");
        if (xpMult > 0) {
            int bonus = (int) Math.round(e.getAmount() * xpMult);
            if (bonus > 0) {
                e.setAmount(e.getAmount() + bonus);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFallDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        RunState run = runManager.getRun(p);
        if (run == null) return;
        double reduction = run.getMultiplier("fall_damage_reduction");
        if (reduction > 0) {
            double newDamage = e.getDamage() * (1.0 - Math.min(reduction, 1.0));
            e.setDamage(newDamage);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        RunState run = runManager.getRun(p);
        if (run == null) return;

        Material blockType = e.getBlock().getType();
        boolean isOre = Tag.ITEMS_COALS.getValues().stream().anyMatch(m -> m == blockType)
            || blockType.name().endsWith("_ORE")
            || blockType == Material.ANCIENT_DEBRIS;

        double oreDropChance = run.getMultiplier("double_ore_drop");
        if (oreDropChance > 0 && isOre && Math.random() < oreDropChance) {
            Collection<ItemStack> drops = e.getBlock().getDrops(p.getInventory().getItemInMainHand());
            for (ItemStack drop : drops) {
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), drop);
            }
        }

        double cropYieldMult = run.getMultiplier("crop_yield");
        if (cropYieldMult > 0 && isCrop(blockType)) {
            Collection<ItemStack> drops = e.getBlock().getDrops(p.getInventory().getItemInMainHand());
            for (ItemStack drop : drops) {
                int extra = (int) Math.round(drop.getAmount() * cropYieldMult);
                if (extra > 0) {
                    drop.setAmount(drop.getAmount() + extra);
                }
            }
        }

        double miningSpeed = run.getMultiplier("mining_speed");
        if (miningSpeed > 0) {
            int amplifier = (int) Math.round(miningSpeed * 10);
            p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, Math.min(amplifier, 3), false, false, true));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (!(e.getEntity().getKiller() instanceof Player killer)) return;
        RunState run = runManager.getRun(killer);
        if (run == null) return;

        double extraDropChance = run.getMultiplier("extra_mob_drops");
        if (extraDropChance > 0 && Math.random() < extraDropChance) {
            List<ItemStack> drops = e.getDrops();
            if (!drops.isEmpty()) {
                ItemStack sample = drops.getFirst();
                ItemStack extra = sample.clone();
                extra.setAmount(1);
                e.getDrops().add(extra);
            }
        }

        for (var onKill : run.onKillEffects()) {
            switch (onKill.effect()) {
                case "gold_drop" -> {
                    ItemStack gold = new ItemStack(Material.GOLD_NUGGET, (int) onKill.value());
                    e.getEntity().getWorld().dropItemNaturally(e.getEntity().getLocation(), gold);
                }
                case "heal_and_mana" -> {
                    double heal = onKill.value();
                    killer.setHealth(Math.min(killer.getMaxHealth(), killer.getHealth() + heal));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!(e.getDamager() instanceof org.bukkit.entity.LivingEntity)) return;
        RunState run = runManager.getRun(p);
        if (run == null) return;

        double blindChance = run.getMultiplier("blind_on_hit_chance");
        if (blindChance > 0 && Math.random() < blindChance) {
            if (e.getDamager() instanceof Player damager) {
                damager.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false, true));
            }
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return;
        RunState run = runManager.getRun(p);
        if (run == null) return;
        double dj = run.getMultiplier("double_jump");
        if (dj <= 0) return;

        e.setCancelled(true);
        p.setFlying(false);
        p.setAllowFlight(false);

        Vector vel = p.getVelocity();
        vel.setY(0.8);
        vel.setX(vel.getX() * 1.3);
        vel.setZ(vel.getZ() * 1.3);
        p.setVelocity(vel);
    }

    @EventHandler
    public void onPlayerFall(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        RunState run = runManager.getRun(p);
        if (run == null) return;
        double dj = run.getMultiplier("double_jump");
        if (dj > 0) {
            p.setAllowFlight(true);
        }
    }

    private boolean isCrop(Material mat) {
        return switch (mat) {
            case WHEAT, CARROTS, POTATOES, BEETROOTS, NETHER_WART,
                 MELON, PUMPKIN, SUGAR_CANE, CACTUS, BAMBOO,
                 KELP_PLANT, SWEET_BERRY_BUSH, COCOA -> true;
            default -> false;
        };
    }

    public void tickMagnet(Player p) {
        RunState run = runManager.getRun(p);
        if (run == null) return;
        double range = run.getMultiplier("item_magnet_range");
        if (range <= 0) return;

        Location loc = p.getLocation();
        for (Entity ent : p.getNearbyEntities(range, range, range)) {
            if (ent instanceof Item item && !item.isDead()) {
                item.setVelocity(loc.toVector().subtract(ent.getLocation().toVector()).normalize().multiply(0.5));
            }
            if (ent instanceof ExperienceOrb orb && !orb.isDead()) {
                orb.setVelocity(loc.toVector().subtract(ent.getLocation().toVector()).normalize().multiply(0.5));
            }
        }
    }
}
