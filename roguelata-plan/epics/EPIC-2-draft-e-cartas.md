# EPIC-2 — Sistema de Draft (Choose-3) + Catálogo de Cartas

**Fase:** F2 · **Prioridade:** P0 (coração do roguelike) · **Dependências:** EPIC-0, EPIC-1, EPIC-9
**Objetivo:** substituir a compra/equipar por um **draft de 3 cartas a cada X níveis**. As 35 habilidades viram cartas; somam-se ~50 augments novos. Pool dividido por **tier (raridade)**; a chance de tier alto **escala com o nível**, mas tier alto pode cair cedo num rate baixo. Tudo acumulado é **resetado na morte** (EPIC-3).

> Catálogo completo de cartas: [appendices/A-catalogo-cartas.md](../appendices/A-catalogo-cartas.md).

---

## 1. Conceito unificado: `Card`

Habilidades (ability) e augments (stat/passiva/regra) convergem para **uma** abstração draftável.

```java
public interface Card {
    String id();
    CardTier tier();                 // BRONZE | SILVER | GOLD (= raridade)
    Set<CardTag> tags();             // EXPLORER, MINER, BUILDER, TANK, DPS, LOOT, RISK, MOBILITY, SUSTAIN, UTILITY
    CardKind kind();                 // ABILITY | AUGMENT
    Material icon();
    int maxStacks();                 // 1 = único; >1 = empilhável
    boolean offerable(RunState run); // pré-requisitos / exclusões (não ofertar se já no máximo)
    void onAcquire(Player p, RunState run);   // aplica efeito imediato (ex: +vida)
    void onRemove(Player p, RunState run);    // desfaz no reset (recalcular stats)
    default String nameKey() { return "card." + id() + ".name"; }
    default String descKey() { return "card." + id() + ".desc"; }
}
```

```java
public enum CardKind { ABILITY, AUGMENT }

public enum CardTag {
    EXPLORER, MINER, BUILDER,          // tipos (sinergia — EPIC-10)
    TANK, DPS, MOBILITY, LOOT,         // funções
    SUSTAIN, RISK, UTILITY, ECONOMY
}
```

- **Ability cards** envolvem um `Skill` do EPIC-1 (`onAcquire` adiciona o id em `run.ownedAbilities`).
- **Augment cards** alteram stats/regras: ajustam atributos via `StatService` (recálculo idempotente) ou registram hooks de evento.

**`StatService` (recálculo idempotente):** dado o conjunto de cartas da run, recalcula atributos do player (vida máx, dano, velocidade, armadura, redução de cooldown, % XP, etc.) a partir do zero. Chamado em: aquisição de carta, respawn e reset. Evita drift de buffs empilhados.

```java
public final class StatService {
    public void recompute(Player p, RunState run) {
        // 1. zera modificadores RogueLata (limpa AttributeModifiers com nosso NamespacedKey)
        // 2. soma contribuições de cada carta possuída (respeitando stacks)
        // 3. aplica como AttributeModifier (MAX_HEALTH, ATTACK_DAMAGE, MOVEMENT_SPEED, ARMOR...)
        // 4. atualiza multiplicadores não-atributo no RunState (xpMult, cooldownMult, lifestealPct...)
    }
}
```

---

## 2. `CardRegistry`

```java
public final class CardRegistry {
    private final Map<String, Card> byId = new LinkedHashMap<>();
    public void register(Card c) { /* rejeita id duplicado */ }
    public List<Card> byTier(CardTier t) { ... }
    public List<Card> byTag(CardTag t) { ... }
    public List<Card> offerable(RunState run) { ... }   // filtra offerable() == true
    public Collection<Card> all() { ... }
}
```

Registro central no `onEnable`: registra as 35 ability cards (envolvendo os `Skill` do EPIC-1) + os augments do catálogo, **ou** carrega tudo de `cards.yml`/`augments.yml` (preferível para balanceamento sem recompilar).

---

## 3. Motor do Draft

### 3.1 Gatilho
A cada **`draft.every_levels`** níveis (default 3), ao subir de nível, abrir um draft. Se o player subir vários níveis de uma vez, **enfileirar** os drafts (não perder nenhum). Drafts pendentes reabrem ao fechar o menu / ao logar.

