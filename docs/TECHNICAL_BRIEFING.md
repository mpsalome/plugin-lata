# RogueLata — Technical Briefing v3.2.0

> **Gerado em:** 09/07/2026
> **Propósito:** Raio-X arquitetural completo para consultor externo gerar código compatível sem acesso ao repositório.

---

## 1. Arquitetura de Injeção de Dependência e Ciclo de Vida (`onEnable`)

**Classe principal:** `com.project.rpgplugin.RPGPlugin`  
**Package base:** `com.project.rpgplugin`  
**Extends:** `JavaPlugin implements CommandExecutor`

### Ordem de instanciação no `onEnable()` (linhas 136–259):

```java
// 1. Config + PDC keys
saveDefaultConfig();
ItemKeys.init(this);                      // ItemKeys.init(RPGPlugin) — cria NamespacedKeys
cooldownService = new CooldownService();

// 2. Gate registry
gateRegistry = new GateRegistry(this);     // carrega gates.yml

// 3. Bridges externas (soft-detect por reflection)
auraMobsBridge = new AuraMobsBridge();
mythicMobsBridge = new MythicMobsBridge();
modelEngineBridge = new ModelEngineBridge();

// 4. Skill system (EPIC-1)
skillsConfig = new SkillsConfig(this);     // skills.yml
skillServices = new SkillServices(this);  // cooldowns, reinforced blocks
skillRegistry = new SkillRegistry();       // LinkedHashMap<String, Skill>
SkillRegistration.registerAll(skillRegistry, skillServices); // 37 skills

// 5. Mayhem system (EPIC-4) — needed by EPIC-3
modifierRegistry = new ModifierRegistry();
ModifierRegistration.registerAll(modifierRegistry); // 23 modifiers
mayhemConfig = new MayhemConfig(this);      // mayhem.yml
milestoneService = new MilestoneService(mayhemConfig);
mayhemService = new MayhemService(this, modifierRegistry, milestoneService, mayhemConfig);

// 6. Mana system (EPIC-6) — init provider BEFORE ResetService/RunManager
ManaProvider manaProvider; // try AuraSkills, fallback StandaloneDummy
manaService = new ManaService(this, manaProvider);

// 7. Card system + Draft + Run (EPIC-2, EPIC-3)
cardRegistry = new CardRegistry();
statService = new StatService();
spawnResolver = new SpawnResolver(this);
runPersistence = new RunPersistenceService(this, cardRegistry);
resetService = new ResetService(this, cardRegistry, statService, mayhemService, spawnResolver, manaService, runPersistence);
runManager = new RunManager(this, cardRegistry, statService, resetService, mayhemService);
draftWeighting = new DraftWeighting(this);   // carrega draft.yml

AbilityCardRegistration.registerAll(cardRegistry, skillRegistry);
AugmentLoader.load(this, cardRegistry);    // carrega augments.yml

auraSkillsIntegration = new AuraSkillsIntegration(this, gateRegistry);

draftService = new DraftService(cardRegistry, draftWeighting, statService, runManager, auraSkillsIntegration);

// 8. Listeners
playerLevelListener = new PlayerLevelListener(runManager, draftService, draftWeighting, this, milestoneService);
draftMenuListener = new DraftMenuListener(runManager, draftService, playerLevelListener);
playerLifecycleListener = new PlayerLifecycleListener(runManager, runPersistence);
runCommand = new RunCommand(runManager);

// 9. Gameplay triggers (EPIC-5)
distanceTracker = new DistanceTracker(runManager);
distanceTask = new DistanceTask();
recallProgression = new RecallProgression(this, runManager, spawnResolver);
recallCommand = new RecallCommand(runManager, recallProgression);
recallListener = new RecallListener(runManager, recallProgression);

// 10. Difficulty & Combat (EPIC-7)
difficultyService = new DifficultyService(runManager);
mobScalingListener = new MobScalingListener(difficultyService, auraMobsBridge);
combatListener = new CombatListener(runManager);
mobSpawnService = new MobSpawnService(this, runManager, mythicMobsBridge, modelEngineBridge);

// 11. Augment handlers (EPIC-10)
augmentListener = new AugmentListener(runManager);

// 12. Boss loot (v3.2.0)
bossLootService = new BossLootService(this, cardRegistry);

// 13. HUD & Menu framework (EPIC-8)
hudService = new HudService(this, manaService, runManager, bossLootService);
menuListener = new MenuListener();

// 14. Persistence (EPIC-9)
dataStore = new YamlDataStore(this, cardRegistry);

// 15. Synergies (EPIC-10)
synergyService = new SynergyService(cardRegistry);

// 16. Passive task
passiveTask = new PassiveTask();

// 17. Skill dispatch listener (with RunManager)
skillDispatchListener = new SkillDispatchListener(skillRegistry, skillServices, runManager, cardRegistry, this);
skillDispatchListener.setManaService(manaService);
auraSkillsIntegration.setManaService(manaService);

// 18. Register event listeners (10 listeners)
getServer().getPluginManager().registerEvents(playerLevelListener, this);
getServer().getPluginManager().registerEvents(draftMenuListener, this);
getServer().getPluginManager().registerEvents(playerLifecycleListener, this);
getServer().getPluginManager().registerEvents(recallListener, this);
getServer().getPluginManager().registerEvents(skillDispatchListener, this);
getServer().getPluginManager().registerEvents(mobScalingListener, this);
getServer().getPluginManager().registerEvents(combatListener, this);
getServer().getPluginManager().registerEvents(augmentListener, this);
getServer().getPluginManager().registerEvents(menuListener, this);

// 19. Start tasks
distanceTask.start(this, distanceTracker, augmentListener);
passiveTask.start(this, runManager, synergyService, auraSkillsIntegration, skillRegistry, skillServices);
hudService.startAll();

// 20. Wire commands
getCommand("skills").setExecutor(this);
getCommand("rpg").setExecutor(this);
getCommand("run").setExecutor(runCommand);
getCommand("recall").setExecutor(recallCommand);
var lataExecutor = new LataCommand(this, runManager, mobSpawnService);
getCommand("lata").setExecutor(lataExecutor);
getCommand("rogue").setExecutor(lataExecutor);
```

