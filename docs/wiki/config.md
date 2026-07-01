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
| `messages/messages_pt.yml` | Textos em português | MessagesConfig |
| `messages/messages_en.yml` | Textos em inglês | MessagesConfig |

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

## Mensagens (i18n)

Os arquivos em `messages/` usam o formato MiniMessage do Adventure. Para adicionar um novo idioma, crie `messages_<lang>.yml` baseado no `messages_en.yml` e recarregue.

> Em implementação: suporte a locale automático baseado no cliente do jogador.
