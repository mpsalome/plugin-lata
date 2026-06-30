# EPIC-3 — Ciclo de Run & Reset Total na Morte

**Fase:** F2 · **Prioridade:** P0 · **Dependências:** EPIC-1, EPIC-2, EPIC-9
**Objetivo:** modelar a **run** como unidade central e garantir o **reset total na morte** — nada de poder sobrevive entre runs. Define início, fim (derrota **e** vitória) e o estado limpo de recomeço.

> Decisão travada: **reset total, sem meta-progressão.** Tudo que dá poder é apagado ao morrer.

---

## 1. Modelo de estado da run

```java
public final class RunState {
    private final UUID player;
    private int level;                          // nível da run (XP vanilla espelhado)
    private final List<OwnedCard> cards = new ArrayList<>();   // cartas + stacks
    private final Set<String> activeModifiers = new LinkedHashSet<>(); // Mayhem (EPIC-4)
    private int milestonesReached;
    private long blocksWalked;                  // EPIC-5
    private int recallUses;                     // EPIC-5 (custo exponencial)
    private boolean phoenixCharge;              // ex.: carta phoenix
    private long startedAt;
    private RunOutcome outcome = RunOutcome.ONGOING;  // ONGOING | DIED | VICTORY

    // multiplicadores derivados das cartas (recalculados pelo StatService)
    private double xpMult = 1.0, cooldownMult = 1.0, lifestealPct = 0.0, damageDealtMult = 1.0, damageTakenMult = 1.0;

    public List<String> ownedAbilities() { /* ids de cartas kind==ABILITY */ }
    public int stacksOf(String cardId) { ... }
    public void add(Card c) { ... }   // incrementa stack ou adiciona
}
```

`OwnedCard` = `{ String cardId; int stacks; }`. Persistido pelo `PlayerDataStore` (EPIC-9), serialização versionada.

---

## 2. `RunManager` — ciclo de vida

```java
public final class RunManager {
    public void startRun(Player p) {            // primeiro join / após reset
        RunState run = new RunState(p.getUniqueId());
        run.setStartedAt(now());
        rollInitialKit(p, run);                 // (opcional) kit inicial — ver §6
        store.save(run);
        statService.recompute(p, run);
        hud.refresh(p);
    }

    public void endRun(Player p, RunOutcome outcome) {
        RunState run = store.get(p);
        run.setOutcome(outcome);
        if (outcome == RunOutcome.VICTORY) victory.celebrate(p, run);   // EPIC-7
        resetService.fullReset(p, run);         // §3
        startRun(p);                            // nova run limpa
    }
}
```

Gatilhos:
- **DIED:** `PlayerDeathEvent` → `endRun(VICTORY? no → DIED)`.
- **VICTORY:** boss final derrotado / objetivo cumprido (EPIC-7) → `endRun(VICTORY)`.

---

## 3. `ResetService.fullReset` — o que o reset total apaga

Especificação **exata** do reset (cada item é um CA verificável):

```java
public void fullReset(Player p, RunState run) {
    // 1. CARTAS: chama onRemove de cada carta (desfaz hooks), limpa lista
    run.cards().forEach(oc -> registry.byId(oc.cardId()).ifPresent(c -> c.onRemove(p, run)));
    run.cards().clear();
    run.ownedAbilities().clear();

    // 2. STATS: recompute zera todos os AttributeModifier do RogueLata (volta ao vanilla)
    statService.recompute(p, run);          // com 0 cartas => baseline
    p.setHealth(Math.min(p.getHealth(), maxHealthOf(p)));  // evita > max

    // 3. MAYHEM: limpa modificadores ativos (re-sorteiam na nova run)
    mayhemService.clear(p, run);            // EPIC-4

    // 4. AURASKILLS: zera skills + XP nativos (alinhado a on_death do AuraSkills)
    auraSkillsBridge.resetAll(p);           // EPIC-6

    // 5. NÍVEL/XP vanilla -> 0
    p.setLevel(0); p.setExp(0f);

    // 6. ITENS de skill no inventário (tagueados) removidos
    inventoryUtil.removeTaggedSkillItems(p);

    // 7. PROGRESSÃO da run: distância, usos de recall, cooldowns, charges
    cooldownService.clearAll(p.getUniqueId());
    run.resetProgression();                 // blocksWalked=0, recallUses=0, phoenixCharge=false, milestones=0

    // 8. EFEITOS de poção aplicados por nós (limpa só os nossos / passivas)
    effectUtil.clearPluginEffects(p);

    // 9. TELEPORTE para spawn (ou ponto fresco — §5)
    p.teleport(spawnResolver.resolve(p));

    // 10. PRESERVA: o Livro de RPG (recria se faltar)
    bookUtil.ensureRpgBook(p);
    msg.send(p, "run.reset");
}
```