### 3.2 Ponderação por tier escalada pelo nível (`DraftWeighting`)

Tabela default (configurável em `draft.yml`):

| Faixa de nível | 🥉 Bronze | 🥈 Prata | 🥇 Ouro |
|----------------|-----------|----------|---------|
| 1–9 | 80% | 18% | 2% |
| 10–19 | 60% | 30% | 10% |
| 20–29 | 40% | 40% | 20% |
| 30+ | 25% | 40% | 35% |

```java
public final class DraftWeighting {
    // retorna {bronze, silver, gold} normalizado para o nível
    public double[] weightsFor(int level) { /* lê faixas de draft.yml */ }
}
```

### 3.3 Sorteio das 3 cartas (`DraftService.roll`)

Algoritmo:
1. Determinar pool `offerable(run)` (exclui cartas no máximo de stacks, exclui únicas já possuídas, respeita prereqs).
2. Para **cada um dos 3 slots**: sortear um tier pelos pesos da faixa do nível; dentro do tier, sortear uma carta uniformemente (ou por `weight` individual da carta).
3. **Sem repetir** carta dentro da mesma oferta de 3.
4. Se um tier não tiver carta ofertável, **fazer fallback** para o tier inferior (nunca devolver slot vazio).
5. Garantir pelo menos 1 carta **utilizável** (não ofertar só passivas redundantes).

```java
public DraftSession roll(Player p, RunState run) {
    double[] w = weighting.weightsFor(run.level());
    List<Card> pool = registry.offerable(run);
    List<Card> picks = new ArrayList<>(3);
    while (picks.size() < 3) {
        CardTier tier = pickTier(w);                      // ponderado
        Card c = pickFromTier(pool, tier, picks);         // fallback p/ tier menor
        if (c != null && !picks.contains(c)) picks.add(c);
    }
    return new DraftSession(p.getUniqueId(), picks);
}
```

### 3.4 Escolha e aplicação
- Player clica 1 das 3 → `card.onAcquire(p, run)` → `run.add(card)` → `StatService.recompute` → feedback (som/título por tier) → fecha menu → abre próximo draft enfileirado, se houver.
- **Reroll opcional** (`draft.reroll.enabled`): re-sorteia as 3 por um custo (XP/nível). Limite por draft (`draft.reroll.max_per_draft`) para não virar busca infinita.
- **Skip opcional** (`draft.allow_skip`): pular concede um bônus pequeno (ex: cura/mana) para não travar o jogador indeciso.

### 3.5 Persistência e reset
- Cartas adquiridas vivem em `RunState.cards` (EPIC-3), persistido pelo `PlayerDataStore`.
- **Na morte, tudo é apagado** (EPIC-3, `ResetService`): `onRemove` de cada carta é chamado e `StatService.recompute` zera os modificadores.

---

## 4. Tarefas

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T2.1** | Modelar `Card`, `CardTier`, `CardTag`, `CardKind`, `CardRegistry` | `core/card/*` | Compila; IDs únicos; abilities e augments coexistem |
| **T2.2** | `StatService` idempotente (recompute a partir das cartas) | `core/card/StatService.java` | Adquirir/remover carta nunca causa drift de buff; reset zera tudo |
| **T2.3** | `DraftWeighting` lendo a tabela de `draft.yml` | `core/draft/DraftWeighting.java`, `resources/draft.yml` | Pesos batem com a tabela por faixa; editáveis sem recompilar |
| **T2.4** | `DraftService.roll` (sorteio ponderado, sem repetição, fallback de tier) | `core/draft/DraftService.java` | 3 cartas distintas; ouro raro no early (teste estatístico) |
| **T2.5** | Gatilho a cada X níveis + **fila** de drafts pendentes | `listener/PlayerLevelListener.java`, `RunState` | Subir N níveis de uma vez enfileira N drafts; nada se perde |
| **T2.6** | Migrar as 35 habilidades para **ability cards** (envolvendo `Skill` do EPIC-1) | `core/card/ability/*` ou `cards.yml` | Draftar ability card ativa o poder na run |
| **T2.7** | Implementar os **augments** do catálogo (ver Apêndice A) | `core/card/augment/*`, `resources/augments.yml` | Todos os augments do catálogo aplicáveis e empilháveis conforme `maxStacks` |
| **T2.8** | Reroll/skip configuráveis | `DraftService`, `draft.yml` | Reroll consome custo e respeita limite; skip dá bônus |
| **T2.9** | Integração com reset total (chama `onRemove` + recompute) | `core/run/ResetService.java` | Após morte, 0 cartas, atributos no baseline vanilla |
| **T2.10** | `DraftMenu` (UI) — ver EPIC-8 para o polish | `ui/DraftMenu.java` | 3 cartas grandes; cor/borda por tier; som por raridade |
| **T2.11** | Testes: weighting, sorteio sem repetição, stacking, idempotência do StatService | `src/test/java/...` | `mvn test` verde |

