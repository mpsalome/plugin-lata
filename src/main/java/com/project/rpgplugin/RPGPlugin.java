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

    @Override
    public void onEnable() {
        // Save default config.yml
        saveDefaultConfig();

        // Initialize Managers and Listeners
        this.playerManager = new PlayerManager(this);
        this.classListeners = new ClassListeners(this, playerManager);

        // Register Event Listeners
        getServer().getPluginManager().registerEvents(classListeners, this);

        // Register Commands
        getCommand("class").setExecutor(this);
        getCommand("rpg").setExecutor(this);

        getLogger().info("RPG Classes Plugin has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("RPG Classes Plugin has been disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("class")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can choose an RPG class!");
                return true;
            }
            Player player = (Player) sender;
            classListeners.openSelectionGUI(player);
            return true;
        }

        if (command.getName().equalsIgnoreCase("rpg")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("rpg.admin")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to reload the config.");
                    return true;
                }
                reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "[RPGPlugin] Configuration reloaded successfully!");
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
                            "§7Use para abrir o Menu de Classes e Skills!",
                            "§eClique com o botão direito para abrir."
                        ));
                        rpgBook.setItemMeta(meta);
                    }
                    player.getInventory().addItem(rpgBook);
                    player.sendMessage(ChatColor.GREEN + "[RPGPlugin] Você recebeu o Livro de RPG fixo!");
                    return true;
                }
            }

            // Command /rpg or /rpg help - show beautiful in-game guide
            sender.sendMessage(ChatColor.GOLD + "=================== RPG CLASSES HELP ===================");
            sender.sendMessage(ChatColor.YELLOW + "/class " + ChatColor.WHITE + "- Abre o menu visual para escolher sua classe.");
            sender.sendMessage(ChatColor.YELLOW + "/rpg " + ChatColor.WHITE + "- Recebe o Livro de RPG se não tiver, ou abre o menu.");
            sender.sendMessage(ChatColor.YELLOW + "/rpg reload " + ChatColor.WHITE + "- Recarrega as configurações do plugin (Admin).");
            sender.sendMessage(ChatColor.YELLOW + "/rpg help " + ChatColor.WHITE + "- Exibe esta lista de ajuda com todos os poderes.");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.AQUA + "--- CLASSE: EXPLORER ---");
            sender.sendMessage(ChatColor.GRAY + "• Dano de Queda Reduzido (Passiva): Toma menos dano de queda.");
            sender.sendMessage(ChatColor.GRAY + "• Speed & Invisibility (Ativa): Use uma FLOR (Poppy) para ganhar velocidade e invisibilidade.");
            sender.sendMessage(ChatColor.GRAY + "• Hidratação (Passiva): Beber garrafa de água recupera fome.");
            sender.sendMessage(ChatColor.GRAY + "• Jump Boost (Passiva): Ganha efeito de pulo automático ao atingir o Nível 10+.");
            sender.sendMessage(ChatColor.GRAY + "• Respiração de Lapis (Ativa): Consuma LAPIS LAZULI para ganhar Respiração Debaixo d'água III.");
            sender.sendMessage(ChatColor.GRAY + "• Retorno do Dragão (Ativa): Use DRAGON'S BREATH para teleportar instantaneamente ao Spawn.");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GOLD + "--- CLASSE: MINER ---");
            sender.sendMessage(ChatColor.GRAY + "• Dieta de Carvão (Ativa): Coma CARVÃO/CARVÃO VEGETAL para recuperar fome.");
            sender.sendMessage(ChatColor.GRAY + "• Visão Noturna (Passiva): Ganha visão noturna infinita automática ao atingir o nível configurado.");
            sender.sendMessage(ChatColor.GRAY + "• Corrida do Ouro (Ativa): Clique com o botão direito usando PICARETA segurando BARRA DE OURO para ativar Haste rápido.");
            sender.sendMessage("");
            sender.sendMessage(ChatColor.GREEN + "--- CLASSE: BUILDER ---");
            sender.sendMessage(ChatColor.GRAY + "• Banquete de Folhas (Ativa): Coma BLOCOS DE FOLHAS diretamente do inventário para restaurar fome.");
            sender.sendMessage(ChatColor.GRAY + "• Silk Touch com a Mão (Passiva): Quebre blocos com a mão vazia para obter Silk Touch (coleta blocos frágeis).");
            sender.sendMessage(ChatColor.GRAY + "• Graça de Pena (Ativa): Clique com o botão direito segurando PENA (Nível configurado+) para ganhar Jump Boost e Slow Falling.");
            sender.sendMessage(ChatColor.GOLD + "=======================================================");
            return true;
        }

        return false;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
