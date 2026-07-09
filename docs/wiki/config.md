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
| `bosses.yml` | Definições dos bosses (tipo, vida, dano, escala, equipamento, loot sets) | MobSpawnService / BossLootService |
| `mobs.yml` | Definições de mobs elites | MobSpawnService |
| `loot_sets.yml` | Definições dos sets temáticos dropados pelos bosses | BossLootService |

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
  boss_chance: 0.5            # chance de invocar milestone boss ao invés de mayhem
weight:
  common: 60
  rare: 30
  epic: 10
```

## `bosses.yml`

```yaml
# Bosses configuracao — bosses da run (v3.2.0: 10 bosses)
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
    loot_set: frostmaw
  magma_tyrant:
    base_type: MAGMA_CUBE
    display_name: "<bold><red>Tirano Magmatico <gray>| <white>Coracao do Inferno"
    health: 200
    damage: 15
    speed: 0.3
    scale: 4.0
    knockback_resist: 0.8
    victory: true
    loot_set: magma_tyrant
  storm_wyvern:
    base_type: RAVAGER
    display_name: "<bold><yellow>Furia Tempestuosa <gray>| <white>Asa do Ceu"
    health: 350
    damage: 18
    speed: 0.35
    scale: 2.8
    knockback_resist: 0.9
    victory: true
    loot_set: storm_wyvern
  void_lich:
    base_type: WITHER_SKELETON
    display_name: "<bold><dark_purple>Lich do Vazio <gray>| <white>A Noite Eterna"
    health: 250
    damage: 20
    speed: 0.2
    scale: 2.0
    knockback_resist: 1.0
    victory: true
    loot_set: void_lich
  sir_creeper:
    base_type: CREEPER
    display_name: "<bold><green>Sir Creeper-A-Lot"
    health: 280
    damage: 16
    speed: 0.3
    scale: 2.0
    knockback_resist: 0.5
    victory: false
    loot_set: sir_creeper
  slime_shady:
    base_type: SLIME
    display_name: "<bold><yellow>Slime Shady"
    health: 220
    damage: 10
    speed: 0.2
    scale: 3.0
    knockback_resist: 0.3
    victory: false
    loot_set: slime_shady
  o_decapitador:
    base_type: WITHER_SKELETON
    display_name: "<bold><dark_red>O Decapitador"
    health: 320
    damage: 22
    speed: 0.35
    scale: 2.2
    knockback_resist: 0.9
    victory: false
    loot_set: decapitador
  guardiao_ancestral:
    base_type: IRON_GOLEM
    display_name: "<bold><aqua>Guardiao Ancestral"
    health: 400
    damage: 14
    speed: 0.15
    scale: 2.5
    knockback_resist: 1.0
    victory: false
    loot_set: guardiao
  senhor_da_guerra_piglin:
    base_type: PIGLIN_BRUTE
    display_name: "<bold><gold>Senhor da Guerra Piglin"
    health: 300
    damage: 18
    speed: 0.3
    scale: 2.0
    knockback_resist: 0.8
    victory: false
    loot_set: piglin_war
  rei_fantasma:
    base_type: SKELETON
    display_name: "<bold><gray>Rei Fantasma"
    health: 260
    damage: 16
    speed: 0.25
    scale: 2.3
    knockback_resist: 0.6
    victory: false
    loot_set: rei_fantasma
```

## `loot_sets.yml`

```yaml
# Sets tematicos dropados pelos bosses (v3.2.0)
sets:
  frostmaw:
    items:
      - material: DIAMOND_HELMET
        name: "<aqua>Elmo Glacial"
        enchants: [PROTECTION_ENVIRONMENTAL:3, AQUA_AFFINITY:1]
        lore: ["<gray>Forjado nas profundezas do gelo eterno"]
      - material: DIAMOND_CHESTPLATE
        name: "<aqua>Peitoral Glacial"
        enchants: [PROTECTION_ENVIRONMENTAL:3]
        lore: ["<gray>Resiste ao frio mais absoluto"]
      - material: DIAMOND_SWORD
        name: "<aqua>Espada Glacial"
        enchants: [DAMAGE_ALL:4, FIRE_ASPECT:1]
        lore: ["<gray>O frio corta mais que qualquer lamina"]
  sir_creeper:
    items:
      - material: LEATHER_HELMET
        name: "<green>Capacete Explosivo"
        enchants: [BLAST_PROTECTION:4]
        lore: ["<gray>Feito para quem gosta de explosões"]
      - material: LEATHER_CHESTPLATE
        name: "<green>Colete Anti-Estilhaços"
        enchants: [BLAST_PROTECTION:4]
        lore: ["<gray>Não protege contra más decisoes"]
  # demais sets definidos analogamente...
```

## `shop.yml` (novo na 3.2.0)

```yaml
shop:
  items:
    - slot: 11
      item:
        material: PAPER
        name: "<white>Reroll de Sorte"
        lore: ["<gray>Ganha 1 reroll gratuito no proximo draft"]
      cost: 2
    - slot: 12
      item:
        material: BOOK
        name: "<white>Carta Avulsa"
        lore: ["<gray>+1 draft pendente"]
      cost: 3
    - slot: 13
      item:
        material: REDSTONE
        name: "<white>Absolvição do Caos"
        lore: ["<gray>Reduz o Mayhem em 1 nivel"]
      cost: 10
    - slot: 14
      item:
        material: BLAZE_ROD
        name: "<white>Sinalizador do Chefe"
        lore: ["<gray>Clique direito para invocar um boss"]
      cost: 15
    - slot: 15
      item:
        material: TOTEM_OF_UNDYING
        name: "<white>Beque"
        lore: ["<gray>Na morte, preserva itens do inventario"]
      cost: 30
    - slot: 18
      material: NETHER_STAR
      name: "<gold>Purificação do Mundo"
      lore: ["<gray>Remove TODO o mayhem do mundo"]
      cost: 30
```