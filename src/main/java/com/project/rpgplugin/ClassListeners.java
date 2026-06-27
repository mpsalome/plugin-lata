package com.project.rpgplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.attribute.Attribute;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.Location;

import java.util.*;

public class ClassListeners implements Listener {

    private final RPGPlugin plugin;
    private final PlayerManager playerManager;

    // Cooldown Maps (UUID -> Timestamp in milliseconds)
    private final Map<UUID, Long> explorerDashCooldown = new HashMap<>();
    private final Map<UUID, Long> explorerWaterBreathingCooldown = new HashMap<>();
    private final Map<UUID, Long> explorerRecallCooldown = new HashMap<>();
    private final Map<UUID, Long> minerHasteCooldown = new HashMap<>();
    private final Map<UUID, Long> builderGraceCooldown = new HashMap<>();
    private final Map<UUID, Long> stepAssistCooldown = new HashMap<>();
    private final Map<UUID, Long> grappleCooldown = new HashMap<>();
    private final Map<UUID, Long> thermalResistanceCooldown = new HashMap<>();
    private final Map<UUID, Long> sonarCooldown = new HashMap<>();
    private final Map<UUID, Long> windBurstCooldown = new HashMap<>();
    private final Map<UUID, Long> torchLightCooldown = new HashMap<>();
    private final Map<UUID, Long> oreSonarCooldown = new HashMap<>();
    private final Map<UUID, Long> moltenTouchCooldown = new HashMap<>();
    private final Map<UUID, Long> gravityShieldCooldown = new HashMap<>();
    private final Map<UUID, Long> woodcutterCooldown = new HashMap<>();
    private final Map<UUID, Long> fertilizeCooldown = new HashMap<>();
    private final Map<UUID, Long> floraShieldCooldown = new HashMap<>();
    private final Map<UUID, Long> lumberjackCooldown = new HashMap<>();
    private final Map<UUID, Long> scaffoldCooldown = new HashMap<>();
    private final Map<UUID, Long> gravityDefianceCooldown = new HashMap<>();

    // State maps
    private final Set<Location> reinforcedBlocks = new HashSet<>();
    private final Map<UUID, Long> moltenTouchActiveUntil = new HashMap<>();

    private final String guiTitle = "§8Select Your RPG Class & Skills";

    public ClassListeners(RPGPlugin plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }

    /**
     * Updates walk speed, max health, and other synergy state properties.
     */
    public void updateSynergiesAndAttributes(Player player) {
        String currentClass = playerManager.getPlayerClass(player);
        if (currentClass.equalsIgnoreCase("NONE")) {
            player.setWalkSpeed(0.20f);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(20.0);
            return;
        }

        int aventureiro = playerManager.getSubtypeCount(player, "Aventureiro");
        int combatente = playerManager.getSubtypeCount(player, "Combatente");
        int escavador = playerManager.getSubtypeCount(player, "Escavador");
        int alquimista = playerManager.getSubtypeCount(player, "Alquimista");
        int silvicultor = playerManager.getSubtypeCount(player, "Silvicultor");
        int arquiteto = playerManager.getSubtypeCount(player, "Arquiteto");

        if (currentClass.equalsIgnoreCase("EXPLORER")) {
            // Aventureiro 3+: Speed permanent +10%
            player.setWalkSpeed(aventureiro >= 3 ? 0.22f : 0.20f);
            // Combatente 3+: Max HP +4 (+2 Hearts)
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(combatente >= 3 ? 24.0 : 20.0);
        } else {
            player.setWalkSpeed(0.20f);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(20.0);
        }
    }

