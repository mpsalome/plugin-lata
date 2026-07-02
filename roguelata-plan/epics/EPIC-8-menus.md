# EPIC-8 — Menus & GUI (pendências)

**Dependências:** o mesmo trabalho de remoção de legado desta epic (T8.4/T8.5) é **o mesmo** que EPIC-10 T10.23 — resolver uma vez só.

| Tarefa | Status | Descrição | Arquivos | CA |
|---|---|---|---|---|
| **T8.2** | 🔴 parcial | `DraftMenu` não herda do framework `Menu`/`MenuHolder` (T8.1, já pronto) — ainda abre inventário "solto" e roteia clique comparando **título** do inventário (frágil, quebra com i18n). | `ui/DraftMenu.java`, `ui/DraftMenuListener.java` | Draft roteado pelo `MenuListener` único, sem comparar título |
| **T8.4** | 🔴 **crítico** | `ClassListeners` continua registrado em `RPGPlugin.onEnable()` e seu `onPlayerInteract()` intercepta o clique no item, abrindo o `SkillGUI` legado (comprar-por-nível) em vez do `CollectionMenu` novo. `/skills` já abre `CollectionMenu` corretamente. | `RPGPlugin.java` (desregistrar `ClassListeners`) | Clicar no item abre só `CollectionMenu`; nada abre `SkillGUI` |
| **T8.4b** | 🔴 decisão não aplicada | Decisão de produto (2026-07-01): item deixa de ser `BOOK` e vira **"Lata de Pão"** (`Material.BREAD`). Hoje `createRpgBook()` ainda usa `Material.BOOK`, e `ItemKeys.isRpgBook()` ainda checa **material E tag** (deveria checar **só a tag PDC**, independente do material). | `RPGPlugin.createRpgBook()`, `util/ItemKeys.java` | Item é `BREAD` renomeado; detecção funciona só pela tag PDC |
| **T8.5** | 🔴 bloqueada por T8.4 | InvUI e `SkillGUI` ainda não foram removidos do projeto. | `pom.xml`, `SkillGUI.java` | Build sem InvUI; `SkillGUI` deletado |
| **T8.6** | 🔴 bug confirmado | `DraftMenu` sem i18n (mesmo bug de EPIC-2 T2.10) e sem som por raridade da carta. | `ui/DraftMenu.java` | Cartas com nome/descrição traduzidos + som por tier |
| **T8.9** | ❌ não existe | `docs/UI_STYLE.md` (guia de estilo de cores/ícones/padrões de menu) não existe. | novo `docs/UI_STYLE.md` | Documento existe e é seguido pelos menus atuais |
| **T8.8** | ❌ opcional | Protótipo de resource pack com fonte de espaço negativo não existe — baixa prioridade. | — | — |

## Definition of Done
- [ ] T8.2, T8.4, T8.4b, T8.5, T8.6, T8.9 resolvidas (T8.8 opcional).