### Getters públicos expostos no `RPGPlugin`:
```java
getAuraSkillsIntegration(), getAuraMobsBridge(), getMythicMobsBridge(), getModelEngineBridge()
getSkillRegistry(), getSkillServices(), getSkillsConfig()
getCardRegistry(), getStatService(), getDraftService(), getDraftWeighting()
getRunManager(), getMayhemService(), getMilestoneService(), getMayhemConfig()
getResetService(), getDifficultyService(), getMobSpawnService()
getDataStore(), getCooldownService(), getSynergyService()
getHudService(), getGateRegistry(), getDistanceTracker()
getBossLootService(),
getPlayerLevelListener()
createRpgBook() -> ItemStack  // BREAD com PDC rpg_book
createBossBeacon() -> ItemStack // BEACON com PDC boss_beacon
createShopItem() -> ItemStack // HAY_BLOCK com PDC shop_item
```

### plugin.yml — Comandos registrados:
```yaml
commands:
  skills:  /skills
  rpg:     /rpg [reload|reset|debug]
  run:     /run
  recall:  /recall
  lata:    /lata [tp <jogador>|boss spawn <frostmaw|magma_tyrant|storm_wyvern|void_lich|sir_creeper_a_lot|slime_shady|o_decapitador|guardiao_ancestral|senhor_da_guerra_piglin|rei_fantasma>|loja|draft|book]
           aliases: [rogue, pao, roguelata]
  rogue:   /rogue (sinonimo de /lata)
           aliases: [lata, pao, roguelata]
```

---

## 2. Concorrência e Folia (`SchedulerUtil`)

**Package:** `com.project.rpgplugin.util.SchedulerUtil`

### Assinaturas de métodos públicos (todos static):

```java
public static boolean isFolia()  // detecta Class.forName("io.papermc.paper.threadedregions.RegionizedServer")

public static BukkitTask runTimer(JavaPlugin plugin, Runnable task, long delay, long period)
public static BukkitTask runTimer(JavaPlugin plugin, Consumer<BukkitTask> task, long delay, long period)
  // Folia: Bukkit.getGlobalRegionScheduler().runAtFixedRate()
  // Bukkit: Bukkit.getScheduler().runTaskTimer()

public static void runLater(JavaPlugin plugin, Runnable task, long delay)
  // Folia: Bukkit.getGlobalRegionScheduler().runDelayed()
  // Bukkit: Bukkit.getScheduler().runTaskLater()

public static void runAsync(JavaPlugin plugin, Runnable task)
  // Folia: Bukkit.getAsyncScheduler().runNow()
  // Bukkit: Bukkit.getScheduler().runAsynchronously()
```

### Padrão de agendamento no projeto:

**Sempre que possível, usamos agendadores nativos do Paper (Folia-safe) diretamente:**

```java
// Player scheduler (delayed) — usado para boss beacon, boss commands
player.getScheduler().runDelayed(plugin, st -> { ... }, null, delayTicks);

// Player scheduler (fixed rate) — usado pelo HUDService
player.getScheduler().runAtFixedRate(plugin, st -> tick(player), () -> {}, delayTicks, periodTicks);

// Chunk/Region scheduler — usado no boss safe spawn
world.getChunkAtAsync(safeLocation).thenAccept(chunk -> {
    // spawn no chunk carregado
    world.strikeLightningEffect(finalLoc);
});
```

