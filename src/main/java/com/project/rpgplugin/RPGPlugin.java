package com.project.rpgplugin;

import com.project.rpgplugin.command.RecallCommand;
import com.project.rpgplugin.command.RunCommand;
import com.project.rpgplugin.config.MessagesConfig;
import com.project.rpgplugin.config.SkillsConfig;
import com.project.rpgplugin.core.build.SynergyService;
import com.project.rpgplugin.core.card.CardRegistry;
import com.project.rpgplugin.core.card.StatService;
import com.project.rpgplugin.core.card.ability.AbilityCardRegistration;
import com.project.rpgplugin.core.card.augment.AugmentLoader;
import com.project.rpgplugin.core.difficulty.DifficultyService;
import com.project.rpgplugin.core.draft.DraftService;
import com.project.rpgplugin.core.draft.DraftWeighting;
import com.project.rpgplugin.core.mayhem.MayhemConfig;
import com.project.rpgplugin.core.mayhem.MayhemService;
import com.project.rpgplugin.core.mayhem.MilestoneService;
import com.project.rpgplugin.core.mayhem.ModifierRegistration;
import com.project.rpgplugin.core.mayhem.ModifierRegistry;
import com.project.rpgplugin.core.mob.MobSpawnService;
import com.project.rpgplugin.core.progression.DistanceTracker;
import com.project.rpgplugin.core.progression.GateRegistry;
import com.project.rpgplugin.core.progression.RecallProgression;
import com.project.rpgplugin.core.run.ResetService;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.run.SpawnResolver;
import com.project.rpgplugin.core.skill.SkillRegistry;
import com.project.rpgplugin.core.skill.SkillRegistration;
import com.project.rpgplugin.core.skill.SkillServices;
import com.project.rpgplugin.data.CooldownService;
import com.project.rpgplugin.data.PlayerDataStore;
import com.project.rpgplugin.data.YamlDataStore;
import com.project.rpgplugin.listener.AugmentListener;
import com.project.rpgplugin.listener.CombatListener;
import com.project.rpgplugin.listener.MobScalingListener;
import com.project.rpgplugin.listener.PlayerLevelListener;
import com.project.rpgplugin.listener.PlayerLifecycleListener;
import com.project.rpgplugin.listener.RecallListener;
import com.project.rpgplugin.listener.SkillDispatchListener;
import com.project.rpgplugin.task.DistanceTask;
import com.project.rpgplugin.task.PassiveTask;
import com.project.rpgplugin.ui.CollectionMenu;
import com.project.rpgplugin.ui.DraftMenuListener;
import com.project.rpgplugin.ui.HudService;
import com.project.rpgplugin.ui.menu.MenuListener;
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

import java.util.List;

public class RPGPlugin extends JavaPlugin implements CommandExecutor {

    private AuraSkillsIntegration auraSkillsIntegration;
    private MessagesConfig messagesConfig;

    private SkillRegistry skillRegistry;
    private SkillServices skillServices;
    private SkillDispatchListener skillDispatchListener;
    private SkillsConfig skillsConfig;

    // EPIC-2: Card & Draft system
    private CardRegistry cardRegistry;
    private StatService statService;
    private DraftWeighting draftWeighting;
    private DraftService draftService;
    private RunManager runManager;
    private PlayerLevelListener playerLevelListener;
    private DraftMenuListener draftMenuListener;

    // EPIC-4: Mayhem modifiers
    private ModifierRegistry modifierRegistry;
    private MayhemConfig mayhemConfig;
    private MilestoneService milestoneService;
    private MayhemService mayhemService;

    // EPIC-3: Run cycle
    private SpawnResolver spawnResolver;
    private ResetService resetService;
    private PlayerLifecycleListener playerLifecycleListener;
    private RunCommand runCommand;

    // EPIC-5: Gameplay triggers
    private GateRegistry gateRegistry;
    private DistanceTracker distanceTracker;
    private DistanceTask distanceTask;
    private RecallProgression recallProgression;
    private RecallCommand recallCommand;
    private RecallListener recallListener;

