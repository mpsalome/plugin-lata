# Integrações

RogueLata roda **standalone** (só Paper). Os plugins abaixo são opcionais e só enriquecem a experiência.

## AuraSkills

| Aspecto | Com AuraSkills | Sem AuraSkills |
|---------|----------------|----------------|
| Gates | Skills nativas (Mining, Agility...) liberam cartas ao subir de nível | Gates não funcionam (draft segue normal) |
| Draft bias | Níveis altos em Mining favorecem cartas MINER no draft | Todas as classes com peso igual |
| Reset na morte | Skills AuraSkills zeradas junto com o resto | Só XP vanilla é resetado |
| Mana | Habilidades podem usar mana | Habilidades usam cooldown próprio |
| Custom skills | 35 skills do RogueLata aparecem como skills custom no `/skills` do AuraSkills | Não aparecem (só existem como cartas) |

### Configuração recomendada (`plugins/AuraSkills/config.yml`)

```yaml
on_death:
  reset_skills: true
  reset_xp: true
  reset_xp_ratio: 0.0
```

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

## AuraMobs

Escala a dificuldade dos mobs pelo **nível médio dos jogadores na região**. Sem ele, a dificuldade escala apenas pela **profundidade da run** (via DifficultyService nativo).

> O DifficultyService nativo do RogueLata já escala vida/dano dos mobs. AuraMobs é um reforço opcional.

## MythicMobs

Permite criar **bosses com AI avançada** (skills, fases, drops). Sem ele, bosses são mobs vanilla turbinados (via EliteFactory: nome, vida, dano, escala, equipamento).

> Todos os bosses do RogueLata funcionam sem MythicMobs — ele só adiciona tempero de IA.

## Detecção automática

O RogueLata detecta cada plugin no boot e loga no console quais integrações foram ativadas:

```
[RogueLata] AuraSkills integrado com sucesso! 35 skills registradas.
[RogueLata] RogueLata + AuraMobs detectado.
[RogueLata] RogueLata + MythicMobs detectado.
```

Se um plugin não estiver presente, o RogueLata simplesmente não usa aquela feature — sem erros, sem warnings.
