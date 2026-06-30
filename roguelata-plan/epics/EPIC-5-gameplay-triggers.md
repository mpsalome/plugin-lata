# EPIC-5 — Gatilhos por Gameplay (distância, recall exponencial, gates)

**Fase:** F3 · **Prioridade:** P1 · **Dependências:** EPIC-1, EPIC-2, EPIC-3
**Objetivo:** progressão e ativações guiadas por **ação de jogo** (andar/minerar/etc.), incluindo a skill de **recall por distância com custo exponencial** (pedido explícito da conversa) e os **gates** que injetam cartas no pool conforme o jogador investe numa atividade.

> Conversa: *"depois de andar 2k blocos o player pode voltar ao respawn"* + *"bota 5k"* + *"a cada vez que usar aumenta um teco"* (exponencial). Também: *"minerar lvl Y libera a passiva de comer carvão"*.

---

## 1. `DistanceTracker` — distância sem custo de CPU

**Não** usar `PlayerMoveEvent` cru (dispara dezenas de vezes/seg). Usar **amostragem periódica**:

```java
public final class DistanceTracker {  // task a cada N ticks (default 10 = 0.5s)
    private final Map<UUID, Location> last = new HashMap<>();
    public void tick() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            Location prev = last.get(p.getUniqueId());
            Location cur = p.getLocation();
            if (prev != null && prev.getWorld() == cur.getWorld()) {
                double d = prev.distance(cur);
                if (d > 0.05 && d < 64) {            // ignora teleporte/jitter
                    RunState run = store.get(p);
                    run.addBlocksWalked(d);
                    onDistanceTick(p, run);          // dispara DistanceTriggers
                }
            }
            last.put(p.getUniqueId(), cur.clone());
        }
    }
    public void clear(UUID id) { last.remove(id); }  // PlayerQuitEvent
}
```

`run.blocksWalked` é **resetado na morte** (EPIC-3).

---

## 2. Recall por distância com custo exponencial

A carta `recall` (Apêndice A) deixa de consumir item e passa a ser **liberada por distância acumulada**, com requisito **crescente por uso**:

```
req(uses) = base * growth^uses           (com cap opcional)
```

```yaml
# skills.yml
recall:
  base_distance: 2000     # 1º uso libera após 2000 blocos
  growth: 1.5             # cada uso aumenta o requisito (2000 -> 3000 -> 4500 ...)
  cap: 0                  # 0 = sem teto
  cooldown: 0             # gating é pela distância, não por tempo
```

```java
public final class RecallProgression {
    public double required(RunState run) {
        double r = cfg.base() * Math.pow(cfg.growth(), run.recallUses());
        return cfg.cap() > 0 ? Math.min(r, cfg.cap()) : r;
    }
    public boolean ready(RunState run) {
        return run.hasCard("recall") && run.blocksWalkedSinceRecall() >= required(run);
    }
    public void use(Player p, RunState run) {
        p.teleport(spawnResolver.resolve(p));
        run.incRecallUses();
        run.resetBlocksSinceRecall();
        feedback(p, "recall.used", Sound.ENTITY_ENDERMAN_TELEPORT);
    }
}
```

- Ativação: clique direito com o Livro / item de recall **ou** keybind, **somente quando `ready`**.
- HUD mostra progresso: `Recall: 1340/2000` (action bar/boss bar).

---

## 3. Gates — investir numa atividade injeta cartas no pool

No novo design, os **gates** do AuraSkills não "desbloqueiam para equipar" — eles **alimentam o draft**. Duas modalidades (config por gate):

- `pool`: ao atingir o nível, a carta entra no **pool ofertável** (passa a poder aparecer no draft).
- `grant`: ao atingir o nível, a carta é **concedida direto** (sem draft) — para os exemplos clássicos da conversa.

```yaml
# gates.yml — substitui o setupProgressionGates() hardcoded
gates:
  - { skill: MINING,  level: 5,  card: diet,        mode: grant }   # "minerar lvl Y -> comer carvão"
  - { skill: AGILITY, level: 2,  card: dash,        mode: grant }   # "andar X -> flower dash"
  - { skill: MINING,  level: 10, card: haste,       mode: pool }
  - { skill: FORAGING,level: 8,  card: woodcutter,  mode: pool }
  # ... gates configuráveis sem recompilar
```

`GateRegistry` ouve `SkillLevelUpEvent` (AuraSkills) e aplica o gate. Em **standalone** (sem AuraSkills), gates do tipo `grant` podem ser disparados por contadores próprios (blocos minerados/andados) — opcional.

---

## 4. Tarefas

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T5.1** | `DistanceTracker` por amostragem + task; limpa no quit | `core/progression/DistanceTracker.java`, `task/DistanceTask.java` | Distância acumula com CPU baixo; ignora teleporte |
| **T5.2** | `RecallProgression` com requisito exponencial e cap | `core/progression/RecallProgression.java`, `skills.yml` | 1º uso a 2000, 2º a 3000, 3º a 4500 (growth 1.5) |
| **T5.3** | Ativação de recall só quando `ready` + feedback/HUD | `listener`, `ui/HudService.java` | HUD mostra progresso; recall bloqueado antes do requisito |
| **T5.4** | `GateRegistry` (modos `pool`/`grant`) lendo `gates.yml` | `core/progression/GateRegistry.java`, `resources/gates.yml` | Gates editáveis sem recompilar; injetam no pool/concedem |
| **T5.5** | Migrar `setupProgressionGates()` hardcoded → `gates.yml` | remove `AuraSkillsIntegration` gates | Nenhum gate hardcoded em Java |
| **T5.6** | `DistanceTrigger` genérico (andar X → efeito/carta) | `core/skill/trigger/DistanceTrigger.java` | Reutilizável por outras cartas além do recall |
| **T5.7** | Testes: requisito exponencial, amostragem de distância, gates | `src/test/...` | `mvn test` verde |

---

## 5. Edge cases

- **Teleporte/elytra/portal:** `DistanceTracker` ignora deltas grandes (>64 blocos) e troca de mundo.
- **AFK/jitter:** delta mínimo (>0.05) evita acumular parado.
- **Recall + mundo fresco (`random_far`):** recall leva ao **spawn da run atual** (coerente), não ao ponto de morte.
- **Gate `grant` com carta já possuída:** no-op (não duplica além de `maxStacks`).
- **Reset:** `recallUses` e `blocksWalked` zeram na morte (EPIC-3).

## 6. Definition of Done

- [ ] T5.1–T5.7 com CA satisfeitos.
- [ ] Recall por distância com custo exponencial funcionando e visível no HUD.
- [ ] Gates 100% em `gates.yml` (zero hardcode).
- [ ] `DistanceTracker` sem impacto perceptível de TPS.
- [ ] `mvn test` e smoke test verdes.
