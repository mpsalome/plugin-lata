# EPIC-9 — Dados, Cooldowns, Performance & Persistência (pendências)

**Dependências:** nenhuma

| Tarefa | Status | Descrição | Arquivos | CA |
|---|---|---|---|---|
| **T9.5** | 🔴 leak confirmado | `ResetService.fullReset()` **não limpa** `SkillServices.reinforcedBlocks` nem `moltenTouchActiveUntil` — vazam entre runs. | `core/run/ResetService.java` | `fullReset()` também limpa essas duas estruturas |
| **T9.6** | ❌ não existe | Passivas ainda disparam direto no `onMove` (`SkillDispatchListener`); falta uma `PassiveTask` periódica dedicada (ex.: a cada 10 ticks) para desacoplar do movimento. | novo `task/PassiveTask.java`, `listener/SkillDispatchListener.java` | Passivas disparam por tarefa periódica, não só em `onMove` |
| **T9.7** | 🔴 parcial | `YamlDataStore` grava de forma síncrona (só o `flushAll()` no `onDisable` deveria ser síncrono; o resto deveria ser assíncrono). | `data/YamlDataStore.java` | I/O de save roda fora da main thread, exceto o flush final |
| **T9.9** | ❌ não existe | Sem Folia-awareness — scheduler é 100% `Bukkit.getScheduler()` vanilla. | tarefas/scheduler em geral | Detecta Folia no boot e usa `RegionScheduler` quando aplicável |
| **T9.10** | 🔴 não confirmado | Cobertura de teste do reset/round-trip de persistência não verificada — `SMOKE_TEST.md` cita `mvn test` mas conteúdo real não auditado. | `src/test/java/` | Testes de round-trip/cleanup/idempotência existem e passam |

## Definition of Done
- [ ] T9.5, T9.6, T9.7, T9.9, T9.10 resolvidas.