    // EPIC-7: Difficulty & Combat
    private DifficultyService difficultyService;
    private MobScalingListener mobScalingListener;
    private CombatListener combatListener;
    private MobSpawnService mobSpawnService;

    // EPIC-10: Augment handlers
    private AugmentListener augmentListener;

    // EPIC-8: Menus & HUD
    private HudService hudService;
    private MenuListener menuListener;

    // EPIC-9: Persistence & tasks
    private PlayerDataStore dataStore;
    private CooldownService cooldownService;
    private PassiveTask passiveTask;

    // EPIC-10: Synergies
    private SynergyService synergyService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        ItemKeys.init(this);
        this.cooldownService = new CooldownService();
        this.messagesConfig = new MessagesConfig(this);

        this.gateRegistry = new GateRegistry(this);
        this.auraSkillsIntegration = new AuraSkillsIntegration(this, gateRegistry);

        // EPIC-1: Data-driven skill architecture
        this.skillsConfig = new SkillsConfig(this);
        this.skillServices = new SkillServices(this);
        this.skillRegistry = new SkillRegistry();
        SkillRegistration.registerAll(skillRegistry, skillServices);

        // EPIC-4: Mayhem system (needed by EPIC-3)
        this.modifierRegistry = new ModifierRegistry();
        ModifierRegistration.registerAll(modifierRegistry);
        this.mayhemConfig = new MayhemConfig(this);
        this.milestoneService = new MilestoneService(mayhemConfig);
        this.mayhemService = new MayhemService(this, modifierRegistry, milestoneService, mayhemConfig);

        // EPIC-2: Card system
        this.cardRegistry = new CardRegistry();
        this.statService = new StatService();
        this.spawnResolver = new SpawnResolver(this);
        this.resetService = new ResetService(this, cardRegistry, statService, mayhemService, spawnResolver);
        this.runManager = new RunManager(this, cardRegistry, statService, resetService, mayhemService);
        this.draftWeighting = new DraftWeighting(this);

        // Register ability cards (wrapping EPIC-1 skills)
        AbilityCardRegistration.registerAll(cardRegistry, skillRegistry);

        // Load augment cards from augments.yml
        AugmentLoader.load(this, cardRegistry);

        this.draftService = new DraftService(cardRegistry, draftWeighting, statService, runManager, auraSkillsIntegration);

        // EPIC-2 listeners
        this.playerLevelListener = new PlayerLevelListener(runManager, draftService, draftWeighting, this, milestoneService);
        this.draftMenuListener = new DraftMenuListener(runManager, draftService, playerLevelListener);

        // EPIC-3: Run lifecycle
        this.playerLifecycleListener = new PlayerLifecycleListener(runManager);
        this.runCommand = new RunCommand(runManager);

        // EPIC-5: Gameplay triggers
        this.distanceTracker = new DistanceTracker(runManager);
        this.distanceTask = new DistanceTask();
        this.recallProgression = new RecallProgression(this, runManager, spawnResolver);
        this.recallCommand = new RecallCommand(runManager, recallProgression);
        this.recallListener = new RecallListener(runManager, recallProgression);

        // EPIC-7: Difficulty & Combat
        this.difficultyService = new DifficultyService(runManager);
        this.mobScalingListener = new MobScalingListener(difficultyService);
        this.combatListener = new CombatListener(runManager);
        this.mobSpawnService = new MobSpawnService(this, runManager);

        // EPIC-10: Augment handlers
        this.augmentListener = new AugmentListener(runManager);

        // EPIC-8: HUD & Menu framework
        this.hudService = new HudService(this, runManager, recallProgression);
        this.menuListener = new MenuListener();

        // EPIC-9: Persistence
        this.dataStore = new YamlDataStore(this, cardRegistry);

        // EPIC-10: Synergies
        this.synergyService = new SynergyService(cardRegistry);

