# Handoff — RogueLata Plugin

## Estado Atual (2026-07-02)

- **Branch:** `main`, remoto: `origin/main`
- **Versão:** `1.5.0` (pom.xml)
- **Build:** `mvn clean package` — 145 source files, 70 testes, 0 falhas
- **CI:** `.github/workflows/ci.yml` roda `mvn clean package` em push/PR

## O que já foi feito (todas as fases)

### Phase 1 — Legacy removal + critical bugs
- T8.4/T8.4b/T8.5 + T10.23: `ClassListeners`, `PlayerManager`, `SkillGUI` deletados
- InvUI removido do `pom.xml`
- Item do menu: `BOOK` → `BREAD` (Lata de Pão), detecção por PDC tag
- T2.6: `AbilityCard.onAcquire()` adiciona a `run.ownedAbilities()`
- T10.20: `§` removido de código novo

### Phase 2 — Augments + bosses
- T10.5-T10.18: `AugmentListener` com 11 handlers + GiantEffect
- T7.3: `EliteFactory` com BossBar + fases (50%, 25% HP)
- T7.4: `mobs.yml` / `bosses.yml`
- T7.5: `MobSpawnService`
- T7.7/T7.8: CombatListener com lifesteal, boss kill → milestone → Mayhem
- T2.10: i18n nos cards (messages_pt/messages_en com 88 cards)
- T10.26: CI workflow
- T11.1: ModelEngine no plugin.yml

### Phase 3 — Performance + i18n polish
- T9.6: `PassiveTask` — reapleca potion effects de augments a cada 2s
- T9.7: `YamlDataStore.save()` async via `SchedulerUtil`
- T9.9: `SchedulerUtil` — Folia-awareness (detecta via classpath, usa RegionScheduler)
- T10.19/21: `CollectionMenu` + `DraftMenu` usam `MessagesConfig` (zero strings hardcoded)
- T8.2: `DraftMenu` extends `Menu` framework; click routing via `MenuHolder` (não por título)
- T2.11: `DraftServiceTest` (stacking, session, no-repetition)
- T10.25: `SynergyServiceTest` (countByTag, detectArchetype)
- T9.9: Todas as tasks e modifiers migrados para `SchedulerUtil`

### EPIC-6 — Mana System (último completado)
- T6.2: `ManaService` + `mana_abilities.yml` (12 abilities com custo de mana)
- T6.2: Mana check em `SkillDispatchListener.dispatch()` antes de ativar ability
- T6.7: Custo de mana aparece na descrição das custom skills no `/skills` do AuraSkills
- T6.8: `mana_pool` conditional (só ofertado se AuraSkills presente)
- T6.8: `Card.requiredPlugin()` interface + filtro no `DraftService`
- T6.9: `ManaServiceTest` (requiredPlugin, draft filtering)
- T6.5: on_death alignment docs + mana docs em `docs/wiki/integrations.md`

## Arquivos críticos

### Plugin entry
- `src/main/java/com/project/rpgplugin/RPGPlugin.java` — onEnable/onDisable, wiring

### Core packages
- `core/card/` — `Card` interface, `CardRegistry`, `AugmentCard`, `AbilityCard`
- `core/card/augment/` — `AugmentEffect`, `PotionEffectAugment`, `GiantEffect`, `MultiplierEffect`, etc
- `core/run/` — `RunState`, `RunManager`, `ResetService`
- `core/draft/` — `DraftService`, `DraftSession`, `DraftWeighting`
- `core/mob/` — `EliteFactory`, `MobSpawnService`
- `core/mayhem/` — modifiers, `MilestoneService`
- `core/build/` — `SynergyService`
- `core/mana/` — `ManaService` (novo)

### Tasks
- `task/PassiveTask.java` — periodic potion effect maintenance
- `task/DistanceTask.java` — magnet tick + distance tracking

### Listeners
- `listener/AugmentListener.java` — 11 augment event handlers
- `listener/CombatListener.java` — crit, execute, thorns, lifesteal, boss kill
- `listener/SkillDispatchListener.java` — skill activation, agora com mana check
- `listener/DraftMenuListener.java` — só onClose (clicks vão pelo MenuListener)