**Proibido: `Bukkit.getScheduler()`. Sempre priorizar APIs `player.getScheduler()`, `entity.getScheduler()`, ou `world.getChunkAtAsync()`.**

---

## 3. Abstração de Habilidades e Cartas

### 3.1. `Skill` Interface (`com.project.rpgplugin.core.skill.Skill`)

```java
public interface Skill {
    String id();                    // ex: "dash", "stone_smash"
    SkillTier tier();               // enum: BRONZE, SILVER, GOLD
    SkillType type();               // enum: EXPLORER, MINER, BUILDER
    Material icon();
    boolean passive();
    Duration cooldown();
    SkillTrigger trigger();         // define WHEN a skill ativa
    void activate(SkillContext ctx);
    default List<String> effectDescription();
}
```

### 3.2 `SkillTier` enum
```java
public enum SkillTier {
    BRONZE("<gradient:#CD7F32:#B8860B>", 1),
    SILVER("<gradient:#C0C0C0:#E8E8E8>", 2),
    GOLD("<gradient:#FFD700:#FFA500>", 3);
    String color(); int rank(); String nameKey();
}
```

### 3.3 `SkillType` enum
```java
public enum SkillType {
    EXPLORER("<green>", "explorador"),
    MINER("<aqua>", "minerador"),
    BUILDER("<gold>", "construtor");
    String color(); String key();
}
```

### 3.4 `AbstractSkill` — classe base concreta (`com.project.rpgplugin.core.skill.AbstractSkill`)

```java
public abstract class AbstractSkill implements Skill {
    protected final SkillServices services;
    protected AbstractSkill(SkillServices services);

    // Métodos utilitários para subclasses:
    protected boolean onCooldown(SkillContext ctx);
    protected long cooldownRemaining(SkillContext ctx);
    protected String displayName();                // "dash" -> "Dash"
    protected void startCooldown(SkillContext ctx);    // registra no SkillServices + HUD
    protected void consume(SkillContext ctx, int amount); // subtrai do ItemStack
    protected void feedback(SkillContext ctx, String message, Sound sound);
    protected ConfigurationSection cfg();         // da config.yml
    protected int cfgInt(String path, int def);   // defesa contra seção faltante
    protected double cfgDouble(String path, double def);
    protected String cfgString(String path, String def);
}
```

### 3.5 Triggers (`com.project.rpgplugin.core.skill.trigger`)

```java
public interface SkillTrigger {
    Set<TriggerKind> kinds();       // ex: {INTERACT}, {CONSUME}, {BLOCK_BREAK}, {MOVE, PASSIVE}
    boolean matches(Skill skill, SkillContext ctx);
    default List<String> activationDescription();
}

// TriggerKind enum:
INTERACT, CONSUME, BLOCK_BREAK, PASSIVE, DISTANCE, MOVE, DAMAGE, FOOD_LEVEL_CHANGE
```

Implementações concretas de Trigger no pacote: `com.project.rpgplugin.core.skill.trigger`:
- `PassiveTrigger` — kinds: `{PASSIVE}`, matches = true (sempre ativa)
- `ConsumeTrigger` — kinds: `{CONSUME}` 
- `BlockBreakTrigger` — kinds: `{BLOCK_BREAK}`
- `InteractTrigger` — kinds: `{INTERACT}`; checa se item na mão é o esperado
- `MoveTrigger` — kinds: `{MOVE, PASSIVE}`
- `CompositeTrigger` — suporta `TriggerKind.DISTANCE` com callback distance-based

### 3.6 Exemplo de construção de Skill concreta:
```java
// SkillDispatchListener.onInteract() intercepta, depois:
//   dispatch(p, TriggerKind.INTERACT, e, item, block)
// -> itera ownedAbilities do RunState
// -> skill.trigger().kinds().contains(kind) && skill.trigger().matches(skill, ctx)
// -> mana cost check
// -> event.setCancelled(true) (se INTERACT ou CONSUME)
// -> skill.activate(ctx)
```

Skills toggleáveis (ex: SonarSkill) usam sneak right-click para ligar/desligar. Quando desligadas, o dispatch as pula via `run.isToggledOn(skillId)`. Quando ligadas com trigger PASSIVE, o efeito contínuo (ex: glowing reveal) persiste.

### 3.7 Card System (`com.project.rpgplugin.core.card`)

```java
public interface Card {
    String id();
    CardTier tier();
    Set<CardTag> tags();     // EXPLORER, MINER, BUILDER, TANK, DPS, MOBILITY, ...
    CardKind kind();          // ABILITY ou AUGMENT
    Material icon();
    int maxStacks();
    boolean offerable(RunState run);
    void onAcquire(Player p, RunState run);
    void onRemove(Player p, RunState run);
    default void toggleOn(Player p, RunState run) {}
    default void toggleOff(Player p, RunState run) {}
    default List<String> lore(RunState run);
}
```