        // EPIC-9: Passive task for maintaining potion effects
        this.passiveTask = new PassiveTask();

        // Prepare SkillDispatchListener with RunManager
        RunManager rm = this.runManager;
        this.skillDispatchListener = new SkillDispatchListener(skillRegistry, skillServices, rm, messagesConfig);

        // Register listeners
        getServer().getPluginManager().registerEvents(playerLevelListener, this);
        getServer().getPluginManager().registerEvents(draftMenuListener, this);
        getServer().getPluginManager().registerEvents(playerLifecycleListener, this);
        getServer().getPluginManager().registerEvents(recallListener, this);
        getServer().getPluginManager().registerEvents(skillDispatchListener, this);
        getServer().getPluginManager().registerEvents(mobScalingListener, this);
        getServer().getPluginManager().registerEvents(combatListener, this);
        getServer().getPluginManager().registerEvents(augmentListener, this);
        getServer().getPluginManager().registerEvents(menuListener, this);

        // Start periodic tasks
        this.distanceTask.start(this, distanceTracker, augmentListener);
        this.passiveTask.start(this, runManager);
        this.hudService.start();

        getCommand("skills").setExecutor(this);
        getCommand("rpg").setExecutor(this);
        getCommand("run").setExecutor(runCommand);
        getCommand("recall").setExecutor(recallCommand);

        getLogger().info("SkillRegistry loaded: " + skillRegistry.size() + " skills registered.");
        getLogger().info("CardRegistry loaded: " + cardRegistry.size() + " cards registered.");
        getLogger().info("ModifierRegistry loaded: " + modifierRegistry.size() + " modifiers registered.");

