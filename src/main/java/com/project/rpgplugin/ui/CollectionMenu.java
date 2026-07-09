package com.project.rpgplugin.ui;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardKind;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.CardTag;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.card.StatService;
import com.project.rpgplugin.ui.menu.Menu;
import com.project.rpgplugin.ui.menu.MenuHolder;
import com.project.rpgplugin.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CollectionMenu extends Menu {

    private static final int SIZE = 54;
    private static final int CARDS_PER_PAGE = 36;

    private static final int SLOT_INFO = 8;
    private static final int SLOT_PREV = 48;
    private static final int SLOT_PAGE = 49;
    private static final int SLOT_NEXT = 50;
    private static final int SLOT_COUNT = 53;
    private static final int SLOT_MAYHEM = 45;
    private static final int SLOT_HUB = 46;

    private final RPGPlugin plugin;
    private final Player player;
    private final RunState run;
    private final CardRegistry cardRegistry;
    private final StatService statService;
    private final Map<Integer, String> slotToCard = new HashMap<>();

    private final CardTag activeTag;
    private final CardKind activeKind;
    private final int page;

    public CollectionMenu(Player p, RunState run, CardRegistry cardRegistry, StatService statService) {
        this(null, p, run, cardRegistry, statService, null, null, 0);
    }

    public CollectionMenu(RPGPlugin plugin, Player p, RunState run, CardRegistry cardRegistry, StatService statService) {
        this(plugin, p, run, cardRegistry, statService, null, null, 0);
    }

    private CollectionMenu(RPGPlugin plugin, Player p, RunState run, CardRegistry cardRegistry, StatService statService,
                           CardTag activeTag, CardKind activeKind, int page) {
        super(SIZE, "<dark_purple><bold>Sua Colecao");
        this.plugin = plugin;
        this.player = p;
        this.run = run;
        this.cardRegistry = cardRegistry;
        this.statService = statService;
        this.activeTag = activeTag;
        this.activeKind = activeKind;
        this.page = page;

        new MenuHolder(this);
        fillBackground();
        buildFilterBar();
        buildCardGrid();
        buildNavBar();
        p.openInventory(getInventory());
    }

    // ─── Filter bar (row 0) ───────────────────────────────────────

    private void buildFilterBar() {
        // Tag filters
        setItem(0, filterItem(Material.WHITE_STAINED_GLASS_PANE, "<white><bold>Todas", null, null));
        setItem(1, filterItem(Material.LIME_STAINED_GLASS_PANE, "<lime><bold>Explorador", CardTag.EXPLORER, null));
        setItem(2, filterItem(Material.GOLD_BLOCK, "<yellow><bold>Minerador", CardTag.MINER, null));
        setItem(3, filterItem(Material.GREEN_STAINED_GLASS_PANE, "<green><bold>Construtor", CardTag.BUILDER, null));

        // Kind filters
        setItem(4, filterItem(Material.MAGENTA_STAINED_GLASS_PANE, "<light_purple><bold>Habilidades", null, CardKind.ABILITY));
        setItem(5, filterItem(Material.CYAN_STAINED_GLASS_PANE, "<aqua><bold>Aprimoramentos", null, CardKind.AUGMENT));

        // Spacer
        ItemStack spacer = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        var spacerMeta = spacer.getItemMeta();
        if (spacerMeta != null) {
            spacerMeta.displayName(Component.text("\u00a7r"));
            spacer.setItemMeta(spacerMeta);
        }
        setItem(6, spacer);
        setItem(7, spacer);

        // Info book
        setItem(SLOT_INFO, buildInfoBook());
    }

    private ItemStack filterItem(Material material, String label, CardTag tag, CardKind kind) {
        boolean active = (this.activeTag == tag && this.activeKind == kind)
                      || (tag == null && this.activeTag == null && kind == null && this.activeKind == null)
                      || (kind != null && this.activeKind == kind)
                      || (tag != null && this.activeTag == tag);
        Material mat = active ? Material.PLAYER_HEAD : material;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.mm(label));
            List<String> lore = new ArrayList<>();
            if (active) {
                lore.add("<green>[V] Ativo");
            } else {
                lore.add("<gray>Clique para filtrar");
            }
            meta.lore(lore.stream().map(Text::mm).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    // ─── Card grid (rows 1-4, slots 9-44) ────────────────────────

    private void buildCardGrid() {
        slotToCard.clear();

        List<String> owned = new ArrayList<>(run.ownedCards());
        owned.sort(Comparator.naturalOrder());

        Stream<String> filtered = owned.stream();
        if (activeTag != null) {
            filtered = filtered.filter(id -> {
                Card c = cardRegistry.byId(id).orElse(null);
                return c != null && c.tags().contains(activeTag);
            });
        }
        if (activeKind != null) {
            filtered = filtered.filter(id -> {
                Card c = cardRegistry.byId(id).orElse(null);
                return c != null && c.kind() == activeKind;
            });
        }
        List<String> filteredCards = filtered.toList();

        int totalCards = filteredCards.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCards / CARDS_PER_PAGE));
        int safePage = Math.min(page, totalPages - 1);
        int start = safePage * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, totalCards);
        List<String> pageCards = filteredCards.subList(start, end);

        int slot = 9;
        for (String cardId : pageCards) {
            slotToCard.put(slot, cardId);
            int count = run.cardCount(cardId);
            boolean toggled = run.isToggledOn(cardId);
            Card card = cardRegistry.byId(cardId).orElse(null);

            ItemStack icon = new ItemStack(toggled ? Material.ENCHANTED_BOOK : Material.BOOK);
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                String displayName = cardId.replace("_", " ");
                meta.displayName(Text.mm((toggled ? "<green>" : "<gray>") + displayName));
                List<String> lore = new ArrayList<>();
                if (count > 1) lore.add("<gray>" + count + " pilhas");
                if (card != null) {
                    lore.add("<dark_gray>" + card.kind().name() + " [" + String.join(" ", card.tags().stream().map(t -> t.name()).toList()) + "]");
                    List<String> desc = card.lore(run);
                    if (!desc.isEmpty()) {
                        lore.addAll(desc);
                        lore.add("");
                    }
                }
                if (toggled) {
                    lore.add("<yellow>Clique para desativar");
                } else {
                    lore.add("<red>DESATIVADO");
                    lore.add("<yellow>Clique para ativar");
                }
                meta.lore(lore.stream().map(Text::mm).toList());
                icon.setItemMeta(meta);
            }
            setItem(slot, icon);
            slot++;
        }

        // Fill remaining card slots with black glass
        ItemStack empty = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        var emptyMeta = empty.getItemMeta();
        if (emptyMeta != null) {
            emptyMeta.displayName(Component.text("\u00a7r"));
            empty.setItemMeta(emptyMeta);
        }
        while (slot < 45) {
            setItem(slot, empty);
            slot++;
        }
    }

    // ─── Nav bar (row 5, slots 45-53) ─────────────────────────────

    private void buildNavBar() {
        // Mayhem indicator
        if (!run.activeModifiers().isEmpty()) {
            ItemStack mayhemIcon = new ItemStack(Material.TOTEM_OF_UNDYING);
            var mayhemMeta = mayhemIcon.getItemMeta();
            if (mayhemMeta != null) {
                mayhemMeta.displayName(Text.mm("<red><bold>Mayhem Ativos!"));
                List<String> mayhemLore = new ArrayList<>();
                for (String mod : run.activeModifiers()) {
                    mayhemLore.add("<red>  - " + mod.replace("_", " "));
                }
                mayhemMeta.lore(mayhemLore.stream().map(Text::mm).toList());
                mayhemIcon.setItemMeta(mayhemMeta);
            }
            setItem(SLOT_MAYHEM, mayhemIcon);
        } else {
            ItemStack empty = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            var emptyMeta = empty.getItemMeta();
            if (emptyMeta != null) {
                emptyMeta.displayName(Component.text("\u00a7r"));
                empty.setItemMeta(emptyMeta);
            }
            setItem(SLOT_MAYHEM, empty);
        }

        // Hub button
        ItemStack hubBtn = new ItemStack(Material.GOLD_NUGGET);
        var hubMeta = hubBtn.getItemMeta();
        if (hubMeta != null) {
            hubMeta.displayName(Text.mm("<gold>Menu Principal"));
            hubBtn.setItemMeta(hubMeta);
        }
        setItem(SLOT_HUB, hubBtn);

        ItemStack spacer = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        var spacerMeta = spacer.getItemMeta();
        if (spacerMeta != null) {
            spacerMeta.displayName(Component.text("\u00a7r"));
            spacer.setItemMeta(spacerMeta);
        }
        setItem(47, spacer);
        setItem(51, spacer);
        setItem(52, spacer);

        // Pagination
        int totalCards = countFiltered();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCards / CARDS_PER_PAGE));
        int safePage = Math.min(page, totalPages - 1);

        ItemStack prevItem = new ItemStack(safePage > 0 ? Material.SPECTRAL_ARROW : Material.BARRIER);
        var prevMeta = prevItem.getItemMeta();
        if (prevMeta != null) {
            prevMeta.displayName(Text.mm(safePage > 0 ? "<yellow>Pagina anterior" : "<gray>Primeira pagina"));
            prevItem.setItemMeta(prevMeta);
        }
        setItem(SLOT_PREV, prevItem);

        ItemStack pageItem = new ItemStack(Material.ARROW);
        var pageMeta = pageItem.getItemMeta();
        if (pageMeta != null) {
            pageMeta.displayName(Text.mm("<gold>Pagina " + (safePage + 1) + "/" + totalPages));
            pageItem.setItemMeta(pageMeta);
        }
        setItem(SLOT_PAGE, pageItem);

        ItemStack nextItem = new ItemStack(safePage < totalPages - 1 ? Material.SPECTRAL_ARROW : Material.BARRIER);
        var nextMeta = nextItem.getItemMeta();
        if (nextMeta != null) {
            nextMeta.displayName(Text.mm(safePage < totalPages - 1 ? "<yellow>Proxima pagina" : "<gray>Ultima pagina"));
            nextItem.setItemMeta(nextMeta);
        }
        setItem(SLOT_NEXT, nextItem);

        // Card count
        ItemStack countItem = new ItemStack(Material.PAPER);
        var countMeta = countItem.getItemMeta();
        if (countMeta != null) {
            countMeta.displayName(Text.mm("<gray>Total: " + totalCards + " carta(s)"));
            countItem.setItemMeta(countMeta);
        }
        setItem(SLOT_COUNT, countItem);
    }

    private int countFiltered() {
        Stream<String> s = run.ownedCards().stream();
        if (activeTag != null) {
            s = s.filter(id -> {
                Card c = cardRegistry.byId(id).orElse(null);
                return c != null && c.tags().contains(activeTag);
            });
        }
        if (activeKind != null) {
            s = s.filter(id -> {
                Card c = cardRegistry.byId(id).orElse(null);
                return c != null && c.kind() == activeKind;
            });
        }
        return (int) s.count();
    }

    // ─── Info book ────────────────────────────────────────────────

    private ItemStack buildInfoBook() {
        ItemStack infoBook = new ItemStack(Material.KNOWLEDGE_BOOK);
        var infoMeta = infoBook.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(Text.mm("<gold><bold>Resumo da Run"));
            List<String> infoLore = new ArrayList<>();
            infoLore.add("<gray>Nivel: <white>" + run.level());
            infoLore.add("<gray>Cartas: <white>" + run.ownedCards().size());
            infoLore.add("<gray>Milestones: <white>" + run.milestonesReached());
            infoLore.add("<gray>Distancia: <white>" + run.blocksWalked());
            if (run.extraDraftSlots() > 0) {
                infoLore.add("<green>Draft Extra: " + run.extraDraftSlots() + " disponivel(eis)");
            }
            if (run.skipHealthBonus() > 0) {
                infoLore.add("<green>Vida Extra: +" + ((int) run.skipHealthBonus() / 2) + " coracoes");
            }
            if (!run.multipliers().isEmpty()) {
                infoLore.add("");
                infoLore.add("<gold>Multiplicadores:");
                for (Map.Entry<String, Double> e : run.multipliers().entrySet()) {
                    infoLore.add("<gray>  " + e.getKey() + ": <white>+" + String.format("%.0f%%", e.getValue() * 100));
                }
            }
            infoMeta.lore(infoLore.stream().map(Text::mm).toList());
            infoBook.setItemMeta(infoMeta);
        }
        return infoBook;
    }

    // ─── Background ───────────────────────────────────────────────

    private void fillBackground() {
        ItemStack bg = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        var bgMeta = bg.getItemMeta();
        if (bgMeta != null) {
            bgMeta.displayName(Component.text("\u00a7r"));
            bg.setItemMeta(bgMeta);
        }
        for (int i = 0; i < SIZE; i++) {
            setItem(i, bg);
        }

        ItemStack border = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        var bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.displayName(Component.text("\u00a7r"));
            border.setItemMeta(bMeta);
        }
        for (int col = 0; col < 9; col++) {
            setItem(col, border);
            setItem(SIZE - 9 + col, border);
        }
        for (int row = 0; row < SIZE; row += 9) {
            setItem(row, border);
            setItem(row + 8, border);
        }
    }

    // ─── Click handler ────────────────────────────────────────────

    @Override
    public void handleClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();

        // Filter bar (slots 0-5)
        CardTag newTag = activeTag;
        CardKind newKind = activeKind;
        switch (slot) {
            case 0 -> { newTag = null; newKind = null; }
            case 1 -> { newTag = CardTag.EXPLORER; newKind = null; }
            case 2 -> { newTag = CardTag.MINER; newKind = null; }
            case 3 -> { newTag = CardTag.BUILDER; newKind = null; }
            case 4 -> { newTag = null; newKind = CardKind.ABILITY; }
            case 5 -> { newTag = null; newKind = CardKind.AUGMENT; }
        }
        if (slot >= 0 && slot <= 5) {
            new CollectionMenu(plugin, player, run, cardRegistry, statService, newTag, newKind, 0);
            return;
        }

        // Hub / back
        if (slot == SLOT_HUB && plugin != null) {
            new HubMenu(player, plugin, plugin.getRunManager(), cardRegistry, statService);
            return;
        }

        // Navigation
        if (slot == SLOT_PREV) {
            int totalCards = countFiltered();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalCards / CARDS_PER_PAGE));
            if (page > 0) {
                new CollectionMenu(plugin, player, run, cardRegistry, statService, activeTag, activeKind, page - 1);
            }
            return;
        }
        if (slot == SLOT_NEXT) {
            int totalCards = countFiltered();
            int totalPages = Math.max(1, (int) Math.ceil((double) totalCards / CARDS_PER_PAGE));
            if (page < totalPages - 1) {
                new CollectionMenu(plugin, player, run, cardRegistry, statService, activeTag, activeKind, page + 1);
            }
            return;
        }

        // Card toggle
        String cardId = slotToCard.get(slot);
        if (cardId == null) return;

        boolean wasOn = run.isToggledOn(cardId);
        run.toggle(cardId);
        Card card = cardRegistry.byId(cardId).orElse(null);
        if (card != null) {
            if (wasOn) {
                card.toggleOff(player, run);
            } else {
                card.toggleOn(player, run);
            }
        }
        statService.recompute(player, run);
        new CollectionMenu(plugin, player, run, cardRegistry, statService, activeTag, activeKind, page);
    }
}
