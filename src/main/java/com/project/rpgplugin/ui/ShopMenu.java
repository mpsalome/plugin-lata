package com.project.rpgplugin.ui;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.draft.DraftSession;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.ui.menu.Menu;
import com.project.rpgplugin.util.ItemKeys;
import com.project.rpgplugin.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ShopMenu extends Menu {

    private static final int SIZE = 27;
    private final Player player;
    private final RPGPlugin plugin;

    public ShopMenu(Player player, RPGPlugin plugin) {
        super(SIZE, "<gradient:#ff8c00:#ff4500>\uD83D\uDED2 Loja Pao em Lata");
        this.player = player;
        this.plugin = plugin;

        build();

        setClickHandler(event -> {
            event.setCancelled(true);
            int slot = event.getRawSlot();
            if (slot < 0 || slot >= SIZE) return;

            var runManager = plugin.getRunManager();
            if (!runManager.hasActiveRun(player)) {
                runManager.startRun(player);
            }
            RunState run = runManager.getRun(player);
            if (run == null) return;

            switch (slot) {
                case 10 -> buyReroll(run);
                case 12 -> buyDraftToken(run);
                case 14 -> buyAbsolution(run);
                case 16 -> buyBeacon(run);
            }
        });
    }

    private void build() {
        fillBackground();

        setItem(10, buildShopItem(Material.ENDER_EYE,
                "<light_purple><bold>\uD83C\uDFB2 Reroll de Sorte",
                List.of(
                        "<gray>Custo: <yellow>2 niveis</yellow></gray>",
                        "<gray>Ganha 1 reroll gratuito no proximo draft</gray>"
                )));

        setItem(12, buildShopItem(Material.PAPER,
                "<green><bold>\uD83C\uDFB4 Carta Avulsa",
                List.of(
                        "<gray>Custo: <yellow>5 niveis</yellow></gray>",
                        "<gray>Adiciona 1 carta de build e abre o draft</gray>"
                )));

        setItem(14, buildShopItem(Material.FEATHER,
                "<aqua><bold>\uD83D\uDD4A Absolvicao do Caos",
                List.of(
                        "<gray>Custo: <yellow>10 niveis</yellow></gray>",
                        "<gray>Reduz o Mayhem da sua regiao em 1 nivel</gray>"
                )));

        setItem(16, buildShopItem(Material.NETHER_STAR,
                "<red><bold>\uD83D\uDC51 Sinalizador do Chefe",
                List.of(
                        "<gray>Custo: <yellow>15 niveis</yellow></gray>",
                        "<gray>Invoca um boss apos 5 segundos</gray>"
                )));
    }

    private void buyReroll(RunState run) {
        if (!checkLevels(2)) return;
        player.setLevel(player.getLevel() - 2);
        run.addFreeReroll();
        successSound();
        player.sendActionBar(Text.mm("<light_purple>\uD83C\uDFB2 Reroll de Sorte adquirido!"));
    }

    private void buyDraftToken(RunState run) {
        if (!checkLevels(5)) return;
        player.setLevel(player.getLevel() - 5);
        run.addPendingDrafts(1);
        successSound();
        player.sendActionBar(Text.mm("<green>\uD83C\uDFB4 Carta Avulsa adquirida!"));
        plugin.getPlayerLevelListener().openNextDraft(player, run);
    }

    private void buyAbsolution(RunState run) {
        if (!checkLevels(10)) return;
        player.setLevel(player.getLevel() - 10);
        plugin.getMayhemService().reduceMayhemByOne(player, run);
        successSound();
    }

    private void buyBeacon(RunState run) {
        if (!checkLevels(15)) return;
        player.setLevel(player.getLevel() - 15);
        successSound();

        ItemStack beacon = createBeaconItem();
        player.getInventory().addItem(beacon);
        player.sendActionBar(Text.mm("<red>\uD83D\uDC51 Sinalizador do Chefe adicionado ao inventario!"));
    }

    private boolean checkLevels(int cost) {
        if (player.getLevel() < cost) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendActionBar(Text.mm("<red>Voce precisa de <white>" + cost + "</white> niveis de XP!"));
            return false;
        }
        return true;
    }

    private void successSound() {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    private ItemStack buildShopItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.mm(name));
            meta.lore(lore.stream().map(Text::mm).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBeaconItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.mm("<red>\uD83D\uDC51 Sinalizador do Chefe</red>"));
            meta.lore(List.of(
                    Text.mm("<gray>Clique com o direito no chao para invocar um Chefe!</gray>"),
                    Text.mm("<dark_gray>5 segundos de aviso antes da invocacao</dark_gray>")
            ));
            meta.getPersistentDataContainer().set(ItemKeys.bossBeacon(), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fillBackground() {
        ItemStack border = decorativePane(Material.ORANGE_STAINED_GLASS_PANE, "<gradient:#ff8c00:#ff4500>\uD83D\uDED2");
        for (int i = 0; i < 9; i++) {
            setItem(i, border);
            setItem(SIZE - 9 + i, border);
        }
        setItem(0, decorativePane(Material.GOLD_BLOCK, null));
        setItem(8, decorativePane(Material.GOLD_BLOCK, null));
        setItem(18, decorativePane(Material.GOLD_BLOCK, null));
        setItem(26, decorativePane(Material.GOLD_BLOCK, null));

        ItemStack bg = decorativePane(Material.BLACK_STAINED_GLASS_PANE, null);
        for (int i = 9; i < SIZE - 9; i++) {
            if (i != 10 && i != 12 && i != 14 && i != 16) {
                setItem(i, bg);
            }
        }
    }

    private ItemStack decorativePane(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name != null ? Text.mm(name) : Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    public void open() {
        player.openInventory(getInventory());
    }
}