**Dois tipos de carta:**

| Tipo | Classe | maxStacks | offerable | onAcquire |
|------|--------|-----------|-----------|-----------|
| ABILITY | `AbilityCard` | 1 | `!run.hasCard(id)` | `run.addCard(id)` + `run.ownedAbilities().add(id)` |
| AUGMENT | `AugmentCard` | configurável | `run.cardCount(id) < maxStacks` | `run.addCard(id)` + `effect.apply(p, run, count)` |

**AbilityCard** wrappeia uma `Skill` existente + `Set<CardTag>` mapeada no `AbilityCardRegistration` (37 skills mapeadas manualmente).

**AugmentCard** é carregada do `augments.yml` via `AugmentLoader.load()`. Contém `AugmentEffect` que pode ser:
- `AttributeEffect` (MAX_HEALTH, ATTACK_DAMAGE, MOVEMENT_SPEED, ARMOR com ADD_NUMBER/ADD_SCALAR)
- `MultiplierEffect` (chave do RunState + valor additivo)
- `LifestealEffect` (heal_pct)
- `MultiMultiplierEffect` (damage_dealt + damage_taken)
- `OnKillEffect` (efeito ao matar: heal, explode, etc)
- `PotionEffectAugment` (potion constante)
- `GiantEffect` (health/damage + slow)

### 3.8 `SkillDispatchListener` — o roteador central de eventos

`com.project.rpgplugin.listener.SkillDispatchListener` escuta 7 eventos:

```java
@EventHandler onInteract(PlayerInteractEvent)
  -> if (ItemKeys.isRpgBook(item))        new CollectionMenu(p, run, ...)
  -> if (ItemKeys.isShopItem(item))       new ShopMenu(p, plugin).open()
  -> if (ItemKeys.isBossBeacon(item))     handleBossBeacon(p)
  -> else dispatch(p, TriggerKind.INTERACT, e, item, null)

@EventHandler onBlockPlace(BlockPlaceEvent) // se sneaking, dispatch INTERACT
@EventHandler onFish(PlayerFishEvent)     // dispatch INTERACT
@EventHandler onConsume(PlayerItemConsumeEvent) // dispatch CONSUME
@EventHandler onBlockBreak(BlockBreakEvent)      // dispatch BLOCK_BREAK
@EventHandler onMove(PlayerMoveEvent)             // dispatch MOVE
@EventHandler onDamage(EntityDamageEvent)         // dispatch DAMAGE
@EventHandler onFoodLevelChange(FoodLevelChangeEvent) // hunger decay reduction
```

O método `dispatch()`:
```java
private void dispatch(Player player, TriggerKind kind, Event event, ItemStack item, Block block) {
    RunState run = runManager.getRun(player);
    List<String> owned = run.ownedAbilities().stream().toList();
    for (String skillId : owned) {
        if (!run.isToggledOn(skillId)) continue;
        Skill skill = registry.byId(skillId);
        if (!skill.trigger().kinds().contains(kind)) continue;
        SkillContext ctx = new SkillContext(player, services, item, block, event);
        if (skill.trigger().matches(skill, ctx)) {
            // mana check
            if (event instanceof Cancellable c && (kind == INTERACT || kind == CONSUME)) c.setCancelled(true);
            skill.activate(ctx);
        }
    }
}
```

---

## 4. Camada de UI e Menus

### 4.1 `Menu` — classe base (`com.project.rpgplugin.ui.menu.Menu`)

```java
public abstract class Menu implements InventoryHolder {
    protected final Inventory inventory;
    protected Consumer<InventoryClickEvent> clickHandler;

    public Menu(int size, String title)  // size = linhas * 9; title em MiniMessage

    public void setClickHandler(Consumer<InventoryClickEvent> handler);
    public void handleClick(InventoryClickEvent event);  // delega ao handler

    public void setItem(int slot, ItemStack item);
    public void fillBorder(ItemStack filler);  // preenche borda, mantém interior null
    public Inventory getInventory();
}
```

`MenuListener` (global listener registrado no `onEnable`):
```java
public void onClick(InventoryClickEvent e) {
    InventoryHolder holder = e.getInventory().getHolder();
    if (holder instanceof Menu menu) {
        e.setCancelled(true);
        menu.handleClick(e);
    }
}
```

### 4.2 `DraftMenu` — exemplo concreto de extensão (`com.project.rpgplugin.ui.DraftMenu`)

- Size: 54 (6 linhas)
- Título: `<gold><bold>Selecione sua carta`
- Slots de carta: 20, 23, 26 (3 opções padrão) + 29 (4ª se extraDraftSlots > 0)
- Slots de ação: 40 (Reroll — se habilitado), 44 (Skip — se habilitado)
- Click handler: cancela evento, identifica slot, chama `draftService.applyChoice()`, `draftService.reroll()` ou `draftService.skipDraft()`
- Ao fechar sem decisão: sessão fica ativa; jogador reabre via `/lata draft`

