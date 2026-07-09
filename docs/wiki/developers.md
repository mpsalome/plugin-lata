# Desenvolvedores

## Build

```bash
mvn clean package
```

Requisitos: Java 21, Maven 3.9+, Paper API 1.21.4.

O JAR é gerado em `target/RogueLata-<versão>.jar` (shaded).

## Arquitetura

```
com.project.rpgplugin
├── RPGPlugin.java                  # Bootstrap + DI manual
├── AuraSkillsIntegration.java      # Ponte AuraSkills (custom skills, gates, draft bias)
├── command/
│   ├── LataCommand.java            # /lata e subcomandos (tp, boss, loja, craft, book)
│   ├── RunCommand.java             # /run
│   └── RecallCommand.java          # /recall
├── config/                         # SkillsConfig, ShopConfig
├── core/
│   ├── build/        SynergyService           # Sinergias por tags
│   ├── card/          Card, CardRegistry...    # 90 cartas (37 ability + 53 augment)
│   ├── difficulty/    DifficultyService        # Escala vanilla (profundidade + players)
│   ├── draft/         DraftService, Weighting  # Motor do draft 1-de-3 (não-bloqueante)
│   ├── mayhem/        MayhemService, 8+ mods   # Modificadores cumulativos (limpos na morte)
│   ├── mob/           EliteFactory, MobSpawnService  # Bosses/elites via bosses.yml/mobs.yml
│   ├── boss/          BossLootService, BossSet       # Geração de loot temático por boss (v3.2.0)
│   ├── milestone/     MilestoneService          # Milestone boss (50% chance a cada 10 níveis)
│   ├── progression/   GateRegistry, Recall     # Gates, recall, distância
│   ├── run/           RunState, RunManager     # Ciclo da run, persistência, migração veteran
│   └── skill/         Skill, 38 impls          # Sistema de skills data-driven (cfgInt/cfgDouble/cfgString)
├── data/              YamlDataStore, RunPersistenceService  # Persistência runs/{uuid}.yml
├── listener/          CombatListener, PlayerLifecycleListener, SkillDispatchListener
├── task/              DistanceTask             # Tasks periódicas
├── ui/
│   ├── HubMenu.java               # Menu principal (Coleção, Loja, Draft)
│   ├── CollectionMenu.java        # Coleção paginada (36 cards/página, filtros)
│   ├── ShopMenu.java              # Loja (inclui Purificação do Mundo, slot 18)
│   ├── DraftMenu.java             # Draft 1-de-3 (fechável, sessão preservada)
│   └── HUD.java                   # Actionbar (mana+health) + BossBar (cooldowns/efeitos)
├── util/              ItemKeys, Text, ItemBuilder  # Utilitários
└── integration/       MythicMobsBridge, ModelEngineBridge, AuraMobsIntegration
```

## Serviços principais

| Serviço | Função | Acesso |
|---------|--------|--------|
| `RunManager` | Iniciar/terminar runs, obter RunState | `RPGPlugin.getRunManager()` |
| `CardRegistry` | 90 cartas registradas, consulta por tag/tier | `RPGPlugin.getCardRegistry()` |
| `DraftService` | Rolar draft, aplicar escolha, reroll, pular | `RPGPlugin.getDraftService()` |
| `AuraSkillsIntegration` | Ponte com AuraSkills (se ativo) | `RPGPlugin.getAuraSkillsIntegration()` |
| `PlayerDataStore` | Salvar/carregar runs do disco | `RPGPlugin.getDataStore()` |
| `DifficultyService` | Multiplicadores de dificuldade | `RPGPlugin.getDifficultyService()` |
| `MobSpawnService` | Spawn de bosses e elites configuráveis via YAML | `RPGPlugin.getMobSpawnService()` |
| `EliteFactory` | Cria entidades boss/elite (MiniMessage display, BossBar, MythicMobs fallback) | via `MobSpawnService` |
| `MayhemConfig` | Modificadores Mayhem, marcos e severidade | `RPGPlugin.getMayhemConfig()` |
| `BossLootService` | Gera peças do set temático + loot aleatório, escala com nível do boss | `RPGPlugin.getBossLootService()` |
| `MilestoneService` | Gerencia milestone boss (50% de chance a cada 10 níveis) | `RPGPlugin.getMilestoneService()` |

## LataCommand — extensibilidade

O `LataCommand` usa um switch no primeiro argumento para rotear subcomandos. Para adicionar um novo subcomando:

1. Adicione um `case` no `onCommand`
2. Crie um método `handle<Nome>(Player, String[], String)`
3. Adicione a entrada de ajuda em `sendHelp`

Subcomandos atuais: `tp`, `boss spawn`, `loja`, `book`, `craft`, `draft`.

## Como adicionar uma carta

### Habilidade (Ability)

1. Crie uma classe em `core/skill/impl/` estendendo `AbstractSkill`
2. Implemente `id()`, `type()`, `tier()`, `icon()`, `trigger()`, `activate()`
3. Use `cfgInt()`, `cfgDouble()`, `cfgString()` para leitura defensiva de valores do YAML (nunca hardcode)
4. Registre em `SkillRegistration.registerAll()`
5. Adicione as tags em `AbilityCardRegistration.SKILL_TAGS`

### Augment

1. Adicione a entrada em `augments.yml`
2. Defina `tier`, `tags`, `max_stacks`, `effect`

## Convenções

- **Adventure/MiniMessage** para todo texto (nunca `ChatColor`/`§`)
- **Sem switch por carta** — tudo via CardRegistry + handlers
- **Toda constante de gameplay** vem de YAML (nunca hardcoded)
- **Leitura defensiva**: `cfgInt(path, def)`, `cfgDouble(path, def)`, `cfgString(path, def)` com fallback e log de warning
- **Standalone-first** — AuraSkills/AuraMobs são soft-deps
- **Reset total na morte** + **Mayhem limpo** (timers cancelados, entidades removidas)
- **Respawn vanilla** — o plugin não sobrescreve `setRespawnLocation`
- **Migração veteran** na primeira join: níveis AuraSkills existentes são convertidos em pending drafts
- **Draft não-bloqueante**: level-ups acumulam drafts, jogador abre manualmente
- **Limpeza de estado** em quit/death para evitar leaks de UUID
- **Boss level scaling**: vida = base * (1 + 0.15 × nível do invocador), dano = base * (1 + 0.10 × nível do invocador), ±20% RNG
- **BossSet drops**: todo boss tem um loot_set em `loot_sets.yml` — itens customizados com nome, encantamentos e lore. A quantidade de peças dropadas escala com o nível do boss.
- **Milestone boss**: em `mayhem.yml`, `boss_chance: 0.5` define 50% de chance de invocar um boss aleatório a cada 10 níveis ao invés de mayhem
- **SonarSkill**: ativado por sneak + clique direito; glow contínuo em entidades até desativar (clique novamente)
- **HUD split**: actionbar = mana + vida; BossBar = cooldowns e efeitos ativos
- **Purificação**: ShopMenu slot 18, item Purificação do Mundo, custo 30 níveis, remove todas as entidades e efeitos mayhem do mundo
- **BossLootService**: gera peças de set temático + loot aleatório (pérolas, drops raros), quantidade escalada por nível do boss

## Testes

```bash
mvn test
```

Atualmente 106+ testes (CardRegistry, DraftWeighting, RunState, Mayhem, SkillRegistry, Progression, ResetService, MilestoneService, SynergyService, AugmentCard, ManaService, ModifierRegistry, DraftService, BossLootService).