# RogueLata — Backlog do que falta

> Esta pasta documenta **só o trabalho pendente** do plugin RogueLata (Paper). Design completo, catálogo de cartas, glossário e decisões já tomadas (inclusive a avaliação Forge vs. Paper) foram removidos por não serem mais "pendência" — não são recriados aqui.

**Última verificação de código:** 2026-07-02, leitura direta do repositório remoto (`mpsalome/plugin-lata`, branch `main`).

## 🔴 Bugs confirmados em jogo (prioridade máxima)

1. **Skills draftadas não ativam** — [EPIC-2 T2.6](epics/EPIC-2-draft-e-cartas.md)
2. **Draft sem nome/descrição (i18n)** — [EPIC-2 T2.10](epics/EPIC-2-draft-e-cartas.md) (mesmo bug em [EPIC-8 T8.6](epics/EPIC-8-menus.md))
3. **Item do menu ainda abre o menu antigo de comprar-por-nível** — [EPIC-8 T8.4/T8.4b](epics/EPIC-8-menus.md)

## Épicos com trabalho pendente

| Épico | Resumo do que falta | Arquivo |
|---|---|---|
| EPIC-2 | 2 bugs críticos (ownedAbilities, i18n do draft) + testes | [epics/EPIC-2-draft-e-cartas.md](epics/EPIC-2-draft-e-cartas.md) |
| EPIC-6 | Sistema de mana não existe; integração ainda usa `PlayerManager` legado | [epics/EPIC-6-auraskills.md](epics/EPIC-6-auraskills.md) |
| EPIC-7 | Boss sem BossBar/fases; `mobs.yml`/`bosses.yml` não existem; combate incompleto; vitória não conta milestone | [epics/EPIC-7-auramobs-mobs-bosses.md](epics/EPIC-7-auramobs-mobs-bosses.md) |
| EPIC-8 | `ClassListeners` legado ainda ativo; item ainda é `BOOK`; `DraftMenu` fora do framework de menus | [epics/EPIC-8-menus.md](epics/EPIC-8-menus.md) |
| EPIC-9 | Leak de memória no reset; sem `PassiveTask` periódica; sem Folia-awareness | [epics/EPIC-9-dados-performance.md](epics/EPIC-9-dados-performance.md) |
| EPIC-10 | **Maior pendência**: legado não removido, 11 augments sem handler, 27 arquivos com `§`, sem CI | [epics/EPIC-10-builds-qualidade.md](epics/EPIC-10-builds-qualidade.md) |
| EPIC-11 | Falta `ModelEngine` no softdepend, `SERVER_SETUP.md`, testes de matriz de degradação | [epics/EPIC-11-dependencias-setup.md](epics/EPIC-11-dependencias-setup.md) |
| EPIC-12 | Revisão final de documentação (só depois dos épicos acima fecharem) | [epics/EPIC-12-revisao-documentacao.md](epics/EPIC-12-revisao-documentacao.md) |

## Épicos já concluídos (fora do backlog)

EPIC-0, EPIC-1, EPIC-3, EPIC-4 e EPIC-5 estão ~100% implementados e verificados no código. Os poucos itens residuais (cobertura de teste, itens inconclusivos) estão consolidados em [epics/PENDENCIAS-RESIDUAIS.md](epics/PENDENCIAS-RESIDUAIS.md).

## Bloqueio crítico compartilhado

**T8.4/T8.4b/T8.5** (EPIC-8) e **T10.23** (EPIC-10) são o mesmo trabalho: remover de vez `ClassListeners`, `PlayerManager` e `SkillGUI` do `RPGPlugin`. Resolver isso mata o bug #3 da lista acima e desbloqueia a remoção do InvUI.

## Processo deste backlog

Ao concluir uma tarefa, remova-a do arquivo do épico correspondente. Quando um épico ficar sem tarefas, apague o arquivo e remova a linha da tabela acima. Ao criar uma nova decisão/spec, adicione-a no épico correspondente (ou crie um novo arquivo `EPIC-N` se for assunto novo).
