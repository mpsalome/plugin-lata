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
├── command/                        # RunCommand, RecallCommand
├── config/                         # MessagesConfig (i18n)
├── core/
│   ├── build/       SynergyService           # Sinergias por tags
│   ├── card/        Card, CardRegistry...    # 88 cartas (35 ability + 53 augment)
│   ├── difficulty/  DifficultyService        # Escala vanilla (profundidade + players)
│   ├── draft/       DraftService, Weighting  # Motor do draft 1-de-3
│   ├── mayhem/      MayhemService, 8 mods    # Modificadores cumulativos
│   ├── mob/         EliteFactory             # Bosses/elites vanilla
│   ├── progression/ GateRegistry, Recall     # Gates, recall, distância
│   ├── run/         RunState, RunManager     # Ciclo da run
│   └── skill/       Skill, 35 impls          # Sistema de skills data-driven
├── data/            YamlDataStore            # Persistência runs/{uuid}.yml
├── listener/        CombatListener, etc.     # Eventos
├── task/            DistanceTask             # Tasks periódicas
├── ui/              CollectionMenu, HUD      # Interfaces
└── util/            ItemKeys, Text           # Utilitários
```

## Serviços principais

| Serviço | Função | Acesso |
|---------|--------|--------|
| `RunManager` | Iniciar/terminar runs, obter RunState | `RPGPlugin.getRunManager()` |
| `CardRegistry` | 88 cartas registradas, consulta por tag/tier | `RPGPlugin.getCardRegistry()` |
| `DraftService` | Rolar draft, aplicar escolha, reroll, pular | `RPGPlugin.getDraftService()` |
| `AuraSkillsIntegration` | Ponte com AuraSkills (se ativo) | `RPGPlugin.getAuraSkillsIntegration()` |
| `PlayerDataStore` | Salvar/carregar runs do disco | `RPGPlugin.getDataStore()` |
| `DifficultyService` | Multiplicadores de dificuldade | `RPGPlugin.getDifficultyService()` |

## Como adicionar uma carta

### Habilidade (Ability)

1. Crie uma classe em `core/skill/impl/` estendendo `AbstractSkill`
2. Implemente `id()`, `type()`, `tier()`, `icon()`, `trigger()`, `activate()`
3. Registre em `SkillRegistration.registerAll()`
4. Adicione as tags em `AbilityCardRegistration.SKILL_TAGS`

### Augment

1. Adicione a entrada em `augments.yml`
2. Defina `tier`, `tags`, `max_stacks`, `effect`

## Convenções

- **Adventure/MiniMessage** para todo texto (nunca `ChatColor`/`§`)
- **Sem switch por carta** — tudo via CardRegistry + handlers
- **Toda constante de gameplay** vem de YAML (nunca hardcoded)
- **Standalone-first** — AuraSkills/AuraMobs são soft-deps
- **Reset total na morte** é sagrado
- **i18n** em `messages_<lang>.yml`
- **Limpeza de estado** em quit/death para evitar leaks de UUID

## Testes

```bash
mvn test
```

Atualmente 57 testes (CardRegistry, DraftWeighting, RunState, Mayhem, SkillRegistry, Progression).
