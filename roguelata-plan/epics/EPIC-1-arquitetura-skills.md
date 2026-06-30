# EPIC-1 — Arquitetura data-driven de skills

**Fase:** F1 · **Prioridade:** P0 · **Dependências:** EPIC-0
**Objetivo:** eliminar o if-else gigante (`ClassListeners`, ~900 linhas) e os 5 `switch` paralelos (`PlayerManager`), transformando cada skill em **dado + comportamento plugável** registrado num `SkillRegistry`. **Zero regressão de gameplay.**

> **Relação com EPIC-2 (importante após a revisão de design):** este épico entrega a fundação técnica — `SkillRegistry` + triggers para as **35 habilidades** existentes. O [EPIC-2](EPIC-2-draft-e-cartas.md) generaliza isso para o conceito **`Card`** (habilidades **+** augments de stat) e **substitui o sistema de "comprar com XP/equipar 9"** pelo **draft de 3 cartas**. Ou seja: aqui as habilidades deixam de ser hardcoded; lá elas viram cartas adquiríveis por draft. Não implemente "equipar/desequipar" nem "comprar com XP" — esse fluxo morre no EPIC-2. O que o player **possui** numa run passa a ser a lista de cartas adquiridas (ver `RunState`, EPIC-3).

---

## Por que (dívidas atacadas)

- **D1:** `ClassListeners.onPlayerInteract` concentra a lógica de ~30 skills hardcoded.
- **D2:** `PlayerManager` decide tier/tipo/nome/descrição/material por 5 switches que precisam ser mantidos em sincronia manualmente.

Sem isso, **nenhum** dos épicos seguintes (augments, gates, mana abilities) é viável sem multiplicar a bagunça.

---

## Modelo conceitual

```
Skill (dado imutável + comportamento)
  ├─ id: String            ("dash")
  ├─ tier: SkillTier        (BRONZE)
  ├─ type: SkillType        (EXPLORER)
  ├─ icon: Material         (NETHER_WART)
  ├─ displayKey/descKey     (chaves i18n)
  ├─ passive: boolean
  ├─ cooldown: Duration
  ├─ trigger: SkillTrigger  (como é disparada)
  └─ activate(SkillContext) (o que faz)
```

Um **único** listener (`SkillDispatchListener`) recebe eventos do Bukkit, descobre quais skills equipadas têm trigger compatível e delega.

---

## Tarefas

### T1.1 — Enums `SkillTier` e `SkillType`

**Arquivos:** `core/skill/SkillTier.java`, `core/skill/SkillType.java`

```java
public enum SkillTier {
    BRONZE(1, "<gold>",  3),   // xpCost, color, maxEquipped
    SILVER(3, "<gray>",  3),
    GOLD  (5, "<yellow>", 3);
    private final int xpCost; private final String color; private final int maxEquipped;
    SkillTier(int xpCost, String color, int maxEquipped) { ... }
    public int xpCost() { return xpCost; }
    public String color() { return color; }
    public int maxEquipped() { return maxEquipped; }
    public String i18nKey() { return "tier." + name().toLowerCase(); }
}

public enum SkillType {
    EXPLORER("<aqua>", "explorer"),
    MINER("<gold>", "miner"),
    BUILDER("<green>", "builder");
    // color() + i18nKey()
}
```

**CA:** custos e limites vêm do enum; nenhum `getSkillCost` por `switch`.

---

### T1.2 — Interface `Skill`, `AbstractSkill`, `SkillContext`, `SkillTrigger`

**Arquivos:** `core/skill/Skill.java`, `AbstractSkill.java`, `SkillContext.java`, `trigger/SkillTrigger.java`

```java
public interface Skill {
    String id();
    SkillTier tier();
    SkillType type();
    Material icon();
    boolean passive();
    Duration cooldown();              // Duration.ZERO se não tem
    SkillTrigger trigger();           // como dispara
    void activate(SkillContext ctx);  // efeito
    default String displayKey() { return "skill." + id() + ".name"; }
    default String descKey()    { return "skill." + id() + ".desc"; }
}
```

```java
// Contexto entregue à skill na ativação (desacopla do evento bruto)
public record SkillContext(
    Player player,
    RPGServices services,     // acesso a cooldowns, config, messages, world ops
    @Nullable ItemStack usedItem,
    @Nullable Block targetBlock,
    @Nullable Event sourceEvent
) {}
```

```java
// SkillTrigger: classifica e filtra o evento de origem
public interface SkillTrigger {
    TriggerKind kind();                       // INTERACT, CONSUME, BLOCK_BREAK, PASSIVE, DISTANCE, MOVE
    boolean matches(Skill skill, SkillContext ctx); // ex: item é flor? mão tem picareta?
}
```