### 4.3 `ShopMenu` — implementado (`com.project.rpgplugin.ui.ShopMenu`)

- Size: 27 (3 linhas)
- Título: `<gradient:#ff8c00:#ff4500>🛒 Loja Pão em Lata</gradient>`
- 5 slots de itens: 10 (Reroll 2 níveis - ENDER_EYE), 12 (Carta Avulsa 5 níveis - MAP), 14 (Absolvição 10 níveis - TOTEM_OF_UNDYING), 16 (Sinalizador 15 níveis - BEACON), 18 (Purificação do Mundo 30 níveis - HEART_OF_THE_SEA)
- Validação: `player.getLevel() >= cost` senão `Sound.ENTITY_VILLAGER_NO` + actionbar
- Sucesso: `Sound.ENTITY_EXPERIENCE_ORB_PICKUP` + dedução + ação
- Purificação do Mundo: chama `mayhemService.clear(player, run)` removendo todos os mayhem effects globais

### 4.5 `HubMenu` — Menu Principal (`com.project.rpgplugin.ui.HubMenu`)

- Size: 27 (3 linhas)
- Título: `<gold><bold>Menu Principal`
- 3 navigation buttons: slot 11 (Coleção), 13 (Loja), 15 (Draft)
- Close button at slot 22 (BARRIER)
- Opened by right-clicking the RPG Book (BREAD with ItemKeys.isRpgBook()) in SkillDispatchListener.onInteract

### 4.6 Criando um novo Menu — padrão:

```java
public class MeuMenu extends Menu {
    public MeuMenu(Player player, ...) {
        super(27, "<gradient:...>Titulo");
        build();
        setClickHandler(event -> {
            event.setCancelled(true);
            // tratar slot
        });
    }
    private void build() { /* setItem(slot, item) */ }
    public void open() { player.openInventory(getInventory()); }
}
```

---

## 5. Serviços Centrais e Persistência (PDC)

### 5.1 `StatService` (`com.project.rpgplugin.core.card.StatService`)

```java
public void recompute(Player p, RunState run)
```
1. Remove todos `AttributeModifier` cujo key.namespace = `"roguelata"` e key.key começa com `"roguelata_"` de 4 atributos: MAX_HEALTH, ATTACK_DAMAGE, MOVEMENT_SPEED, ARMOR
2. Soma contribuições de todas as cartas do tipo `AugmentCard` com `AttributeEffect`
3. Soma `run.skipHealthBonus()` como health bonus
4. Reaplica modifiers com `ItemKeys.withKey("roguelata_max_health")` etc, usando `AttributeModifier.Operation.ADD_NUMBER` (ou ADD_SCALAR para speed)

```java
public void resetToBaseline(Player p)  // limpa todos modifiers RogueLata
```

### 5.2 `HudService` (`com.project.rpgplugin.ui.HudService`)

- Actionbar contínua via `player.getScheduler().runAtFixedRate(plugin, st -> tick(player), ...)` a cada 4 ticks
- `tick(player)`: actionbar compõe mana + health via MiniMessage → `player.sendActionBar(Text.mm(composed))`
- Cooldowns ativos + efeitos ativos são exibidos via BossBar separada (não na actionbar)
- Cooldown display: `Map<UUID, Map<String, Long>> cooldownDisplays` — chave = nome da skill, valor = timestamp de expiração
- Registro de cooldown manual: `setCooldown(Player, displayName, durationSeconds)` ou `registerCooldown(Player, skillId, displayName, durationMillis)`
- Item cooldown nativo (hotbar): `static void setItemCooldown(Player, Material, int ticks)` e `static void setItemCooldownDelayed(Player, Material, int ticks, JavaPlugin)` (1 tick de delay para evitar desync)

### 5.3 `RunPersistenceService` (`com.project.rpgplugin.core.run.RunPersistenceService`)

Persistência via `PersistentDataContainer` do jogador. Chaves (todas prefixadas com `run_`):

