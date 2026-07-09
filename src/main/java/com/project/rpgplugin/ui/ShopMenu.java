package com.project.rpgplugin.ui;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.mayhem.MayhemService;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.ui.menu.Menu;
import com.project.rpgplugin.util.ItemKeys;
import com.project.rpgplugin.util.Text;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.user.SkillsUser;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

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
                case 18 -> buyMayhemCleanse(run);
                case 22 -> buyAuraTraining(run);
                case 20, 26 -> new HubMenu(player, plugin, plugin.getRunManager(),
                    plugin.getCardRegistry(), plugin.getRunManager().statService());
            }
        });
    }

    private void build() {
        fillBackground();

        // Row 0: gradient header
        ItemStack corner = decorativePane(Material.GOLD_NUGGET, "<gold>Pao em Lata");
        setItem(0, corner);
        for (int i = 1; i < 8; i++) {
            setItem(i, decorativePane(
                i < 4 ? Material.ORANGE_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE,
                null
            ));
        }
        setItem(8, corner.clone());

        // Row 1: shop items at 10, 12, 14, 16 with colored backgrounds
        Material[] bgColors = {
            Material.ORANGE_STAINED_GLASS_PANE,   // reroll
            Material.LIME_STAINED_GLASS_PANE,      // draft
            Material.CYAN_STAINED_GLASS_PANE,      // absolution
            Material.RED_STAINED_GLASS_PANE,       // beacon
            Material.PURPLE_STAINED_GLASS_PANE     // cleanse
        };
        int[] itemSlots = {10, 12, 14, 16, 18};
        for (int i = 0; i < 5; i++) {
            int bgSlot = itemSlots[i] - 1;
            setItem(bgSlot, decorativePane(bgColors[i], null));
            setItem(bgSlot + 1, decorativePane(bgColors[i], null));
        }

        setItem(21, decorativePane(Material.LIME_STAINED_GLASS_PANE, null));
        setItem(23, decorativePane(Material.LIME_STAINED_GLASS_PANE, null));

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

        setItem(18, buildShopItem(Material.HEART_OF_THE_SEA,
                "<dark_purple><bold>\uD83D\uDD25 Purificacao do Mundo",
                List.of(
                        "<gray>Custo: <yellow>30 niveis</yellow></gray>",
                        "<gray>Remove <red>TODOS</red> os efeitos mayhem do mundo.</gray>",
                        "<dark_gray>A calma retorna... ate o proximo caos.</dark_gray>"
                )));

        setItem(22, buildShopItem(Material.EXPERIENCE_BOTTLE,
                "<green><bold>\uD83D\uDCD8 Treinamento Acelerado",
                List.of(
                        "<gray>Custo: <yellow>3 niveis</yellow></gray>",
                        "<gray>Ganha XP em <yellow>TODAS</yellow> as habilidades AuraSkills</gray>",
                        "<dark_gray>Requer AuraSkills instalado.</dark_gray>"
                )));

        // Row 2: footer
        ItemStack backBtn = new ItemStack(Material.GOLD_NUGGET);
        var backMeta = backBtn.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(Text.mm("<gold>\uD83C\uDF7C Menu Principal"));
            backBtn.setItemMeta(backMeta);
        }
        setItem(20, backBtn);

        ItemStack footer = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        var footerMeta = footer.getItemMeta();
        if (footerMeta != null) {
            footerMeta.displayName(Text.mm("<dark_gray>Clique em um item para comprar"));
            footer.setItemMeta(footerMeta);
        }
        for (int i = 21; i < 26; i++) {
            setItem(i, footer);
        }
        setItem(26, backBtn.clone());
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

    private void buyMayhemCleanse(RunState run) {
        if (!checkLevels(30)) return;
        player.setLevel(player.getLevel() - 30);
        plugin.getMayhemService().clear(player, run);
        successSound();
        player.sendActionBar(Text.mm("<dark_purple>\uD83D\uDD25 O caos foi purificado do mundo!</dark_purple>"));
    }

    private void buyBeacon(RunState run) {
        if (!checkLevels(15)) return;
        player.setLevel(player.getLevel() - 15);
        successSound();

        ItemStack beacon = createBeaconItem();
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(beacon);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItem(player.getLocation(), beacon);
            player.sendActionBar(Text.mm("<red>Inventario cheio! Sinalizador caiu no chao."));
        } else {
            player.sendActionBar(Text.mm("<red>\uD83D\uDC51 Sinalizador do Chefe adicionado ao inventario!"));
        }
    }

    private void buyAuraTraining(RunState run) {
        if (!checkLevels(3)) return;

        var integration = plugin.getAuraSkillsIntegration();
        if (!integration.isEnabled()) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendActionBar(Text.mm("<red>AuraSkills nao esta instalado!"));
            return;
        }

        player.setLevel(player.getLevel() - 3);

        try {
            AuraSkillsApi api = AuraSkillsApi.get();
            SkillsUser user = api.getUser(player.getUniqueId());
            double xpAmount = 100;

            for (Skills skill : Skills.values()) {
                double current = user.getSkillXp(skill);
                user.setSkillXp(skill, current + xpAmount);
            }

            successSound();
            player.sendActionBar(Text.mm("<green>+100 XP em todas as habilidades AuraSkills!"));
        } catch (Exception e) {
            player.sendActionBar(Text.mm("<red>Erro ao conceder XP."));
        }
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
            if (i != 10 && i != 12 && i != 14 && i != 16 && i != 18 && i != 22) {
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
