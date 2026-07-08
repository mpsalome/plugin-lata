package com.project.rpgplugin.core.skill.trigger;

import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class CompositeTriggerHelper {

    private CompositeTriggerHelper() {}

    public static SkillTrigger sneakJump() {
        return new CompositeTrigger(
            Set.of(TriggerKind.MOVE),
            ctx -> {
                if (!(ctx.sourceEvent() instanceof PlayerMoveEvent e)) return false;
                Player p = ctx.player();
                return p.isSneaking()
                    && !p.isOnGround()
                    && e.getTo().getY() > e.getFrom().getY();
            },
            "<gray>Agache e pule no ar"
        );
    }

    public static SkillTrigger sneakLeftClickAir() {
        return new CompositeTrigger(
            Set.of(TriggerKind.INTERACT),
            ctx -> {
                if (!(ctx.sourceEvent() instanceof PlayerInteractEvent e)) return false;
                return ctx.player().isSneaking()
                    && e.getAction() == Action.LEFT_CLICK_AIR;
            },
            "<gray>Agache e clique esquerdo no ar"
        );
    }

    public static SkillTrigger sneakRightClick(Material... items) {
        Set<Material> allowed = items.length == 0 ? Set.of() : Set.of(items);
        String desc = items.length == 0
            ? "<gray>Agache e clique direito com a mão vazia"
            : "<gray>Agache e clique direito com " + Arrays.stream(items)
                .map(m -> m.name().toLowerCase().replace('_', ' '))
                .collect(Collectors.joining(", "));
        return new CompositeTrigger(
            Set.of(TriggerKind.INTERACT),
            ctx -> {
                if (!(ctx.sourceEvent() instanceof PlayerInteractEvent e)) return false;
                if (!ctx.player().isSneaking()) return false;
                if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
                if (allowed.isEmpty()) return ctx.usedItem() == null || ctx.usedItem().getType().isAir();
                return ctx.usedItem() != null && allowed.contains(ctx.usedItem().getType());
            },
            desc
        );
    }

    public static SkillTrigger sneakItemInteract(Material heldItem) {
        return new CompositeTrigger(
            Set.of(TriggerKind.INTERACT),
            ctx -> {
                if (!(ctx.sourceEvent() instanceof PlayerInteractEvent e)) return false;
                if (!ctx.player().isSneaking()) return false;
                if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return false;
                return ctx.usedItem() != null && ctx.usedItem().getType() == heldItem;
            },
            "<gray>Agache e clique direito com " + heldItem.name().toLowerCase().replace('_', ' ')
        );
    }

    /**
     * Trigger for block-placement skills (Catalyst Pattern).
     * Only activates when the player is sneaking while placing a block.
     * Non-sneaking block placements pass through as vanilla building.
     * <p>
     * The corresponding {@code BlockPlaceEvent} is cancelled by the dispatch
     * layer, so the block item is never consumed from the player's inventory.
     */
    public static SkillTrigger sneakBlockPlace() {
        return new CompositeTrigger(
            Set.of(TriggerKind.INTERACT),
            ctx -> {
                if (!(ctx.sourceEvent() instanceof BlockPlaceEvent)) return false;
                return ctx.player().isSneaking();
            },
            "<gray>Agache e coloque um bloco no chão"
        );
    }

    public static SkillTrigger onDamageTaken(DamageCause... causes) {
        Set<DamageCause> causeSet = Set.of(causes);
        String desc = Arrays.stream(causes)
            .map(c -> c.name().toLowerCase().replace('_', ' '))
            .collect(Collectors.joining(", "));
        return new CompositeTrigger(
            Set.of(TriggerKind.DAMAGE),
            ctx -> {
                if (!(ctx.sourceEvent() instanceof EntityDamageEvent e)) return false;
                return causeSet.contains(e.getCause());
            },
            "<gray>Ao sofrer dano de " + desc
        );
    }

    public static SkillTrigger onDamageTaken(Predicate<EntityDamageEvent> predicate, String description) {
        return new CompositeTrigger(
            Set.of(TriggerKind.DAMAGE),
            ctx -> {
                if (!(ctx.sourceEvent() instanceof EntityDamageEvent e)) return false;
                return predicate.test(e);
            },
            description
        );
    }

    public static SkillTrigger onFallDamage() {
        return onDamageTaken(
            e -> e.getCause() == DamageCause.FALL,
            "<gray>Ao sofrer dano de queda"
        );
    }

    public static SkillTrigger onKnockback() {
        return new CompositeTrigger(
            Set.of(TriggerKind.DAMAGE),
            ctx -> {
                if (!(ctx.sourceEvent() instanceof EntityDamageEvent e)) return false;
                return e.getCause() == DamageCause.ENTITY_ATTACK
                    || e.getCause() == DamageCause.ENTITY_SWEEP_ATTACK
                    || e.getCause() == DamageCause.PROJECTILE
                    || e.getCause() == DamageCause.ENTITY_EXPLOSION;
            },
            "<gray>Ao sofrer knockback"
        );
    }
}