`AbstractSkill` provê: checagem de cooldown via `CooldownService`, consumo de item, feedback padrão (som + action bar via i18n), e leitura de parâmetros do `SkillsConfig`.

```java
public abstract class AbstractSkill implements Skill {
    protected boolean onCooldown(SkillContext ctx) {
        return ctx.services().cooldowns().isOnCooldown(ctx.player().getUniqueId(), id());
    }
    protected void startCooldown(SkillContext ctx) {
        ctx.services().cooldowns().start(ctx.player().getUniqueId(), id(), cooldown());
    }
    protected void consume(SkillContext ctx, int amount) { ctx.usedItem().subtract(amount); }
    protected void feedback(SkillContext ctx, String msgKey, Sound sound) { ... }
}
```

**CA:** uma skill encapsula todos os seus metadados e lógica num único arquivo; compila sem depender do antigo `ClassListeners`.

---

### T1.3 — `SkillRegistry` + migração de TODAS as skills atuais

**Arquivos:** `core/skill/SkillRegistry.java`, `core/skill/impl/*`

```java
public final class SkillRegistry {
    private final Map<String, Skill> byId = new LinkedHashMap<>();
    public void register(Skill s) {
        if (byId.containsKey(s.id())) throw new IllegalStateException("dup skill id: " + s.id());
        byId.put(s.id(), s);
    }
    public Optional<Skill> byId(String id) { return Optional.ofNullable(byId.get(id.toLowerCase())); }
    public List<Skill> byTier(SkillTier t) { ... }
    public List<Skill> byType(SkillType t) { ... }
    public Collection<Skill> all() { return byId.values(); }
    public int size() { return byId.size(); }
}
```

Migrar **cada** skill do `ClassListeners`/`PlayerManager` para uma classe em `impl/`. Exemplo completo (Dash):

```java
public final class DashSkill extends AbstractSkill {
    public String id() { return "dash"; }
    public SkillTier tier() { return SkillTier.BRONZE; }
    public SkillType type() { return SkillType.EXPLORER; }
    public Material icon() { return Material.NETHER_WART; }
    public boolean passive() { return false; }
    public Duration cooldown() { return cfg().duration("dash.cooldown", Duration.ofSeconds(30)); }
    public SkillTrigger trigger() { return InteractTrigger.flower(); } // item tagueado FLOWERS

    public void activate(SkillContext ctx) {
        if (onCooldown(ctx)) { ctx.services().msg().cooldown(ctx, this); return; }
        Player p = ctx.player();
        consume(ctx, 1);
        int amp = cfg().getInt("dash.speed", 1);
        int dur = cfg().getTicks("dash.duration", 200);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur, amp));
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, dur, 0));
        p.getWorld().spawnParticle(Particle.HEART, p.getLocation().add(0,1,0), 10, .5,.5,.5,.05);
        startCooldown(ctx);
        feedback(ctx, "skill.dash.activated", Sound.ENTITY_BAT_TAKEOFF);
    }
}
```

Registro central:
```java
// SkillRegistration.java — chamado no onEnable
registry.register(new DashSkill(services));
registry.register(new RecallSkill(services));
// ... todas as 35
```

> **Checklist de migração (não perder nada):** percorrer todas as skills do [Apêndice A — Catálogo de Cartas](../appendices/A-catalogo-cartas.md). Cada skill deve preservar: item de ativação, efeito, duração/amplificador, cooldown, partícula, som, mensagem e penalidades (ex: `core_overdrive` aplica Slowness+Hunger; `dim_shift` aplica Blindness+Hunger). Passivas (`sight`, `jump_boost`, `canopy_step`, `safe_fall`) vão para `PassiveTrigger`.

**CA:**
- `SkillRegistry.size()` == número canônico de skills (35).
- Todo comportamento idêntico ao 1.2.0 (validar via smoke test ampliado).
- GUI e HUD passam a ler do registry.

---

### T1.4 — `SkillDispatchListener` (um único listener fino)

**Arquivos:** `listener/SkillDispatchListener.java`, `core/skill/trigger/*`

Substitui o `ClassListeners` monolítico. Para cada evento relevante:

```java
@EventHandler
public void onInteract(PlayerInteractEvent e) {
    Player p = e.getPlayer();
    if (ItemKeys.isRpgBook(e.getItem())) { e.setCancelled(true); ui.openCollectionMenu(p); return; }
    dispatch(p, TriggerKind.INTERACT, e, e.getItem(), null);
}

// Itera sobre as cartas de habilidade ADQUIRIDAS na run (não há mais "equipadas").
private void dispatch(Player p, TriggerKind kind, Event ev, ItemStack item, Block block) {
    for (String skillId : run.ownedAbilities(p)) {   // RunState (EPIC-3)
        Skill skill = registry.byId(skillId).orElse(null);
        if (skill == null || skill.trigger().kind() != kind) continue;
        SkillContext ctx = new SkillContext(p, services, item, block, ev);
        if (skill.trigger().matches(skill, ctx)) {
            if (ev instanceof Cancellable c) c.setCancelled(true);
            skill.activate(ctx);
            return; // uma skill por interação
        }
    }
}
```

