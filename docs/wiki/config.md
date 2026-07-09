# Configuração

Arquivos em `plugins/RogueLata/` (gerados automaticamente na primeira execução).

## Arquivos

| Arquivo | Função | Gerenciado por |
|---------|--------|----------------|
| `config.yml` / `skills.yml` | Parâmetros das habilidades (durações, cooldowns, amplificadores) | SkillsConfig |
| `draft.yml` | Frequência do draft, pesos por tier, reroll, skip | DraftWeighting |
| `augments.yml` | Catálogo completo de 53 augments | AugmentLoader |
| `gates.yml` | Gates: quais skills/níveis do AuraSkills liberam quais cartas | GateRegistry |
| `mayhem.yml` | Modificadores Mayhem, marcos e severidade | MayhemConfig |
| `run.yml` | Configuração de spawn da run | RunManager |
| `bosses.yml` | Definições dos bosses (tipo, vida, dano, escala, equipamento) | MobSpawnService |
| `mobs.yml` | Definições de mobs elites | MobSpawnService |

## Uso

Edite os arquivos desejados e execute `/rpg reload` para aplicar as mudanças sem reiniciar o servidor.

## `draft.yml`

```yaml
draft:
  every_levels: 3            # a cada quantos niveis abre um draft
  weights:
    - { from: 1, to: 9, bronze: 80, silver: 18, gold: 2 }
    - { from: 10, to: 19, bronze: 60, silver: 30, gold: 10 }
    - { from: 20, to: 29, bronze: 40, silver: 40, gold: 20 }
    - { from: 30, to: 999, bronze: 25, silver: 40, gold: 35 }
  reroll:
    enabled: true
    cost_levels: 1
    max_per_draft: 1
  allow_skip: true
```

## `run.yml`

```yaml
spawn:
  mode: world_spawn          # world_spawn | random_in_radius
  radius: 500                # usado se mode = random_in_radius
recall:
  base_distance: 2000
  growth: 1.5
  cap: 0                     # 0 = sem cap
```

## `mayhem.yml`

```yaml
milestone:
  every_levels: 10            # a cada quantos niveis um modificador é aplicado
  allow_repeat: false         # mesmo modificador pode cair de novo?
weight:
  common: 60
  rare: 30
  epic: 10
```

## `bosses.yml`

```yaml
# Bosses configuracao — bosses da run
bosses:
  frostmaw:
    base_type: POLAR_BEAR
    display_name: "<bold><aqua>Frostmaw <gray>| <white>Senhor do Gelo"
    health: 300
    damage: 12
    speed: 0.25
    scale: 2.5
    knockback_resist: 1.0
    victory: true
  magma_tyrant:
    base_type: MAGMA_CUBE
    display_name: "<bold><red>Tirano Magmatico <gray>| <white>Coracao do Inferno"
    health: 200
    damage: 15
    speed: 0.3
    scale: 4.0
    knockback_resist: 0.8
    victory: true
  storm_wyvern:
    base_type: RAVAGER
    display_name: "<bold><yellow>Furia Tempestuosa <gray>| <white>Asa do Ceu"
    health: 350
    damage: 18
    speed: 0.35
    scale: 2.8
    knockback_resist: 0.9
    victory: true
  void_lich:
    base_type: WITHER_SKELETON
    display_name: "<bold><dark_purple>Lich do Vazio <gray>| <white>A Noite Eterna"
    health: 250
    damage: 20
    speed: 0.2
    scale: 2.0
    knockback_resist: 1.0
    victory: true
```
