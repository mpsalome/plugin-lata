# Handoff — RogueLata Plugin

## Estado Atual (2026-07-09)

- **Versão:** `3.2.0` (pom.xml)
- **Build:** `mvn clean package` — 163 source files
- **CI:** `.github/workflows/ci.yml` roda `mvn clean package` + `mvn test` em push/PR
- **Release:** `.github/workflows/release.yml` triggered on tag `v*`

## O que foi feito

### v3.2.0 — New Features

#### Bosses
- 10 bosses (4 originais + 6 novos: Sir Creeper-A-Lot, Slime Shady, O Decapitador, Guardião Ancestral, Senhor da Guerra Piglin, Rei Fantasma)
- BossSet system: cada boss dropa equipamento temático (nome custom, encantamentos, lore)
- Boss drops escalam por nível (mais peças em níveis altos)
- Boss level exibido no summon + dica do possível loot
- BossLootService: loot baseado no tier do boss (COMMON→EPIC por HP), XP recompensa escala com HP do boss
- Milestone boss: 50% de chance de spawnar boss aleatório em vez de mayhem a cada milestone

#### Gameplay
- SonarSkill é toggle (sneak right-click on/off), glowing reveal contínuo
- Boss stats escalam com nível do invocador (+15% HP, +10% damage por nível, ±20% random)
- Purificação do Mundo: novo item na Loja (slot 18, 30 níveis), remove TODOS os efeitos Mayhem do mundo

#### UI/HUD
- HUD separada: actionbar só mana+health, BossBar para cooldowns + efeitos ativos

#### Skills
- Defensive cfg reads (cfgInt/cfgDouble/cfgString) no AbstractSkill

#### Fluxo de Jogo
- Draft não-bloqueante: sem auto-open, player usa `/lata draft`
- Respawn vanilla (sem setRespawnLocation)
- Mayhem limpa na morte

### v3.1.0 — New Features

#### Commands
- Main command is now `/lata` with subcommands: `tp <player>`, `boss spawn <boss>`, `loja`, `draft`, `book`
- Aliases: `rogue`, `pao`, `roguelata`
- Right-click the RPG Book (`BREAD`) opens the HubMenu

#### HubMenu
- Central menu with navigation: Coleção, Loja, Draft
- 27 slots, items at 11/13/15, "Fechar" at 22
- Fill: `BLACK_STAINED_GLASS_PANE` for borders

#### ShopMenu
- 5 items: Carta Avulsa (MAP), Reroll (ENDER_EYE), Absolvição (TOTEM_OF_UNDYING), Sinalizador (BEACON), Beque (HEART_OF_THE_SEA)
- Each costs levels
- 27 slots, items at 10/12/14/16/18

#### CollectionMenu improvements
- Pagination: 36 cards per page
- Category filters (toggle at slots 3-6)
- Alphabetical sort
- "Menu Principal" button at slot 49

#### Draft (não-bloqueante)
- Level-ups add pending drafts silently
- Player opens via `/lata draft` or HubMenu
- DraftMenu: 54 slots, cards at 20/23/26/29, reroll at 40, skip at 44

#### Bosses
- 4 bosses: frostmaw, magma_tyrant, storm_wyvern, void_lich
- Each spawns with BossBar and named entity

#### Respawn
- Fully vanilla (no custom respawn handling)

#### Mayhem
- Clears on death (timers cancelled, entities removed)

#### Veteran Migration
- On join, converts AuraSkills levels to pending drafts

### v1.5.0 — Anterior

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

### Commands
- `command/LataCommand.java` — `/lata` main command with subcommands (tp, boss spawn, loja, draft, book)

### Core packages
- `core/card/` — `Card` interface, `CardRegistry`, `AugmentCard`, `AbilityCard`
- `core/card/augment/` — `AugmentEffect`, `PotionEffectAugment`, `GiantEffect`, `MultiplierEffect`, etc
- `core/run/` — `RunState`, `RunManager`, `ResetService`
- `core/draft/` — `DraftService`, `DraftSession`, `DraftWeighting`
- `core/mob/` — `EliteFactory`, `MobSpawnService`
- `core/mayhem/` — 8 modifiers, `MilestoneService`, `MayhemService` (inclui clear)
- `core/build/` — `SynergyService` (inclui ascendant cards)
- `core/mana/` — `ManaService`
- `core/boss/` — Boss definitions, spawn logic, BossSet, BossLootService

### Tasks
- `task/PassiveTask.java` — periodic potion effect maintenance + synergy application
- `task/DistanceTask.java` — magnet tick + distance tracking

### Listeners
- `listener/AugmentListener.java` — 11 augment event handlers
- `listener/CombatListener.java` — crit, execute, thorns, lifesteal, boss kill
- `listener/SkillDispatchListener.java` — skill activation + mana check
- `listener/HubMenuListener.java` — right-click BREAD opens HubMenu
- `listener/DeathListener.java` — mayhem clear on death

### UI
- `ui/DraftMenu.java` — 54 slots, cards at 20/23/26/29, reroll at 40, skip at 44
- `ui/CollectionMenu.java` — 54 slots, pagination 36/page, category filters, "Menu Principal" at 49
- `ui/ShopMenu.java` — 27 slots, 5 items at 10/12/14/16/18
- `ui/HubMenu.java` — 27 slots, items at 11/13/15, "Fechar" at 22
- `ui/HudService.java` — action bar (mana+health) + BossBar (cooldowns+effects)
- `ui/menu/Menu.java`, `MenuHolder.java`, `MenuListener.java`

### Data
- `data/YamlDataStore.java` — async I/O
- `data/CooldownService.java`

### Util
- `util/SchedulerUtil.java` — Folia-aware scheduler wrapper
- `util/ItemKeys.java`, `util/Text.java`

### Config
- `config/BossConfig.java` — bosses.yml loader
- `config/BossLootConfig.java` — boss loot tables
- `config/ShopConfig.java` — shop item definitions

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
