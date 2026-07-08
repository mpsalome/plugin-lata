package com.project.rpgplugin.ui;

import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardTier;
import com.project.rpgplugin.core.draft.DraftService;
import com.project.rpgplugin.core.draft.DraftSession;
import com.project.rpgplugin.core.draft.DraftWeighting;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.listener.PlayerLevelListener;
import com.project.rpgplugin.ui.menu.Menu;
import com.project.rpgplugin.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DraftMenu extends Menu {

    private static final int SIZE = 54;
    private final Player player;
    private final DraftSession session;
    private final DraftService draftService;
    private final RunState run;
    private final RunManager runManager;
    private final DraftWeighting weighting;
    private final RPGPlugin plugin;
    private final PlayerLevelListener levelListener;

    public DraftMenu(Player player, DraftSession session, DraftService draftService, RunState run,
                     RunManager runManager, DraftWeighting weighting, RPGPlugin plugin,
                     PlayerLevelListener levelListener) {
        super(SIZE, "<gold><bold>Selecione sua carta");
        this.player = player;
        this.session = session;
        this.draftService = draftService;
        this.run = run;
        this.runManager = runManager;
        this.weighting = weighting;
        this.plugin = plugin;
        this.levelListener = levelListener;

        build();

        setClickHandler(event -> {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot == 20 || slot == 23 || slot == 26) {
                int index = (slot - 20) / 3;
                Card card = session.options().get(index);
                playTierSound(player, card.tier());
                draftService.applyChoice(player, run, session, index);
                player.closeInventory();
                if (run.hasPendingDrafts()) {
                    levelListener.openNextDraft(player, run);
                }
            } else if (slot == 40 && DraftWeighting.isRerollEnabled(plugin)) {
                draftService.reroll(player, run, session);
                new DraftMenu(player, session, draftService, run, runManager, weighting, plugin, levelListener).open();
            } else if (slot == 44 && DraftWeighting.isSkipAllowed(plugin)) {
                draftService.skipDraft(player, run, session);
                player.closeInventory();
                if (run.hasPendingDrafts()) {
                    levelListener.openNextDraft(player, run);
                }
            }
        });

        player.openInventory(getInventory());
    }

    private void build() {
        List<Card> options = session.options();
        for (int i = 0; i < Math.min(3, options.size()); i++) {
            Card card = options.get(i);
            setItem(20 + i * 3, buildCardItem(card, i));
        }

        if (DraftWeighting.isRerollEnabled(plugin) && session.rerollsUsed() < DraftWeighting.getMaxRerollPerDraft(plugin)) {
            int cost = DraftWeighting.getRerollCostLevels(plugin);
            ItemStack rerollItem = new ItemStack(Material.ENDER_EYE);
            ItemMeta meta = rerollItem.getItemMeta();
            if (meta != null) {
                meta.displayName(Text.mm("<light_purple>Reroll"));
                meta.lore(List.of(
                    Text.mm("<gray>Custo: <white>" + cost + " niveis"),
                    Text.mm("<gray>Usados: <white>" + session.rerollsUsed() + "/" + DraftWeighting.getMaxRerollPerDraft(plugin))
                ));
                rerollItem.setItemMeta(meta);
            }
            setItem(40, rerollItem);
        }

        if (DraftWeighting.isSkipAllowed(plugin)) {
            ItemStack skipItem = new ItemStack(Material.BARRIER);
            ItemMeta meta = skipItem.getItemMeta();
            if (meta != null) {
                meta.displayName(Text.mm("<red>Pular"));
                meta.lore(List.of(
                    Text.mm("<green>+1 coracao permanente"),
                    Text.mm("<green>+1 opcao no proximo draft"),
                    Text.mm("<gray>Nenhuma carta sera ganha")
                ));
                skipItem.setItemMeta(meta);
            }
            setItem(44, skipItem);
        }

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.displayName(Component.text(" "));
            border.setItemMeta(bMeta);
        }
        for (int i = 0; i < SIZE; i++) {
            if (getInventory().getItem(i) == null) {
                setItem(i, border);
            }
        }
    }

    public void open() {
        player.openInventory(getInventory());
    }

    private ItemStack buildCardItem(Card card, int slotIndex) {
        ItemStack item = new ItemStack(card.icon());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String tierColor = switch (card.tier()) {
            case BRONZE -> "<gradient:#CD7F32:#B8860B>";
            case SILVER -> "<gradient:#C0C0C0:#E8E8E8>";
            case GOLD -> "<gradient:#FFD700:#FFA500>";
        };

        String tierEmoji = switch (card.tier()) {
            case BRONZE -> "\uD83E\uDD48";
            case SILVER -> "\uD83E\uDD47";
            case GOLD -> "\uD83C\uDFC6";
        };

        String kindIcon = switch (card.kind()) {
            case ABILITY -> "\u26A1";
            case AUGMENT -> "\u2726";
        };

        String cardName = card.id().replace("_", " ");

        meta.displayName(Text.mm(tierColor + "<bold>" + kindIcon + " " + cardName));

        List<Component> lore = new ArrayList<>();
        lore.add(Text.mm(tierColor + tierEmoji + " " + card.tier().name()));
        lore.add(Text.mm("<gray>" + card.kind().name()));

        StringBuilder tagsStr = new StringBuilder();
        for (var tag : card.tags()) {
            if (!tagsStr.isEmpty()) tagsStr.append(" ");
            tagsStr.append("<dark_gray>[").append(tag.name()).append("]");
        }
        if (!tagsStr.isEmpty()) {
            lore.add(Text.mm(tagsStr.toString()));
        }

        List<String> descLines = card.lore(run);
        if (!descLines.isEmpty()) {
            lore.add(Component.empty());
            for (String line : descLines) {
                lore.add(Text.mm(line));
            }
        }
        lore.add(Component.empty());
        lore.add(Text.mm("<yellow>Clique para escolher"));
        if (card.maxStacks() > 1) {
            lore.add(Text.mm("<gray>Maximo: " + card.maxStacks() + " pilhas"));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void playTierSound(Player p, CardTier tier) {
        Sound sound = switch (tier) {
            case BRONZE -> Sound.BLOCK_NOTE_BLOCK_HAT;
            case SILVER -> Sound.BLOCK_NOTE_BLOCK_PLING;
            case GOLD -> Sound.UI_TOAST_CHALLENGE_COMPLETE;
        };
        p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
    }
}
