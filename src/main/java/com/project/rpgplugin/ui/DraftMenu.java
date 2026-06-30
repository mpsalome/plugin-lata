package com.project.rpgplugin.ui;

import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardTier;
import com.project.rpgplugin.core.draft.DraftService;
import com.project.rpgplugin.core.draft.DraftSession;
import com.project.rpgplugin.core.draft.DraftWeighting;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DraftMenu {

    private final Player player;
    private final DraftSession session;
    private final DraftService draftService;
    private final RunState run;
    private final RunManager runManager;
    private final DraftWeighting weighting;
    private final RPGPlugin plugin;

    public DraftMenu(Player player, DraftSession session, DraftService draftService, RunState run,
                     RunManager runManager, DraftWeighting weighting, RPGPlugin plugin) {
        this.player = player;
        this.session = session;
        this.draftService = draftService;
        this.run = run;
        this.runManager = runManager;
        this.weighting = weighting;
        this.plugin = plugin;
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27,
            Component.text("§8⬡ Escolha sua carta"));

        List<Card> options = session.options();
        for (int i = 0; i < Math.min(3, options.size()); i++) {
            Card card = options.get(i);
            inv.setItem(11 + i * 3, buildCardItem(card, i));
        }

        // Reroll button (slot 22)
        if (DraftWeighting.isRerollEnabled(plugin) && session.rerollsUsed() < DraftWeighting.getMaxRerollPerDraft(plugin)) {
            int cost = DraftWeighting.getRerollCostLevels(plugin);
            ItemStack rerollItem = new ItemStack(Material.ENDER_EYE);
            ItemMeta meta = rerollItem.getItemMeta();
            if (meta != null) {
                meta.displayName(Text.mm("<yellow>🔄 Reroll"));
                meta.lore(List.of(
                    Text.mm("<gray>Custo: <yellow>" + cost + " nível(is)</gray>"),
                    Text.mm("<dark_gray>Usados: " + session.rerollsUsed() + "/" + DraftWeighting.getMaxRerollPerDraft(plugin))
                ));
                rerollItem.setItemMeta(meta);
            }
            inv.setItem(22, rerollItem);
        }

        // Skip button (slot 26)
        if (DraftWeighting.isSkipAllowed(plugin)) {
            ItemStack skipItem = new ItemStack(Material.BARRIER);
            ItemMeta meta = skipItem.getItemMeta();
            if (meta != null) {
                meta.displayName(Text.mm("<red>⏭ Pular draft"));
                meta.lore(List.of(
                    Text.mm("<gray>Ganha <red>❤ 6 <gray>de vida."),
                    Text.mm("<dark_gray>Sem carta desta vez.")
                ));
                skipItem.setItemMeta(meta);
            }
            inv.setItem(26, skipItem);
        }

        // Fill border
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.displayName(Component.text(" "));
            border.setItemMeta(bMeta);
        }
        for (int i = 0; i < 27; i++) {
            if (inv.getItem(i) == null) {
                inv.setItem(i, border);
            }
        }

        player.openInventory(inv);
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

        String tierLabel = switch (card.tier()) {
            case BRONZE -> "🥉";
            case SILVER -> "🥈";
            case GOLD -> "🥇";
        };

        String kindLabel = switch (card.kind()) {
            case ABILITY -> "⚡";
            case AUGMENT -> "✦";
        };

        meta.displayName(Text.mm(tierColor + "<bold>" + kindLabel + " " + card.id().replace("_", " ")));

        List<Component> lore = new ArrayList<>();
        lore.add(Text.mm(tierColor + tierLabel + " " + card.tier().name()));
        lore.add(Text.mm("<gray>" + card.kind().name()));

        StringBuilder tagsStr = new StringBuilder();
        for (var tag : card.tags()) {
            if (!tagsStr.isEmpty()) tagsStr.append(" ");
            tagsStr.append("<dark_gray>[").append(tag.name()).append("]");
        }
        if (!tagsStr.isEmpty()) {
            lore.add(Text.mm(tagsStr.toString()));
        }

        lore.add(Component.empty());
        lore.add(Text.mm("<dark_gray>Clique para escolher"));
        if (card.maxStacks() > 1) {
            lore.add(Text.mm("<dark_gray>Máx: " + card.maxStacks() + " stacks"));
        }

        meta.lore(lore);

        List<Component> customLore = meta.lore();
        item.setItemMeta(meta);
        return item;
    }
}
