# Apêndice B — Referência de Configuração

Estado **real** dos arquivos de config no repositório (auditado) + arquivos **planejados** pelos épicos. Caminhos relativos a `src/main/resources/`.

> Legenda: ✅ existe · 🟡 existe parcial · 🆕 planejado.

---

## Arquivos existentes

### `config.yml` 🟡 (legado, parcialmente lido)
Parâmetros antigos por classe (explorer/miner/builder). **Sobreposto** por `skills.yml`. A `SkillServices.skillConfig(id)` ainda procura em `classes.<cls>.<id>`. Migrar tudo para `skills.yml` (EPIC-1/10) e aposentar este.
```yaml
classes:
  explorer: { fall_reduction: 0.5, dash: {duration, cooldown, speed}, hydration: {hunger, saturation}, jump_boost_lvl, water_breathing: {duration, cooldown}, recall: {cooldown} }
  miner:    { diet: {hunger, saturation}, sight_lvl, haste: {duration, cooldown, amp} }
  builder:  { feast: {hunger, saturation}, grace_lvl, grace: {duration, cooldown, jump} }
```

### `skills.yml` ✅ (parâmetros de skill por id)
Durações (s), cooldowns (s), amplificadores. **Fonte preferida** de valores de gameplay. Inclui o recall por distância:
```yaml
recall: { base_distance: 2000, growth: 1.5, cap: 0 }   # custo exponencial (EPIC-5)
dash:   { duration: 10, cooldown: 30, speed: 1 }
# ... cooldowns de step_assist, grapple, sonar, haste, molten_touch, etc.
```

### `draft.yml` ✅ (EPIC-2)
```yaml
draft:
  every_levels: 3
  cards_per_offer: 3
  queue_pending: true
  allow_skip: true
  skip_bonus: { heal: 6, mana: 20 }
  reroll: { enabled: true, cost_levels: 1, max_per_draft: 1 }
  weights:   # tier scaling por faixa de nível
    - { from: 1,  to: 9,   bronze: 80, silver: 18, gold: 2 }
    - { from: 10, to: 19,  bronze: 60, silver: 30, gold: 10 }
    - { from: 20, to: 29,  bronze: 40, silver: 40, gold: 20 }
    - { from: 30, to: 999, bronze: 25, silver: 40, gold: 35 }
```

### `augments.yml` 🟡 (EPIC-2 — ~28 das ~53 cartas do Apêndice A)
Cada entrada: `tier`, `tags`, `kind`, `icon`, `max_stacks`, `effect`. Tipos de `effect` em uso:
```
ATTRIBUTE   { attribute: MAX_HEALTH|ATTACK_DAMAGE|MOVEMENT_SPEED|ARMOR, add_per_stack }
MULT        { key: <chave>, add }                  # grava em RunState.multipliers
MULT_MULTI  { damage_dealt, damage_taken }         # glass_cannon
POTION      { potion: NIGHT_VISION, amplifier }
ON_DAMAGE_DEALT { heal_pct }                        # lifesteal/vampire_lord
ON_KILL     { effect: gold_drop|heal_and_mana, value }
```
> **Pendente (EPIC-7/10):** muitos `MULT`/`ON_*` ainda **não têm handler** que os aplique. Ver [EPIC-7 §4](../epics/EPIC-7-auramobs-mobs-bosses.md) (combate) e [EPIC-10 §2.2](../epics/EPIC-10-builds-qualidade.md) (não-combate). Catálogo completo: [Apêndice A](A-catalogo-cartas.md).

### `gates.yml` ✅ (EPIC-5)
Lista de gates `{ skill: <AuraSkill>, level: N, card: <id>, mode: grant|pool }`. `grant` concede a carta; `pool` libera no draft. Cobre AGILITY/FIGHTING/MINING/ENCHANTING/FORAGING.

### `mayhem.yml` ✅ (EPIC-4)
```yaml
mayhem:
  scope: server                  # server | player
  milestones: { type: level, thresholds: [10,20,30,40,50] }
  severity_by_index: [ {index:0, allow:[MILD]}, ... {index:4, allow:[INSANE]} ]
  announce: true
  max_active: 6
  incompatibilities: [ "glass_body + glass_cannon_world" ]
```

### `run.yml` ✅ (EPIC-3)
```yaml
spawn_resolver:
  mode: world_spawn              # world_spawn | random_far | instanced
  random_far: { min_radius: 5000, max_radius: 20000 }
```
> **Recomendação (EPIC-3):** default `random_far` para "mundo fresco" por run.

### `plugin.yml` ✅
`api-version: '1.21'` (⚠️ EPIC-0 quer **26.2**). Comandos: `skills`, `rpg [reload|reset|debug]`, `run`, `recall`. `softdepend: [AuraSkills]` (adicionar **AuraMobs** no EPIC-7). Permissão `rpg.admin`.

### `pom.xml` ✅
`version 1.3.0`; `paper.version 1.21.4-R0.1-SNAPSHOT` (⚠️ ainda **não** é 26.2); `invui 2.2.0` **provided** (EPIC-8 quer remover); `auraskills-api-bukkit 2.3.12` provided; JUnit 5.10.2; surefire 3.2.5; shade ativo (mas InvUI não é mais empacotada).

---

## Arquivos planejados (a criar)

| Arquivo | Épico | Conteúdo |
|---------|-------|----------|
| `mana_abilities.yml` 🆕 | EPIC-6 | custo de mana + cooldown por ability card (AuraSkills) |
| `mobs.yml` 🆕 | EPIC-7 | custom mobs (base entity, atributos, drops, spawn) |
| `bosses.yml` 🆕 | EPIC-7 | bosses (fases, boss bar, recompensa, `victory`) |
| `synergies.yml` 🆕 | EPIC-10 | bônus por `CardTag` em patamares |
| `messages_pt.yml` / `messages_en.yml` 🆕 | EPIC-10 | i18n (todas as strings + nomes/descrições de carta) |

---

## Chaves de `RunState.multipliers` (contrato entre YAML e código)

Referência das chaves usadas em `effect: { type: MULT, key: ... }` — quem **grava** (augment) e quem deve **consumir** (listener):

| Chave | Augment(s) | Consumidor (épico) |
|-------|-----------|--------------------|
| `xp_gain` | xp_boost(+plus) | XP listener (EPIC-10) |
| `cooldown_reduction` | quick_hands | startCooldown (EPIC-10) |
| `fall_damage_reduction` | soft_landing | dano FALL (EPIC-10) |
| `hunger_decay_reduction` | hearty_appetite | FoodLevelChange (EPIC-10) |
| `extra_mob_drops` | scavenger | EntityDeath (EPIC-10) |
| `mining_speed` | miner_grip | PassiveTask (EPIC-10) |
| `crop_yield` | green_thumb | colheita (EPIC-10) |
| `double_ore_drop` | ore_greed | BlockBreak minério (EPIC-10) |
| `item_magnet_range` | magnet | PassiveTask (EPIC-10) |
| `double_jump` | double_jump | pulo (EPIC-10) |
| `blind_on_hit_chance` | pocket_sand | apanhar (EPIC-10) |
| `crit_chance` | crit_strike | combate (EPIC-7) |
| `low_hp_damage` | berserker | combate (EPIC-7) |
| `thorns_reflect` | thorns | combate (EPIC-7) |
| `execute_threshold` | executioner | combate (EPIC-7) |
| `early_combat_damage` | warmup | combate (EPIC-7) |
| `damage_dealt` / `damage_taken` | glass_cannon (MULT_MULTI) | combate (EPIC-7) |
