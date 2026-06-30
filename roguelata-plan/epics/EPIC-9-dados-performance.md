# EPIC-9 — Dados, Cooldowns, Performance & Persistência

**Fase:** F1 (fundação) · **Prioridade:** P0 · **Dependências:** EPIC-1, EPIC-3
**Objetivo:** persistir a run, unificar/limpar cooldowns e estado em memória, matar leaks e garantir TPS saudável. Hoje **a run vive só em memória e some no restart**, e **não há limpeza no quit**.

---

## 1. Estado atual no código (auditado) — gaps reais

| Gap | Evidência | Impacto |
|-----|-----------|---------|
| **`RunState` não persiste** | `RunManager.activeRuns` é um `HashMap<UUID,RunState>`; não há store/serialização | Restart do servidor **apaga a run** de todos |
| **Sem `onQuit`** | `PlayerLifecycleListener` tem join/death/respawn, **não tem quit** | `activeRuns`, cooldowns, distância, molten — nada é limpo/salvo no logout |
| **Cooldowns em memória** | `SkillServices.cooldowns: Map<UUID,Map<String,Long>>` | Some no restart; sem cleanup no quit |
| **`reinforcedBlocks` global** | `SkillServices` `Set<Location>` | Cresce indefinidamente; **não é limpo no reset da run** (vaza entre runs) |
| **`moltenTouchActiveUntil`** | `SkillServices` `Map<UUID,Long>` | Sem cleanup no quit |
| **`DistanceTracker.last`** | tem `clear(UUID)` mas **ninguém chama** (sem onQuit) | Leak do mapa de última localização |
| **`onMove` por evento** | `SkillDispatchListener.onMove` dispara a cada mudança de bloco | Custo de CPU com muitos players |

---

## 2. Tarefas

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T9.1** | `PlayerDataStore` (interface) + impl (YAML por player **ou** SQLite) com **serialização versionada** do `RunState` | `data/PlayerDataStore.java`, `data/YamlDataStore.java` | Run sobrevive a relog **e** restart do servidor |
| **T9.2** | Serializar/desserializar `RunState`: `ownedCards`, `cardCounts`, `ownedAbilities`, `multipliers`, `level`, `pendingDrafts`, `milestonesReached`, `activeModifiers`, `phoenixCharge`, `blocksWalked/SinceRecall`, `recallUses`, `startedAt`, `outcome` | `RunState`, store | Round-trip salva/carrega íntegro (teste) |
| **T9.3** | `onQuit` no `PlayerLifecycleListener`: **salvar** run + **limpar** mapas em memória (`SkillServices.clearPlayerCooldowns`, `DistanceTracker.clear`, molten, `activeRuns` se aplicável) | `listener/PlayerLifecycleListener.java` | Nenhum `Map<UUID,...>` retém jogador após logout |
| **T9.4** | `onJoin`: **carregar** run persistida (em vez de `startRun` sempre) e `StatService.recompute` para reaplicar buffs; só `startRun` se não houver run salva | `PlayerLifecycleListener`, `RunManager` | Relogar no meio da run mantém cartas/atributos/mayhem |
| **T9.5** | Mover `reinforcedBlocks` e `moltenTouch` para estado **com TTL/limpeza** e **limpá-los no `fullReset`** (não vazar entre runs) | `SkillServices` ou novo `WorldStateService`, `ResetService` | `reinforcedBlocks` não cresce sem limite; zera por run |
| **T9.6** | Passivas via **`PassiveTask`** periódica (a cada ~10 ticks) em vez de `onMove` cru; `onMove` fica só para gatilhos que exigem evento | `task/PassiveTask.java`, `SkillDispatchListener` | Efeitos passivos mantidos com 1 task; queda de eventos por movimento |
| **T9.7** | Persistência **assíncrona** (I/O fora da main thread) com flush no `onDisable` | store, `RPGPlugin.onDisable` | Save não trava o tick; shutdown salva todos |
| **T9.8** | `CooldownService` dedicado (extrair de `SkillServices`) com cleanup no quit e (opcional) persistência | `data/CooldownService.java` | Cooldowns isolados, limpos no quit |
| **T9.9** | Folia-awareness: se alvo for Folia, schedulers regionais para ops de mundo/entidade | tasks/listeners | Sem `IllegalStateException` de thread em Folia |
| **T9.10** | Testes: round-trip de serialização, cleanup no quit (sem leak), idempotência do recompute pós-load | `src/test/...` | `mvn test` verde |

---

## 3. Decisão: a run sobrevive a quê?

| Evento | Comportamento |
|--------|---------------|
| **Relog (quit/join)** | Run **persiste** (carrega no join, salva no quit) — T9.3/T9.4 |
| **Restart do servidor** | Run **persiste** (store em disco) — T9.1 |
| **Morte** | Run **reseta** (EPIC-3 `fullReset`) — sagrado |
| **Vitória** | Run encerra + nova run (EPIC-3/EPIC-7) |

> Persistir a run **não** viola o "reset na morte" — só evita perder progresso por causa de logout/restart, que seria frustrante e não-intencional.

---

## 4. Serialização do `RunState` (esboço)

```yaml
# <dataFolder>/runs/<uuid>.yml
level: 14
pendingDrafts: 0
milestonesReached: 1
phoenixCharge: false
blocksWalked: 4210
blocksSinceRecall: 1210
recallUses: 1
startedAt: 1751240000000
outcome: ONGOING
cards:                 # id -> stacks
  dash: 1
  max_health: 4
  glass_cannon: 1
activeModifiers: [eternal_night]
```
Multiplicadores/abilities são **derivados** das cartas no load (via `StatService.recompute` + reaplicação de hooks) — não precisam ser persistidos, evitando drift.

## 5. Edge cases

- **Corrupção/versão antiga do arquivo:** ao falhar o parse, logar e iniciar run nova (não crashar o join).
- **Player nunca jogou:** sem arquivo → `startRun`.
- **Reaplicar passivas/hooks no load:** `onAcquire` de cartas com hook (onKill/onDamage) precisa rodar de novo no load (ou registrar via recompute).
- **Crash do servidor (sem `onDisable`):** autosave periódico mitiga perda.

## 6. Definition of Done

- [ ] T9.1–T9.10 com CA satisfeitos.
- [ ] Run sobrevive a relog e restart; morte continua resetando tudo.
- [ ] Zero leak de `Map<UUID,...>`/`Set<Location>` (teste de quit).
- [ ] `onMove` aliviado por `PassiveTask`; TPS estável com vários players.
- [ ] `mvn test` e smoke test verdes.