| NamespacedKey | Type | Descrição |
|---|---|---|
| `run_has_run` | BYTE | Flag de existência |
| `run_level` | INTEGER | Nível da run |
| `run_pending_drafts` | INTEGER | Drafts pendentes na fila |
| `run_milestones` | INTEGER | Milestones de mayhem |
| `run_phoenix` | BYTE | Fênix charge |
| `run_started_at` | LONG | Timestamp de início |
| `run_blocks_walked` | LONG | Total caminhado |
| `run_blocks_since_recall` | LONG | Desde último recall |
| `run_recall_uses` | INTEGER | Usos de recall |
| `run_extra_slots` | INTEGER | Slots extras de skill |
| `run_skip_health` | DOUBLE | Bônus de HP por skip |
| `run_extra_draft` | INTEGER | Slots extras de draft |
| `run_free_rerolls` | INTEGER | Créditos de reroll grátis |
| `run_outcome` | STRING | ONGOING, VICTORY, DEFEAT |
| `run_xp_mult` | INTEGER | Multiplicador de XP |
| `run_cooldown_mult` | DOUBLE | Multiplicador de cooldown |
| `run_cards` | STRING | Cards (comma-separated, fallback) |
| `run_card_counts` | STRING | `id1:count1,id2:count2` |
| `run_abilities` | STRING | Abilities (comma-separated) |
| `run_multipliers` | STRING | `key1:val1,key2:val2` |
| `run_modifiers` | STRING | Mayhem ativos (comma-separated) |
| `run_toggled_off` | STRING | Skills desativadas (comma-separated) |

```java
public boolean hasRun(Player player)          // checa run_has_run
public void saveRun(Player player, RunState run)
public RunState loadRun(Player player)        // null se não existir
public void clearRun(Player player)           // remove TODAS as chaves
```

### 5.4 `RunState` (`com.project.rpgplugin.core.run.RunState`)

Estado mutável de uma run ativa. Campos principais:
```java
UUID playerId; CardRegistry cardRegistry;
Set<String> ownedCards; Map<String, Integer> cardCounts;
Set<String> ownedAbilities; Map<String, Double> multipliers;
List<OnKillEffect> onKillEffects;
int level, pendingDrafts, milestonesReached;
Set<String> activeModifiers; Set<String> sharedModifiers;
Set<String> activePotionTypes;
int xpMultTotal; double cooldownMultTotal; RunOutcome outcome;
long startedAt, blocksWalked, blocksSinceRecall;
int recallUses, extraSkillSlots, extraDraftSlots, freeRerolls;
double skipHealthBonus;
boolean phoenixCharge;
Set<String> toggledOff;
void reset(); // limpa TUDO
```

### 5.5 `MayhemService` (`com.project.rpgplugin.core.mayhem.MayhemService`)

```java
public MayhemService(RPGPlugin plugin, ModifierRegistry registry,
    MilestoneService milestoneService, MayhemConfig config)

public void rollAndApply(RunState run, World world);
  // Sorteia modifier baseado no milestone atual, ativa globalmente ou por player
  // Se milestone atinge threshold e random < 0.5: spawna boss aleatório em vez de modifier

public void tryRelieveMayhem(Player deadPlayer, RunState run);
  // Se player morreu no nível >= 10: remove último modifier global, broadcast

public void reduceMayhemByOne(Player player, RunState run);
  // Remove último modifier da run do jogador, broadcast global (usado pela Loja)

public void clear(Player p, RunState run);
public void reapplyOnJoin(Player p, RunState run);
public Set<String> getActiveModifiers();
```

### 5.6 `ResetService` / `BuildResetService` / `RunResetService`

```java
public class ResetService {
    public void fullReset(Player p, RunState run);   // RunResetService: mayhem + cards + attrs + potions + mana + teleport spawn
    public void resetBuild(Player p, RunState run); // BuildResetService: só remove cartas/attrs/skills, mantém run
}
```

**`BuildResetService`** limpa: `AttributeModifier` de TODOS os 17 atributos com prefixo `"roguelata_"` (namespace `"roguelata"`), poções, mana, nível vanilla, itens com lore "Skill Item:", AuraSkills, e chama `run.reset()` + `persistence.clearRun(p)`.

### 5.7 `BossLootService` (`com.project.rpgplugin.core.boss.BossLootService`)

```java
public BossLootService(RPGPlugin plugin, CardRegistry cardRegistry);

public void giveLoot(Player player, BossDefinition boss, int invokerLevel);
  // Dropa equipamento temático baseado no tier do boss (COMMON→EPIC por HP do boss)
  // Quantidade de peças escala com invokerLevel
public ItemStack createBossSetItem(BossDefinition boss, EquipmentSlot slot);
  // Cria item com nome do boss, encantamentos e lore
```

- Boss tier determinado por max HP: 180-220 = COMMON, 250-280 = RARE, 300-350 = EPIC
- XP reward escala com HP do boss: `boss.maxHp * 0.5`
- BossSet items têm formato de nome: `<boss display name> <item type>`
- BossSet items têm encantamentos que combinam com o tema do boss
- BossSet items têm linha de lore: `<gray>Item do BossSet: <boss id></gray>`

---

## 6. Integrações Externas

### 6.1 `AuraSkillsIntegration` (`com.project.rpgplugin.AuraSkillsIntegration`)

