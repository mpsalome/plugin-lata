# EPIC-7 — Dificuldade, Elites & Bosses vanilla-first (pendências)

**Dependências:** nenhuma

| Tarefa | Status | Descrição | Arquivos | CA |
|---|---|---|---|---|
| **T7.3** | 🔴 parcial | `EliteFactory` aplica atributos (escala, vida) mas **não tem BossBar nem fases via scheduler** — a experiência de "boss" descrita no design não existe de fato. | `core/mob/EliteFactory.java` (ou pacote correspondente) | Boss spawna com BossBar visível e muda de comportamento em pelo menos 2 fases de vida |
| **T7.4** | ❌ não existe | `mobs.yml`/`bosses.yml` data-driven não existem — definições de elite/boss estão hardcoded (se existirem). | novos `resources/mobs.yml`, `resources/bosses.yml` | Adicionar/ajustar um boss não exige recompilar |
| **T7.5** | ❌ não existe | `MobSpawnService` (regras de quando/onde spawnar elites e o boss da run) não existe. | novo `core/mob/MobSpawnService.java` | Elites/boss spawnam segundo regras configuráveis (profundidade, milestone) |
| **T7.7** | 🔴 parcial | `CombatListener` cobre `crit`, `low_hp_dmg`, `damage_dealt`, `thorns`, `execute`; **faltam** `lifesteal` (Lorde Vampiro), `heal_pct`, e efeitos `ON_KILL` (`midas`/Toque de Midas, `soul_harvest`/Colheita de Almas). | `listener/CombatListener.java` | Augments com esses efeitos funcionam em combate |
| **T7.8** | ❌ não existe | Matar o boss não conta como milestone — `MilestoneService` não é notificado. | `listener/CombatListener.java`, `core/mayhem/MilestoneService.java` | Derrotar o boss dispara um milestone (e Mayhem, se aplicável) |
| **T7.9–T7.11** | ❌ não existe (baixa prioridade) | Bridges opcionais de enriquecimento (AuraMobs, MythicMobs, ModelEngine) não existem — mas são **enriquecimento opcional**, não bloqueiam o core (que já funciona 100% vanilla). | `integration/` | Bridges detectam o plugin e enriquecem sem quebrar o modo standalone |

## Definition of Done
- [ ] T7.3, T7.4, T7.5, T7.7, T7.8 resolvidas (T7.9–T7.11 opcionais, podem ficar para depois).
