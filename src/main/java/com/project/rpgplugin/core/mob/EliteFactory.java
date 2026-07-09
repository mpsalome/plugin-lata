package com.project.rpgplugin.core.mob;

import com.project.rpgplugin.integration.ModelEngineBridge;
import com.project.rpgplugin.integration.MythicMobsBridge;
import com.project.rpgplugin.util.ItemKeys;
import com.project.rpgplugin.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class EliteFactory {

    private final JavaPlugin plugin;
    private final MythicMobsBridge mythicMobs;
    private final ModelEngineBridge modelEngine;

    public EliteFactory(JavaPlugin plugin, MythicMobsBridge mythicMobs, ModelEngineBridge modelEngine) {
        this.plugin = plugin;
        this.mythicMobs = mythicMobs;
        this.modelEngine = modelEngine;
    }

    public LivingEntity spawnBoss(Location loc, BossDef def) {
        return spawnBoss(loc, def, 1);
    }

    public LivingEntity spawnBoss(Location loc, BossDef def, int bossLevel) {
        if (mythicMobs.isEnabled()) {
            LivingEntity mm = mythicMobs.trySpawnMob(def.id(), loc).orElse(null);
            if (mm != null) {
                mm.getPersistentDataContainer().set(ItemKeys.eliteId(), PersistentDataType.STRING, def.id());
                mm.getPersistentDataContainer().set(ItemKeys.isBoss(), PersistentDataType.BYTE, (byte) (def.victory() ? 1 : 0));
                mm.getPersistentDataContainer().set(ItemKeys.withKey("boss_level"), PersistentDataType.INTEGER, bossLevel);
                mm.customName(Text.mm(def.displayName()));
                mm.setCustomNameVisible(true);
                modelEngine.applyModel(mm, def.id());
                trackBossBar(mm, def);
                return mm;
            }
        }
        LivingEntity e = (LivingEntity) loc.getWorld().spawnEntity(loc, def.baseType());
        e.customName(Text.mm(def.displayName()));
        e.setCustomNameVisible(true);

        setAttr(e, Attribute.MAX_HEALTH, def.health());
        e.setHealth(def.health());
        setAttr(e, Attribute.ATTACK_DAMAGE, def.damage());
        setAttr(e, Attribute.MOVEMENT_SPEED, def.speed());
        setAttr(e, Attribute.KNOCKBACK_RESISTANCE, def.knockbackResist());
        setAttr(e, Attribute.SCALE, def.scale());

        if (def.equipment() != null) {
            EntityEquipment eq = e.getEquipment();
            if (eq != null) {
                def.equipment().forEach((slot, item) -> {
                    switch (slot.toLowerCase()) {
                        case "helmet" -> eq.setHelmet(item);
                        case "chestplate" -> eq.setChestplate(item);
                        case "leggings" -> eq.setLeggings(item);
                        case "boots" -> eq.setBoots(item);
                        case "mainhand" -> eq.setItemInMainHand(item);
                        case "offhand" -> eq.setItemInOffHand(item);
                    }
                });
            }
        }

        e.getPersistentDataContainer().set(ItemKeys.eliteId(), PersistentDataType.STRING, def.id());
        e.getPersistentDataContainer().set(ItemKeys.isBoss(), PersistentDataType.BYTE, (byte) (def.victory() ? 1 : 0));
        e.getPersistentDataContainer().set(ItemKeys.withKey("boss_level"), PersistentDataType.INTEGER, bossLevel);

        modelEngine.applyModel(e, def.id());
        trackBossBar(e, def);

        return e;
    }

    private String stripTags(String mm) {
        return mm.replaceAll("<[^>]+>", "").trim();
    }

    private void trackBossBar(LivingEntity boss, BossDef def) {
        BossBar bar = Bukkit.createBossBar(stripTags(def.displayName()), BarColor.RED, BarStyle.SOLID);
        bar.setVisible(true);
        UUID bossId = boss.getUniqueId();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (boss.isDead() || !boss.isValid()) {
                    bar.removeAll();
                    cancel();
                    return;
                }
                double pct = boss.getHealth() / boss.getMaxHealth();
                bar.setProgress(Math.max(0, Math.min(1, pct)));

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!bar.getPlayers().contains(p) && p.getWorld().equals(boss.getWorld())
                            && p.getLocation().distance(boss.getLocation()) < 64) {
                        bar.addPlayer(p);
                    }
                }

                // Phase triggers
                if (pct <= 0.5 && pct > 0.25) {
                    bar.setColor(BarColor.YELLOW);
                    bar.setTitle(def.id() + " | Fase 2");
                } else if (pct <= 0.25) {
                    bar.setColor(BarColor.WHITE);
                    bar.setTitle(def.id() + " | Fase 3");
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    public LivingEntity spawnElite(Location loc, MobDef def) {
        LivingEntity e = (LivingEntity) loc.getWorld().spawnEntity(loc, def.baseType());
        e.customName(Text.mm(def.displayName()));
        e.setCustomNameVisible(true);

        setAttr(e, Attribute.MAX_HEALTH, def.health());
        e.setHealth(def.health());
        setAttr(e, Attribute.ATTACK_DAMAGE, def.damage());
        setAttr(e, Attribute.SCALE, def.scale());

        if (def.equipment() != null) {
            EntityEquipment eq = e.getEquipment();
            if (eq != null) {
                def.equipment().forEach((slot, item) -> {
                    switch (slot.toLowerCase()) {
                        case "helmet" -> eq.setHelmet(item);
                        case "chestplate" -> eq.setChestplate(item);
                        case "leggings" -> eq.setLeggings(item);
                        case "boots" -> eq.setBoots(item);
                    }
                });
            }
        }

        e.getPersistentDataContainer().set(ItemKeys.eliteId(), PersistentDataType.STRING, def.id());
        modelEngine.applyModel(e, def.id());
        return e;
    }

    private void setAttr(LivingEntity e, Attribute attr, double value) {
        var instance = e.getAttribute(attr);
        if (instance != null) instance.setBaseValue(value);
    }

    public record BossDef(
        String id, EntityType baseType, String displayName,
        double health, double damage, double speed, double scale, double knockbackResist,
        boolean victory, Map<String, ItemStack> equipment
    ) {
        public BossDef scaleByLevel(int level) {
            double hpMult = 1 + Math.max(0, (level - 1) * 0.15);
            double dmgMult = 1 + Math.max(0, (level - 1) * 0.1);
            double hpRand = 0.85 + Math.random() * 0.3;
            double dmgRand = 0.9 + Math.random() * 0.2;
            return new BossDef(id, baseType, displayName,
                Math.round(health * hpMult * hpRand),
                Math.round(damage * dmgMult * dmgRand * 10.0) / 10.0,
                speed, scale, knockbackResist, victory, equipment);
        }
    }

    public record MobDef(
        String id, EntityType baseType, String displayName,
        double health, double damage, double scale,
        Map<String, ItemStack> equipment
    ) {}
}