```java
public class AuraSkillsIntegration implements Listener {
    public void setManaService(ManaService manaService);
    public void syncAuraSkillLevel(Player, String rogueSkillKey, int level);
    public void resetPlayerSkills(Player);
    public void resetAllAuraSkills(Player);
    public Map<CardTag, Double> getClassWeights(Player); // EXPLORER=Agility+Archery+Defense+Fighting, MINER=..., BUILDER=...
    public void syncSkillSlots(Player, int totalSlots); // via PermissionAttachment
    public void removeSkillSlotAttachment(Player);
    public boolean isEnabled();
}
```

### 6.2 `ManaProvider` interface (`com.project.rpgplugin.core.mana.ManaProvider`)

```java
public interface ManaProvider {
    boolean isAvailable();
    double getMana(Player);
    double getMaxMana(Player);
    boolean hasEnoughMana(Player, double amount);
    boolean consumeMana(Player, double amount);
    void addMana(Player, double amount);
    void setMana(Player, double amount);
    String name();
}
```

Implementações:
- `AuraSkillsManaProvider` — usa `AuraSkillsApi.get().getUser(playerId).getMana()/setMana()`
- `StandaloneDummyManaProvider` — mana local em mapa, sem dependência externa

### 6.3 `MythicMobsBridge` (`com.project.rpgplugin.integration.MythicMobsBridge`)

```java
public class MythicMobsBridge {
    public boolean isEnabled();  // detecta Bukkit.getPluginManager().getPlugin("MythicMobs")
    public Optional<LivingEntity> trySpawnMob(String mobName, Location loc);
    // Reflection: MythicBukkit.inst().getMobManager().spawnMob(name, loc) -> ActiveMob.getEntity().getBukkitEntity()
}
```

Retorna `Optional.empty()` se MythicMobs não estiver presente ou se o mob não existir na config deles.

### 6.4 `ModelEngineBridge` (`com.project.rpgplugin.integration.ModelEngineBridge`)
```java
public void applyModel(LivingEntity entity, String modelId);
// Reflection: ModelEngineAPI.getModeledEntity(entity).setActiveModel(...)
```

### 6.5 `AuraMobsBridge` (`com.project.rpgplugin.integration.AuraMobsBridge`)
```java
public void applyScaling(LivingEntity entity);
// Reflection: AuraMobsApi.get().getLevelManager().applyLevel(entity)
```

---

## 7. `ItemKeys` — Utilitário de PDC (`com.project.rpgplugin.util.ItemKeys`)

```java
public static void init(JavaPlugin plugin);  // cria NamespacedKeys
public static NamespacedKey rpgBook();
public static NamespacedKey shopItem();
public static NamespacedKey bossBeacon();
public static NamespacedKey eliteId();
public static NamespacedKey isBoss();
public static NamespacedKey skillItem();
public static NamespacedKey withKey(String key);          // cria novo NSKey com namespace "roguelata"

public static boolean isRpgBook(ItemStack is);
public static boolean isShopItem(ItemStack is);
public static boolean isBossBeacon(ItemStack is);
```

---

## 8. `Text` — MiniMessage Helper (`com.project.rpgplugin.util.Text`)

```java
public static Component mm(String s);           // MiniMessage.deserialize(s) com ITALIC=false
public static String legacyToMiniMessage(String legacy); // traduz § codes para MiniMessage tags
```

---

## 9. `LataCommand` — Comando principal (`com.project.rpgplugin.command.LataCommand`)

```java
public class LataCommand implements CommandExecutor {
    // Subcomandos: tp, boss, loja, draft, book
    public void spawnBossAtSafeLocation(Player player, String bossId, String bossName);
}
```

- `/lata book` → `plugin.createRpgBook()`: entrega um RPG Book ao jogador

**`spawnBossAtSafeLocation`** — método público reusável (chamado pelo item Sinalizador via `SkillDispatchListener`):
1. `computeSafeLocation(Location origin)`: 15-20 blocos na direção do jogador, `world.getHighestBlockYAt(bx, bz)` + 1
2. `world.getChunkAtAsync(safeLoc).thenAccept(chunk -> ...)`: Folia-safe
3. Tenta `mobSpawnService.getEliteFactory().spawnBoss(finalLoc, def)` com def do `bosses.yml`
4. Se null: fallback `spawnTitanEmLata()` → WARDEN, 500 HP, Netherite armor, nome gradient
5. `world.strikeLightningEffect(finalLoc)` + `Sound.ENTITY_WITHER_SPAWN`
6. Broadcast global com coordenadas X/Y/Z, nível do boss, e dica do loot possível

---

## 10. Bosses (`bosses.yml`)

### 10.1 Boss Definitions

10 bosses defined in `bosses.yml`:

