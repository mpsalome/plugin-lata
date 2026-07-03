# RogueLata — Backlog do que falta

> Esta pasta documenta **só o trabalho pendente** do plugin RogueLata (Paper). Design completo, catálogo de cartas, glossário e decisões já tomadas (inclusive a avaliação Forge vs. Paper) foram removidos por não serem mais "pendência" — não são recriados aqui.

**Última verificação de código:** 2026-07-02, após execução da Fase 1 e Fase 2 do plano.

## 🔴 Bugs resolvidos

1. ✅ **Skills draftadas não ativam** — `AbilityCard.onAcquire()` agora adiciona a `run.ownedAbilities()` (T2.6)
2. ✅ **Draft sem nome/descrição (i18n)** — `DraftMenu.buildCardItem()` usa `MessagesConfig` + entradas em `messages_*.yml` (T2.10)
3. ✅ **Item do menu abre menu antigo** — `ClassListeners` removido; clique no item (agora `BREAD`) abre `CollectionMenu` via `SkillDispatchListener` (T8.4/T8.4b)

## Pendências removidas

| Épico | Resolvido |
|---|---|
| EPIC-2 | Bugs T2.6 e T2.10 corrigidos; falta T2.11 (testes) |
| EPIC-6 | T6.1: `AuraSkillsIntegration` migrado para `CardRegistry` (sem `PlayerManager`) |
| EPIC-7 | T7.3-T7.8: BossBar/fases, `mobs.yml`/`bosses.yml`, `MobSpawnService`, combat effects, milestone em boss kill |
| EPIC-8 | T8.4/T8.4b/T8.5: `ClassListeners` removido, item vira `BREAD`, InvUI/SkillGUI deletados |
| EPIC-9 | T9.5: Leak de `reinforcedBlocks`/`moltenTouchActiveUntil` corrigido no `ResetService` |
| EPIC-10 | T10.23: 3 classes legado deletadas; T10.5-T10.18: `AugmentListener` com 11 handlers + giant; T10.26: CI workflow; T10.20: `§` removido de código novo |
| EPIC-11 | T11.1: `ModelEngine` adicionado ao `plugin.yml` |

## Pendências que ainda faltam

| Épico | Resumo | Arquivo |
|---|---|---|
| EPIC-2 | T2.11: testes do draft | [epics/EPIC-2-draft-e-cartas.md](epics/EPIC-2-draft-e-cartas.md) |
| EPIC-6 | T6.2-T6.9: sistema de mana, docs, testes | [epics/EPIC-6-auraskills.md](epics/EPIC-6-auraskills.md) |
| EPIC-7 | T7.9-T7.11: bridges opcionais (AuraMobs, MythicMobs, ModelEngine) | [epics/EPIC-7-auramobs-mobs-bosses.md](epics/EPIC-7-auramobs-mobs-bosses.md) |
| EPIC-8 | T8.2: DraftMenu no framework; T8.6/T8.9: polish de menus | [epics/EPIC-8-menus.md](epics/EPIC-8-menus.md) |
| EPIC-9 | T9.6-T9.10: PassiveTask, async I/O, Folia-awareness, testes | [epics/EPIC-9-dados-performance.md](epics/EPIC-9-dados-performance.md) |
| EPIC-10 | T10.2/T10.4/T10.19/21/T10.25: sinergias, i18n em menus, testes | [epics/EPIC-10-builds-qualidade.md](epics/EPIC-10-builds-qualidade.md) |
| EPIC-11 | T11.3-T11.8: docs de setup, matriz de degradação | [epics/EPIC-11-dependencias-setup.md](epics/EPIC-11-dependencias-setup.md) |
| EPIC-12 | Revisão final (bloqueado) | [epics/EPIC-12-revisao-documentacao.md](epics/EPIC-12-revisao-documentacao.md) |

## Épicos já concluídos (fora do backlog)

EPIC-0, EPIC-1, EPIC-3, EPIC-4, EPIC-5.

Obs.: o bloqueio crítico compartilhado (T8.4/T8.4b/T8.5 + T10.23) foi resolvido — `ClassListeners`, `PlayerManager` e `SkillGUI` não existem mais no projeto.

## Processo deste backlog

Ao concluir uma tarefa, remova-a do arquivo do épico correspondente. Quando um épico ficar sem tarefas, apague o arquivo e remova a linha da tabela acima. Ao criar uma nova decisão/spec, adicione-a no épico correspondente (ou crie um novo arquivo `EPIC-N` se for assunto novo).
