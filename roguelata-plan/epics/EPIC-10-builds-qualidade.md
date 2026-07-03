# EPIC-10 — Sinergias, Augments Faltantes, i18n, Qualidade & Remoção de Legado (pendências)

**Dependências:** T10.23 é **o mesmo trabalho** de EPIC-8 T8.4/T8.5 — resolver uma vez só.

> Maior concentração de pendências do backlog. Onze augments de `augments.yml` estão **declarados mas sem handler nenhum** — a carta existe no catálogo mas não faz nada em jogo.

| Tarefa | Status | Descrição | Arquivos | CA |
|---|---|---|---|---|
| **T10.2** | ❌ não existe | Cartas "ascendant" (upgrade de sinergia ao atingir patamar) sem handler. | `core/build/SynergyService.java` | Sinergia "ascendant" aplica bônus extra no patamar certo |
| **T10.4** | ❌ não existe | Conceito de `extra_skill_slot` citado no design não tem implementação — decidir se ainda faz sentido (não há mais "loadout" desde o draft substituir a compra). | — | Decisão registrada + implementação ou remoção formal do conceito |
| **T10.5** | ❌ augment sem handler | `xp_gain` — falta listener de `PlayerExpChangeEvent` aplicando o multiplicador. | novo listener | XP ganho reflete o multiplicador do augment |
| **T10.7** | ❌ augment sem handler | `fall_damage_reduction` — falta listener de `EntityDamageEvent` (causa QUEDA). | novo listener | Dano de queda reduzido conforme o augment |
| **T10.9** | ❌ augment sem handler | `extra_mob_drops` — falta listener de `EntityDeathEvent`. | novo listener | Mobs dropam item extra conforme o augment |
| **T10.10** | ❌ augment sem handler | `double_ore_drop` — falta listener de `BlockBreakEvent`. | novo listener | Minério quebrado tem chance de dobrar |
| **T10.11** | ❌ augment sem handler | `mining_speed` — falta handler (Haste passivo enquanto minerando). | novo listener/`PassiveTask` | Mineração mais rápida conforme o augment |
| **T10.12** | ❌ augment sem handler | `crop_yield` — falta listener de colheita. | novo listener | Colheitas rendem mais conforme o augment |
| **T10.13** | ❌ augment sem handler | `item_magnet_range` — falta `PassiveTask` que atrai itens próximos. | novo `PassiveTask` | Itens no raio configurado são atraídos |
| **T10.14** | ❌ augment sem handler | `double_jump` — falta listener/toggle de segundo pulo no ar. | novo listener | Segundo pulo funciona quando o augment está ativo |
| **T10.15** | ❌ augment sem handler | `blind_on_hit_chance` — falta listener de dano aplicando cegueira por chance. | novo listener | Chance de cegar o atacante ao ser atingido |
| **T10.17** | 🔴 parcial | Vários efeitos `ON_KILL` declarados em `augments.yml` não têm handler correspondente (além dos já listados no EPIC-7: `midas`, `soul_harvest`). | `listener/CombatListener.java` (ou dedicado) | Todo efeito `ON_KILL` declarado tem handler |
| **T10.18** | 🔴 incompleto | Augment `giant` só aplica `MAX_HEALTH` — falta o dano extra e a penalidade de velocidade especificados no catálogo original. | handler do `giant` | `giant` aplica os 3 efeitos (vida, dano, lentidão) |
| **T10.19 / T10.21** | 🔴 parcial | `MessagesConfig`/i18n existe, mas `DraftMenu`/`CollectionMenu` não usam — várias strings ainda hardcoded em português direto no Java. | `ui/DraftMenu.java`, `ui/CollectionMenu.java`, `config/MessagesConfig.java` | Menus usam só `MessagesConfig`, zero string hardcoded |
| **T10.20** | 🔴 confirmado | **27 arquivos `.java`** ainda usam `§` em vez de Adventure (`Text.mm()`). | (levantar lista exata via `grep -rl '§' src/main/java`) | Zero ocorrência de `§` no código |
| **T10.23** | 🔴 **crítico** | `ClassListeners`, `PlayerManager` e `SkillGUI` (legado) ainda existem e estão registrados em `RPGPlugin`. Mesmo bloqueio de EPIC-8 T8.4/T8.5. | `RPGPlugin.java` (remover registro + deletar as 3 classes) | As 3 classes não existem mais no projeto |
| **T10.25** | 🔴 quase ausente | Testes de sinergia, wiring de augments e i18n praticamente inexistentes. | `src/test/java/` | Testes cobrindo sinergias/augments/i18n passam |
| **T10.26** | ❌ não existe | Sem CI — nenhum `.github/workflows/`. | novo `.github/workflows/ci.yml` | PRs rodam `mvn clean package`/testes automaticamente |
| **T10.27** | ❓ não verificado | README/CHANGELOG não conferidos nesta rodada quanto à precisão. | `README.md`, `CHANGELOG.md` | Conteúdo confere com o código atual |

## Definition of Done
- [ ] Todas as tarefas acima resolvidas.
