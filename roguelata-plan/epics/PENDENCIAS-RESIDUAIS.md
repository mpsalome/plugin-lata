# Itens residuais — épicos já ~100% prontos (EPIC-0, 1, 3, 4, 5)

Esses épicos foram verificados como concluídos no código. Restam só estes itens pontuais (baixa prioridade):

| Item (épico original) | Status | Descrição |
|---|---|---|
| EPIC-3 T3.7 | ❓ inconclusivo | Não foi possível confirmar com certeza se `/run info` está implementado — verificar `command/RunCommand.java`. |
| EPIC-3 T3.8 | ❌ ausente | Testes automatizados do fluxo de reset (`ResetService.fullReset`) não encontrados. |
| EPIC-4 T4.8 | ❌ ausente | Testes automatizados dos modificadores Mayhem não encontrados. |
| EPIC-5 T5.6 | ❓ inconclusivo | `DistanceTrigger` genérico/reutilizável não encontrado como classe separada — `RecallProgression` cobre o caso de uso atual; avaliar se vale abstrair antes de criar um segundo gatilho por distância. |
| EPIC-5 T5.7 | ❌ ausente | Testes automatizados de distância/recall/gates não encontrados. |

## Definition of Done
- [ ] Confirmar T3.7; implementar testes de T3.8, T4.8, T5.7; decidir T5.6.
