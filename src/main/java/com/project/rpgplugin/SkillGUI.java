package com.project.rpgplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.Markers;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.TabGui;
import xyz.xenondevs.invui.item.BoundItem;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.ItemBuilder;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;

public class SkillGUI {

    private final PlayerManager playerManager;

    public SkillGUI(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    public void open(Player player) {
        PagedGui<Item> skillsGui = PagedGui.itemsBuilder()
            .setStructure(
                "x x x x x x x x x",
                "x x x x x x x x x",
                "x x x x x x x x x",
                "# # # < # > # # #"
            )
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .addIngredient('<', createBackButton())
            .addIngredient('>', createForwardButton())
            .addIngredient('#', Item.simple(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("")))
            .setContent(createSkillItems(player, "bronze"))
            .build();

        BoundItem tab0 = BoundItem.tabBuilder()
            .setItemProvider((p, gui) -> {
                boolean selected = gui.getTab() == 0;
                return new ItemBuilder(selected ? Material.GOLD_INGOT : Material.COPPER_INGOT)
                    .setName(selected ? "<gold><bold>Bronze" : "<gray>Bronze")
                    .addLoreLines(selected ? "<green>✔ Ativo" : "<gray>Clique para selecionar");
            })
            .addClickHandler((item, gui, click) -> {
                gui.setTab(0);
                updateSkillsGui(skillsGui, player, "bronze");
            })
            .build();

        BoundItem tab1 = BoundItem.tabBuilder()
            .setItemProvider((p, gui) -> {
                boolean selected = gui.getTab() == 1;
                return new ItemBuilder(selected ? Material.GOLD_INGOT : Material.IRON_INGOT)
                    .setName(selected ? "<gray><bold>Prata" : "<gray>Prata")
                    .addLoreLines(selected ? "<green>✔ Ativo" : "<gray>Clique para selecionar");
            })
            .addClickHandler((item, gui, click) -> {
                gui.setTab(1);
                updateSkillsGui(skillsGui, player, "silver");
            })
            .build();

        BoundItem tab2 = BoundItem.tabBuilder()
            .setItemProvider((p, gui) -> {
                boolean selected = gui.getTab() == 2;
                return new ItemBuilder(selected ? Material.GOLD_INGOT : Material.GOLD_NUGGET)
                    .setName(selected ? "<yellow><bold>Ouro" : "<gray>Ouro")
                    .addLoreLines(selected ? "<green>✔ Ativo" : "<gray>Clique para selecionar");
            })
            .addClickHandler((item, gui, click) -> {
                gui.setTab(2);
                updateSkillsGui(skillsGui, player, "gold");
            })
            .build();

        Item infoItem = createInfoItem(player);
        Item synergyItem = createSynergyItem(player);
        Item difficultyItem = createDifficultyItem(player);

        Gui bronzePreview = createTabPreview(Material.ORANGE_STAINED_GLASS_PANE, "<gold>Bronze", "<gray>Habilidades de tier bronze");
        Gui silverPreview = createTabPreview(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "<gray>Prata", "<gray>Habilidades de tier prata");
        Gui goldPreview = createTabPreview(Material.YELLOW_STAINED_GLASS_PANE, "<yellow>Ouro", "<gray>Habilidades de tier ouro");

        TabGui tabGui = TabGui.builder()
            .setStructure(
                "0 1 2 # # i s d #",
                "x x x x x x x x x",
                "x x x x x x x x x"
            )
            .addIngredient('0', tab0)
            .addIngredient('1', tab1)
            .addIngredient('2', tab2)
            .addIngredient('i', infoItem)
            .addIngredient('s', synergyItem)
            .addIngredient('d', difficultyItem)
            .addIngredient('#', Item.simple(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("")))
            .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
            .setTabs(List.of(bronzePreview, silverPreview, goldPreview))
            .build();

        Window window = Window.builder()
            .setTitle("<dark_gray>RogueLata - Habilidades")
            .setViewer(player)
            .setUpperGui(tabGui)
            .setLowerGui(skillsGui)
            .addOpenHandler(() -> {
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
            })
            .build(player);

        window.open();
    }

    private void updateSkillsGui(PagedGui<Item> gui, Player player, String tier) {
        gui.setContent(createSkillItems(player, tier));
        gui.setPage(0);
        gui.bake();
        gui.notifyWindows();
    }

    private BoundItem createBackButton() {
        return BoundItem.pagedBuilder()
            .setItemProvider((p, gui) -> {
                if (gui.getPage() > 0) {
                    return new ItemBuilder(Material.ARROW)
                        .setName("<yellow>← Anterior")
                        .addLoreLines("<gray>Pagina " + (gui.getPage()) + "<gray>/<green>" + gui.getPageCount());
                }
                return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("");
            })
            .addClickHandler((item, gui, click) -> {
                if (gui.getPage() > 0) gui.setPage(gui.getPage() - 1);
            })
            .build();
    }

    private BoundItem createForwardButton() {
        return BoundItem.pagedBuilder()
            .setItemProvider((p, gui) -> {
                if (gui.getPage() < gui.getPageCount() - 1) {
                    return new ItemBuilder(Material.ARROW)
                        .setName("<yellow>Proxima →")
                        .addLoreLines("<gray>Pagina " + (gui.getPage() + 2) + "<gray>/<green>" + gui.getPageCount());
                }
                return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("");
            })
            .addClickHandler((item, gui, click) -> {
                if (gui.getPage() < gui.getPageCount() - 1) gui.setPage(gui.getPage() + 1);
            })
            .build();
    }

    private Gui createTabPreview(Material material, String title, String subtitle) {
        return Gui.of(9, 2, Item.simple(
            new ItemBuilder(material).setName(title).addLoreLines(subtitle)
        ));
    }

    private List<Item> createSkillItems(Player player, String tier) {
        List<String> skillKeys;
        switch (tier) {
            case "silver": skillKeys = playerManager.getAllSilverSkills(); break;
            case "gold": skillKeys = playerManager.getAllGoldSkills(); break;
            default: skillKeys = playerManager.getAllBronzeSkills();
        }
        List<String> unlocked = playerManager.getUnlockedSkills(player);
        List<String> equipped = playerManager.getEquippedSkills(player);
        List<Item> items = new ArrayList<>();
        for (String key : skillKeys) {
            items.add(createSkillButton(player, key, unlocked, equipped));
        }
        return items;
    }

    private Item createSkillButton(Player player, String skillKey, List<String> unlocked, List<String> equipped) {
        String tier = playerManager.getSkillTier(skillKey);
        String type = playerManager.getSkillType(skillKey);
        int cost = playerManager.getSkillCost(skillKey);
        String displayName = playerManager.getSkillDisplayName(skillKey);
        String desc = playerManager.getSkillDescription(skillKey);
        boolean isUnlocked = unlocked.contains(skillKey);
        boolean isEquipped = equipped.contains(skillKey);
        boolean isPassive = !playerManager.isSkillEquippable(skillKey);
        Material mat = playerManager.determineSkillMaterial(skillKey);

        String tierColor;
        switch (tier) {
            case "bronze": tierColor = "<gold>"; break;
            case "silver": tierColor = "<gray>"; break;
            case "gold": tierColor = "<yellow>"; break;
            default: tierColor = "<white>";
        }

        String typeColor;
        String typeDisplay;
        switch (type) {
            case "explorer": typeColor = "<aqua>"; typeDisplay = "Explorador"; break;
            case "miner": typeColor = "<gold>"; typeDisplay = "Minerador"; break;
            case "builder": typeColor = "<green>"; typeDisplay = "Construtor"; break;
            default: typeColor = "<gray>"; typeDisplay = type;
        }

        ItemBuilder builder = new ItemBuilder(mat)
            .setName(tierColor + legacyToMiniMessage(displayName));

        builder.addLoreLines("<gray>" + desc);
        builder.addLoreLines("<dark_gray>✦ roguelata/" + skillKey);
        builder.addLoreLines(typeColor + typeDisplay);
        builder.addLoreLines("");

        if (isUnlocked) {
            if (isPassive) {
                builder.addLoreLines("<green>✔ Desbloqueada (Passiva permanente)");
                builder.setGlint(true);
            } else if (isEquipped) {
                builder.addLoreLines("<green>✔ Equipada!");
                builder.addLoreLines("<yellow>Clique para desequipar.");
                builder.setGlint(true);
            } else {
                int tierCount = playerManager.countEquippedByTier(player, tier);
                builder.addLoreLines("<gray>Disponivel (" + tierCount + "/3 no tier)");
                builder.addLoreLines("<yellow>Clique para equipar.");
            }
        } else {
            builder.addLoreLines("<red>✕ Bloqueada");
            builder.addLoreLines("<yellow>Custo: <white>" + cost + " XP");
            builder.addLoreLines("<yellow>Clique para desbloquear.");
        }

        builder.addLoreLines("");
        builder.addLoreLines(typeColor + "✦ " + typeDisplay);

        return Item.builder()
            .setItemProvider(builder)
            .addClickHandler(click -> handleSkillClick(player, skillKey))
            .build();
    }

    private void handleSkillClick(Player player, String skillKey) {
        List<String> unlocked = playerManager.getUnlockedSkills(player);
        List<String> equipped = playerManager.getEquippedSkills(player);
        String tier = playerManager.getSkillTier(skillKey);
        boolean isPassive = !playerManager.isSkillEquippable(skillKey);

        if (!unlocked.contains(skillKey)) {
            int cost = playerManager.getSkillCost(skillKey);
            if (player.getLevel() < cost) {
                player.sendMessage("§cXP insuficiente! Custo: " + cost + " niveis. Voce tem: " + player.getLevel());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return;
            }
            player.setLevel(player.getLevel() - cost);
            playerManager.unlockSkill(player, skillKey);
            RPGPlugin rpg = (RPGPlugin) Bukkit.getPluginManager().getPlugin("RogueLata");
            if (rpg != null && rpg.getAuraSkillsIntegration().isEnabled()) {
                rpg.getAuraSkillsIntegration().syncAuraSkillLevel(player, skillKey, 1);
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage("§a§l[RogueLata] §aHabilidade desbloqueada: " + playerManager.getSkillDisplayName(skillKey) + "! Gastou " + cost + " XP.");
            return;
        }

        if (isPassive) {
            player.sendMessage("§eEsta habilidade e passiva e sempre fica ativa!");
            return;
        }

        if (equipped.contains(skillKey)) {
            playerManager.unequipSkill(player, skillKey);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage("§eHabilidade desequipada: " + playerManager.getSkillDisplayName(skillKey));
            return;
        }

        int tierCount = playerManager.countEquippedByTier(player, tier);
        if (tierCount >= 3) {
            player.sendMessage("§cVoce ja tem 3 habilidades " + tier + " equipadas! Desequipe uma primeiro.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        if (equipped.size() >= 9) {
            player.sendMessage("§cVoce ja tem 9 habilidades equipadas (max)! Desequipe uma primeiro.");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        playerManager.equipSkill(player, skillKey);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        player.sendMessage("§aHabilidade equipada: " + playerManager.getSkillDisplayName(skillKey));
    }

    private Item createInfoItem(Player player) {
        List<String> unlocked = playerManager.getUnlockedSkills(player);
        List<String> equipped = playerManager.getEquippedSkills(player);
        int totalSkills = playerManager.getAllSkillKeys().size();

        ItemBuilder builder = new ItemBuilder(Material.KNOWLEDGE_BOOK)
            .setName("<gold>Suas Habilidades")
            .addLoreLines("<gray>Desbloqueadas: <yellow>" + unlocked.size() + "<gray>/<yellow>" + totalSkills)
            .addLoreLines("<gray>Equipadas: <green>" + equipped.size() + "<gray>/<green>9")
            .addLoreLines("");

        if (!equipped.isEmpty()) {
            builder.addLoreLines("<yellow>Equipadas:");
            for (String eq : equipped) {
                builder.addLoreLines(" <green>- " + playerManager.getSkillDisplayName(eq));
            }
        } else {
            builder.addLoreLines("<red>Nenhuma equipada.");
        }

        return Item.simple(builder);
    }

    private Item createSynergyItem(Player player) {
        int explorer = playerManager.getSkillCountByType(player, "explorer");
        int miner = playerManager.getSkillCountByType(player, "miner");
        int builder = playerManager.getSkillCountByType(player, "builder");

        Material mat;
        if (explorer >= 4 || miner >= 4 || builder >= 4) {
            mat = Material.NETHER_STAR;
        } else {
            mat = Material.ENDER_PEARL;
        }

        ItemBuilder itembuilder = new ItemBuilder(mat)
            .setName("<light_purple>Sinergias Ativas")
            .addLoreLines("<gray>4+ habilidades do mesmo tipo")
            .addLoreLines("<gray>ativam passiva global:")
            .addLoreLines("");

        String explorerStatus = explorer >= 4 ? "<green>✔ Speed I" : "<gray>" + explorer + "/4";
        String minerStatus = miner >= 4 ? "<green>✔ Haste I" : "<gray>" + miner + "/4";
        String builderStatus = builder >= 4 ? "<green>✔ Regen I" : "<gray>" + builder + "/4";

        itembuilder.addLoreLines("<aqua>Explorador: " + explorerStatus);
        itembuilder.addLoreLines("<gold>Minerador: " + minerStatus);
        itembuilder.addLoreLines("<green>Construtor: " + builderStatus);

        return Item.simple(itembuilder);
    }

    private Item createDifficultyItem(Player player) {
        int count = playerManager.getTotalUnlockedCount(player);
        double dmgMult = playerManager.getDifficultyDamageMultiplier(player);
        double hungerMult = playerManager.getDifficultyHungerMultiplier(player);

        ItemBuilder builder = new ItemBuilder(Material.WITHER_SKELETON_SKULL)
            .setName("<red>Dificuldade Mundial")
            .addLoreLines("<gray>Quanto mais habilidades,")
            .addLoreLines("<gray>maior o desafio!")
            .addLoreLines("")
            .addLoreLines("<gray>Dano recebido: <red>x" + String.format("%.2f", dmgMult))
            .addLoreLines("<gray>Fome: <red>x" + String.format("%.2f", hungerMult))
            .addLoreLines("")
            .addLoreLines("<dark_gray>" + count + " habilidades desbloqueadas");

        return Item.simple(builder);
    }

    private static String legacyToMiniMessage(String legacy) {
        return legacy
            .replace("§0", "<black>")
            .replace("§1", "<dark_blue>")
            .replace("§2", "<dark_green>")
            .replace("§3", "<dark_aqua>")
            .replace("§4", "<dark_red>")
            .replace("§5", "<dark_purple>")
            .replace("§6", "<gold>")
            .replace("§7", "<gray>")
            .replace("§8", "<dark_gray>")
            .replace("§9", "<blue>")
            .replace("§a", "<green>")
            .replace("§b", "<aqua>")
            .replace("§c", "<red>")
            .replace("§d", "<light_purple>")
            .replace("§e", "<yellow>")
            .replace("§f", "<white>")
            .replace("§k", "<obf>")
            .replace("§l", "<bold>")
            .replace("§m", "<strike>")
            .replace("§n", "<u>")
            .replace("§o", "<i>")
            .replace("§r", "<reset>");
    }
}