        // Detect soft-dependencies
        if (auraSkillsIntegration.isEnabled()) {
            getLogger().info("RogueLata + AuraSkills integrado com sucesso!");
        } else {
            getLogger().info("RogueLata ativado em modo standalone (sem AuraSkills).");
        }
        if (getServer().getPluginManager().getPlugin("AuraMobs") != null) {
            getLogger().info("RogueLata + AuraMobs detectado (reforco de dificuldade).");
        }
        if (getServer().getPluginManager().getPlugin("MythicMobs") != null) {
            getLogger().info("RogueLata + MythicMobs detectado (enriquecimento de bosses).");
        }
    }

    @Override
    public void onDisable() {
        if (distanceTask != null) distanceTask.stop();
        if (passiveTask != null) passiveTask.stop();
        if (hudService != null) hudService.stop();
        if (dataStore != null) dataStore.flushAll();
        getLogger().info("RogueLata Plugin desativado.");
    }

    public AuraSkillsIntegration getAuraSkillsIntegration() {
        return auraSkillsIntegration;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    public SkillRegistry getSkillRegistry() {
        return skillRegistry;
    }

    public SkillServices getSkillServices() {
        return skillServices;
    }

    public SkillsConfig getSkillsConfig() {
        return skillsConfig;
    }

    public CardRegistry getCardRegistry() {
        return cardRegistry;
    }

    public StatService getStatService() {
        return statService;
    }

    public RunManager getRunManager() {
        return runManager;
    }

    public DraftService getDraftService() {
        return draftService;
    }

    public MayhemService getMayhemService() {
        return mayhemService;
    }

    public MilestoneService getMilestoneService() {
        return milestoneService;
    }

    public MayhemConfig getMayhemConfig() {
        return mayhemConfig;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("skills")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("Apenas jogadores podem usar /skills!").color(NamedTextColor.RED));
                return true;
            }
            Player player = (Player) sender;
            if (!runManager.hasActiveRun(player)) {
                runManager.startRun(player);
            }
            RunState run = runManager.getRun(player);
            if (run != null) {
                new CollectionMenu(player, run, this.messagesConfig);
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("rpg")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("rpg.admin")) {
                    sender.sendMessage(Component.text("Voce nao tem permissao para recarregar.").color(NamedTextColor.RED));
                    return true;
                }
                reloadConfig();
                skillsConfig.reload();
                sender.sendMessage(Component.text("[RogueLata] Configuracao recarregada!").color(NamedTextColor.GREEN));
                return true;
            }

            if (args.length > 0 && args[0].equalsIgnoreCase("reset") && sender.hasPermission("rpg.admin")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Component.text("Apenas jogadores podem resetar.").color(NamedTextColor.RED));
                    return true;
                }
                Player player = (Player) sender;
                if (runManager.hasActiveRun(player)) {
                    RunState run = runManager.getRun(player);
                    if (run != null) {
                        resetService.fullReset(player, run);
                    }
                }
                player.sendMessage(Component.text("[RogueLata] Todos os dados RPG foram resetados!").color(NamedTextColor.RED));
                return true;
            }

            if (args.length > 0 && args[0].equalsIgnoreCase("debug")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Component.text("Apenas jogadores podem usar /rpg debug.").color(NamedTextColor.RED));
                    return true;
                }
                Player player = (Player) sender;
                if (!runManager.hasActiveRun(player)) {
                    runManager.startRun(player);
                }
                RunState run = runManager.getRun(player);
                if (run != null) {
                    player.sendMessage(Component.text("=== RogueLata Debug ===").color(NamedTextColor.GOLD));
                    player.sendMessage(Component.text("Level: " + run.level()).color(NamedTextColor.WHITE));
                    player.sendMessage(Component.text("Cartas: " + String.join(", ", run.ownedCards())).color(NamedTextColor.WHITE));
                    player.sendMessage(Component.text("Drafts pendentes: " + run.pendingDrafts()).color(NamedTextColor.WHITE));
                    player.sendMessage(Component.text("Multipliers: " + run.multipliers()).color(NamedTextColor.WHITE));
                player.sendMessage(Component.text("Milestones: " + run.milestonesReached()).color(NamedTextColor.WHITE));
                player.sendMessage(Component.text("Mayhem ativos: " + String.join(", ", run.activeModifiers())).color(NamedTextColor.RED));
                }
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
            sender.sendMessage(Component.text("/rpg debug ").color(NamedTextColor.YELLOW).append(Component.text("- Mostra estado interno da run (Admin).").color(NamedTextColor.WHITE)));
            sender.sendMessage(Component.text("/run ").color(NamedTextColor.YELLOW).append(Component.text("- Mostra informacoes da run atual.").color(NamedTextColor.WHITE)));
            sender.sendMessage(Component.empty());
            sender.sendMessage(Component.text("=== MODO ROGUE-LIKE ATIVO ===").color(NamedTextColor.RED));
            sender.sendMessage(Component.text("Ao morrer, voce perde TODAS as habilidades e XP!").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Escolha sabiamente suas cartas no draft!").color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("=================================").color(NamedTextColor.GOLD));
            return true;
        }

        return false;
    }

    public ItemStack createRpgBook() {
        ItemStack book = new ItemStack(Material.BREAD, 1);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.mm("<gold><bold>Lata de Pão"));
            meta.lore(List.of(
                    Text.mm("<gray>Use para abrir o Menu de Habilidades!"),
                    Text.mm("<yellow>Clique com o direito para abrir.")
            ));
            meta.getPersistentDataContainer().set(ItemKeys.rpgBook(), org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
            book.setItemMeta(meta);
        }
        return book;
    }

    public DifficultyService getDifficultyService() { return difficultyService; }
    public MobSpawnService getMobSpawnService() { return mobSpawnService; }
    public PlayerDataStore getDataStore() { return dataStore; }
    public CooldownService getCooldownService() { return cooldownService; }
    public SynergyService getSynergyService() { return synergyService; }
    public HudService getHudService() { return hudService; }
    public GateRegistry getGateRegistry() { return gateRegistry; }
    public DistanceTracker getDistanceTracker() { return distanceTracker; }
}
