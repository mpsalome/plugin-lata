# EPIC-6 — Integração AuraSkills / Mana (pendências)

**Dependências:** nenhuma

| Tarefa | Status | Descrição | Arquivos | CA |
|---|---|---|---|---|
| **T6.1** | 🔴 parcial | `AuraSkillsIntegration` ainda lê skills via `PlayerManager.getAllSkillKeys()` (classe legada). Migrar para consultar o `CardRegistry`. | `AuraSkillsIntegration.java` (mover para `integration/`) | Nenhuma referência a `PlayerManager` na integração |
| **T6.2** | ❌ não existe | Sistema de mana (`CustomManaAbility`, custo/cooldown por mana) descrito no design não tem nenhuma implementação. Não existe `mana_abilities.yml`. | novo `resources/mana_abilities.yml`, nova classe `CustomManaAbility` | Uma ability com custo de mana funciona e consome mana do AuraSkills |
| **T6.5** | ❌ ausente | Alinhamento/documentação do `on_death` do AuraSkills (reset nativo de skills/XP) não está documentado. | docs | Documento explica a config recomendada de `on_death` |
| **T6.7** | ❌ ausente | Nenhuma carta é exposta como mana ability dentro do menu nativo `/skills` do AuraSkills. | integração AuraSkills | Cartas com custo de mana aparecem no `/skills` nativo |
| **T6.8** | ❌ ausente | Carta `mana_pool` deveria só ser oferecida no draft se AuraSkills estiver presente — falta checagem condicional (`offerable`). | `core/card/` (registro condicional) | `mana_pool` não aparece no draft em modo standalone |
| **T6.9** | ❌ ausente | Sem testes de `AuraSkillsIntegration`/mana abilities. | `src/test/java/.../integration/` | Testes cobrindo a integração passam |

## Definition of Done
- [ ] T6.1, T6.2, T6.5, T6.7, T6.8, T6.9 resolvidas.
