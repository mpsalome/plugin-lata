package com.project.rpgplugin.listener;

import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillRegistry;
import com.project.rpgplugin.core.skill.SkillServices;
import com.project.rpgplugin.core.skill.trigger.TriggerKind;
import com.project.rpgplugin.util.ItemKeys;
import com.project.rpgplugin.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.Cancellable;

import java.util.List;

public class SkillDispatchListener implements Listener {

    private final SkillRegistry registry;
    private final SkillServices services;
    private final RunManager runManager;

    public SkillDispatchListener(SkillRegistry registry, SkillServices services, RunManager runManager) {
        this.registry = registry;
        this.services = services;
        this.runManager = runManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        if (ItemKeys.isRpgBook(item)) {
            e.setCancelled(true);
            return;
        }

        dispatch(p, TriggerKind.INTERACT, e, item, null);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        dispatch(p, TriggerKind.CONSUME, e, e.getItem(), null);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block block = e.getBlock();

        if (services.isReinforced(block.getLocation())) {
            e.setCancelled(true);
            p.sendMessage(Component.text("Este bloco foi reforcado e esta temporariamente indestrutivel!").color(net.kyori.adventure.text.format.NamedTextColor.RED));
            return;
        }

        dispatch(p, TriggerKind.BLOCK_BREAK, e, p.getInventory().getItemInMainHand(), block);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (e.getFrom().getBlockX() == e.getTo().getBlockX()
                && e.getFrom().getBlockY() == e.getTo().getBlockY()
                && e.getFrom().getBlockZ() == e.getTo().getBlockZ()) {
            return;
        }
        Block under = p.getLocation().clone().subtract(0, 0.1, 0).getBlock();
        dispatch(p, TriggerKind.MOVE, e, null, under);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        dispatch(p, TriggerKind.DAMAGE, e, null, null);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        RunState run = runManager.getRun(p);
        if (run == null) return;
        if (e.getFoodLevel() < p.getFoodLevel()) {
            double decayReduction = run.getMultiplier("hunger_decay_reduction");
            if (decayReduction > 0) {
                int reduction = (int) Math.round((p.getFoodLevel() - e.getFoodLevel()) * decayReduction);
                if (reduction > 0) {
                    p.setFoodLevel(Math.max(0, p.getFoodLevel() - reduction));
                }
            }
        }
    }

    private void dispatch(Player player, TriggerKind kind, Event event, ItemStack item, Block block) {
        RunState run = runManager.getRun(player);
        if (run == null) return;
        List<String> owned = run.ownedAbilities().stream().toList();
        if (owned.isEmpty()) return;

        for (String skillId : owned) {
            Skill skill = registry.byId(skillId).orElse(null);
            if (skill == null) continue;
            if (skill.passive() && kind == TriggerKind.INTERACT) continue;
            if (!skill.trigger().kinds().contains(kind)) continue;

            SkillContext ctx = new SkillContext(player, services, item, block, event);
            if (skill.trigger().matches(skill, ctx)) {
                if (event instanceof Cancellable c) c.setCancelled(true);
                skill.activate(ctx);
                return;
            }
        }
    }
}