---

## 5. Schema de config

```yaml
# resources/draft.yml
draft:
  every_levels: 3            # abre draft a cada N níveis
  cards_per_offer: 3
  queue_pending: true        # enfileira drafts se subir vários níveis juntos
  allow_skip: true
  skip_bonus: { heal: 6, mana: 20 }
  reroll:
    enabled: true
    cost_levels: 1
    max_per_draft: 1
  # pesos por faixa de nível (tier scaling)
  weights:
    - { from: 1,  to: 9,  bronze: 80, silver: 18, gold: 2 }
    - { from: 10, to: 19, bronze: 60, silver: 30, gold: 10 }
    - { from: 20, to: 29, bronze: 40, silver: 40, gold: 20 }
    - { from: 30, to: 999, bronze: 25, silver: 40, gold: 35 }
```

```yaml
# resources/augments.yml (exemplo — catálogo completo no Apêndice A)
max_health:
  tier: bronze
  tags: [tank, sustain]
  kind: augment
  icon: APPLE
  max_stacks: 10
  effect: { type: ATTRIBUTE, attribute: MAX_HEALTH, add: 2 }   # +1 coração por stack
xp_boost:
  tier: bronze
  tags: [utility]
  kind: augment
  icon: EXPERIENCE_BOTTLE
  max_stacks: 5
  effect: { type: MULT, key: xp_gain, add: 0.10 }              # +10% XP por stack
lifesteal:
  tier: silver
  tags: [sustain, dps]
  kind: augment
  icon: REDSTONE
  max_stacks: 3
  effect: { type: ON_DAMAGE_DEALT, heal_pct: 0.05 }
glass_cannon:
  tier: gold
  tags: [risk, dps]
  kind: augment
  icon: TNT
  max_stacks: 1
  effect: { type: MULT_MULTI, damage_dealt: 0.40, damage_taken: 0.30 }
```

---

## 6. Edge cases / decisões de balanceamento

- **Pool esgotado de um tier:** fallback para tier inferior; nunca slot vazio.
- **Cartas únicas já possuídas:** `offerable` retorna false (não reaparecem).
- **Stacking de atributos vs. teto do MC:** vida máx tem limite prático; o `StatService` deve respeitar tetos e/ou usar `scale` configurável.
- **Power creep:** como tudo reseta na morte e a dificuldade sobe via AuraMobs + Mayhem, o acúmulo é auto-balanceado pela profundidade. Ainda assim, manter `maxStacks` e custos de reroll para não trivializar.
- **Cartas que dependem de plugin (mana/AuraSkills):** ter fallback standalone (ex: carta de mana só entra no pool se AuraSkills presente).
- **Anti-frustração:** garantir que toda oferta tenha ao menos uma carta "útil agora" para o estado do player (heurística simples por tags do que ele já tem).

## 7. Definition of Done

- [ ] T2.1–T2.11 com CA satisfeitos.
- [ ] Draft abre a cada X níveis; 3 cartas distintas; tier escala com nível.
- [ ] Todas as cartas do [Apêndice A](../appendices/A-catalogo-cartas.md) registradas e aplicáveis.
- [ ] Sistema antigo de comprar/equipar **removido**.
- [ ] Reset total zera cartas e atributos (validado com EPIC-3).
- [ ] `mvn test` e smoke test verdes.
