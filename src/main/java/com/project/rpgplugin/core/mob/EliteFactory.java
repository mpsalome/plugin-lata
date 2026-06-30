package com.project.rpgplugin.core.mob;

import com.project.rpgplugin.util.ItemKeys;
import com.project.rpgplugin.util.Text;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class EliteFactory {

    public LivingEntity spawnBoss(Location loc, BossDef def) {
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

        return e;
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
    ) {}

    public record MobDef(
        String id, EntityType baseType, String displayName,
        double health, double damage, double scale,
        Map<String, ItemStack> equipment
    ) {}
}
