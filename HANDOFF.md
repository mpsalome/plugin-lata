# Handoff — RogueLata Plugin

## Estado Atual (2026-07-06)

- **Versão:** `1.5.0` (pom.xml)
- **Build:** `mvn clean package` — 148 source files, 106 testes, 0 falhas
- **CI:** `.github/workflows/ci.yml` roda `mvn clean package` + `mvn test` em push/PR

## O que foi feito

### Bloco 1 — Ascendant Cards (T10.2)
- `SynergyService.applySynergies()` agora lê multiplicadores `explorer_ascendant`, `miner_ascendant`, `builder_ascendant`
- Ascendant cards concedem bônus extras ao atingir cada patamar de sinergia:
  - Explorer: Dolphin's Grace / Speed+ / Jump Boost
  - Miner: Night Vision em qualquer patamar
  - Builder: Resistance / Absorption extras

### Bloco 2 — Draft Condicional (T11.8)
- `mana_pool` tem `require_plugin: AuraSkills` no `augments.yml`
- `DraftService.cardPluginAvailable()` filta cartas cujo `requiredPlugin()` não está presente no servidor

### Bloco 3 — Testes (T3.8, T4.8, T5.7, T6.9, T10.25)
- `ResetServiceTest` — 12 testes: reset de cards/counts/multipliers/onKill/modifiers/level/milestones/outcome/potions/recall/slots/toggles
- `MilestoneServiceTest` — 12 testes: thresholds, run state, reset
- `AugmentCardTest` — 7 testes: lifecycle, offerable, effects, registry
- `CardRegistryTest` — 11 testes (novos: offerable/offerableByTier)
- `SynergyServiceTest` — 10 testes (novos: ascendant multiplier)
- Total: **106 testes** (de 57)

### Bloco 4 — CI Workflow (T10.26)
- `.github/workflows/ci.yml` — `actions/setup-java@v4` (JDK 21, temurin), `mvn clean package`, `mvn test`

### Bloco 5 — Remoção de i18n (dead code)
- `config/MessagesConfig.java` deletado (não era referenciado por ninguém)
- `resources/messages/messages_pt.yml` e `messages_en.yml` deletados
- Menus usam strings hardcoded diretamente (DraftMenu, CollectionMenu)

### Bloco 6 — Docs de Setup (T11.4/T11.5)
- `docs/SERVER_SETUP.md` — requisitos, ordem de instalação, matriz de compatibilidade
- `docs/packs/mythicmobs-frostmaw.yml` — exemplo de config MythicMobs
- `docs/packs/modelengine-frostmaw.yml` — exemplo de config ModelEngine

### Bloco 7 — UI Style Guide (T8.9)
- `docs/UI_STYLE.md` — cores, ícones, sons, padrões de layout

### Bloco 8 — Smoke Test (T11.7)
- `docs/SMOKE_TEST.md` reorganizado em 4 seções: A (Standalone), B (+AuraSkills), C (+AuraMobs), D (+MythicMobs)

### Bloco 9 — Revisão Final (EPIC-12)
- `README.md`: softdepend atualizado com ModelEngine
- `docs/wiki/config.md`: seção de mensagens/i18n removida
- `docs/wiki/developers.md`: referências a MessagesConfig/i18n removidas, número de testes atualizado
- `docs/wiki/index.md`: ModelEngine adicionado à tabela de dependências
- `HANDOFF.md`: atualizado

## Arquivos críticos

### Plugin entry
- `src/main/java/com/project/rpgplugin/RPGPlugin.java` — onEnable/onDisable, wiring

### Core packages
- `core/card/` — `Card` interface, `CardRegistry`, `AugmentCard`, `AbilityCard`
- `core/card/augment/` — `AugmentEffect`, `PotionEffectAugment`, `GiantEffect`, `MultiplierEffect`, etc
- `core/run/` — `RunState`, `RunManager`, `ResetService`
- `core/draft/` — `DraftService`, `DraftSession`, `DraftWeighting`
- `core/mob/` — `EliteFactory`, `MobSpawnService`
- `core/mayhem/` — 8 modifiers, `MilestoneService`
- `core/build/` — `SynergyService` (inclui ascendant cards)
- `core/mana/` — `ManaService`

### Tasks
- `task/PassiveTask.java` — periodic potion effect maintenance + synergy application
- `task/DistanceTask.java` — magnet tick + distance tracking

### Listeners
- `listener/AugmentListener.java` — 11 augment event handlers
- `listener/CombatListener.java` — crit, execute, thorns, lifesteal, boss kill
- `listener/SkillDispatchListener.java` — skill activation + mana check

### UI
- `ui/DraftMenu.java` — extends Menu, strings hardcoded
- `ui/CollectionMenu.java` — extends Menu, strings hardcoded
- `ui/HudService.java` — action bar
- `ui/menu/Menu.java`, `MenuHolder.java`, `MenuListener.java`

### Data
- `data/YamlDataStore.java` — async I/O
- `data/CooldownService.java`

### Util
- `util/SchedulerUtil.java` — Folia-aware scheduler wrapper
- `util/ItemKeys.java`, `util/Text.java`

## O que NÃO foi feito (propositalmente não incluído no escopo)

### T7.9-T7.11 — Bridges opcionais (AuraMobs, MythicMobs, ModelEngine)
- Já existem como bridges de detecção e chamada via reflection
- Consideradas enriquecimento opcional; core funciona 100% vanilla

### T8.8 — Resource pack de fontes negativas
- Opcional, baixíssimo impacto

## Constraints & Regras

1. **Standalone-first** — Toda feature core funciona sem plugins externos
2. **Adventure/MiniMessage** para todo texto (nunca `ChatColor`/`§`)
3. **Toda constante de gameplay** vem de YAML (nunca hardcoded)
4. **Reset total na morte** é sagrado
5. **Folia-aware** — Todo scheduler via `SchedulerUtil`
6. **Java 21, Paper 1.21.4+, Maven**
7. **Nunca commitar sem ser instruído**

## Comandos úteis

```powershell
cd C:\Users\marcus\Documents\repos\plugin-lata; mvn clean package
git status; git diff --stat
git log --oneline -10
```

## Próximo passo sugerido

O plugin está funcional e completo para o escopo definido. Possíveis próximos passos:
1. Deploy em servidor de teste para smoke test real (seguir `docs/SMOKE_TEST.md`)
2. Balanceamento de cartas (pesos no draft, valores de efeitos)
3. Mais modificadores Mayhem
4. Sistema de conquistas/metas entre runs (meta-progression)
