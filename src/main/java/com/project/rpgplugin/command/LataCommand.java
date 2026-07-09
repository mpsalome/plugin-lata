package com.project.rpgplugin.command;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.mob.EliteFactory;
import com.project.rpgplugin.core.mob.MobSpawnService;
import com.project.rpgplugin.core.run.RunManager;
import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.ui.DraftMenu;
import com.project.rpgplugin.ui.ShopMenu;
import com.project.rpgplugin.util.CombatTracker;
import com.project.rpgplugin.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.Material;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LataCommand implements CommandExecutor {

    private static final long TP_COOLDOWN_MS = 180_000;

    private final RPGPlugin plugin;
    private final RunManager runManager;
    private final MobSpawnService mobSpawnService;
    private final Map<UUID, Long> lastTpTime = new ConcurrentHashMap<>();

    public LataCommand(RPGPlugin plugin, RunManager runManager, MobSpawnService mobSpawnService) {
        this.plugin = plugin;
        this.runManager = runManager;
        this.mobSpawnService = mobSpawnService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Apenas jogadores podem usar /" + label + ".").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "tp" -> handleTeleport(player, args, label);
            case "boss" -> handleBossSpawn(player, args, label);
            case "loja" -> handleShop(player);
            case "book", "pao", "bread" -> handleBook(player);
            case "draft" -> handleDraft(player, label);
            default -> sendHelp(player, label);
        }

        return true;
    }

    private void handleTeleport(Player player, String[] args, String label) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<red>Uso: /" + label + " tp <jogador>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Jogador nao encontrado ou offline."));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(Text.mm("<red>Voce nao pode teleportar para si mesmo."));
            return;
        }

        if (CombatTracker.isInCombat(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Voce esta em combate! Aguarde 10s sem tomar dano."));
            return;
        }

        boolean fullHealth = player.getHealth() >= player.getMaxHealth();

        if (!fullHealth) {
            Long last = lastTpTime.get(player.getUniqueId());
            long remaining = last != null ? (TP_COOLDOWN_MS - (System.currentTimeMillis() - last)) : 0;
            if (remaining > 0) {
                player.sendMessage(Text.mm("<red>Teleporte em recarga. Aguarde " + (remaining / 1000) + "s ou recupere a vida total."));
                return;
            }
        }

        player.teleportAsync(target.getLocation());
        lastTpTime.put(player.getUniqueId(), System.currentTimeMillis());

        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        player.sendMessage(Text.mm("<green>Teleportado para <white>" + target.getName() + "</white>!"));
        target.sendMessage(Text.mm("<gray><white>" + player.getName() + "</white> se teleportou ate voce.</gray>"));
    }

    private void handleBossSpawn(Player player, String[] args, String label) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<red>Uso: /" + label + " boss spawn <frostmaw|magma_tyrant|storm_wyvern|void_lich>"));
            return;
        }

        if (!args[1].equalsIgnoreCase("spawn")) {
            player.sendMessage(Text.mm("<red>Uso: /" + label + " boss spawn <frostmaw|magma_tyrant|storm_wyvern|void_lich>"));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(Text.mm("<red>Uso: /" + label + " boss spawn <frostmaw|magma_tyrant|storm_wyvern|void_lich>"));
            return;
        }

        String bossId = args[2].toLowerCase();
        if (!bossId.equals("frostmaw") && !bossId.equals("magma_tyrant")
                && !bossId.equals("storm_wyvern") && !bossId.equals("void_lich")) {
            player.sendMessage(Text.mm("<red>Boss desconhecido. Opcoes: frostmaw, magma_tyrant, storm_wyvern, void_lich"));
            return;
        }

        String bossName = switch (bossId) {
            case "frostmaw" -> "Frostmaw, Senhor do Gelo";
            case "magma_tyrant" -> "Tirano Magmatico, Coracao do Inferno";
            case "storm_wyvern" -> "Furia Tempestuosa, Asa do Ceu";
            case "void_lich" -> "Lich do Vazio, A Noite Eterna";
            default -> bossId;
        };
        int level = 1;
        if (runManager.hasActiveRun(player)) {
            RunState r = runManager.getRun(player);
            if (r != null) level = r.level();
        }
        final int bossLevel = level;

        String hint = switch (bossId) {
            case "frostmaw" -> "<aqua>Dizem que guarda equipamentos congelados...</aqua>";
            case "magma_tyrant" -> "<red>Reza a lenda que carrega o calor do nucleo...</red>";
            case "storm_wyvern" -> "<yellow>Contam que suas asas guardam segredos eletricos...</yellow>";
            case "void_lich" -> "<dark_purple>Sussurros falam de armaduras do esquecimento...</dark_purple>";
            case "sir_creeper_alot" -> "<green>Dizem que sua armadura e tao famosa quanto ele...</green>";
            case "slime_shady" -> "<light_purple>Boato que seus pertences sao bem... escorregadios...</light_purple>";
            case "the_beheader" -> "<dark_red>Falam que seu machado nunca erra o alvo...</dark_red>";
            case "ancient_guardian" -> "<dark_aqua>Lendas contam de tesouros das profundezas...</dark_aqua>";
            case "piglin_warlord" -> "<gold>Segundo os piglins, seu ouro e amaldicoado...</gold>";
            case "phantom_king" -> "<white>Historias de tesouros que flutuam entre ceu e terra...</white>";
            default -> "<gray>Ninguem sabe o que este boss carrega...</gray>";
        };

        Bukkit.broadcast(Text.mm(
            "<gold><bold>\u26A0 " + bossName + "</bold></gold> <gray>|</gray> <yellow>Nivel " + bossLevel + "</yellow>\n"
            + hint
        ));
        player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1.0f, 0.5f);

        player.getScheduler().runDelayed(plugin, st -> {
            if (!player.isOnline()) return;
            spawnBossAtSafeLocation(player, bossId, bossName, bossLevel);
        }, null, 100L);
    }

    private void handleShop(Player player) {
        new ShopMenu(player, plugin).open();
    }

    private void handleBook(Player player) {
        ItemStack book = plugin.createRpgBook();
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(book);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItem(player.getLocation(), book);
            player.sendMessage(Text.mm("<green>Voce recebeu uma Lata de Pao! (caiu no chao — inventario cheio)"));
        } else {
            player.sendMessage(Text.mm("<green>Voce recebeu uma Lata de Pao!"));
        }
    }

    private void handleDraft(Player player, String label) {
        if (!runManager.hasActiveRun(player)) {
            runManager.startRun(player);
        }
        RunState run = runManager.getRun(player);
        if (run == null) {
            player.sendMessage(Text.mm("<red>Erro ao iniciar run."));
            return;
        }

        // Se o jogador fechou o menu sem escolher, reabre a sessao ativa
        var draftService = plugin.getDraftService();
        if (draftService != null) {
            var session = draftService.getActiveSession(player.getUniqueId());
            if (session != null && !session.isDecided()) {
                new DraftMenu(player, session, draftService, run, runManager,
                    plugin.getDraftWeighting(), plugin, plugin.getPlayerLevelListener()).open();
                return;
            }
        }

        if (!run.hasPendingDrafts()) {
            player.sendMessage(Text.mm("<gray>Voce nao tem drafts pendentes. Upe de nivel ou compre uma Carta Avulsa na /" + label + " loja!</gray>"));
            return;
        }
        plugin.getPlayerLevelListener().openNextDraft(player, run);
    }

    private String bossDisplayName(String bossId) {
        EliteFactory.BossDef def = mobSpawnService.getBossDef(bossId);
        if (def != null) return def.displayName();
        return switch (bossId) {
            case "frostmaw" -> "<bold><aqua>Frostmaw <gray>| <white>Senhor do Gelo";
            case "magma_tyrant" -> "<bold><red>Tirano Magmatico <gray>| <white>Coracao do Inferno";
            case "storm_wyvern" -> "<bold><yellow>Furia Tempestuosa <gray>| <white>Asa do Ceu";
            case "void_lich" -> "<bold><dark_purple>Lich do Vazio <gray>| <white>A Noite Eterna";
            default -> bossId;
        };
    }

    public void spawnBossAtSafeLocation(Player player, String bossId, String bossName, int playerLevel) {
        Location playerLoc = player.getLocation();
        var world = playerLoc.getWorld();

        Location safeLoc = computeSafeLocation(playerLoc);

        // Folia-safe: schedule on the target chunk's region
        world.getChunkAtAsync(safeLoc).thenAccept(chunk -> {
            Location finalLoc = safeLoc.clone();

            LivingEntity boss = null;
            EliteFactory.BossDef def = mobSpawnService.getBossDef(bossId);
            if (def != null) {
                boss = mobSpawnService.getEliteFactory().spawnBoss(finalLoc, def.scaleByLevel(playerLevel));
            }

            // Fallback: if boss is null or def was missing, spawn the Titan em Lata
            String displayName = bossDisplayName(bossId);
            if (boss == null) {
                boss = spawnTitanEmLata(finalLoc);
                displayName = "<red><bold>O TITA EM LATA";
            }

            if (boss == null) return;

            // Lightning effect (visual only, no fire/block damage)
            world.strikeLightningEffect(finalLoc);
            world.playSound(finalLoc, Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);

            int x = finalLoc.getBlockX();
            int y = finalLoc.getBlockY();
            int z = finalLoc.getBlockZ();

            // Strip formatting for the broadcast message
            String plainName = displayName.replaceAll("<[^>]+>", "").trim();

            Bukkit.broadcast(Text.mm(
                "<red>\u26A1 <bold>O CAOS DESCEU DOS CEUS!</bold></red> <gray>O chefe <yellow>" + plainName
                + "</yellow> foi invocado por <white>" + player.getName()
                + "</white> nas coordenadas <green>X: " + x + ", Y: " + y + ", Z: " + z + "</green>!</gray>"
            ));
        });
    }

    private Location computeSafeLocation(Location origin) {
        var world = origin.getWorld();
        var dir = origin.getDirection().normalize();

        // Target 15-20 blocks ahead in the direction the player is looking
        double distance = 15 + (Math.random() * 5);
        double rawX = origin.getX() + dir.getX() * distance;
        double rawZ = origin.getZ() + dir.getZ() * distance;

        int bx = (int) Math.floor(rawX);
        int bz = (int) Math.floor(rawZ);

        // Surface Y: highest solid block
        int surfaceY = world.getHighestBlockYAt(bx, bz);
        if (surfaceY <= origin.getY() - 10) {
            surfaceY = origin.getBlockY();
        }

        return new Location(world, rawX, surfaceY + 1, rawZ);
    }

    private LivingEntity spawnTitanEmLata(Location loc) {
        var world = loc.getWorld();
        if (world == null) return null;

        org.bukkit.entity.Warden titan = (org.bukkit.entity.Warden) world.spawnEntity(loc, org.bukkit.entity.EntityType.WARDEN);
        titan.customName(Text.mm("<gradient:#ff0000:#ff8c00><bold>\uD83D\uDC51 CHEFE: O TITA EM LATA</bold></gradient>"));
        titan.setCustomNameVisible(true);

        var attr = titan.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) attr.setBaseValue(500.0);
        titan.setHealth(500.0);

        var dmg = titan.getAttribute(Attribute.ATTACK_DAMAGE);
        if (dmg != null) dmg.setBaseValue(30.0);

        var armor = titan.getAttribute(Attribute.ARMOR);
        if (armor != null) armor.setBaseValue(20.0);

        var kb = titan.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (kb != null) kb.setBaseValue(1.0);

        var speed = titan.getAttribute(Attribute.MOVEMENT_SPEED);
        if (speed != null) speed.setBaseValue(0.3);

        var eq = titan.getEquipment();
        if (eq != null) {
            eq.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
            eq.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
            eq.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
            eq.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
        }

        titan.getPersistentDataContainer().set(
            com.project.rpgplugin.util.ItemKeys.isBoss(),
            org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1
        );
        titan.getPersistentDataContainer().set(
            com.project.rpgplugin.util.ItemKeys.withKey("boss_level"),
            org.bukkit.persistence.PersistentDataType.INTEGER, 1
        );

        return titan;
    }

    private void sendHelp(Player player, String label) {
        player.sendMessage(Text.mm("<gold>=== RogueLata Comandos ==="));
        player.sendMessage(Text.mm("<yellow>/" + label + " tp <jogador></yellow> <gray>- Teleporta ate um amigo (vida cheia = sem recarga)</gray>"));
        player.sendMessage(Text.mm("<yellow>/" + label + " boss spawn <frostmaw|magma_tyrant|storm_wyvern|void_lich></yellow> <gray>- Invoca um boss com 5s de delay</gray>"));
        player.sendMessage(Text.mm("<yellow>/" + label + " loja</yellow> <gray>- Abre a Loja Pao em Lata</gray>"));
        player.sendMessage(Text.mm("<yellow>/" + label + " book</yellow> <gray>- Recebe uma Lata de Pao (caso tenha perdido)</gray>"));
        player.sendMessage(Text.mm("<yellow>/" + label + " draft</yellow> <gray>- Abre o proximo draft pendente</gray>"));
        player.sendMessage(Text.mm("<yellow>/run</yellow> <gray>- Mostra informacoes da sua run</gray>"));
        player.sendMessage(Text.mm("<yellow>/skills</yellow> <gray>- Abre o menu de habilidades</gray>"));
    }
}