Triggers concretos:
- `InteractTrigger` (item específico/Tag), `ConsumeTrigger` (PlayerItemConsumeEvent), `BlockBreakTrigger`, `MoveTrigger`/`PassiveTrigger` (delegado à `PassiveTask` do EPIC-9), `DistanceTrigger` (EPIC-3).

**CA:** o arquivo `ClassListeners.java` é removido; `SkillDispatchListener` tem < 200 linhas e nenhuma lógica específica de skill.

---

### T1.5 — `PlayerManager` vira só estado; metadados saem para registry/config

**Arquivos:** `PlayerManager.java` (enxugar), `config/SkillsConfig.java`

Remover de `PlayerManager`: `determineSkillMaterial`, `getSkillTier`, `getSkillType`, `getSkillCost`, `getSkillDisplayName`, `getSkillDescription`, `getAll*Skills` **e todo o fluxo de equipar/desequipar/comprar com XP** (será substituído pelo draft no EPIC-2). Tudo isso passa a vir de `registry`/`messages`/`SkillsConfig`.

O estado do player deixa de ser "desbloqueadas/equipadas" e passa a ser **cartas adquiridas na run** — modelado em `RunState` (EPIC-3) e persistido pelo `PlayerDataStore` (EPIC-9). `PlayerManager` é absorvido por `RunState`/`ProfileService`; ele não decide mais nada sobre conteúdo de skill.

`SkillsConfig` carrega parâmetros por skill de `skills.yml`:
```yaml
# resources/skills.yml — parâmetros ajustáveis sem recompilar
dash:        { duration: 10s, cooldown: 30s, speed: 1 }
step_assist: { duration: 15s, cooldown: 15s }
recall:      { base_distance: 2000, growth: 1.5, cap: 0 }   # ver EPIC-3
core_overdrive: { haste: 2, strength: 1, slowness: 1, hunger: 2, duration: 20s }
# ...
```

**CA:** `PlayerManager` não contém nenhum `switch` por skill; valores de gameplay leem de `skills.yml`.

---

### T1.6 — Testes unitários do registry

**Arquivos:** `src/test/java/com/project/rpgplugin/skill/SkillRegistryTest.java`

Casos:
- IDs únicos (nenhuma duplicata).
- Contagem por tier (11/11/13) e por tipo.
- Custo derivado do tier (Bronze=1, Prata=3, Ouro=5).
- Toda skill tem `icon`, `trigger` e chaves i18n não-nulas.
- Passivas marcadas corretamente (`sight`, `jump_boost`).

```java
@Test void everySkillHasUniqueIdAndMetadata() {
    SkillRegistry r = TestRegistries.full();
    assertEquals(35, r.size());
    assertEquals(11, r.byTier(SkillTier.BRONZE).size());
    r.all().forEach(s -> {
        assertNotNull(s.icon()); assertNotNull(s.trigger());
        assertEquals(s.tier().xpCost(), costOf(s));
    });
}
```

**CA:** `mvn test` verde; cobertura dos invariantes acima.

---

## Estratégia de migração segura (evitar regressão)

1. Implementar registry + triggers **ao lado** do `ClassListeners` antigo (feature flag interna).
2. Migrar skill por skill, comparando comportamento no smoke test.
3. Quando todas migradas e validadas, **remover** `ClassListeners` e os switches de `PlayerManager`.
4. Commit por grupo de skills (explorer/miner/builder) facilita revisão e bisect.

## Edge cases

- Skills que combinam **interact + block break** (ex: `silk_touch`, `molten_touch`) precisam de trigger em dois eventos; modelar como `CompositeTrigger` ou registrar handlers nos dois pontos.
- `transmutation` tem dois ramos (ferro→ouro, ouro→diamante) e exige `amount >= 5`; preservar no `matches`/`activate`.
- Sinergia e dificuldade **não** são skills — ficam em `SynergyService`/`DifficultyService`, alimentados pelo registry.

## Definition of Done

- [ ] `ClassListeners.java` removido; dispatcher fino no lugar.
- [ ] `PlayerManager` sem `switch` por skill.
- [ ] `SkillRegistry.size() == 35` e testes verdes.
- [ ] Smoke test sem regressão (todas as ativações idênticas).
- [ ] Parâmetros de skill lidos de `skills.yml`.
