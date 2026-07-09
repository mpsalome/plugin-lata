package com.project.rpgplugin.ui;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.ui.menu.Menu;
import com.project.rpgplugin.util.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.project.rpgplugin.core.card.StatService;

public class HubMenu extends Menu {

    private static final int SIZE = 27;

    private final Player player;
    private final RPGPlugin plugin;
    private final RunManager runManager;
    private final CardRegistry cardRegistry;
    private final StatService statService;

    public HubMenu(Player player, RPGPlugin plugin, RunManager runManager, CardRegistry cardRegistry, StatService statService) {
        super(SIZE, "<gold><bold>Pao em Lata <gray>| Menu Principal");
        this.player = player;
        this.plugin = plugin;
        this.runManager = runManager;
        this.cardRegistry = cardRegistry;
        this.statService = statService;

        fillBackground();
        buildItems();

        setClickHandler(event -> {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= SIZE) return;

            if (!runManager.hasActiveRun(player)) {
                runManager.startRun(player);
            }
            RunState run = runManager.getRun(player);
            if (run == null) return;

            switch (slot) {
                case 10 -> new CollectionMenu(plugin, player, run, cardRegistry, statService);
                case 12 -> new ShopMenu(player, plugin).open();
                case 14 -> plugin.getPlayerLevelListener().openNextDraft(player, run);
                case 16 -> {
                    if (run.hasPendingDrafts()) {
                        plugin.getPlayerLevelListener().openNextDraft(player, run);
                    } else {
                        player.sendMessage(Text.mm("<gray>Sem drafts pendentes. Upe de nivel ou compre na loja!"));
                    }
                }
            }
        });

        player.openInventory(getInventory());
    }

    private void buildItems() {
        setItem(10, hubItem(Material.ENCHANTED_BOOK,
                "<light_purple><bold>Colecao de Cartas",
                "<gray>Visualize e ative/desative suas cartas",
                "<gray>Filtre por categoria e pagina"
        ));

        setItem(12, hubItem(Material.HAY_BLOCK,
                "<gradient:#ffd700:#ff8c00><bold>Loja Pao em Lata",
                "<gray>Compre upgrades com seus niveis de XP!",
                "<gray>Reroll, Carta Avulsa, Absolvicao, Sinalizador"
        ));

        setItem(14, hubItem(Material.ENDER_CHEST,
                "<gold><bold>Draft de Cartas",
                "<gray>Abra o proximo draft pendente",
                "<gray>Escolha 1 entre 3 cartas aleatorias"
        ));

        setItem(16, hubItem(Material.ARROW,
                "<red><bold>Voltar ao Jogo",
                "<gray>Feche o menu e continue sua run"
        ));
    }

    private ItemStack hubItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.mm(name));
            meta.lore(java.util.List.of(loreLines).stream().map(Text::mm).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillBackground() {
        ItemStack bg = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        var bgMeta = bg.getItemMeta();
        if (bgMeta != null) {
            bgMeta.displayName(net.kyori.adventure.text.Component.text("\u00a7r"));
            bg.setItemMeta(bgMeta);
        }
        for (int i = 0; i < SIZE; i++) {
            setItem(i, bg);
        }

        ItemStack border = new ItemStack(Material.GOLD_BLOCK);
        var bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.displayName(net.kyori.adventure.text.Component.text("\u00a7r"));
            border.setItemMeta(bMeta);
        }
        for (int col = 0; col < 9; col++) {
            setItem(col, border);
            setItem(18 + col, border);
        }
    }
}