package com.project.rpgplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.attribute.Attribute;
import org.bukkit.Sound;
import org.bukkit.Particle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class ClassListeners implements Listener {

    private final RPGPlugin plugin;
    private final PlayerManager playerManager;

    // Cooldown Maps (UUID -> Timestamp in millis)
    private final Map<UUID, Long> explorerDashCooldown = new HashMap<>();
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
    private final Map<UUID, Long> minerHasteCooldown = new HashMap<>();
    private final Map<UUID, Long> builderGraceCooldown = new HashMap<>();

    private final Set<Location> reinforcedBlocks = new HashSet<>();
    private final Map<UUID, Long> moltenTouchActiveUntil = new HashMap<>();

    private final String guiTitle = "Selecione suas Habilidades RPG";

    public ClassListeners(RPGPlugin plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        startHUDUpdater();
    }

    private void startHUDUpdater() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayerHUD(player);
            }
        }, 40L, 40L);
    }

    private void updatePlayerHUD(Player player) {
        List<String> equipped = playerManager.getEquippedSkills(player);
        List<String> unlocked = playerManager.getUnlockedSkills(player);
        int totalUnlocked = unlocked.size();

        if (equipped.isEmpty()) {
            String msg = "§7[RogueLata] §cNenhuma skill equipada! Use §e/skills";
            player.sendActionBar(Component.text(msg));
            player.sendPlayerListHeaderAndFooter(
                Component.text("§6§lRogueLata §7- §cSem habilidades ativas"),
                Component.text("§eUse /skills para escolher suas skills!")
            );
            return;
        }

        int explorerCount = playerManager.getSkillCountByType(player, "explorer");
        int minerCount = playerManager.getSkillCountByType(player, "miner");
        int builderCount = playerManager.getSkillCountByType(player, "builder");

        double dmgMult = playerManager.getDifficultyDamageMultiplier(player);
        double hungerMult = playerManager.getDifficultyHungerMultiplier(player);

        StringBuilder sb = new StringBuilder("§6[RogueLata] ");
        for (String skill : equipped) {
            String tier = playerManager.getSkillTier(skill);
            String color;
            switch (tier) {
                case "bronze": color = "§6"; break;
                case "silver": color = "§b"; break;
                case "gold": color = "§e"; break;
                default: color = "§7";
            }
            String typeColor;
            String type = playerManager.getSkillType(skill);
            switch (type) {
                case "explorer": typeColor = "§b"; break;
                case "miner": typeColor = "§6"; break;
                case "builder": typeColor = "§2"; break;
                default: typeColor = "§7";
            }
            sb.append(color).append("■").append(typeColor).append(skill.substring(0, Math.min(3, skill.length()))).append("§r ");
        }
        sb.append("§7(").append(equipped.size()).append("/9)");
        sb.append(" §f| §c⬆x").append(String.format("%.2f", dmgMult));

        String actionBar = sb.toString();
        player.sendActionBar(Component.text(actionBar));

        // Header: detailed breakdown
        StringBuilder header = new StringBuilder("§6§lRogueLata§r\n");
        header.append("§7Skills: §e").append(totalUnlocked).append("§7/§e").append(playerManager.getAllSkillKeys().size());
        header.append(" §7| Equipadas: §a").append(equipped.size()).append("§7/§9").append(9);
        header.append("\n§7Dificuldade: Dano §cx").append(String.format("%.2f", dmgMult));
        header.append(" §7Fome §cx").append(String.format("%.2f", hungerMult));

        StringBuilder footer = new StringBuilder();
        footer.append("\n");
        if (explorerCount >= 4) footer.append("§b✦ Sinergia Explorador: Speed I\n");
        if (minerCount >= 4) footer.append("§6✦ Sinergia Minerador: Haste I\n");
        if (builderCount >= 4) footer.append("§2✦ Sinergia Construtor: Regeneracao I\n");
        if (explorerCount < 4 && minerCount < 4 && builderCount < 4) {
            footer.append("§7Nenhuma sinergia ativa (4+ do mesmo tipo)\n");
        }
        footer.append("§e/skills §7para abrir o menu");

        player.sendPlayerListHeaderAndFooter(
            Component.text(header.toString()),
            Component.text(footer.toString())
        );
    }

    public void openSelectionGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, Component.text(guiTitle));

        fillGlass(gui);
        addTierHeaders(gui);
        addSkillsToGUI(gui, player);
        addLegend(gui, player);
        addDifficultyInfo(gui, player);

        player.openInventory(gui);
    }

    private void fillGlass(Inventory gui) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            glass.setItemMeta(meta);
        }
        for (int slot = 0; slot < 9; slot++) {
            gui.setItem(slot, glass);
        }
        for (int slot = 45; slot < 54; slot++) {
            gui.setItem(slot, glass);
        }
    }

    private void addTierHeaders(Inventory gui) {
        ItemStack bronzeHeader = new ItemStack(Material.COPPER_INGOT);
        ItemMeta bMeta = bronzeHeader.getItemMeta();
        if (bMeta != null) {
            bMeta.displayName(Component.text("§6§lBronze §7(Custo: 1 XP)").decoration(TextDecoration.ITALIC, false));
            bMeta.lore(Arrays.asList(Component.text("§7Max 3 equipadas simultaneamente")));
            bronzeHeader.setItemMeta(bMeta);
        }
        gui.setItem(0, bronzeHeader);

        ItemStack silverHeader = new ItemStack(Material.IRON_INGOT);
        ItemMeta sMeta = silverHeader.getItemMeta();
        if (sMeta != null) {
            sMeta.displayName(Component.text("§b§lPrata §7(Custo: 3 XP)").decoration(TextDecoration.ITALIC, false));
            sMeta.lore(Arrays.asList(Component.text("§7Max 3 equipadas simultaneamente")));
            silverHeader.setItemMeta(sMeta);
        }
        gui.setItem(1, silverHeader);

        ItemStack goldHeader = new ItemStack(Material.GOLD_INGOT);
        ItemMeta gMeta = goldHeader.getItemMeta();
        if (gMeta != null) {
            gMeta.displayName(Component.text("§e§lOuro §7(Custo: 5 XP)").decoration(TextDecoration.ITALIC, false));
            gMeta.lore(Arrays.asList(Component.text("§7Max 3 equipadas simultaneamente")));
            goldHeader.setItemMeta(gMeta);
        }
        gui.setItem(2, goldHeader);
    }

    private void addSkillsToGUI(Inventory gui, Player player) {
        List<String> bronze = playerManager.getAllBronzeSkills();
        List<String> silver = playerManager.getAllSilverSkills();
        List<String> gold = playerManager.getAllGoldSkills();
        List<String> unlocked = playerManager.getUnlockedSkills(player);
        List<String> equipped = playerManager.getEquippedSkills(player);

        int bronzeSlot = 9;
        for (String key : bronze) {
            gui.setItem(bronzeSlot++, createTierSkillItem(player, key, unlocked, equipped));
        }

        int silverSlot = 18;
        for (String key : silver) {
            gui.setItem(silverSlot++, createTierSkillItem(player, key, unlocked, equipped));
        }

        int goldSlot = 27;
        for (String key : gold) {
            gui.setItem(goldSlot++, createTierSkillItem(player, key, unlocked, equipped));
        }
    }

    private ItemStack createTierSkillItem(Player player, String skillKey, List<String> unlocked, List<String> equipped) {
        Material mat = playerManager.determineSkillMaterial(skillKey);
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        String tier = playerManager.getSkillTier(skillKey);
        String displayName = playerManager.getSkillDisplayName(skillKey);
        String desc = playerManager.getSkillDescription(skillKey);
        String type = playerManager.getSkillType(skillKey);
        int cost = playerManager.getSkillCost(skillKey);
        boolean isUnlocked = unlocked.contains(skillKey);
        boolean isEquipped = equipped.contains(skillKey);
        boolean isPassive = !playerManager.isSkillEquippable(skillKey);

        String typeColor;
        switch (type) {
            case "explorer": typeColor = "§b"; break;
            case "miner": typeColor = "§6"; break;
            case "builder": typeColor = "§2"; break;
            default: typeColor = "§7";
        }

        String tierPrefix;
        switch (tier) {
            case "bronze": tierPrefix = "§6[Bronze] "; break;
            case "silver": tierPrefix = "§b[Prata] "; break;
            case "gold": tierPrefix = "§e[Ouro] "; break;
            default: tierPrefix = "§7[] "; 
        }

        meta.displayName(Component.text(tierPrefix + displayName).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7" + desc));
        lore.add(Component.text(typeColor + "Tipo: " + type.substring(0, 1).toUpperCase() + type.substring(1)));
        lore.add(Component.text(" "));

        if (isUnlocked) {
            if (!isPassive) {
                if (isEquipped) {
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    lore.add(Component.text("§a✔ Equipada!"));
                    lore.add(Component.text("§eClique para desequipar."));
                } else {
                    int tierCount = playerManager.countEquippedByTier(player, tier);
                    lore.add(Component.text("§7Disponivel (" + tierCount + "/3 equipadas no tier)"));
                    lore.add(Component.text("§eClique para equipar."));
                }
            } else {
                lore.add(Component.text("§a✔ Desbloqueada (Passiva permanente)"));
            }
        } else {
            lore.add(Component.text("§c✕ Bloqueada"));
            lore.add(Component.text("§eCusto: " + cost + " XP"));
            lore.add(Component.text("§eClique para desbloquear com XP!"));
        }

        lore.add(Component.text(" "));
        lore.add(Component.text(typeColor + "✦ " + type.substring(0, 1).toUpperCase() + type.substring(1)));

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void addLegend(Inventory gui, Player player) {
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§6§lSuas Habilidades").decoration(TextDecoration.ITALIC, false));
            List<String> unlockedList = playerManager.getUnlockedSkills(player);
            List<String> equippedList = playerManager.getEquippedSkills(player);
            List<String> lore = new ArrayList<>();
            lore.add("§7Desbloqueadas: " + unlockedList.size() + "/" + playerManager.getAllSkillKeys().size());
            lore.add("§7Equipadas: " + equippedList.size() + "/9 (max 3 por tier)");
            lore.add(" ");
            if (!equippedList.isEmpty()) {
                lore.add("§eEquipadas:");
                for (String eq : equippedList) {
                    lore.add(" §a- " + playerManager.getSkillDisplayName(eq));
                }
            } else {
                lore.add("§cNenhuma habilidade equipada.");
            }
            int explorer = playerManager.getSkillCountByType(player, "explorer");
            int miner = playerManager.getSkillCountByType(player, "miner");
            int builder = playerManager.getSkillCountByType(player, "builder");
            lore.add(" ");
            lore.add("§bExplorador: " + explorer + "/4 " + (explorer >= 4 ? "§a(SINERGIA ATIVA!)" : ""));
            lore.add("§6Minerador: " + miner + "/4 " + (miner >= 4 ? "§a(SINERGIA ATIVA!)" : ""));
            lore.add("§2Construtor: " + builder + "/4 " + (builder >= 4 ? "§a(SINERGIA ATIVA!)" : ""));
            if (explorer >= 4) lore.add(" §b+ Velocidade Permanente");
            if (miner >= 4) lore.add(" §6+ Haste Permanente");
            if (builder >= 4) lore.add(" §2+ Regeneracao Permanente");
            meta.lore(lore.stream().map(l -> (Component) Component.text(l)).toList());
            info.setItemMeta(meta);
        }
        gui.setItem(4, info);

        List<String> unlockedForDiff = playerManager.getUnlockedSkills(player);
        ItemStack difficulty = new ItemStack(Material.SKELETON_SKULL);
        ItemMeta dMeta = difficulty.getItemMeta();
        if (dMeta != null) {
            int count = unlockedForDiff.size();
            double dmgMult = playerManager.getDifficultyDamageMultiplier(player);
            double hungerMult = playerManager.getDifficultyHungerMultiplier(player);
            dMeta.displayName(Component.text("§c§lDificuldade Mundial").decoration(TextDecoration.ITALIC, false));
            dMeta.lore(Arrays.asList(
                Component.text("§7Quanto mais habilidades, maior o desafio!"),
                Component.text(" "),
                Component.text("§7Dano recebido: §cx" + String.format("%.2f", dmgMult)),
                Component.text("§7Fome: §cx" + String.format("%.2f", hungerMult)),
                Component.text(" "),
                Component.text("§8" + count + " habilidades desbloqueadas")
            ));
            difficulty.setItemMeta(dMeta);
        }
        gui.setItem(5, difficulty);
    }

    private void addDifficultyInfo(Inventory gui, Player player) {
        // Synergy status row
        int explorer = playerManager.getSkillCountByType(player, "explorer");
        int miner = playerManager.getSkillCountByType(player, "miner");
        int builder = playerManager.getSkillCountByType(player, "builder");

        ItemStack passives = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = passives.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§5§lSinergias Ativas").decoration(TextDecoration.ITALIC, false));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§74+ habilidades do mesmo tipo = passiva global"));
            lore.add(Component.text(" "));
            lore.add(Component.text("§bExplorador: " + explorer + "/4 " + (explorer >= 4 ? "§a✔ Speed" : "§7-")));
            lore.add(Component.text("§6Minerador: " + miner + "/4 " + (miner >= 4 ? "§a✔ Haste" : "§7-")));
            lore.add(Component.text("§2Construtor: " + builder + "/4 " + (builder >= 4 ? "§a✔ Regeneracao" : "§7-")));
            meta.lore(lore);
            passives.setItemMeta(meta);
        }
        gui.setItem(6, passives);

        ItemStack deathInfo = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta deathMeta = deathInfo.getItemMeta();
        if (deathMeta != null) {
            deathMeta.displayName(Component.text("§4§lMorte = Progresso Perdido!").decoration(TextDecoration.ITALIC, false));
            deathMeta.lore(Arrays.asList(
                Component.text("§c§l⚠ MODO ROGUE-LIKE ATIVO!"),
                Component.text("§7Ao morrer, voce perde TODAS as"),
                Component.text("§7habilidades, itens e XP acumulados!"),
                Component.text(" "),
                Component.text("§eApenas o Livro de RPG e preservado."),
                Component.text("§4Este e o preco do poder!")
            ));
            deathInfo.setItemMeta(deathMeta);
        }
        gui.setItem(7, deathInfo);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().title().equals(Component.text(guiTitle))) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (slot < 9 || slot >= 45) return;

        String skillKey = getSkillKeyFromSlot(slot);
        if (skillKey == null) return;

        List<String> unlocked = playerManager.getUnlockedSkills(player);
        List<String> equipped = playerManager.getEquippedSkills(player);
        String tier = playerManager.getSkillTier(skillKey);
        boolean isPassive = !playerManager.isSkillEquippable(skillKey);

        if (!unlocked.contains(skillKey)) {
            // Try to unlock
            int cost = playerManager.getSkillCost(skillKey);
            if (player.getLevel() < cost) {
                player.sendMessage(Component.text("§cXP insuficiente! Custo: " + cost + " niveis. Voce tem: " + player.getLevel()));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return;
            }
            player.setLevel(player.getLevel() - cost);
            playerManager.unlockSkill(player, skillKey);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage(Component.text("§a§l[RogueLata] §aHabilidade desbloqueada: " + playerManager.getSkillDisplayName(skillKey) + "! Gastou " + cost + " XP."));

            if (isPassive) {
                player.sendMessage(Component.text("§ePassiva ativada permanentemente!"));
            }

            openSelectionGUI(player);
            return;
        }

        if (isPassive) {
            player.sendMessage(Component.text("§eEsta habilidade e passiva e sempre fica ativa!"));
            return;
        }

        if (equipped.contains(skillKey)) {
            playerManager.unequipSkill(player, skillKey);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
            player.sendMessage(Component.text("§eHabilidade desequipada: " + playerManager.getSkillDisplayName(skillKey)));
            openSelectionGUI(player);
            return;
        }

        int tierCount = playerManager.countEquippedByTier(player, tier);
        if (tierCount >= 3) {
            player.sendMessage(Component.text("§cVoce ja tem 3 habilidades " + tier + " equipadas! Desequipe uma primeiro."));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        int totalEquipped = equipped.size();
        if (totalEquipped >= 9) {
            player.sendMessage(Component.text("§cVoce ja tem 9 habilidades equipadas (max)! Desequipe uma primeiro."));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        playerManager.equipSkill(player, skillKey);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
        player.sendMessage(Component.text("§aHabilidade equipada: " + playerManager.getSkillDisplayName(skillKey)));
        openSelectionGUI(player);
    }

    private String getSkillKeyFromSlot(int slot) {
        List<String> bronze = playerManager.getAllBronzeSkills();
        List<String> silver = playerManager.getAllSilverSkills();
        List<String> gold = playerManager.getAllGoldSkills();

        int bronzeIdx = slot - 9;
        if (bronzeIdx >= 0 && bronzeIdx < bronze.size()) {
            return bronze.get(bronzeIdx);
        }

        int silverIdx = slot - 18;
        if (silverIdx >= 0 && silverIdx < silver.size()) {
            return silver.get(silverIdx);
        }

        int goldIdx = slot - 27;
        if (goldIdx >= 0 && goldIdx < gold.size()) {
            return gold.get(goldIdx);
        }

        return null;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        // Book check FIRST — works regardless of equipped skills
        if (item.getType() == Material.BOOK) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().contains("Livro de RPG")) {
                event.setCancelled(true);
                openSelectionGUI(player);
                return;
            }
        }

        List<String> equipped = playerManager.getEquippedSkills(player);
        if (equipped.isEmpty()) return;

        // --- EXPLORER SKILL INTERACTIONS ---
        if (hasEquippedType(player, "explorer")) {

            if (Tag.FLOWERS.isTagged(item.getType()) && equipped.contains("dash")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (explorerDashCooldown.containsKey(uuid) && (now - explorerDashCooldown.get(uuid)) < 30000) {
                    long rem = (30000 - (now - explorerDashCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Dash em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
                explorerDashCooldown.put(uuid, now);
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05);
                player.sendActionBar(Component.text("Dash das Flores Ativado!").color(NamedTextColor.AQUA));
                return;
            }

            if (item.getType() == Material.SUGAR && equipped.contains("step_assist")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (stepAssistCooldown.containsKey(uuid) && (now - stepAssistCooldown.get(uuid)) < 15000) {
                    long rem = (15000 - (now - stepAssistCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Passo Agil em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 1));
                stepAssistCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_GALLOP, 1.0f, 1.2f);
                player.sendActionBar(Component.text("Passo Agil: Speed II por 15s!").color(NamedTextColor.YELLOW));
                return;
            }

            if (item.getType() == Material.SLIME_BALL && equipped.contains("grapple")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (grappleCooldown.containsKey(uuid) && (now - grappleCooldown.get(uuid)) < 10000) {
                    long rem = (10000 - (now - grappleCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Salto Escalador em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                player.setVelocity(player.getLocation().getDirection().multiply(1.5).setY(0.6));
                grappleCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1.0f, 1.2f);
                player.sendActionBar(Component.text("Salto Escalador!").color(NamedTextColor.GREEN));
                return;
            }

            if (item.getType() == Material.LAPIS_LAZULI && equipped.contains("water_breathing")) {
                event.setCancelled(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 300, 2));
                item.subtract(1);
                player.sendActionBar(Component.text("Respiracao Aquatica Ativada!").color(NamedTextColor.BLUE));
                return;
            }

            if (item.getType() == Material.DRAGON_BREATH && equipped.contains("recall")) {
                event.setCancelled(true);
                player.teleport(player.getWorld().getSpawnLocation());
                item.subtract(1);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Recall! Teleportado ao Spawn!").color(NamedTextColor.LIGHT_PURPLE));
                return;
            }

            if (item.getType() == Material.MAGMA_CREAM && equipped.contains("thermal_resistance")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (thermalResistanceCooldown.containsKey(uuid) && (now - thermalResistanceCooldown.get(uuid)) < 30000) {
                    long rem = (30000 - (now - thermalResistanceCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Escudo de Lava em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 300, 0));
                thermalResistanceCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Escudo de Lava: Fire Resist 15s!").color(NamedTextColor.GOLD));
                return;
            }

            if (item.getType() == Material.AMETHYST_SHARD && equipped.contains("sonar")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (sonarCooldown.containsKey(uuid) && (now - sonarCooldown.get(uuid)) < 20000) {
                    long rem = (20000 - (now - sonarCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Sonar em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                sonarCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Sonar ativo! Localizando entidades...").color(NamedTextColor.DARK_PURPLE));
                for (Entity entity : player.getNearbyEntities(15, 15, 15)) {
                    if (entity instanceof LivingEntity && entity != player) {
                        entity.getWorld().spawnParticle(Particle.GLOW, entity.getLocation().add(0, 1, 0), 10, 0.2, 0.5, 0.2, 0);
                    }
                }
                return;
            }

            if (item.getType() == Material.GUNPOWDER && equipped.contains("wind_burst")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (windBurstCooldown.containsKey(uuid) && (now - windBurstCooldown.get(uuid)) < 25000) {
                    long rem = (25000 - (now - windBurstCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Explosao de Vento em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                player.setVelocity(new org.bukkit.util.Vector(0, 1.3, 0));
                windBurstCooldown.put(uuid, now);
                player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
                player.playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_THROW, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Explosao de Vento!").color(NamedTextColor.WHITE));
                return;
            }

            if (item.getType() == Material.ENDER_PEARL && equipped.contains("dim_shift")) {
                event.setCancelled(true);
                item.subtract(1);
                org.bukkit.util.Vector dir = player.getLocation().getDirection().normalize().multiply(8);
                player.teleport(player.getLocation().add(dir));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 2));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Mudanca Dimensional! Speed IV + Drawbacks.").color(NamedTextColor.DARK_PURPLE));
                return;
            }
        }

        // --- MINER SKILL INTERACTIONS ---
        if (hasEquippedType(player, "miner")) {

            if ((item.getType() == Material.COAL || item.getType() == Material.CHARCOAL) && equipped.contains("diet")) {
                event.setCancelled(true);
                if (player.getFoodLevel() < 20) {
                    item.subtract(1);
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + 4));
                    player.setSaturation((float) Math.min(20, player.getSaturation() + 2.0));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);
                    player.sendActionBar(Component.text("Dieta de Carvao!").color(NamedTextColor.GOLD));
                }
                return;
            }

            if (item.getType() == Material.TORCH && equipped.contains("torch_light")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (torchLightCooldown.containsKey(uuid) && (now - torchLightCooldown.get(uuid)) < 30000) {
                    long rem = (30000 - (now - torchLightCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Luz de Tocha em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0));
                torchLightCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.2f);
                player.sendActionBar(Component.text("Visao Noturna 30s!").color(NamedTextColor.YELLOW));
                return;
            }

            if (item.getType() == Material.GLOWSTONE_DUST && equipped.contains("ore_sonar")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (oreSonarCooldown.containsKey(uuid) && (now - oreSonarCooldown.get(uuid)) < 30000) {
                    long rem = (30000 - (now - oreSonarCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Radar de Minerio em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                oreSonarCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ENTITY_ALLAY_DEATH, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Radar de Minerio ativo!").color(NamedTextColor.GOLD));
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

            if (item.getType() == Material.FLINT && equipped.contains("molten_touch")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (moltenTouchCooldown.containsKey(uuid) && (now - moltenTouchCooldown.get(uuid)) < 45000) {
                    long rem = (45000 - (now - moltenTouchCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Toque de Fusao em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                moltenTouchActiveUntil.put(uuid, now + 30000);
                moltenTouchCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Toque de Fusao: Minerios fundem por 30s!").color(NamedTextColor.RED));
                return;
            }

            if (item.getType() == Material.OBSIDIAN && equipped.contains("gravity_shield")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (gravityShieldCooldown.containsKey(uuid) && (now - gravityShieldCooldown.get(uuid)) < 45000) {
                    long rem = (45000 - (now - gravityShieldCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Escudo Gravitacional em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 300, 2));
                gravityShieldCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 0.5f);
                player.sendActionBar(Component.text("Escudo Gravitacional: Resistencia III 15s!").color(NamedTextColor.DARK_GRAY));
                return;
            }

            if (item.getType() == Material.REDSTONE_BLOCK && equipped.contains("core_overdrive")) {
                event.setCancelled(true);
                item.subtract(1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 400, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 400, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 300, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 300, 2));
                player.playSound(player.getLocation(), Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Sobrecarga do Nucleo Ativada!").color(NamedTextColor.RED));
                return;
            }

            if (item.getType() == Material.GOLD_INGOT && equipped.contains("haste")) {
                event.setCancelled(true);
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (isPickaxe(mainHand.getType())) {
                    UUID uuid = player.getUniqueId();
                    long now = System.currentTimeMillis();
                    if (minerHasteCooldown.containsKey(uuid) && (now - minerHasteCooldown.get(uuid)) < 20000) {
                        long rem = (20000 - (now - minerHasteCooldown.get(uuid))) / 1000;
                        player.sendActionBar(Component.text("Febre do Ouro em cooldown! " + rem + "s").color(NamedTextColor.RED));
                        return;
                    }
                    item.subtract(1);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 300, 1));
                    minerHasteCooldown.put(uuid, now);
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                    player.sendActionBar(Component.text("Febre do Ouro: Haste II!").color(NamedTextColor.GOLD));
                }
                return;
            }

            if (item.getType() == Material.IRON_INGOT && equipped.contains("ore_repair")) {
                event.setCancelled(true);
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (mainHand.getType() == Material.IRON_PICKAXE || mainHand.getType() == Material.DIAMOND_PICKAXE || mainHand.getType() == Material.NETHERITE_PICKAXE) {
                    Damageable dmg = (Damageable) mainHand.getItemMeta();
                    if (dmg != null && dmg.getDamage() > 0) {
                        item.subtract(1);
                        int rep = (int) (mainHand.getType().getMaxDurability() * 0.30);
                        dmg.setDamage(Math.max(0, dmg.getDamage() - rep));
                        mainHand.setItemMeta(dmg);
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.0f);
                        player.sendActionBar(Component.text("Picareta reparada +30%!").color(NamedTextColor.GREEN));
                    }
                }
                return;
            }

            if (item.getType() == Material.IRON_INGOT && equipped.contains("transmutation") && item.getAmount() >= 5) {
                event.setCancelled(true);
                item.subtract(5);
                player.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, 1));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.5f);
                player.sendActionBar(Component.text("Transmutacao: 5 Ferro -> 1 Ouro!").color(NamedTextColor.YELLOW));
                return;
            }

            if (item.getType() == Material.GOLD_INGOT && equipped.contains("transmutation") && item.getAmount() >= 5) {
                event.setCancelled(true);
                item.subtract(5);
                player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 1.8f);
                player.sendActionBar(Component.text("Transmutacao: 5 Ouro -> 1 Diamante!").color(NamedTextColor.AQUA));
                return;
            }
        }

        // --- BUILDER SKILL INTERACTIONS ---
        if (hasEquippedType(player, "builder")) {

            if (Tag.LEAVES.isTagged(item.getType()) && equipped.contains("feast")) {
                event.setCancelled(true);
                if (player.getFoodLevel() < 20) {
                    item.subtract(1);
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + 2));
                    player.setSaturation((float) Math.min(20, player.getSaturation() + 0.8));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.2f);
                    player.sendActionBar(Component.text("Banquete de Folhas!").color(NamedTextColor.GREEN));
                }
                return;
            }

            if (item.getType() == Material.WHEAT && equipped.contains("woodcutter")) {
                event.setCancelled(true);
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (mainHand.getType().name().endsWith("_AXE")) {
                    UUID uuid = player.getUniqueId();
                    long now = System.currentTimeMillis();
                    if (woodcutterCooldown.containsKey(uuid) && (now - woodcutterCooldown.get(uuid)) < 15000) {
                        long rem = (15000 - (now - woodcutterCooldown.get(uuid))) / 1000;
                        player.sendActionBar(Component.text("Lenhador Rapido em cooldown! " + rem + "s").color(NamedTextColor.RED));
                        return;
                    }
                    item.subtract(1);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 200, 0));
                    woodcutterCooldown.put(uuid, now);
                    player.playSound(player.getLocation(), Sound.ENTITY_COW_MILK, 1.0f, 1.0f);
                    player.sendActionBar(Component.text("Lenhador Rapido: Haste I!").color(NamedTextColor.GREEN));
                }
                return;
            }

            if (item.getType() == Material.BONE_MEAL && equipped.contains("fertilize")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (fertilizeCooldown.containsKey(uuid) && (now - fertilizeCooldown.get(uuid)) < 10000) {
                    long rem = (10000 - (now - fertilizeCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Adubo Verde em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                fertilizeCooldown.put(uuid, now);
                player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation(), 20, 2.0, 1.0, 2.0, 0.1);
                player.playSound(player.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Adubo Verde!").color(NamedTextColor.GREEN));
                return;
            }

            if (Tag.FLOWERS.isTagged(item.getType()) && equipped.contains("flora_shield")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (floraShieldCooldown.containsKey(uuid) && (now - floraShieldCooldown.get(uuid)) < 15000) {
                    long rem = (15000 - (now - floraShieldCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Escudo Floral em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                if (player.getHealth() < Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue()) {
                    item.subtract(1);
                    player.setHealth(Math.min(Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue(), player.getHealth() + 8.0));
                    floraShieldCooldown.put(uuid, now);
                    player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                    player.sendActionBar(Component.text("Escudo Floral: +4 Coracoes!").color(NamedTextColor.GREEN));
                }
                return;
            }

            if (item.getType() == Material.IRON_BLOCK && equipped.contains("lumberjack")) {
                event.setCancelled(true);
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                if (mainHand.getType().name().endsWith("_AXE")) {
                    UUID uuid = player.getUniqueId();
                    long now = System.currentTimeMillis();
                    if (lumberjackCooldown.containsKey(uuid) && (now - lumberjackCooldown.get(uuid)) < 60000) {
                        long rem = (60000 - (now - lumberjackCooldown.get(uuid))) / 1000;
                        player.sendActionBar(Component.text("Golpe do Lenhador em cooldown! " + rem + "s").color(NamedTextColor.RED));
                        return;
                    }
                    item.subtract(1);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 3));
                    lumberjackCooldown.put(uuid, now);
                    player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0f, 1.0f);
                    player.sendActionBar(Component.text("Golpe do Lenhador: Haste IV 5s!").color(NamedTextColor.GREEN));
                }
                return;
            }

            if (item.getType() == Material.DIRT && equipped.contains("scaffold")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (scaffoldCooldown.containsKey(uuid) && (now - scaffoldCooldown.get(uuid)) < 12000) {
                    long rem = (12000 - (now - scaffoldCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Salto do Andaime em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                player.setVelocity(new org.bukkit.util.Vector(0, 1.0, 0));
                scaffoldCooldown.put(uuid, now);
                Location bloc = player.getLocation().clone().subtract(0, 1, 0);
                if (bloc.getBlock().getType() == Material.AIR) {
                    bloc.getBlock().setType(Material.HAY_BLOCK);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (bloc.getBlock().getType() == Material.HAY_BLOCK) {
                            bloc.getBlock().setType(Material.AIR);
                        }
                    }, 100L);
                }
                player.playSound(player.getLocation(), Sound.BLOCK_GRASS_PLACE, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Salto do Andaime!").color(NamedTextColor.YELLOW));
                return;
            }

            if (item.getType() == Material.STONE_BRICKS && equipped.contains("architect_focus")) {
                event.setCancelled(true);
                item.subtract(1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 600, 3));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 600, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 3));
                player.playSound(player.getLocation(), Sound.BLOCK_STONE_PLACE, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Foco do Arquiteto: Resistencia IV!").color(NamedTextColor.GREEN));
                return;
            }

            if (item.getType() == Material.CLAY_BALL && equipped.contains("unbreakable_block")) {
                Block target = player.getTargetBlockExact(5);
                if (target != null && target.getType() != Material.AIR && target.getType() != Material.BEDROCK) {
                    event.setCancelled(true);
                    item.subtract(1);
                    Location targetLoc = target.getLocation();
                    reinforcedBlocks.add(targetLoc);
                    player.playSound(targetLoc, Sound.BLOCK_ANVIL_PLACE, 1.0f, 1.5f);
                    player.sendActionBar(Component.text("Bloco Reforcado: Inquebravel por 15s!").color(NamedTextColor.AQUA));
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        reinforcedBlocks.remove(targetLoc);
                    }, 300L);
                }
                return;
            }

            if (item.getType() == Material.FEATHER && equipped.contains("grace")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (builderGraceCooldown.containsKey(uuid) && (now - builderGraceCooldown.get(uuid)) < 45000) {
                    long rem = (45000 - (now - builderGraceCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Graca da Pena em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 200, 2));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 0));
                builderGraceCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.0f, 1.0f);
                player.sendActionBar(Component.text("Graca da Pena: Super Salto + Slow Fall!").color(NamedTextColor.GREEN));
                return;
            }

            if (item.getType() == Material.SLIME_BLOCK && equipped.contains("gravity_defiance")) {
                event.setCancelled(true);
                UUID uuid = player.getUniqueId();
                long now = System.currentTimeMillis();
                if (gravityDefianceCooldown.containsKey(uuid) && (now - gravityDefianceCooldown.get(uuid)) < 40000) {
                    long rem = (40000 - (now - gravityDefianceCooldown.get(uuid))) / 1000;
                    player.sendActionBar(Component.text("Desafio Gravitacional em cooldown! " + rem + "s").color(NamedTextColor.RED));
                    return;
                }
                item.subtract(1);
                player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 0));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 0));
                gravityDefianceCooldown.put(uuid, now);
                player.playSound(player.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 1.0f, 1.2f);
                player.sendActionBar(Component.text("Desafio Gravitacional: Flutuar!").color(NamedTextColor.AQUA));
                return;
            }

            if (item.getType() == Material.SHEARS && equipped.contains("silk_touch")) {
                event.setCancelled(true);
                player.sendActionBar(Component.text("Use maos vazias para Toque de Seda!").color(NamedTextColor.GREEN));
                return;
            }
        }

    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        List<String> equipped = playerManager.getEquippedSkills(player);

        if (equipped.contains("hydration") && item.getType() == Material.POTION) {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof PotionMeta) {
                PotionMeta potMeta = (PotionMeta) meta;
                if (potMeta.getBasePotionType() == PotionType.WATER) {
                    player.setFoodLevel(Math.min(20, player.getFoodLevel() + 1));
                    player.setSaturation((float) Math.min(20, player.getSaturation() + 0.5));
                    player.sendMessage(Component.text("Hidratado! Fome regenerada.").color(NamedTextColor.AQUA));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        List<String> equipped = playerManager.getEquippedSkills(player);
        if (equipped.isEmpty()) return;

        Block under = player.getLocation().clone().subtract(0, 0.1, 0).getBlock();

        if (equipped.contains("jump_boost")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 1, true, false, false));
        }

        if (equipped.contains("sight")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, true, false, false));
        }

        if (equipped.contains("canopy_step")) {
            if (under.getType() == Material.GRASS_BLOCK || Tag.LEAVES.isTagged(under.getType())) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, true, false, false));
            }
        }

        if (equipped.contains("safe_fall")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, true, false, false));
        }

        playerManager.applySynergyEffects(player);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        List<String> equipped = playerManager.getEquippedSkills(player);
        if (equipped.isEmpty()) return;

        if (reinforcedBlocks.contains(block.getLocation())) {
            event.setCancelled(true);
            player.sendMessage(Component.text("§cEste bloco foi reforcado e e temporariamente indestrutivel!"));
            return;
        }

        if (equipped.contains("stone_smash")) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType() == Material.STONE || hand.getType() == Material.COBBLESTONE) {
                if (block.getType() == Material.STONE || block.getType() == Material.COBBLESTONE || block.getType() == Material.DEEPSLATE) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 40, 0, true, false, false));
                }
            }
        }

        if (equipped.contains("silk_touch")) {
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
                    player.sendActionBar(Component.text("Toque de Seda Manual!").color(NamedTextColor.GREEN));
                }
            }
        }

        // Molten Touch (auto-smelt)
        UUID uuid = player.getUniqueId();
        if (equipped.contains("molten_touch") && moltenTouchActiveUntil.containsKey(uuid) && System.currentTimeMillis() < moltenTouchActiveUntil.get(uuid)) {
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

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        List<String> equipped = playerManager.getEquippedSkills(player);
        if (equipped.isEmpty()) return;

        double difficultyDmg = playerManager.getDifficultyDamageMultiplier(player);
        double finalDmg = event.getDamage() * difficultyDmg;

        if (equipped.contains("safe_fall") && event.getCause() == DamageCause.FALL) {
            finalDmg *= 0.5;
        }

        event.setDamage(finalDmg);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<String> unlocked = playerManager.getUnlockedSkills(player);
        if (unlocked.isEmpty()) return;

        playerManager.clearAllSkills(player);
        player.sendMessage(Component.text("§4§l╔══════════════════════════════════╗"));
        player.sendMessage(Component.text("§4§l║   VOCE MORREU!                  ║"));
        player.sendMessage(Component.text("§4§l║   Todas as habilidades foram     ║"));
        player.sendMessage(Component.text("§4§l║   perdidas! (Modo Rogue-Like)    ║"));
        player.sendMessage(Component.text("§4§l║   Comece novamente no spawn!    ║"));
        player.sendMessage(Component.text("§4§l╚══════════════════════════════════╝"));

        Location spawn = player.getWorld().getSpawnLocation();
        player.teleport(spawn);

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    for (String line : meta.getLore()) {
                        if (line.contains("Skill Item:")) {
                            player.getInventory().remove(item);
                            break;
                        }
                    }
                }
            }
        }

        player.setLevel(0);
        player.setExp(0);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        event.setRespawnLocation(player.getWorld().getSpawnLocation());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            giveRpgBookIfMissing(player);
            player.setLevel(0);
            player.setExp(0);
        }, 5L);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (item.getType() == Material.BOOK) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.getDisplayName().contains("Livro de RPG")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Component.text("§cVoce nao pode descartar o Livro de RPG!"));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        giveRpgBookIfMissing(event.getPlayer());
        playerManager.applySynergyEffects(event.getPlayer());
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
                    Component.text("§7Use para abrir o Menu de Habilidades!"),
                    Component.text("§eClique com o direito para abrir.")
                ));
                rpgBook.setItemMeta(meta);
            }
            ItemStack currentItemInSlot8 = player.getInventory().getItem(8);
            if (currentItemInSlot8 == null || currentItemInSlot8.getType() == Material.AIR) {
                player.getInventory().setItem(8, rpgBook);
            } else {
                player.getInventory().addItem(rpgBook);
            }
            player.sendMessage(Component.text("§a§l[RogueLata] §aVoce recebeu o §6§lLivro de RPG§a. Use-o para evoluir!"));
        }
    }

    @EventHandler
    public void onPlayerFoodLevelChange(org.bukkit.event.entity.FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        int unlocked = playerManager.getTotalUnlockedCount(player);
        if (unlocked > 0 && event.getFoodLevel() < player.getFoodLevel()) {
            double hungerMult = playerManager.getDifficultyHungerMultiplier(player);
            int extraHunger = (int) Math.round((player.getFoodLevel() - event.getFoodLevel()) * (hungerMult - 1.0));
            if (extraHunger > 0) {
                player.setFoodLevel(Math.max(0, player.getFoodLevel() - extraHunger));
            }
        }
    }

    private boolean hasEquippedType(Player player, String type) {
        return playerManager.getSkillCountByType(player, type) > 0;
    }

    private boolean isPickaxe(Material material) {
        return material.name().endsWith("_PICKAXE");
    }
}
