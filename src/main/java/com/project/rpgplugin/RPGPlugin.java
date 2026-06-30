package com.project.rpgplugin;

import com.project.rpgplugin.util.ItemKeys;
import com.project.rpgplugin.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class RPGPlugin extends JavaPlugin implements CommandExecutor {

    private PlayerManager playerManager;
    private ClassListeners classListeners;
    private AuraSkillsIntegration auraSkillsIntegration;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ItemKeys.init(this);

        this.playerManager = new PlayerManager(this);
        this.auraSkillsIntegration = new AuraSkillsIntegration(this, playerManager);
        this.classListeners = new ClassListeners(this, playerManager, auraSkillsIntegration);

        getServer().getPluginManager().registerEvents(classListeners, this);

        getCommand("skills").setExecutor(this);
        getCommand("rpg").setExecutor(this);

        if (auraSkillsIntegration.isEnabled()) {
            getLogger().info("RogueLata + AuraSkills integrado com sucesso!");
        } else {
            getLogger().info("RogueLata ativado em modo standalone (sem AuraSkills).");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("RogueLata Plugin desativado.");
    }

    public AuraSkillsIntegration getAuraSkillsIntegration() {
        return auraSkillsIntegration;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("skills")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("Apenas jogadores podem usar /skills!").color(NamedTextColor.RED));
                return true;
            }
            Player player = (Player) sender;
            classListeners.openSelectionGUI(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("rpg")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("rpg.admin")) {
                    sender.sendMessage(Component.text("Voce nao tem permissao para recarregar.").color(NamedTextColor.RED));
                    return true;
                }
                reloadConfig();
                sender.sendMessage(Component.text("[RogueLata] Configuracao recarregada!").color(NamedTextColor.GREEN));
                return true;
            }

            if (args.length > 0 && args[0].equalsIgnoreCase("reset") && sender.hasPermission("rpg.admin")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Component.text("Apenas jogadores podem resetar.").color(NamedTextColor.RED));
                    return true;
                }
                Player player = (Player) sender;
                playerManager.clearPlayerData(player);
                player.sendMessage(Component.text("[RogueLata] Todos os dados RPG foram resetados!").color(NamedTextColor.RED));
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                boolean hasBook = false;
                for (ItemStack invItem : player.getInventory().getContents()) {
                    if (ItemKeys.isRpgBook(invItem)) {
                        hasBook = true;
                        break;
                    }
                }
                if (!hasBook) {
                    ItemStack rpgBook = createRpgBook();
                    player.getInventory().addItem(rpgBook);
                    player.sendMessage(Component.text("[RogueLata] Voce recebeu o Livro de RPG!").color(NamedTextColor.GREEN));
                    return true;
                }
            }

            sender.sendMessage(Component.text("=========== RogueLata HELP ===========").color(NamedTextColor.GOLD));
            sender.sendMessage(Component.text("/skills ").color(NamedTextColor.YELLOW).append(Component.text("- Abre o menu de habilidades (3 tiers).").color(NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("/rpg ").color(NamedTextColor.YELLOW).append(Component.text("- Recebe o Livro de RPG se necessario.").color(NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("/rpg reload ").color(NamedTextColor.YELLOW).append(Component.text("- Recarrega config (Admin).").color(NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("/rpg reset ").color(NamedTextColor.YELLOW).append(Component.text("- Reseta todos os dados RPG (Admin).").color(NamedTextColor.WHITE)));
            sender.sendMessage(Component.empty());
            sender.sendMessage(Component.text("=== MODO ROGUE-LIKE ATIVO ===").color(NamedTextColor.RED));
            sender.sendMessage(Component.text("Ao morrer, voce perde TODAS as habilidades e XP!").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Escolha sabiamente suas 9 habilidades (3 por tier).").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("4+ habilidades do mesmo tipo = SINERGIA PASSIVA!").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("=================================").color(NamedTextColor.GOLD));
            return true;
        }

        return false;
    }

    public ItemStack createRpgBook() {
        ItemStack book = new ItemStack(Material.BOOK, 1);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.mm("<gold><bold>Livro de RPG"));
            meta.lore(List.of(
                    Text.mm("<gray>Use para abrir o Menu de Habilidades!"),
                    Text.mm("<yellow>Clique com o direito para abrir.")
            ));
            meta.getPersistentDataContainer().set(ItemKeys.rpgBook(), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            book.setItemMeta(meta);
        }
        return book;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
