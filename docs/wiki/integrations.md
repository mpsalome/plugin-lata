# Integrações

RogueLata roda **standalone** (só Paper). Os plugins abaixo são opcionais e só enriquecem a experiência.

## AuraSkills

| Aspecto | Com AuraSkills | Sem AuraSkills |
|---------|----------------|----------------|
| Gates | Skills nativas (Mining, Agility...) liberam cartas ao subir de nível | Gates não funcionam (draft segue normal) |
| Draft bias | Níveis altos em Mining favorecem cartas MINER no draft | Todas as classes com peso igual |
| Reset na morte | Skills AuraSkills zeradas junto com o resto | Só XP vanilla é resetado |
| Mana | Habilidades podem usar mana | Habilidades usam cooldown próprio |
| Custom skills | 38 skills do RogueLata aparecem como skills custom no `/skills` do AuraSkills | Não aparecem (só existem como cartas) |
| Migração veteran | Na primeira join, níveis AuraSkills existentes são convertidos em drafts pendentes | Sem migração |

### Configuração recomendada (`plugins/AuraSkills/config.yml`)

```yaml
on_death:
  reset_skills: true
  reset_xp: true
  reset_xp_ratio: 0.0
```

> ⚠️ **Alinhamento com reset da Run:** O RogueLata já chama `resetAllAuraSkills()` na morte do jogador (via `PlayerLifecycleListener`). A config acima garante que o AuraSkills também zere skills/XP nativos no mesmo momento, evitando dessincronia entre os dois sistemas. Sem `reset_skills: true`, o jogador pode ressurgir com níveis AuraSkills que o RogueLata esperava que estivessem zerados.

### Migração veteran

Na primeira vez que um jogador entra no servidor com o RogueLata (não possui dados de run salvos), o plugin verifica se ele tem **níveis vanilla de XP** acumulados. Se sim, esses níveis são convertidos em **drafts pendentes** (1 draft por nível), e o jogador recebe uma mensagem no chat com o link `/lata draft` para abrir seus drafts.

### Gates (exemplo de `gates.yml`)

Gates vinculam níveis de AuraSkills a cartas específicas:

```yaml
gates:
  - skill: FIGHTING
    level: 10
    card: recall
    mode: grant
  - skill: MINING
    level: 5
    card: haste
    mode: unlock
```

Modos: `unlock` libera a skill para draft, `grant` dá a carta automaticamente.

### Mana (`mana_abilities.yml`)

Quando AuraSkills está presente, certas habilidades consumirão mana ao invés de apenas cooldown:

| Habilidade | Mana |
|-----------|------|
| Dash | 10 |
| Step Assist | 5 |
| Grapple | 8 |
| Torch Light | 3 |
| Molten Touch | 20 |
| Core Overdrive | 30 |

O augment `mana_pool` aumenta a mana máxima em +20 por stack. Sem AuraSkills, `mana_pool` não aparece no draft e habilidades usam cooldown próprio.

> A mana é gerenciada nativamente pelo AuraSkills. O RogueLata só verifica e consome antes de ativar a habilidade.

## AuraMobs

Escala a dificuldade dos mobs pelo **nível médio dos jogadores na região**. Sem ele, a dificuldade escala apenas pela **profundidade da run** (via DifficultyService nativo).

> O DifficultyService nativo do RogueLata já escala vida/dano dos mobs. AuraMobs é um reforço opcional.

## MythicMobs

Permite criar **bosses com AI avançada** (skills, fases, drops). Sem ele, bosses são mobs vanilla turbinados (via EliteFactory: nome, vida, dano, escala, equipamento).

> Todos os bosses do RogueLata funcionam sem MythicMobs — ele só adiciona tempero de IA.

## BetterHud

O [BetterHud](https://www.spigotmc.org/resources/betterhud.28330/) permite criar HUDs customizados no cliente via configuração YAML. Quando detectado pelo RogueLata:

- A **BossBar nativa** que exibe cooldowns de habilidades e efeitos ativos é **desativada** automaticamente
- O jogador vê apenas a **ActionBar** (mana + vida) vinda do RogueLata
- A configuração do BetterHud pode consumir placeholders do RogueLata via PlaceholderAPI

> Sem BetterHud, o RogueLata mantém o comportamento padrão: BossBar exibindo cooldowns e efeitos ativos.

## PlaceholderAPI

O [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) expõe dados internos do RogueLata para outros plugins (BetterHud, scoreboards, chat, etc.).

### Placeholders disponíveis

| Placeholder | Descrição | Exemplo |
|-------------|-----------|---------|
| `%roguelata_mana%` | Mana atual do jogador | `72` |
| `%roguelata_max_mana%` | Mana máxima do jogador | `100` |
| `%roguelata_mana_percent%` | Mana como porcentagem | `72` |
| `%roguelata_level%` | Nível atual da run | `14` |
| `%roguelata_health%` | Vida atual | `20` |
| `%roguelata_max_health%` | Vida máxima | `20` |
| `%roguelata_health_percent%` | Vida como porcentagem | `100` |
| `%roguelata_has_run%` | Se o jogador tem uma run ativa | `true` |

### Exemplo de configuração BetterHud + RogueLata

```yaml
# Exemplo: config.yml do BetterHud (parcial)
hud:
  mana:
    enabled: true
    text: "&b⚡ %roguelata_mana%/%roguelata_max_mana%"
    condition: "%roguelata_has_run%=true"
  level:
    enabled: true
    text: "&7Level: &f%roguelata_level%"
    condition: "%roguelata_has_run%=true"
```

## Detecção automática

O RogueLata detecta cada plugin no boot e loga no console quais integrações foram ativadas:

```
[RogueLata] AuraSkills integrado com sucesso! 38 skills registradas.
[RogueLata] RogueLata + AuraMobs detectado.
[RogueLata] RogueLata + MythicMobs detectado.
[RogueLata] RogueLata + BetterHud detectado (BossBar desativada, HUD delegada ao BetterHud).
[RogueLata] RogueLata + PlaceholderAPI integrado com sucesso!
```

Se um plugin não estiver presente, o RogueLata simplesmente não usa aquela feature — sem erros, sem warnings.