| ID | Type | HP | Biome | Display Name (MiniMessage) |
|---|---|---|---|---|
| frostmaw | POLAR_BEAR | 300 | SNOWY | `<bold><aqua>Frostmaw <gray>\| <white>Senhor do Gelo</white></gray></aqua></bold>` |
| magma_tyrant | MAGMA_CUBE | 200 | BADLANDS | `<bold><red>Magma Tyrant <gray>\| <white>Soberano das Profundezas</white></gray></red></bold>` |
| storm_wyvern | RAVAGER | 350 | PLAINS | `<bold><yellow>Storm Wyvern <gray>\| <white>Asa da Tempestade</white></gray></yellow></bold>` |
| void_lich | WITHER_SKELETON | 250 | DARK_FOREST | `<bold><dark_purple>Void Lich <gray>\| <white>Senhor do Vazio</white></gray></dark_purple></bold>` |
| sir_creeper_a_lot | CREEPER | 220 | TAIGA | `<bold><green>Sir Creeper-A-Lot <gray>\| <white>O Explosivo</white></gray></green></bold>` |
| slime_shady | SLIME | 180 | SWAMP | `<bold><yellow>Slime Shady <gray>\| <white>Gosma Real</white></gray></yellow></bold>` |
| o_decapitador | WITHER_SKELETON | 300 | NETHER_WASTES | `<bold><red>O Decapitador <gray>\| <white>Cabeça de Ferro</white></gray></red></bold>` |
| guardiao_ancestral | ELDER_GUARDIAN | 350 | DEEP_OCEAN | `<bold><blue>Guardião Ancestral <gray>\| <white>Protetor das Profundezas</white></gray></blue></bold>` |
| senhor_da_guerra_piglin | PIGLIN_BRUTE | 280 | CRIMSON_FOREST | `<bold><gold>Senhor da Guerra Piglin <gray>\| <white>Força Bruta</white></gray></gold></bold>` |
| rei_fantasma | SKELETON | 320 | DARK_FOREST | `<bold><gray>Rei Fantasma <gray>\| <white>Soberano Eterno</white></gray></gray></bold>` |

### 10.2 Spawning Flow

1. Player buys Sinalizador from ShopMenu (15 levels)
2. Right-clicks the beacon item → `SkillDispatchListener.handleBossBeacon()`
3. Hand detection: checks main hand first, then off-hand
4. Biome mapping selects boss based on player's current biome
5. Falls back to `LataCommand.spawnBossAtSafeLocation()` if biome doesn't match
6. If `bosses.yml` is missing the entry, fallback spawns Titã em Lata (WARDEN, 500 HP)
7. Milestone boss: at each mayhem milestone (every 10 levels), 50% chance a random boss spawns instead of mayhem modifier
8. Boss stats scale by invoker's level: +15% HP and +10% damage per level, with ±20% random variance

### 10.3 BossBar

`EliteFactory.trackBossBar()` creates a plain-text BossBar (stripTags removes MiniMessage tags from the display name). The entity's `customName` retains the MiniMessage Component for nametag display.

### 10.4 BossSet — Themed Loot Drops

Each boss drops equipment themed to its identity via `BossLootService`:

- Loot tier determined by boss max HP: 180-220 = COMMON, 250-280 = RARE, 300-350 = EPIC
- Items have custom name: `<boss display name> <item type>`
- Items have enchantments matching the boss theme
- Items have lore: `<gray>Item do BossSet: <boss id></gray>`
- Number of pieces scales with invoker level (more pieces at higher levels)
- XP reward scales with boss HP: `boss.maxHp * 0.5`

---

## 🔑 Convenções e Regras de Ouro

1. **Folia:** Sempre usar `player.getScheduler()`, `entity.getScheduler()`, ou `world.getChunkAtAsync()`. Evitar `Bukkit.getScheduler()`. Usar `SchedulerUtil` apenas para timers globais.
2. **MiniMessage:** Toda mensagem ao jogador via `Text.mm("...")`. Actionbar: `player.sendActionBar(Text.mm(...))`.
3. **PDC Namespace:** Todas as chaves de attribute modifier usam `namespace = "roguelata"`, key prefix `"roguelata_"`.
4. **Item PDC:** Toda interação com item customizado no `SkillDispatchListener.onInteract()` antes do dispatch.
5. **Run lifecycle:** `RunPersistences.loadRun()` (PDC) → `RunManager.restoreRun()` no join. `saveRun()` + `removeRun()` no quit.
6. **Defensive cfg reads:** usar `cfgInt()`/`cfgDouble()`/`cfgString()` do `AbstractSkill` em vez de `cfg().getInt()` direto.
7. **Respawn vanilla:** plugin não chama `setRespawnLocation()`.
8. **Veteran migration:** `onJoin` converte níveis existentes em pending drafts.
9. **Mayhem clear on death:** `mayhemService.clear()` antes de `resetBuild()`.