    /**
     * Opens the virtual selection chest GUI (54 Slots - 6 rows)
     */
    public void openSelectionGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, Component.text(guiTitle));

        String currentClass = playerManager.getPlayerClass(player);
        updateSynergiesAndAttributes(player);

        // --- ROW 1 (0-8): Classes ---
        // Slot 2: Explorer Class Item
        ItemStack explorerItem = new ItemStack(Material.COMPASS);
        ItemMeta explorerMeta = explorerItem.getItemMeta();
        if (currentClass.equalsIgnoreCase("EXPLORER")) {
            explorerMeta.displayName(Component.text("§a§l[SUA CLASSE] §bExplorer Class").decoration(TextDecoration.ITALIC, false));
            explorerItem.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
        } else {
            explorerMeta.displayName(Component.text("Explorer Class").color(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        }
        explorerMeta.lore(Arrays.asList(
                Component.text("§7Especialidade: Mobilidade, Exploração e Sobrevivência"),
                Component.text("§bEspecializações: §eAventureiro §7e §eCombatente")
        ));
        explorerItem.setItemMeta(explorerMeta);
        gui.setItem(2, explorerItem);

        // Slot 4: Miner Class Item
        ItemStack minerItem = new ItemStack(Material.GOLDEN_PICKAXE);
        ItemMeta minerMeta = minerItem.getItemMeta();
        if (currentClass.equalsIgnoreCase("MINER")) {
            minerMeta.displayName(Component.text("§a§l[SUA CLASSE] §6Miner Class").decoration(TextDecoration.ITALIC, false));
            minerItem.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
        } else {
            minerMeta.displayName(Component.text("Miner Class").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        }
        minerMeta.lore(Arrays.asList(
                Component.text("§7Especialidade: Mineração profunda e Alquimia"),
                Component.text("§6Especializações: §eEscavador §7e §eAlquimista")
        ));
        minerItem.setItemMeta(minerMeta);
        gui.setItem(4, minerItem);

        // Slot 6: Builder Class Item
        ItemStack builderItem = new ItemStack(Material.BRICK);
        ItemMeta builderMeta = builderItem.getItemMeta();
        if (currentClass.equalsIgnoreCase("BUILDER")) {
            builderMeta.displayName(Component.text("§a§l[SUA CLASSE] §2Builder Class").decoration(TextDecoration.ITALIC, false));
            builderItem.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
        } else {
            builderMeta.displayName(Component.text("Builder Class").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        }
        builderMeta.lore(Arrays.asList(
                Component.text("§7Especialidade: Arquitetura, Natureza e Estruturas"),
                Component.text("§2Especializações: §eSilvicultor §7e §eArquiteto")
        ));
        builderItem.setItemMeta(builderMeta);
        gui.setItem(6, builderItem);

        // --- ROW 2 (9-17): Separators ---
        ItemStack separator = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta sepMeta = separator.getItemMeta();
        if (sepMeta != null) {
            sepMeta.displayName(Component.text("§7"));
            separator.setItemMeta(sepMeta);
        }
        for (int i = 9; i <= 17; i++) {
            gui.setItem(i, separator);
        }

        // --- ROW 3 & 4 (18-35): Skills ---
        if (currentClass.equalsIgnoreCase("EXPLORER")) {
            // Row 3: Aventureiro
            gui.setItem(19, createSkillItem(player, "dash", Material.POPPY, "§eFlower Dash (Lvl 1)", 2, 1, "§7Consuma Flor para dash com Speed II e Invisibilidade."));
            gui.setItem(20, createSkillItem(player, "hydration", Material.POTION, "§eWater Hydration (Lvl 1)", 2, 1, "§7Garrafas de água enchem fome e saturação."));
            gui.setItem(21, createSkillItem(player, "step_assist", Material.SUGAR, "§eAgile Step (Lvl 5)", 3, 5, "§7Consuma açúcar para obter Speed II por 15s."));
            gui.setItem(22, createSkillItem(player, "grapple", Material.SLIME_BALL, "§eGrappling Leap (Lvl 5)", 3, 5, "§7Consuma slimeball para um grande salto para a frente."));
            gui.setItem(23, createSkillItem(player, "safe_fall", Material.FEATHER, "§eFeather Shield (Lvl 10)", 4, 10, "§7Anula dano de queda com slow falling por 30s."));
            gui.setItem(24, createSkillItem(player, "water_breathing", Material.LAPIS_LAZULI, "§eLapis Water Breathing (Lvl 15)", 5, 15, "§7Consuma Lapis Lazuli para respirar debaixo d'água."));
            gui.setItem(25, createSkillItem(player, "recall", Material.DRAGON_BREATH, "§eDragon's Recall (Lvl 20)", 6, 20, "§7Teleporta de volta para o ponto de Spawn do mundo."));

            // Row 4: Combatente
            gui.setItem(29, createSkillItem(player, "jump_boost", Material.RABBIT_FOOT, "§cSuper Jump Boost (Lvl 10)", 4, 10, "§7Ganha super pulo passivo e constante nas botas."));
            gui.setItem(30, createSkillItem(player, "thermal_resistance", Material.MAGMA_CREAM, "§cMagma Shield (Lvl 15)", 5, 15, "§7Magma cream dá Fire Resistance por 15s."));
            gui.setItem(31, createSkillItem(player, "sonar", Material.AMETHYST_SHARD, "§cEcho Location (Lvl 20)", 6, 20, "§7Consuma fragmento para revelar inimigos com partículas."));
            gui.setItem(32, createSkillItem(player, "dim_shift", Material.ENDER_PEARL, "§cDimensional Shift (Lvl 25)", 8, 25, "§7Avanço dimensional rápido. Dá Blindness por 5s."));
            gui.setItem(33, createSkillItem(player, "wind_burst", Material.GUNPOWDER, "§cWind Burst (Lvl 25)", 8, 25, "§7Explode pólvora e ejeta-se para o céu para fugir."));

        } else if (currentClass.equalsIgnoreCase("MINER")) {
            // Row 3: Escavador
            gui.setItem(19, createSkillItem(player, "diet", Material.COAL, "§6Coal Diet (Lvl 1)", 2, 1, "§7Alimente-se de carvão bruto para encher fome rapidamente."));
            gui.setItem(20, createSkillItem(player, "stone_smash", Material.COBBLESTONE, "§6Stone Smash (Lvl 1)", 2, 1, "§7Segurando pedra, quebre minas de pedregulho mais rápido."));
            gui.setItem(21, createSkillItem(player, "torch_light", Material.TORCH, "§6Torch Light (Lvl 5)", 3, 5, "§7Consuma tocha para obter Visão Noturna por 30s."));
            gui.setItem(22, createSkillItem(player, "ore_sonar", Material.GLOWSTONE_DUST, "§6Ore Finder (Lvl 10)", 4, 10, "§7Mapeia e revela minérios próximos com partículas."));
            gui.setItem(23, createSkillItem(player, "double_ore", Material.GOLD_NUGGET, "§6Lucky Miner (Lvl 10)", 4, 10, "§7Passiva: Chance de duplicar o carvão minerado."));
            gui.setItem(24, createSkillItem(player, "sight", Material.AMETHYST_SHARD, "§6Cave Vision (Lvl 25)", 8, 25, "§7Passiva: Visão noturna permanente em locais escuros."));

            // Row 4: Alquimista
            gui.setItem(28, createSkillItem(player, "haste", Material.GOLD_INGOT, "§eGold Rush Haste (Lvl 5)", 3, 5, "§7Consuma ouro com picareta na mão para Haste II temporário."));
            gui.setItem(29, createSkillItem(player, "ore_repair", Material.IRON_INGOT, "§eOre Tool Repair (Lvl 15)", 5, 15, "§7Use ferro para consertar 30% de durabilidade da picareta."));
            gui.setItem(30, createSkillItem(player, "molten_touch", Material.FLINT, "§eMolten Touch (Lvl 15)", 5, 15, "§7Fundir minérios automaticamente ao minerar por 30s."));
            gui.setItem(31, createSkillItem(player, "transmutation", Material.DIAMOND, "§eMiner Transmutation (Lvl 20)", 6, 20, "§7Transmute metais de baixo custo em itens nobres."));
            gui.setItem(32, createSkillItem(player, "gravity_shield", Material.OBSIDIAN, "§eBedrock Stance (Lvl 20)", 6, 20, "§7Obsidiana enrijece o corpo dando Resistência III por 15s."));
            gui.setItem(33, createSkillItem(player, "core_overdrive", Material.REDSTONE_BLOCK, "§eDeep Core Overdrive (Lvl 25)", 8, 25, "§7Extremo: Haste III e Força II. Aplica Slowness II."));

        } else if (currentClass.equalsIgnoreCase("BUILDER")) {
            // Row 3: Silvicultor
            gui.setItem(19, createSkillItem(player, "feast", Material.OAK_LEAVES, "§2Leaves Feast (Lvl 1)", 2, 1, "§7Alimente-se de folhas de árvore diretamente para fome."));
            gui.setItem(20, createSkillItem(player, "woodcutter", Material.WHEAT, "§2Fast Woodcutter (Lvl 1)", 2, 1, "§7Consuma trigo com machado para obter Haste I por 10s."));
            gui.setItem(21, createSkillItem(player, "canopy_step", Material.LEATHER_BOOTS, "§2Canopy Step (Lvl 10)", 4, 10, "§7Ganha velocidade Speed II ao pisar em folhas ou grama."));
            gui.setItem(22, createSkillItem(player, "fertilize", Material.BONE_MEAL, "§2Green Thumb (Lvl 10)", 4, 10, "§7Farinha de osso cresce plantas e flores ao redor."));
            gui.setItem(23, createSkillItem(player, "flora_shield", Material.DANDELION, "§2Flora Regrowth (Lvl 20)", 6, 20, "§7Consuma flor para regenerar instantaneamente 8 de vida."));
            gui.setItem(24, createSkillItem(player, "lumberjack", Material.IRON_BLOCK, "§2Timber Axe (Lvl 25)", 8, 25, "§7Permite quebrar uma árvore inteira instantaneamente."));

            // Row 4: Arquiteto
            gui.setItem(28, createSkillItem(player, "silk_touch", Material.SHEARS, "§aEmpty Hand Silk Touch (Lvl 5)", 3, 5, "§7Colete blocos frágeis quebrando de mão vazia."));
            gui.setItem(29, createSkillItem(player, "scaffold", Material.DIRT, "§aScaffold Leap (Lvl 5)", 3, 5, "§7Pula alto gerando blocos temporários sob os pés."));
            gui.setItem(30, createSkillItem(player, "architect_focus", Material.STONE_BRICKS, "§aArchitect's Focus (Lvl 15)", 5, 15, "§7Consuma tijolos para ganhar super Resistência IV por 30s."));
            gui.setItem(31, createSkillItem(player, "unbreakable_block", Material.CLAY_BALL, "§aReinforce Block (Lvl 15)", 5, 15, "§7Reforce blocos temporariamente para torná-los invulneráveis."));
            gui.setItem(32, createSkillItem(player, "grace", Material.FEATHER, "§aFeather Grace (Lvl 20)", 6, 20, "§7Consuma pena para pular muito alto com queda lenta."));
            gui.setItem(33, createSkillItem(player, "gravity_defiance", Material.SLIME_BLOCK, "§aGravity Defiance (Lvl 25)", 8, 25, "§7Flutue no ar temporariamente sem sofrer gravidade."));

        } else {
            ItemStack selectClassFirst = new ItemStack(Material.BARRIER);
            ItemMeta barrierMeta = selectClassFirst.getItemMeta();
            if (barrierMeta != null) {
                barrierMeta.displayName(Component.text("§cEscolha uma classe primeiro!"));
                barrierMeta.lore(Arrays.asList(
                    Component.text("§7Você precisa selecionar uma classe na fileira"),
                    Component.text("§7de cima para ver e desbloquear suas habilidades.")
                ));
                selectClassFirst.setItemMeta(barrierMeta);
            }
            gui.setItem(22, selectClassFirst);
        }

        // --- ROW 5 (36-44): Specialization Synergies status ---
        if (!currentClass.equalsIgnoreCase("NONE")) {
            int aventureiro = playerManager.getSubtypeCount(player, "Aventureiro");
            int combatente = playerManager.getSubtypeCount(player, "Combatente");
            int escavador = playerManager.getSubtypeCount(player, "Escavador");
            int alquimista = playerManager.getSubtypeCount(player, "Alquimista");
            int silvicultor = playerManager.getSubtypeCount(player, "Silvicultor");
            int arquiteto = playerManager.getSubtypeCount(player, "Arquiteto");

            if (currentClass.equalsIgnoreCase("EXPLORER")) {
                gui.setItem(38, createSynergyStatusItem("Aventureiro", aventureiro, "🧭 +10% de Velocidade permanente", aventureiro >= 3));
                gui.setItem(42, createSynergyStatusItem("Combatente", combatente, "⚔️ +4 HP Máximo (+2 Corações)", combatente >= 3));
            } else if (currentClass.equalsIgnoreCase("MINER")) {
                gui.setItem(38, createSynergyStatusItem("Escavador", escavador, "🕳️ Haste II permanente", escavador >= 3));
                gui.setItem(42, createSynergyStatusItem("Alquimista", alquimista, "⚗️ 15% de preservar ingrediente", alquimista >= 3));
            } else if (currentClass.equalsIgnoreCase("BUILDER")) {
                gui.setItem(38, createSynergyStatusItem("Silvicultor", silvicultor, "🌱 Regeneração em folhas/grama", silvicultor >= 3));
                gui.setItem(42, createSynergyStatusItem("Arquiteto", arquiteto, "📐 Resistência I permanente (-10% dano)", arquiteto >= 3));
            }
        }

        // --- ROW 6 (45-53): Footer ---
        for (int i = 45; i <= 53; i++) {
            gui.setItem(i, separator);
        }

        player.openInventory(gui);
    }

    private ItemStack createSynergyStatusItem(String name, int count, String bonus, boolean active) {
        ItemStack item = new ItemStack(active ? Material.EMERALD : Material.REDSTONE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§lSinergia: " + name + " (" + count + "/6)").decoration(TextDecoration.ITALIC, false));
            meta.lore(Arrays.asList(
                Component.text("§7Ativa ao desbloquear 3+ habilidades da especialidade."),
                Component.text("§eEfeito: §f" + bonus),
                Component.text(""),
                Component.text(active ? "§a● ATIVO" : "§c○ INATIVO")
            ));
            if (active) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createSkillItem(Player player, String skillKey, Material material, String displayName, int cost, int minLvl, String desc) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(Component.text(displayName).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(desc));
        lore.add(Component.text("§7"));

        boolean isUnlocked = playerManager.hasSkill(player, skillKey);
        if (isUnlocked) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            lore.add(Component.text("§a✓ Desbloqueada!"));
        } else {
            lore.add(Component.text("§c✕ Bloqueada"));
            lore.add(Component.text("§eCusto: §6" + cost + " Níveis de XP"));
            if (minLvl > 0) {
                lore.add(Component.text("§eNível Mínimo Exigido: §d" + minLvl));
            }
            lore.add(Component.text("§7"));
            lore.add(Component.text("§eClique para desbloquear com XP!"));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text(guiTitle))) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        String currentClass = playerManager.getPlayerClass(player);

        // Class selection slots
        if (slot == 2 || slot == 4 || slot == 6) {
            String selectedClass = null;
            if (slot == 2) selectedClass = "EXPLORER";
            else if (slot == 4) selectedClass = "MINER";
            else if (slot == 6) selectedClass = "BUILDER";

            if (selectedClass != null) {
                if (currentClass.equalsIgnoreCase(selectedClass)) {
                    player.sendMessage(Component.text("§cVocê já pertence a esta classe!"));
                    return;
                }
                playerManager.setPlayerClass(player, selectedClass);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Classe " + selectedClass + " selecionada!")
                        .color(NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true));
                player.sendMessage(Component.text("[RPGPlugin] Você agora pertence à classe " + selectedClass + "!")
                        .color(NamedTextColor.YELLOW));
                
                updateSynergiesAndAttributes(player);
                openSelectionGUI(player);
            }
            return;
        }

        // Skills unlocking slots
        String skillKey = null;
        int cost = 0;
        int minLvl = 0;
        String skillName = "";

        if (currentClass.equalsIgnoreCase("EXPLORER")) {
            if (slot == 19) { skillKey = "dash"; cost = 2; minLvl = 1; skillName = "Flower Dash"; }
            else if (slot == 20) { skillKey = "hydration"; cost = 2; minLvl = 1; skillName = "Water Hydration"; }
            else if (slot == 21) { skillKey = "step_assist"; cost = 3; minLvl = 5; skillName = "Agile Step"; }
            else if (slot == 22) { skillKey = "grapple"; cost = 3; minLvl = 5; skillName = "Grappling Leap"; }
            else if (slot == 23) { skillKey = "safe_fall"; cost = 4; minLvl = 10; skillName = "Feather Shield"; }
            else if (slot == 24) { skillKey = "water_breathing"; cost = 5; minLvl = 15; skillName = "Lapis Water Breathing"; }
            else if (slot == 25) { skillKey = "recall"; cost = 6; minLvl = 20; skillName = "Dragon's Recall"; }
            else if (slot == 29) { skillKey = "jump_boost"; cost = 4; minLvl = 10; skillName = "Permanent Jump Boost"; }
            else if (slot == 30) { skillKey = "thermal_resistance"; cost = 5; minLvl = 15; skillName = "Magma Shield"; }
            else if (slot == 31) { skillKey = "sonar"; cost = 6; minLvl = 20; skillName = "Echo Location"; }
            else if (slot == 32) { skillKey = "dim_shift"; cost = 8; minLvl = 25; skillName = "Dimensional Shift"; }
            else if (slot == 33) { skillKey = "wind_burst"; cost = 8; minLvl = 25; skillName = "Wind Burst"; }
        } else if (currentClass.equalsIgnoreCase("MINER")) {
            if (slot == 19) { skillKey = "diet"; cost = 2; minLvl = 1; skillName = "Coal Diet"; }
            else if (slot == 20) { skillKey = "stone_smash"; cost = 2; minLvl = 1; skillName = "Stone Smash"; }
            else if (slot == 21) { skillKey = "torch_light"; cost = 3; minLvl = 5; skillName = "Torch Light"; }
            else if (slot == 22) { skillKey = "ore_sonar"; cost = 4; minLvl = 10; skillName = "Ore Finder"; }
            else if (slot == 23) { skillKey = "double_ore"; cost = 4; minLvl = 10; skillName = "Lucky Miner"; }
            else if (slot == 24) { skillKey = "sight"; cost = 8; minLvl = 25; skillName = "Cave Vision"; }
            else if (slot == 28) { skillKey = "haste"; cost = 3; minLvl = 5; skillName = "Gold Rush Haste"; }
            else if (slot == 29) { skillKey = "ore_repair"; cost = 5; minLvl = 15; skillName = "Ore Tool Repair"; }
            else if (slot == 30) { skillKey = "molten_touch"; cost = 5; minLvl = 15; skillName = "Molten Touch"; }
            else if (slot == 31) { skillKey = "transmutation"; cost = 6; minLvl = 20; skillName = "Miner Transmutation"; }
            else if (slot == 32) { skillKey = "gravity_shield"; cost = 6; minLvl = 20; skillName = "Bedrock Stance"; }
            else if (slot == 33) { skillKey = "core_overdrive"; cost = 8; minLvl = 25; skillName = "Deep Core Overdrive"; }
        } else if (currentClass.equalsIgnoreCase("BUILDER")) {
            if (slot == 19) { skillKey = "feast"; cost = 2; minLvl = 1; skillName = "Leaves Feast"; }
            else if (slot == 20) { skillKey = "woodcutter"; cost = 2; minLvl = 1; skillName = "Fast Woodcutter"; }
            else if (slot == 21) { skillKey = "canopy_step"; cost = 4; minLvl = 10; skillName = "Canopy Step"; }
            else if (slot == 22) { skillKey = "fertilize"; cost = 4; minLvl = 10; skillName = "Green Thumb"; }
            else if (slot == 23) { skillKey = "flora_shield"; cost = 6; minLvl = 20; skillName = "Flora Regrowth"; }
            else if (slot == 24) { skillKey = "lumberjack"; cost = 8; minLvl = 25; skillName = "Timber Axe"; }
            else if (slot == 28) { skillKey = "silk_touch"; cost = 3; minLvl = 5; skillName = "Empty Hand Silk Touch"; }
            else if (slot == 29) { skillKey = "scaffold"; cost = 3; minLvl = 5; skillName = "Scaffold Leap"; }
            else if (slot == 30) { skillKey = "architect_focus"; cost = 5; minLvl = 15; skillName = "Architect's Focus"; }
            else if (slot == 31) { skillKey = "unbreakable_block"; cost = 5; minLvl = 15; skillName = "Reinforce Block"; }
            else if (slot == 32) { skillKey = "grace"; cost = 6; minLvl = 20; skillName = "Feather Grace"; }
            else if (slot == 33) { skillKey = "gravity_defiance"; cost = 8; minLvl = 25; skillName = "Gravity Defiance"; }
        }

        if (skillKey != null) {
            if (playerManager.hasSkill(player, skillKey)) {
                player.sendMessage(Component.text("§eHabilidade " + skillName + " já está desbloqueada!"));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.5f);
                return;
            }

            if (player.getLevel() < minLvl) {
                player.sendMessage(Component.text("§cRequisito de Nível Mínimo não atingido! Exige nível: " + minLvl));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.5f);
                return;
            }

            if (player.getLevel() < cost) {
                player.sendMessage(Component.text("§cNíveis de XP insuficientes! Custo: " + cost + " níveis. Você tem: " + player.getLevel()));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.5f);
                return;
            }

            // Deduct XP and unlock
            player.setLevel(player.getLevel() - cost);
            playerManager.unlockSkill(player, skillKey);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage(Component.text("§a§l[RPG] §aHabilidade §f" + skillName + " §adesbloqueada com sucesso! Gastou §6" + cost + " §aníveis de XP."));
            
            updateSynergiesAndAttributes(player);
            openSelectionGUI(player);
        }
    }

    /**
     * Alquimista synergy: check if item consumption should skip stack deduction (15% chance).
     */
    private boolean checkAlquimistaPreservation(Player player) {
        if (playerManager.hasClass(player, "MINER") && playerManager.getSubtypeCount(player, "Alquimista") >= 3) {
            if (Math.random() < 0.15) {
                player.sendActionBar(Component.text("✨ [Alquimista] Sinergia Ativa! Item preservado!").color(NamedTextColor.GOLD));
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.5f);
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        String currentClass = playerManager.getPlayerClass(player);
        if (currentClass.equalsIgnoreCase("NONE")) return;

        // --- EXPLORER MECHANICAL INTERACTION ---
        if (currentClass.equalsIgnoreCase("EXPLORER")) {
            // Poppy: Flower Dash
            if (Tag.FLOWERS.isTagged(item.getType())) {
                if (!playerManager.hasSkill(player, "dash")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                long cooldownMs = plugin.getConfig().getLong("classes.explorer.dash.cooldown", 30) * 1000;

                if (explorerDashCooldown.containsKey(uuid) && (now - explorerDashCooldown.get(uuid)) < cooldownMs) {
                    long remaining = (cooldownMs - (now - explorerDashCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Flower Dash em cooldown! " + remaining + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                int dur = plugin.getConfig().getInt("classes.explorer.dash.duration", 10);
                int spd = plugin.getConfig().getInt("classes.explorer.dash.speed", 1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur * 20, spd));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, dur * 20, 0));
                explorerDashCooldown.put(uuid, now);
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05);
                player.sendActionBar(Component.text("Flower Dash Ativado!").color(NamedTextColor.AQUA));
                return;
            }

            // Sugar: Agile Step
            if (item.getType() == Material.SUGAR) {
                if (!playerManager.hasSkill(player, "step_assist")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (stepAssistCooldown.containsKey(uuid) && (now - stepAssistCooldown.get(uuid)) < 15000) {
                    long rem = (15000 - (now - stepAssistCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Agile Step em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 1));
                stepAssistCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_GALLOP, 1.0f, 1.2f);
                player.sendActionBar(Component.text("Agile Step: Velocidade II por 15s!").color(NamedTextColor.YELLOW));
                return;
            }

            // Slimeball: Grappling Leap
            if (item.getType() == Material.SLIME_BALL) {
                if (!playerManager.hasSkill(player, "grapple")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (grappleCooldown.containsKey(uuid) && (now - grappleCooldown.get(uuid)) < 10000) {
                    long rem = (10000 - (now - grappleCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Grappling Leap em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(0.6));
                grappleCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1.0f, 1.2f);
                player.sendActionBar(Component.text("Grappling Leap!").color(NamedTextColor.GREEN));
                return;
            }

            // Magma Cream: Magma Shield
            if (item.getType() == Material.MAGMA_CREAM) {
                if (!playerManager.hasSkill(player, "thermal_resistance")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (thermalResistanceCooldown.containsKey(uuid) && (now - thermalResistanceCooldown.get(uuid)) < 30000) {
                    long rem = (30000 - (now - thermalResistanceCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Magma Shield em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 300, 0));
                thermalResistanceCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Magma Shield: Resistência ao Fogo por 15s!").color(NamedTextColor.GOLD));
                return;
            }

            // Amethyst Shard: Echo Location
            if (item.getType() == Material.AMETHYST_SHARD) {
                if (!playerManager.hasSkill(player, "sonar")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (sonarCooldown.containsKey(uuid) && (now - sonarCooldown.get(uuid)) < 20000) {
                    long rem = (20000 - (now - sonarCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Echo Location em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                sonarCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Sondas de eco ativas! Localizando criaturas...").color(NamedTextColor.DARK_PURPLE));

                for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        entity.getWorld().spawnParticle(Particle.GLOW, entity.getLocation().add(0, 1, 0), 10, 0.2, 0.5, 0.2, 0);
                    }
                }
                return;
            }

            // Gunpowder: Wind Burst
            if (item.getType() == Material.GUNPOWDER) {
                if (!playerManager.hasSkill(player, "wind_burst")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (windBurstCooldown.containsKey(uuid) && (now - windBurstCooldown.get(uuid)) < 25000) {
                    long rem = (25000 - (now - windBurstCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Wind Burst em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                player.setVelocity(new org.bukkit.util.Vector(0, 1.3, 0));
                windBurstCooldown.put(uuid, now);
                player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
                player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_THROW, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Ejeção de Ar!").color(NamedTextColor.WHITE));
                return;
            }

            // Lapis Lazuli: Water Breathing
            if (item.getType() == Material.LAPIS_LAZULI) {
                if (!playerManager.hasSkill(player, "water_breathing")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                long cooldownMs = plugin.getConfig().getLong("classes.explorer.water_breathing.cooldown", 10) * 1000;

                if (explorerWaterBreathingCooldown.containsKey(uuid) && (now - explorerWaterBreathingCooldown.get(uuid)) < cooldownMs) {
                    long remaining = (cooldownMs - (now - explorerWaterBreathingCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Water Breathing em cooldown! " + remaining + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                int dur = plugin.getConfig().getInt("classes.explorer.water_breathing.duration", 15);
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, dur * 20, 2));
                explorerWaterBreathingCooldown.put(uuid, now);
                player.sendActionBar(Component.text("Respiração Debaixo d'Água Ativada!").color(NamedTextColor.BLUE));
                return;
            }

            // Dragon's Breath: Recall
            if (item.getType() == Material.DRAGON_BREATH) {
                if (!playerManager.hasSkill(player, "recall")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                long cooldownMs = plugin.getConfig().getLong("classes.explorer.recall.cooldown", 60) * 1000;

                if (explorerRecallCooldown.containsKey(uuid) && (now - explorerRecallCooldown.get(uuid)) < cooldownMs) {
                    long remaining = (cooldownMs - (now - explorerRecallCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Recall em cooldown! " + remaining + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                player.teleport(player.getWorld().getSpawnLocation());
                explorerRecallCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Teleportado ao Spawn!").color(NamedTextColor.LIGHT_PURPLE));
                return;
            }

            // Ender Pearl: Dimensional Shift
            if (item.getType() == Material.ENDER_PEARL) {
                if (!playerManager.hasSkill(player, "dim_shift")) return;
                event.setCancelled(true);
                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                org.bukkit.util.Vector dir = player.getLocation().getDirection().normalize().multiply(8);
                player.teleport(player.getLocation().add(dir));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 2));
                player.setFoodLevel(Math.max(2, player.getFoodLevel() - 4));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Dimensional Shift! Speed IV & Drawbacks aplicados.").color(NamedTextColor.DARK_PURPLE));
                return;
            }
        }

        // --- MINER MECHANICAL INTERACTION ---
        if (currentClass.equalsIgnoreCase("MINER")) {
            // Coal: Coal Diet
            if (item.getType() == Material.COAL || item.getType() == Material.CHARCOAL) {
                if (!playerManager.hasSkill(player, "diet")) return;
                event.setCancelled(true);
                int hunger = plugin.getConfig().getInt("classes.miner.diet.hunger", 4);
                double saturation = plugin.getConfig().getDouble("classes.miner.diet.saturation", 2.0);

                if (player.getFoodLevel() < 20) {
                    if (!checkAlquimistaPreservation(player)) {
                        item.subtract(1);
                    }
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + hunger));
                    player.setSaturation((float) Math.min(20, player.getSaturation() + saturation));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
                    player.sendActionBar(Component.text("Carbono bruto ingerido!").color(NamedTextColor.GOLD));
                }
                return;
            }

            // Torch: Torch Light
            if (item.getType() == Material.TORCH) {
                if (!playerManager.hasSkill(player, "torch_light")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (torchLightCooldown.containsKey(uuid) && (now - torchLightCooldown.get(uuid)) < 30000) {
                    long rem = (30000 - (now - torchLightCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Torch Light em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0));
                torchLightCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.2f);
                player.sendActionBar(Component.text("Visão Noturna temporária por 30s!").color(NamedTextColor.YELLOW));
                return;
            }

            // Glowstone Dust: Ore Finder
            if (item.getType() == Material.GLOWSTONE_DUST) {
                if (!playerManager.hasSkill(player, "ore_sonar")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (oreSonarCooldown.containsKey(uuid) && (now - oreSonarCooldown.get(uuid)) < 30000) {
                    long rem = (30000 - (now - oreSonarCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Ore Finder em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                oreSonarCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ENTITY_ALLAY_DEATH, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Sondas de minério ativas...").color(NamedTextColor.GOLD));

                Location ploc = player.getLocation();
                for (int x = -8; x <= 8; x++) {
                    for (int y = -8; y <= 8; y++) {
                        for (int z = -8; z <= 8; z++) {
                            Block b = ploc.clone().add(x, y, z).getBlock();
                            if (b.getType() == Material.IRON_ORE || b.getType() == Material.DEEPSLATE_IRON_ORE ||
                                b.getType() == Material.GOLD_ORE || b.getType() == Material.DEEPSLATE_GOLD_ORE ||
                                b.getType() == Material.DIAMOND_ORE || b.getType() == Material.DEEPSLATE_DIAMOND_ORE) {
                                b.getWorld().spawnParticle(Particle.GLOW, b.getLocation().add(0.5, 0.5, 0.5), 5, 0.1, 0.1, 0.1, 0);
                            }
                        }
                    }
                }
                return;
            }

            // Flint: Molten Touch
            if (item.getType() == Material.FLINT) {
                if (!playerManager.hasSkill(player, "molten_touch")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (moltenTouchCooldown.containsKey(uuid) && (now - moltenTouchCooldown.get(uuid)) < 45000) {
                    long rem = (45000 - (now - moltenTouchCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Molten Touch em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                moltenTouchActiveUntil.put(uuid, now + 30000);
                moltenTouchCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Toque de Fundição ativo! Minérios fundem automaticamente por 30s.").color(NamedTextColor.RED));
                return;
            }

            // Obsidian: Bedrock Stance
            if (item.getType() == Material.OBSIDIAN) {
                if (!playerManager.hasSkill(player, "gravity_shield")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (gravityShieldCooldown.containsKey(uuid) && (now - gravityShieldCooldown.get(uuid)) < 45000) {
                    long rem = (45000 - (now - gravityShieldCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Bedrock Stance em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 300, 2)); // Resistance III
                gravityShieldCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.5f);
                player.sendActionBar(Component.text("Bedrock Stance: Resistência III por 15s!").color(NamedTextColor.DARK_GRAY));
                return;
            }

            // Redstone Block: Deep Core Overdrive
            if (item.getType() == Material.REDSTONE_BLOCK) {
                if (!playerManager.hasSkill(player, "core_overdrive")) return;
                event.setCancelled(true);
                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 400, 2)); // Haste III
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 400, 1)); // Strength II
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 300, 1)); // Slowness II
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 300, 2)); // Hunger III
                player.playSound(player.getLocation(), Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Deep Core Overdrive Ativado! (Benefícios e Debuffs aplicados)").color(NamedTextColor.RED));
                return;
            }

            // Iron Ingot: Transmutation & Repair
            if (item.getType() == Material.IRON_INGOT) {
                event.setCancelled(true);
                if (item.getAmount() >= 5 && playerManager.hasSkill(player, "transmutation")) {
                    if (!checkAlquimistaPreservation(player)) {
                        item.subtract(5);
                    }
                    player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 1));
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
                    player.sendActionBar(Component.text("Transmuta: 5x Ferro -> 1x Ouro!").color(NamedTextColor.YELLOW));
                    return;
                }

                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (mainHand.getType() == Material.IRON_PICKAXE && playerManager.hasSkill(player, "ore_repair")) {
                    org.bukkit.inventory.meta.Damageable dmg = (org.bukkit.inventory.meta.Damageable) mainHand.getItemMeta();
                    if (dmg != null && dmg.getDamage() > 0) {
                        if (!checkAlquimistaPreservation(player)) {
                            item.subtract(1);
                        }
                        int rep = (int) (Material.IRON_PICKAXE.getMaxDurability() * 0.30);
                        dmg.setDamage(Math.max(0, dmg.getDamage() - rep));
                        mainHand.setItemMeta(dmg);
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                        player.sendActionBar(Component.text("Ferramenta de ferro reparada em +30%!").color(NamedTextColor.GREEN));
                    }
                    return;
                }
            }

            // Gold Ingot: Transmutation & Haste
            if (item.getType() == Material.GOLD_INGOT) {
                event.setCancelled(true);
                if (item.getAmount() >= 5 && playerManager.hasSkill(player, "transmutation")) {
                    if (!checkAlquimistaPreservation(player)) {
                        item.subtract(5);
                    }
                    player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.8f);
                    player.sendActionBar(Component.text("Transmuta: 5x Ouro -> 1x Diamante!").color(NamedTextColor.AQUA));
                    return;
                }

                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (isPickaxe(mainHand.getType()) && playerManager.hasSkill(player, "haste")) {
                    UUID uuid = player.getUniqueId();
                    long now = System.currentTimeMillis();
                    long cd = plugin.getConfig().getLong("classes.miner.haste.cooldown", 20) * 1000;
                    if (minerHasteCooldown.containsKey(uuid) && (now - minerHasteCooldown.get(uuid)) < cd) {
                        long rem = (cd - (now - minerHasteCooldown.get(uuid))) / 1000;
                        player.sendActionBar(Component.text("Gold Rush Haste em cooldown! " + rem + "s").color(NamedTextColor.RED));
                        return;
                    }

                    if (!checkAlquimistaPreservation(player)) {
                        item.subtract(1);
                    }

                    int dur = plugin.getConfig().getInt("classes.miner.haste.duration", 15);
                    int amp = plugin.getConfig().getInt("classes.miner.haste.amp", 1);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, dur * 20, amp));
                    minerHasteCooldown.put(uuid, now);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                    player.sendActionBar(Component.text("Velocidade de Mineração Aumentada!").color(NamedTextColor.GOLD));
                }
            }
        }

        // --- BUILDER MECHANICAL INTERACTION ---
        if (currentClass.equalsIgnoreCase("BUILDER")) {
            // Leaves Feast
            if (Tag.LEAVES.isTagged(item.getType())) {
                if (!playerManager.hasSkill(player, "feast")) return;
                event.setCancelled(true);
                int hunger = plugin.getConfig().getInt("classes.builder.feast.hunger", 2);
                double sat = plugin.getConfig().getDouble("classes.builder.feast.saturation", 0.8);

                if (player.getFoodLevel() < 20) {
                    if (!checkAlquimistaPreservation(player)) {
                        item.subtract(1);
                    }
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + hunger));
                    player.setSaturation((float) Math.min(20, player.getSaturation() + sat));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.2f);
                    player.sendActionBar(Component.text("Banquetear em folhagens!").color(NamedTextColor.GREEN));
                }
                return;
            }

            // Wheat: Fast Woodcutter
            if (item.getType() == Material.WHEAT) {
                if (!playerManager.hasSkill(player, "woodcutter")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (woodcutterCooldown.containsKey(uuid) && (now - woodcutterCooldown.get(uuid)) < 15000) {
                    long rem = (15000 - (now - woodcutterCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Fast Woodcutter em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (mainHand.getType().name().endsWith("_AXE")) {
                    if (!checkAlquimistaPreservation(player)) {
                        item.subtract(1);
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 200, 0)); // Haste I for 10s
                    woodcutterCooldown.put(uuid, now);
                    player.playSound(player.getLocation(), Sound.ENTITY_COW_MILK, 1.0f, 1.0f);
                    player.sendActionBar(Component.text("Fast Woodcutter ativado!").color(NamedTextColor.GREEN));
                }
                return;
            }

            // Bone Meal: Green Thumb
            if (item.getType() == Material.BONE_MEAL) {
                if (!playerManager.hasSkill(player, "fertilize")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (fertilizeCooldown.containsKey(uuid) && (now - fertilizeCooldown.get(uuid)) < 10000) {
                    long rem = (10000 - (now - fertilizeCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Green Thumb em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                fertilizeCooldown.put(uuid, now);
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 20, 2.0, 1.0, 2.0, 0.1);
                player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Crescimento verde estendido!").color(NamedTextColor.GREEN));
                return;
            }

            // Flowers: Flora Regrowth
            if (Tag.FLOWERS.isTagged(item.getType())) {
                if (!playerManager.hasSkill(player, "flora_shield")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (floraShieldCooldown.containsKey(uuid) && (now - floraShieldCooldown.get(uuid)) < 15000) {
                    long rem = (15000 - (now - floraShieldCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Flora Regrowth em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (player.getHealth() < Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue()) {
                    if (!checkAlquimistaPreservation(player)) {
                        item.subtract(1);
                    }
                    player.setHealth(Math.min(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue(), player.getHealth() + 8.0));
                    floraShieldCooldown.put(uuid, now);
                    player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                    player.sendActionBar(Component.text("Flora Regrowth: +4 Corações de Cura!").color(NamedTextColor.GREEN));
                }
                return;
            }

            // Iron Block: Timber Axe
            if (item.getType() == Material.IRON_BLOCK) {
                if (!playerManager.hasSkill(player, "lumberjack")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (lumberjackCooldown.containsKey(uuid) && (now - lumberjackCooldown.get(uuid)) < 60000) {
                    long rem = (60000 - (now - lumberjackCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Timber Axe em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (mainHand.getType().name().endsWith("_AXE")) {
                    if (!checkAlquimistaPreservation(player)) {
                        item.subtract(1);
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 3)); // Haste IV for 5s
                    lumberjackCooldown.put(uuid, now);
                    player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0f, 1.0f);
                    player.sendActionBar(Component.text("Super Haste IV (Timber Axe) ativado por 5s!").color(NamedTextColor.GREEN));
                }
                return;
            }

            // Dirt: Scaffold Leap
            if (item.getType() == Material.DIRT) {
                if (!playerManager.hasSkill(player, "scaffold")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (scaffoldCooldown.containsKey(uuid) && (now - scaffoldCooldown.get(uuid)) < 12000) {
                    long rem = (12000 - (now - scaffoldCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Scaffold Leap em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                player.setVelocity(new org.bukkit.util.Vector(0, 1.0, 0));
                scaffoldCooldown.put(uuid, now);

                Location bloc = player.getLocation().clone().subtract(0, 1, 0);
                if (bloc.getBlock().getType() == Material.AIR) {
                    bloc.getBlock().setType(Material.HAY_BLOCK);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (bloc.getBlock().getType() == Material.HAY_BLOCK) {
                            bloc.getBlock().setType(Material.AIR);
                        }
                    }, 100L); // Auto-removes after 5 seconds (100 ticks)
                }

                player.playSound(player.getLocation(), Sound.BLOCK_GRASS_PLACE, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Scaffold Leap!").color(NamedTextColor.YELLOW));
                return;
            }

            // Stone Bricks: Architect's Focus
            if (item.getType() == Material.STONE_BRICKS) {
                if (!playerManager.hasSkill(player, "architect_focus")) return;
                event.setCancelled(true);
                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 3)); // Resistance IV
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 600, 0)); // Slowness I
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 3)); // Weakness IV
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Architect's Focus Ativado!").color(NamedTextColor.GREEN));
                return;
            }

            // Clay Ball: Reinforce Block
            if (item.getType() == Material.CLAY_BALL) {
                if (!playerManager.hasSkill(player, "unbreakable_block")) return;
                Block target = player.getTargetBlockExact(5);
                if (target != null && target.getType() != Material.AIR && target.getType() != Material.BEDROCK) {
                    event.setCancelled(true);
                    if (!checkAlquimistaPreservation(player)) {
                        item.subtract(1);
                    }
                    Location targetLoc = target.getLocation();
                    reinforcedBlocks.add(targetLoc);
                    player.playSound(targetLoc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.5f);
                    player.sendActionBar(Component.text("Bloco reforçado! Tornou-se inquebrável por 15s.").color(NamedTextColor.AQUA));

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        reinforcedBlocks.remove(targetLoc);
                        player.sendMessage(Component.text("[RPGPlugin] Bloco em " + targetLoc.getBlockX() + ", " + targetLoc.getBlockY() + " perdeu o reforço.").color(NamedTextColor.GRAY));
                    }, 300L); // 15 seconds
                }
                return;
            }

            // Feather: Feather Grace
            if (item.getType() == Material.FEATHER) {
                if (!playerManager.hasSkill(player, "grace")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                long cd = plugin.getConfig().getLong("classes.builder.grace.cooldown", 45) * 1000;

                if (builderGraceCooldown.containsKey(uuid) && (now - builderGraceCooldown.get(uuid)) < cd) {
                    long rem = (cd - (now - builderGraceCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Feather Grace em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                int dur = plugin.getConfig().getInt("classes.builder.grace.duration", 10);
                int jump = plugin.getConfig().getInt("classes.builder.grace.jump", 2);
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, dur * 20, jump));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, dur * 20, 0));
                builderGraceCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Feather Grace Ativada!").color(NamedTextColor.GREEN));
                return;
            }

            // Slime Block: Gravity Defiance
            if (item.getType() == Material.SLIME_BLOCK) {
                if (!playerManager.hasSkill(player, "gravity_defiance")) return;
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (gravityDefianceCooldown.containsKey(uuid) && (now - gravityDefianceCooldown.get(uuid)) < 40000) {
                    long rem = (40000 - (now - gravityDefianceCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Gravity Defiance em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }

                if (!checkAlquimistaPreservation(player)) {
                    item.subtract(1);
                }

                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 0)); // Levitation I for 5s
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 0)); // Slow falling for 10s
                gravityDefianceCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.2f);
                player.sendActionBar(Component.text("Flutuação Gravitacional Ativa!").color(NamedTextColor.AQUA));
                return;
            }
        }

        // Book: open RPG GUI
        if (item.getType() == Material.BOOK) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().contains("Livro de RPG")) {
                event.setCancelled(true);
                openSelectionGUI(player);
            }
        }
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (playerManager.hasClass(player, "EXPLORER") && item.getType() == Material.POTION) {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof PotionMeta) {
                PotionMeta potMeta = (PotionMeta) meta;
                if (potMeta.getBasePotionType() == PotionType.WATER && playerManager.hasSkill(player, "hydration")) {
                    int hunger = plugin.getConfig().getInt("classes.explorer.hydration.hunger", 1);
                    double sat = plugin.getConfig().getDouble("classes.explorer.hydration.saturation", 0.5);

                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + hunger));
                    player.setSaturation((float) Math.min(20, player.getSaturation() + sat));
                    player.sendMessage(Component.text("Hidratado! Fome regenerada.").color(NamedTextColor.AQUA));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String currentClass = playerManager.getPlayerClass(player);
        if (currentClass.equalsIgnoreCase("NONE")) return;

        Block under = player.getLocation().clone().subtract(0, 0.1, 0).getBlock();

        // --- EXPLORER MOVE MECHANICS ---
        if (currentClass.equalsIgnoreCase("EXPLORER")) {
            if (playerManager.hasSkill(player, "jump_boost")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 1, true, false, false));
            }
        }

        // --- MINER MOVE MECHANICS ---
        if (currentClass.equalsIgnoreCase("MINER")) {
            if (playerManager.hasSkill(player, "sight")) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, true, false, false));
            }
        }

        // --- BUILDER MOVE MECHANICS ---
        if (currentClass.equalsIgnoreCase("BUILDER")) {
            // Canopy Step
            if (playerManager.hasSkill(player, "canopy_step")) {
                if (under.getType() == Material.GRASS_BLOCK || Tag.LEAVES.isTagged(under.getType())) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, true, false, false)); // Speed II
                }
            }

            // Silvicultor Synergy (3+ Silvicultor skills unlocked)
            if (playerManager.getSubtypeCount(player, "Silvicultor") >= 3) {
                if (under.getType() == Material.GRASS_BLOCK || Tag.LEAVES.isTagged(under.getType())) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0, true, false, false)); // Regen I
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String currentClass = playerManager.getPlayerClass(player);
        if (currentClass.equalsIgnoreCase("NONE")) return;

        Block block = event.getBlock();

        // --- REINFORCED BLOCK PROTECTION ---
        if (reinforcedBlocks.contains(block.getLocation())) {
            event.setCancelled(true);
            player.sendMessage(Component.text("§cEste bloco foi reforçado e é temporariamente indestrutível!"));
            return;
        }

        // --- MINER BREAK MECHANICS ---
        if (currentClass.equalsIgnoreCase("MINER")) {
            // Stone Smash: Haste I when breaking stone if holding a block
            if (playerManager.hasSkill(player, "stone_smash")) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType() == Material.STONE || hand.getType() == Material.COBBLESTONE) {
                    if (block.getType() == Material.STONE || block.getType() == Material.COBBLESTONE || block.getType() == Material.DEEPSLATE) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 0, true, false, false));
                    }
                }
            }

            // Double Ore (Lucky Miner)
            if (playerManager.hasSkill(player, "double_ore") && block.getType() == Material.COAL_ORE) {
                if (Math.random() < 0.30) {
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.COAL, 1));
                    player.sendActionBar(Component.text("⛏️ [Lucky Miner] Carvão duplicado!").color(NamedTextColor.GOLD));
                }
            }

            // Molten Touch (Auto-smelt)
            UUID uuid = player.getUniqueId();
            if (playerManager.hasSkill(player, "molten_touch") && moltenTouchActiveUntil.containsKey(uuid) && System.currentTimeMillis() < moltenTouchActiveUntil.get(uuid)) {
                Material smelted = null;
                if (block.getType() == Material.IRON_ORE || block.getType() == Material.DEEPSLATE_IRON_ORE) {
                    smelted = Material.IRON_INGOT;
                } else if (block.getType() == Material.GOLD_ORE || block.getType() == Material.DEEPSLATE_GOLD_ORE) {
                    smelted = Material.GOLD_INGOT;
                } else if (block.getType() == Material.COPPER_ORE || block.getType() == Material.DEEPSLATE_COPPER_ORE) {
                    smelted = Material.COPPER_INGOT;
                }

                if (smelted != null) {
                    event.setDropItems(false);
                    block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smelted, 1));
                    block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.1, 0.1, 0.1, 0.05);
                }
            }
        }

        // --- BUILDER BREAK MECHANICS ---
        if (currentClass.equalsIgnoreCase("BUILDER")) {
            // Silk Touch empty hands
            if (playerManager.hasSkill(player, "silk_touch")) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType() == Material.AIR) {
                    ItemStack silkTool = new ItemStack(Material.NETHERITE_PICKAXE);
                    silkTool.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1);
                    Collection<ItemStack> drops = block.getDrops(silkTool, player);
                    if (!drops.isEmpty()) {
                        event.setDropItems(false);
                        for (ItemStack d : drops) {
                            block.getWorld().dropItemNaturally(block.getLocation(), d);
                        }
                        player.sendActionBar(Component.text("Empty hand Silk Touch!").color(NamedTextColor.GREEN));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        String currentClass = playerManager.getPlayerClass(player);
        if (currentClass.equalsIgnoreCase("NONE")) return;

        // --- EXPLORER DAMAGE ---
        if (currentClass.equalsIgnoreCase("EXPLORER")) {
            if (event.getCause() == DamageCause.FALL) {
                double reduction = plugin.getConfig().getDouble("classes.explorer.fall_reduction", 0.5);
                event.setDamage(event.getDamage() * reduction);
            }
        }

        // --- BUILDER DAMAGE ---
        if (currentClass.equalsIgnoreCase("BUILDER")) {
            // Arquiteto Synergy: -10% damage permanently
            if (playerManager.getSubtypeCount(player, "Arquiteto") >= 3) {
                event.setDamage(event.getDamage() * 0.90);
            }
        }
    }

    private boolean isPickaxe(Material material) {
        return material.name().endsWith("_PICKAXE");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        giveRpgBookIfMissing(event.getPlayer());
        updateSynergiesAndAttributes(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            giveRpgBookIfMissing(event.getPlayer());
            updateSynergiesAndAttributes(event.getPlayer());
        }, 5L);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (item.getType() == Material.BOOK) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().contains("Livro de RPG")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Component.text("§cVocê não pode descartar o seu Livro de RPG!"));
            }
        }
    }

    private void giveRpgBookIfMissing(Player player) {
        boolean hasBook = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.BOOK) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasDisplayName() && meta.getDisplayName().contains("Livro de RPG")) {
                    hasBook = true;
                    break;
                }
            }
        }
        if (!hasBook) {
            ItemStack rpgBook = new ItemStack(Material.BOOK, 1);
            ItemMeta meta = rpgBook.getItemMeta();
            if (meta != null) {
                meta.displayName(Component.text("§6§lLivro de RPG").decoration(TextDecoration.ITALIC, false));
                meta.lore(Arrays.asList(
                    Component.text("§7Use para abrir o Menu de Classes e Skills!"),
                    Component.text("§eClique com o botão direito para abrir.")
                ));
                rpgBook.setItemMeta(meta);
            }
            ItemStack currentItemInSlot8 = player.getInventory().getItem(8);
            if (currentItemInSlot8 == null || currentItemInSlot8.getType() == Material.AIR) {
                player.getInventory().setItem(8, rpgBook);
            } else {
                player.getInventory().addItem(rpgBook);
            }
            player.sendMessage(Component.text("§a§l[RPG] §aVocê recebeu o seu §6§lLivro de RPG§a. Use-o para evoluir!"));
        }
    }
}