**O que NÃO reseta:** apenas o Livro de RPG (ferramenta de menu). Estruturas/itens vanilla do mundo são tratados em §5 (decisão de "mundo fresco").

---

## 4. Tarefas

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T3.1** | `RunState` + `OwnedCard` + serialização versionada | `core/run/RunState.java`, `data/*` | Estado da run persiste e recarrega íntegro |
| **T3.2** | `RunManager` (start/end) + outcome (ONGOING/DIED/VICTORY) | `core/run/RunManager.java` | Morte e vitória disparam fim de run corretamente |
| **T3.3** | `ResetService.fullReset` com os 10 passos da §3 | `core/run/ResetService.java` | Cada passo coberto por teste/checklist; após reset, atributos = baseline vanilla |
| **T3.4** | Listener de morte/respawn integrado ao `RunManager` | `listener/PlayerLifecycleListener.java` | `PlayerDeathEvent` encerra a run; respawn começa nova |
| **T3.5** | Espelhar nível vanilla ↔ `RunState.level` para o draft (EPIC-2) | `RunManager`, listener de XP | Draft usa o nível correto da run |
| **T3.6** | (Decisão) **Mundo fresco**: teleporte aleatório distante OU mesmo spawn | `core/run/SpawnResolver.java`, config | Comportamento configurável; default escolhido em §5 |
| **T3.7** | `/run` e `/run info`: mostra estado da run (cartas, modifiers, milestone) | `command/RunCommand.java` | Comando lista a run atual |
| **T3.8** | Testes do reset (idempotência, sem leak, atributos no baseline) | `src/test/...` | `mvn test` verde |

---

## 5. Decisão: "mundo fresco" a cada run

"Mesmo mundo + perdi itens" é um reset fraco (sua base continua lá). Opções no `SpawnResolver`:

| Modo | Como | Trade-off |
|------|------|-----------|
| `world_spawn` (atual) | teleporta ao spawn do mundo | Simples, mas mundo persiste (fraco como roguelike) |
| `random_far` (**recomendado**) | teleporta a um ponto aleatório distante (raio configurável) | Terreno fresco a cada run, barato; estruturas antigas ficam longe |
| `instanced` | mundo/arena instanciado por run | Mais "puro", porém pesado (geração/limpeza de mundos) |

**Default sugerido:** `random_far` (raio 5k–20k blocos). Deixar `instanced` como evolução futura (casa com o Modo Arena do EPIC-7).

---

## 6. (Opcional) Kit inicial sorteado

Para reforçar o "ARAM" (cada run começa diferente), `rollInitialKit` pode conceder **1 carta Bronze aleatória** (ou um mini-arquétipo) no início da run. Configurável (`run.starting_kit.enabled`). Não viola o reset (é sorteado de novo a cada run).

---

## 7. Edge cases

- **Morte por causas múltiplas / void:** garantir que `endRun` rode uma única vez (flag anti-reentrância).
- **Keep-inventory de outros plugins:** o reset remove itens de skill mesmo com keepInventory; tratar explicitamente.
- **Logout durante run:** persistir `RunState`; ao logar, `StatService.recompute` reaplica buffs.
- **Vitória e morte no mesmo tick:** prioridade VICTORY > DIED.
- **`phoenix`:** ao morrer com `phoenixCharge`, **negar a morte** (revive) em vez de `endRun`; consumir o charge; só encerra a run na próxima morte.

## 8. Definition of Done

- [ ] T3.1–T3.8 com CA satisfeitos.
- [ ] Morte → reset total verificado item a item (§3).
- [ ] Vitória encerra a run com celebração (gancho EPIC-7).
- [ ] Nenhum poder persiste entre runs; atributos voltam ao baseline vanilla.
- [ ] `mvn test` e smoke test verdes.
