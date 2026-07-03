package com.project.rpgplugin.core.run;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.card.Card;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.StatService;
import com.project.rpgplugin.core.mayhem.MayhemService;
import com.project.rpgplugin.util.ItemKeys;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ResetService {

    private final RPGPlugin plugin;
    private final CardRegistry cardRegistry;
    private final StatService statService;
    private final MayhemService mayhemService;
    private final SpawnResolver spawnResolver;

    public ResetService(RPGPlugin plugin, CardRegistry cardRegistry, StatService statService, MayhemService mayhemService, SpawnResolver spawnResolver) {
        this.plugin = plugin;
        this.cardRegistry = cardRegistry;
        this.statService = statService;
        this.mayhemService = mayhemService;
        this.spawnResolver = spawnResolver;
    }

    public void fullReset(Player p, RunState run) {
        // 1. Remove all cards: call onRemove for each
        for (String cardId : run.ownedCards()) {
            Card c = cardRegistry.byId(cardId).orElse(null);
            if (c != null) c.onRemove(p, run);
        }
        run.ownedCards().clear();
        run.ownedAbilities().clear();

        // T9.5: Clean leaked state from SkillServices
        plugin.getSkillServices().reinforcedBlocks().clear();
        plugin.getSkillServices().clearPlayerCooldowns(p.getUniqueId());

        // 2. Stats: recompute with zero cards → baseline
        statService.resetToBaseline(p);
        double maxHp = p.getAttribute(Attribute.MAX_HEALTH) != null
            ? p.getAttribute(Attribute.MAX_HEALTH).getValue() : 20.0;
        if (p.getHealth() > maxHp) p.setHealth(maxHp);

        // 3. Mayhem: clear active modifiers
        mayhemService.clear(p, run);

        // 4. AuraSkills: reset if bridge available
        if (plugin.getAuraSkillsIntegration().isEnabled()) {
            plugin.getAuraSkillsIntegration().resetAllAuraSkills(p);
        }

        // 5. Level/XP vanilla → 0
        p.setLevel(0);
        p.setExp(0f);

        // 6. Remove tagged skill items from inventory
        removeTaggedSkillItems(p);

        // 7. Reset run progression
        run.reset();

        // 8. Clear plugin potion effects
        p.getActivePotionEffects().stream()
            .filter(e -> e.getType().getName().startsWith("roguelata_"))
            .toList()
            .forEach(e -> p.removePotionEffect(e.getType()));

        // 9. Teleport to spawn
        p.teleport(spawnResolver.resolve(p));

        // 10. Ensure RPG book
        ensureRpgBook(p);

        p.sendMessage(com.project.rpgplugin.util.Text.mm("<red><bold>RUN ENCERRADA"));
        p.sendMessage(com.project.rpgplugin.util.Text.mm("<gray>Todos os poderes foram perdidos. Uma nova run começa!"));
    }

    private void removeTaggedSkillItems(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                var meta = item.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    for (String line : meta.getLore()) {
                        if (line.contains("Skill Item:")) {
                            p.getInventory().remove(item);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void ensureRpgBook(Player p) {
        boolean hasBook = false;
        for (ItemStack invItem : p.getInventory().getContents()) {
            if (ItemKeys.isRpgBook(invItem)) {
                hasBook = true;
                break;
            }
        }
        if (!hasBook) {
            p.getInventory().addItem(plugin.createRpgBook());
        }
    }
}