### UI
- `ui/DraftMenu.java` — extends Menu, i18n via MessagesConfig
- `ui/CollectionMenu.java` — extends Menu, i18n via MessagesConfig
- `ui/HudService.java` — action bar
- `ui/menu/Menu.java`, `MenuHolder.java`, `MenuListener.java`

### Data
- `data/YamlDataStore.java` — async I/O
- `data/CooldownService.java`

### Util
- `util/SchedulerUtil.java` — Folia-aware scheduler wrapper
- `util/ItemKeys.java`, `util/Text.java`

### Resources
- `resources/augments.yml` — 53 augments + ascendentes
- `resources/mobs.yml`, `resources/bosses.yml`
- `resources/mana_abilities.yml` (novo)
- `resources/messages/messages_pt.yml`, `messages_en.yml`
- `resources/gates.yml`, `resources/plugin.yml`, `resources/config.yml`

## O que ainda falta

### EPIC-7 T7.9-T7.11 — Bridges opcionais (baixa prioridade)
- AuraMobs bridge: escala mobs pelo AuraMobs quando presente
- MythicMobs bridge: permite definir bosses via MythicMobs
- ModelEngine bridge: modelos 3D para bosses
- Core já funciona 100% vanilla sem elas

### EPIC-8 T8.6/T8.9 — Menu polish (baixa prioridade)
- T8.6: Sons por tier no DraftMenu (já existe `playTierSound` no DraftService)
- T8.9: `UI_STYLE.md` (guia de estilo)
- T8.8: Resource pack de fontes negativas (opcional)

### EPIC-10 T10.2/T10.4 — Sinergias (média prioridade)
- T10.2: `SynergyService.applySynergies()` nunca é chamado — ascendant cards não disparam bônus
- T10.4: `extra_skill_slot` — decisão de design pendente

### EPIC-11 — Documentação (baixa prioridade)
- T11.3: Matriz de degradação (testar 4 combinações: standalone, +AuraSkills, +AuraMobs, +MythicMobs)
- T11.4: `SERVER_SETUP.md` (passo a passo de instalação)
- T11.5: Exemplos de config MythicMobs/ModelEngine
- T11.6: Confirmação de escopo do InvUI (já removido)
- T11.7: `SMOKE_TEST.md` reorganizado por combinação
- T11.8: Aviso no draft quando carta requer plugin ausente (já feito em T6.8 para mana_pool, generalizar)

### EPIC-12 — Revisão final (bloqueado até todo código fechar)

## Constraints & Regras

1. **Maior impacto primeiro** — sempre priorizar o que afeta mais jogadores
2. **Automático total** — executar sem pedir confirmação a cada passo
3. **No fim de cada fase:** atualizar `pom.xml` version, `mvn clean package`, `git add -A`, `git commit`, `git push`
4. **Standalone-first** — Toda feature core funciona sem AuraSkills/AuraMobs/MythicMobs
5. **Comentários:** NUNCA adicionar comentários em código Java (exceto arquivos de config/docs)
6. **i18n:** Toda string visível ao jogador via MessagesConfig, nunca hardcoded
7. **Folia-aware:** Todo scheduler via `SchedulerUtil`, nunca `Bukkit.getScheduler()` direto
8. **Versão Java:** 21, Paper 1.21.4+, Maven
9. **Mensagens minimalistas:** Respostas curtas (<4 linhas), sem explicações desnecessárias
10. **Nunca commitar sem ser instruído** (mas no fim de cada fase é obrigatório: bump version → build → commit → push)

## Comandos úteis

```powershell
# Build + testes
cd C:\Users\marcus\Documents\repos\plugin-lata; mvn clean package

# Git
git add -A; git commit -m "mensagem"; git push

# Ver diff
git status; git diff --stat

# Ver log
git log --oneline -10
```

## Próximo passo sugerido

O maior impacto restante é **T10.2** — `SynergyService.applySynergies()` existe mas nunca é chamado. As cartas ascendentes (`explorer_ascendant`, `miner_ascendant`, `builder_ascendant`) foram draftadas mas os bônus de sinergia (Speed, Haste, Regeneration) nunca são aplicados. É um hook rápido no `PassiveTask` ou `AugmentListener` para chamar `applySynergies()` quando o jogador tem as cartas certas.
