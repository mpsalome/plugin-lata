package com.project.rpgplugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class RPGPlugin extends JavaPlugin implements CommandExecutor {

    private PlayerManager playerManager;
    private ClassListeners classListeners;
    private AuraSkillsIntegration auraSkillsIntegration;

    @Override
    public void onEnable() {
        saveDefaultConfig();

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
                sender.sendMessage(ChatColor.RED + "Apenas jogadores podem usar /skills!");
                return true;
            }
            Player player = (Player) sender;
            classListeners.openSelectionGUI(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("rpg")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("rpg.admin")) {
                    sender.sendMessage(ChatColor.RED + "Voce nao tem permissao para recarregar.");
                    return true;
                }
                reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "[RogueLata] Configuracao recarregada!");
                return true;
            }

            if (args.length > 0 && args[0].equalsIgnoreCase("reset") && sender.hasPermission("rpg.admin")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Apenas jogadores podem resetar.");
                    return true;
                }
                Player player = (Player) sender;
                playerManager.clearPlayerData(player);
                player.sendMessage(ChatColor.RED + "[RogueLata] Todos os dados RPG foram resetados!");
                return true;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                boolean hasBook = false;
                for (ItemStack invItem : player.getInventory().getContents()) {
                    if (invItem != null && invItem.getType() == Material.BOOK) {
                        ItemMeta m = invItem.getItemMeta();
                        if (m != null && m.hasDisplayName() && m.getDisplayName().contains("Livro de RPG")) {
                            hasBook = true;
                            break;
                        }
                    }
                }
                if (!hasBook) {
                    ItemStack rpgBook = new ItemStack(Material.BOOK, 1);
                    ItemMeta meta = rpgBook.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName("§6§lLivro de RPG");
                        meta.setLore(Arrays.asList(
                            "§7Use para abrir o Menu de Habilidades!",
                            "§eClique com o direito para abrir."
                        ));
                        rpgBook.setItemMeta(meta);
                    }
                    player.getInventory().addItem(rpgBook);
                    player.sendMessage(ChatColor.GREEN + "[RogueLata] Voce recebeu o Livro de RPG!");
                    return true;
                }
            }

            sender.sendMessage(ChatColor.GOLD + "=========== RogueLata HELP ===========");
            sender.sendMessage(ChatColor.YELLOW + "/skills " + ChatColor.WHITE + "- Abre o menu de habilidades (3 tiers).");
            sender.sendMessage(ChatColor.YELLOW + "/rpg " + ChatColor.WHITE + "- Recebe o Livro de RPG se necessario.");
            sender.sendMessage(ChatColor.YELLOW + "/rpg reload " + ChatColor.WHITE + "- Recarrega config (Admin).");
            sender.sendMessage(ChatColor.YELLOW + "/rpg reset " + ChatColor.WHITE + "- Reseta todos os dados RPG (Admin).");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.RED + "=== MODO ROGUE-LIKE ATIVO ===");
            sender.sendMessage(ChatColor.GRAY + "Ao morrer, voce perde TODAS as habilidades e XP!");
            sender.sendMessage(ChatColor.GRAY + "Escolha sabiamente suas 9 habilidades (3 por tier).");
            sender.sendMessage(ChatColor.GRAY + "4+ habilidades do mesmo tipo = SINERGIA PASSIVA!");
            sender.sendMessage(ChatColor.GOLD + "=================================");
            return true;
        }

        return false;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
