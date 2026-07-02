# EPIC-2 — Draft de Cartas (pendências)

**Dependências:** nenhuma (núcleo já funciona; só os itens abaixo faltam)

| Tarefa | Status | Descrição | Arquivos | CA |
|---|---|---|---|---|
| **T2.6** | 🔴 bug crítico | `AbilityCard.onAcquire()` só chama `run.addCard(id())` — **nunca** adiciona a `run.ownedAbilities()`, que é o que `SkillDispatchListener.dispatch()` lê para disparar skills. Resultado: nenhuma ability draftada funciona. Corrigir também `onRemove()` (remover de `ownedAbilities`). | `core/card/ability/AbilityCard.java` | Draftar uma ability e usá-la em jogo produz o efeito esperado |
| **T2.10** | 🔴 bug confirmado | `DraftMenu.buildCardItem()` não usa `card.nameKey()`/`descKey()` — mostra `id().replace("_"," ")` cru, sem descrição. Faltam entradas `card.<id>.name`/`card.<id>.desc` em `messages_*.yml`. | `ui/DraftMenu.java`, `resources/messages/messages_pt.yml`, `messages_en.yml`, `config/MessagesConfig.java` | Cartas do draft mostram nome e descrição traduzidos (mesmo fix cobre EPIC-8 T8.6) |
| **T2.11** | ❌ ausente | Sem testes automatizados de: peso do draft por tier/nível, sorteio sem repetição dentro dos 3, idempotência de stacking de cartas. | `src/test/java/.../draft/` | Testes cobrindo `DraftWeighting`/`DraftService` passam |

## Definition of Done
- [ ] T2.6, T2.10, T2.11 resolvidas.
