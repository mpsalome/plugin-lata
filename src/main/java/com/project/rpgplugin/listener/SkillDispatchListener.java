package com.project.rpgplugin.listener;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.command.LataCommand;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.CardTag;
import com.project.rpgplugin.core.mana.ManaService;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.skill.Skill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillRegistry;
import com.project.rpgplugin.core.skill.SkillServices;
import com.project.rpgplugin.core.skill.impl.BladeDanceSkill;
import com.project.rpgplugin.core.skill.trigger.TriggerKind;
import com.project.rpgplugin.ui.CollectionMenu;
import com.project.rpgplugin.ui.HubMenu;
import com.project.rpgplugin.ui.ShopMenu;
import com.project.rpgplugin.util.ItemKeys;
import com.project.rpgplugin.util.Text;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
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
    private final CardRegistry cardRegistry;
    private final RPGPlugin plugin;
    private ManaService manaService;

    public SkillDispatchListener(SkillRegistry registry, SkillServices services, RunManager runManager, CardRegistry cardRegistry, RPGPlugin plugin) {
        this.registry = registry;
        this.services = services;
        this.runManager = runManager;
        this.cardRegistry = cardRegistry;
        this.plugin = plugin;
    }

    public void setManaService(ManaService manaService) {
        this.manaService = manaService;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();

        if (ItemKeys.isRpgBook(item)) {
            e.setCancelled(true);
            new HubMenu(p, plugin, runManager, cardRegistry, runManager.statService());
            return;
        }

        if (ItemKeys.isShopItem(item)) {
            e.setCancelled(true);
            new ShopMenu(p, plugin).open();
            return;
        }

        if (ItemKeys.isBossBeacon(item)) {
            e.setCancelled(true);
            handleBossBeacon(p);
            return;
        }

        dispatch(p, TriggerKind.INTERACT, e, item, null);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        // Vanilla construction mode: not sneaking → place block normally, skip all skill matching
        if (!p.isSneaking()) return;
        dispatch(p, TriggerKind.INTERACT, e, p.getInventory().getItemInMainHand(), e.getBlockPlaced());
    }

    @EventHandler
    public void onFish(PlayerFishEvent e) {
        Player p = e.getPlayer();
        dispatch(p, TriggerKind.INTERACT, e, p.getInventory().getItemInMainHand(), null);
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
            p.sendMessage(Text.mm("<red>Este bloco foi reforcado e esta temporariamente indestrutivel!"));
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
            if (!run.isToggledOn(skillId)) continue;
            Skill skill = registry.byId(skillId).orElse(null);
            if (skill == null) continue;
            if (!skill.trigger().kinds().contains(kind)) continue;

            SkillContext ctx = new SkillContext(player, services, item, block, event);
            if (skill.trigger().matches(skill, ctx)) {
                if (manaService != null && manaService.isEnabled()) {
                    double cost = manaService.getManaCost(skillId);
                    if (cost > 0 && !manaService.tryConsumeMana(player, cost)) {
                        player.sendMessage(Text.mm("<red>Mana insuficiente! Necessario: " + (int) cost));
                        return;
                    }
                }
                if (event instanceof Cancellable c && (kind == TriggerKind.INTERACT || kind == TriggerKind.CONSUME)) {
                    c.setCancelled(true);
                }
                skill.activate(ctx);
                trackBladeDanceMobility(player, skillId);
                return;
            }
        }
    }

    private void handleBossBeacon(Player player) {
        // Consume beacon from whichever hand held it
        ItemStack main = player.getInventory().getItemInMainHand();
        boolean isBeacon = main != null && ItemKeys.isBossBeacon(main);
        ItemStack item = isBeacon ? main : player.getInventory().getItemInOffHand();
        if (item != null && item.getAmount() > 0) {
            item.setAmount(item.getAmount() - 1);
        }

        // Boss aleatorio + stats escaladas pelo nivel do invocador
        int level = 1;
        RunState run = runManager.getRun(player);
        if (run != null) level = run.level();
        final int playerLevel = level;

        String[] bossIds = {"frostmaw", "magma_tyrant", "storm_wyvern", "void_lich"};
        String bossId = bossIds[(int) (Math.random() * bossIds.length)];
        String bossName = switch (bossId) {
            case "magma_tyrant" -> "Tirano Magmatico, Coracao do Inferno";
            case "storm_wyvern" -> "Furia Tempestuosa, Asa do Ceu";
            case "void_lich" -> "Lich do Vazio, A Noite Eterna";
            default -> "Frostmaw, Senhor do Gelo";
        };

        String levelStr = "<yellow>Nivel " + playerLevel + "</yellow>";
        String hint = switch (bossId) {
            case "frostmaw" -> "<aqua>Dizem que guarda equipamentos congelados...</aqua>";
            case "magma_tyrant" -> "<red>Reza a lenda que carrega o calor do nucleo...</red>";
            case "storm_wyvern" -> "<yellow>Contam que suas asas guardam segredos eletricos...</yellow>";
            case "void_lich" -> "<dark_purple>Sussurros falam de armaduras do esquecimento...</dark_purple>";
            case "sir_creeper_alot" -> "<green>Dizem que sua armadura e tao famosa quanto ele...</green>";
            case "slime_shady" -> "<light_purple>Boato que seus pertences sao bem... escorregadios...</light_purple>";
            case "the_beheader" -> "<dark_red>Falam que seu machado nunca erra o alvo...</dark_red>";
            case "ancient_guardian" -> "<dark_aqua>Lendas contam de tesouros das profundezas...</dark_aqua>";
            case "piglin_warlord" -> "<gold>Segundo os piglins, seu ouro e amaldicoado...</gold>";
            case "phantom_king" -> "<white>Historias de tesouros que flutuam entre ceu e terra...</white>";
            default -> "<gray>Ninguem sabe o que este boss carrega...</gray>";
        };

        org.bukkit.Bukkit.broadcast(Text.mm(
            "<gold><bold>\u26A0 " + bossName + "</bold></gold> <gray>|</gray> " + levelStr + "\n"
            + hint
        ));
        player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.0f, 0.5f);

        player.getScheduler().runDelayed(plugin, st -> {
            if (!player.isOnline()) return;
            LataCommand lataCmd = (LataCommand) plugin.getCommand("lata").getExecutor();
            lataCmd.spawnBossAtSafeLocation(player, bossId, bossName, playerLevel);
        }, null, 100L);
    }

    private void trackBladeDanceMobility(Player player, String skillId) {
        cardRegistry.byId(skillId).ifPresent(card -> {
            if (card.tags().contains(CardTag.MOBILITY)) {
                BladeDanceSkill.recordMobility(player.getUniqueId());
            }
        });
    }
}
