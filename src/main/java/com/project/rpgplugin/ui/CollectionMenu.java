package com.project.rpgplugin.ui;

import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.ui.menu.Menu;
import com.project.rpgplugin.ui.menu.MenuHolder;
import com.project.rpgplugin.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectionMenu extends Menu {

    private static final int SIZE = 54;

    public CollectionMenu(Player p, RunState run) {
        super(SIZE, "<gold><bold>Run - Colecao");
        MenuHolder holder = new MenuHolder(this);
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var fillerMeta = filler.getItemMeta();
        if (fillerMeta != null) {
            fillerMeta.displayName(Text.mm(""));
            filler.setItemMeta(fillerMeta);
        }
        fillBorder(filler);

        int slot = 10;
        for (String cardId : run.ownedCards()) {
            if (slot >= 44) break;
            int count = run.cardCount(cardId);
            ItemStack icon = new ItemStack(Material.ENCHANTED_BOOK);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.displayName(Text.mm("<yellow>" + cardId.replace("_", " ")));
                List<String> lore = new ArrayList<>();
                if (count > 1) lore.add("<gray>Stacks: <white>" + count);
                lore.add("<dark_gray>" + cardId);
                meta.lore(lore.stream().map(l -> Text.mm(l)).toList());
                icon.setItemMeta(meta);
            }
            setItem(slot++, icon);
        }

        ItemStack infoBook = new ItemStack(Material.KNOWLEDGE_BOOK);
        var infoMeta = infoBook.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(Text.mm("<gold>Resumo da Run"));
            List<String> infoLore = new ArrayList<>();
            infoLore.add("<gray>Nivel: <white>" + run.level());
            infoLore.add("<gray>Cartas: <white>" + run.ownedCards().size());
            infoLore.add("<gray>Milestones: <white>" + run.milestonesReached());
            infoLore.add("<gray>Andados: <white>" + run.blocksWalked());
            if (!run.multipliers().isEmpty()) {
                infoLore.add("");
                infoLore.add("<yellow>Multiplicadores:");
                for (Map.Entry<String, Double> e : run.multipliers().entrySet()) {
                    infoLore.add("<gray>  " + e.getKey() + ": <white>+" + String.format("%.0f%%", e.getValue() * 100));
                }
            }
            infoMeta.lore(infoLore.stream().map(l -> Text.mm(l)).toList());
            infoBook.setItemMeta(infoMeta);
        }
        setItem(4, infoBook);

        if (!run.activeModifiers().isEmpty()) {
            ItemStack mayhemIcon = new ItemStack(Material.TOTEM_OF_UNDYING);
            var mayhemMeta = mayhemIcon.getItemMeta();
            if (mayhemMeta != null) {
                mayhemMeta.displayName(Text.mm("<red>Mayhem Ativos"));
                List<String> mayhemLore = new ArrayList<>();
                for (String mod : run.activeModifiers()) {
                    mayhemLore.add("<red>  - " + mod.replace("_", " "));
                }
                mayhemMeta.lore(mayhemLore.stream().map(l -> Text.mm(l)).toList());
                mayhemIcon.setItemMeta(mayhemMeta);
            }
            setItem(49, mayhemIcon);
        }

        p.openInventory(getInventory());
    }

    @Override
    public void handleClick(org.bukkit.event.inventory.InventoryClickEvent event) {
    }
}
