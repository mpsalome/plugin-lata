# Apêndice A — Catálogo Completo de Cartas (Cards / Augments / Melhorias)

Este é o **conteúdo draftável** do RogueLata (EPIC-2). Cada linha vira uma entrada de `cards.yml`/`augments.yml` ou uma classe em `core/card/`. Uma LLM pode gerar config/Java diretamente a partir daqui.

**Tier = raridade** (peso no draft escala com o nível — ver [EPIC-2 §3.2](../epics/EPIC-2-draft-e-cartas.md)).
**Kind:** `ABILITY` (poder ativo/passivo) ou `AUGMENT` (stat/regra).
**Stacks:** quantas vezes pode ser draftada (1 = única).
**Tags:** sinergia (EXPLORER/MINER/BUILDER) + função (TANK/DPS/MOBILITY/LOOT/SUSTAIN/RISK/UTILITY/ECONOMY).

> **Nota de ativação (decisão de design):** a maioria das cartas `ABILITY` ativas migra para **mana + cooldown** (AuraSkills, ver [EPIC-6](../epics/EPIC-6-auraskills.md)). Mantemos um **subconjunto "ritual"** que ainda consome um item temático (marcado com 🍽️). Passivas (🌿) são sempre ativas enquanto a carta estiver na run. Em standalone (sem AuraSkills), as ativas caem para clique-direito + cooldown próprio.

Totais: **35 cartas de habilidade** + **53 augments** = **88 cartas**. Valores entre `[ ]` são defaults configuráveis.

---

## 1. CARTAS DE HABILIDADE (ABILITY) — migradas das 35 skills

### 🥉 Bronze (11)

| id | Nome | Tags | Stacks | Ativação | Efeito |
|----|------|------|--------|----------|--------|
| `dash` | Dash das Flores | EXPLORER, MOBILITY | 1 | 🍽️ flor / mana | Speed II + Invisibilidade [10s], cd [30s] |
| `hydration` | Hidratação | EXPLORER, SUSTAIN | 1 | 🌿 ao beber água | Garrafa d'água enche fome [+1] e saturação [+0.5] |
| `step_assist` | Passo Ágil | EXPLORER, MOBILITY | 1 | 🍽️ açúcar / mana | Speed II [15s], cd [15s] |
| `grapple` | Salto Escalador | EXPLORER, MOBILITY | 1 | 🍽️ slimeball / mana | Impulso frontal+vertical, cd [10s] |
| `diet` | Dieta de Carvão | MINER, SUSTAIN | 1 | 🍽️ carvão | Recupera fome [+4] e saturação [+2] |
| `stone_smash` | Quebra-Pedra | MINER | 1 | 🌿 segurando pedra/cobble | Haste ao quebrar pedra/deepslate |
| `torch_light` | Luz de Tocha | MINER, UTILITY | 1 | 🍽️ tocha / mana | Visão Noturna [30s], cd [30s] |
| `feast` | Banquete de Folhas | BUILDER, SUSTAIN | 1 | 🍽️ folhas | Recupera fome [+2] e saturação [+0.8] |
| `woodcutter` | Lenhador Rápido | BUILDER | 1 | 🍽️ trigo + machado / mana | Haste I [10s], cd [15s] |
| `silk_touch` | Toque de Seda Manual | BUILDER, UTILITY | 1 | 🌿 mão vazia | Coleta blocos frágeis com silk touch |
| `scaffold` | Salto do Andaime | BUILDER, MOBILITY | 1 | 🍽️ terra / mana | Salto + bloco temporário [5s], cd [12s] |

### 🥈 Prata (11)

| id | Nome | Tags | Stacks | Ativação | Efeito |
|----|------|------|--------|----------|--------|
| `safe_fall` | Escudo Anti-Queda | EXPLORER, TANK | 1 | 🌿 passiva | Slow Falling + reduz dano de queda [50%] |
| `water_breathing` | Respiração Aquática | EXPLORER, UTILITY | 1 | 🍽️ lápis / mana | Water Breathing [15s] |
| `jump_boost` | Super Salto | EXPLORER, MOBILITY | 1 | 🌿 passiva | Jump Boost permanente |
| `thermal_resistance` | Escudo de Lava | EXPLORER, TANK | 1 | 🍽️ magma cream / mana | Fire Resistance [15s], cd [30s] |
| `ore_sonar` | Radar de Minério | MINER, UTILITY | 1 | 🍽️ glowstone dust / mana | Marca minérios próximos com partículas, cd [30s] |
| `haste` | Febre do Ouro | MINER | 1 | 🍽️ ouro + picareta / mana | Haste II [15s], cd [20s] |
| `canopy_step` | Passo da Canópia | BUILDER, MOBILITY | 1 | 🌿 passiva | Speed II ao pisar em folhas/grama |
| `fertilize` | Adubo Verde | BUILDER, ECONOMY | 1 | 🍽️ bone meal / mana | Cresce plantas ao redor, cd [10s] |
| `flora_shield` | Escudo Floral | BUILDER, SUSTAIN | 1 | 🍽️ flor / mana | Cura [+8 vida], cd [15s] |
| `architect_focus` | Foco do Arquiteto | BUILDER, TANK, RISK | 1 | 🍽️ stone bricks / mana | Resistência IV [30s] + Slowness/Weakness |
| `gravity_defiance` | Desafio Gravitacional | BUILDER, MOBILITY | 1 | 🍽️ slime block / mana | Levitation breve + Slow Falling, cd [40s] |

### 🥇 Ouro (13)

| id | Nome | Tags | Stacks | Ativação | Efeito |
|----|------|------|--------|----------|--------|
| `recall` | Recall do Dragão | EXPLORER, UTILITY | 1 | distância (EPIC-5) | Teleporta ao spawn após andar X blocos (custo **exponencial** por uso) |
| `sonar` | Sonar de Eco | EXPLORER, UTILITY | 1 | 🍽️ amethyst / mana | Revela entidades próximas [15 blocos], cd [20s] |
| `dim_shift` | Mudança Dimensional | EXPLORER, MOBILITY, RISK | 1 | 🍽️ ender pearl / mana | Teleporte [8 blocos] + Speed IV + Blindness/Hunger |
| `wind_burst` | Explosão de Vento | EXPLORER, MOBILITY | 1 | 🍽️ pólvora / mana | Ejetado pro alto, cd [25s] |
| `sight` | Visão Noturna | MINER, UTILITY | 1 | 🌿 passiva | Night Vision permanente |
| `ore_repair` | Reparo de Minério | MINER, UTILITY | 1 | 🍽️ ferro + picareta | Repara [30%] da picareta |
| `molten_touch` | Toque de Fusão | MINER, ECONOMY | 1 | 🍽️ flint / mana | Auto-smelt de minérios [30s], cd [45s] |
| `transmutation` | Transmutação | MINER, ECONOMY | 1 | 🍽️ 5 ferro→ouro / 5 ouro→diamante | Converte metais |
| `gravity_shield` | Escudo Gravitacional | MINER, TANK | 1 | 🍽️ obsidiana / mana | Resistência III [15s], cd [45s] |
| `core_overdrive` | Sobrecarga do Núcleo | MINER, RISK, DPS | 1 | 🍽️ redstone block / mana | Haste III + Força II + Slowness/Hunger |
| `grace` | Graça da Pena | BUILDER, MOBILITY | 1 | 🍽️ feather / mana | Super salto + Slow Falling, cd [45s] |
| `lumberjack` | Golpe do Lenhador | BUILDER | 1 | 🍽️ iron block + machado / mana | Haste IV [5s], cd [60s] |
| `unbreakable_block` | Bloco Reforçado | BUILDER, UTILITY | 1 | 🍽️ clay ball / mana | Torna um bloco indestrutível [15s] |

> Migração: cada uma destas envolve um `Skill` do EPIC-1. Draftar a carta = `run.ownedAbilities.add(id)`. A lógica de ativação (trigger) já existe no EPIC-1; aqui só se conecta ao draft + (EPIC-6) ao sistema de mana.

---

## 2. AUGMENTS (AUGMENT) — cartas novas de stat/regra

### 🥉 Bronze — comuns, incrementais (14)

| id | Nome | Tags | Stacks | Efeito |
|----|------|------|--------|--------|
| `max_health` | Vigor | TANK, SUSTAIN | 10 | +1 coração máx por stack (`MAX_HEALTH +2`) |
| `xp_boost` | Aprendiz | UTILITY | 5 | +10% ganho de XP por stack |
| `swift` | Passada Leve | MOBILITY | 5 | +4% velocidade por stack |
| `tough_skin` | Pele Dura | TANK | 6 | +1 ponto de armadura por stack |
| `quick_hands` | Mãos Rápidas | UTILITY | 5 | −5% cooldown das cartas por stack |
| `soft_landing` | Aterrissagem Suave | MOBILITY, TANK | 3 | −15% dano de queda por stack |
| `hearty_appetite` | Bom de Garfo | SUSTAIN | 3 | Fome cai 20% mais devagar por stack |
| `scavenger` | Catador | LOOT | 3 | +10% chance de drop extra de mob por stack |
| `regen_lite` | Recuperação | SUSTAIN | 3 | Regen lento fora de combate (escala por stack) |
| `miner_grip` | Punho do Minerador | MINER | 3 | Haste leve ao segurar picareta (escala por stack) |
| `green_thumb` | Dedo Verde | BUILDER, ECONOMY | 3 | Colheitas rendem +1 por stack |
| `keen_eye` | Olho Atento | MINER, UTILITY | 1 | Minérios próximos brilham ao ficar parado |
| `warmup` | Aquecimento | DPS | 3 | +5% dano nos primeiros 3s de combate por stack |
| `pocket_sand` | Areia nos Olhos | UTILITY | 2 | Chance de cegar quem te ataca por stack |

### 🥈 Prata — raras, definem direção (20)

| id | Nome | Tags | Stacks | Efeito |
|----|------|------|--------|--------|
| `max_health_plus` | Constituição | TANK, SUSTAIN | 5 | +2 corações máx por stack |
| `lifesteal` | Sanguessuga | SUSTAIN, DPS | 3 | Cura 5% do dano causado por stack |
| `berserker` | Fúria | RISK, DPS | 3 | +15% dano abaixo de 50% de vida (por stack) |
| `thorns` | Espinhos | TANK | 2 | Reflete 20% do dano melee por stack |
| `double_jump` | Pulo Duplo | MOBILITY | 1 | Permite um segundo salto no ar |
| `dash_charge` | Bateria de Dash | MOBILITY | 2 | +1 carga de dash / −cooldown (sinergia c/ `dash`/`grapple`) |
| `crit_strike` | Golpe Crítico | DPS | 3 | 15% de chance de crítico (x1.5) por stack |
| `ore_greed` | Cobiça | MINER, LOOT | 2 | 25% chance de dobrar drop de minério por stack |
| `auto_smelt` | Forja Interna | MINER, ECONOMY | 1 | 25% chance de fundir minério ao quebrar (passivo) |
| `magnet` | Ímã | UTILITY, LOOT | 1 | Puxa itens próximos para o jogador |
| `mana_pool` | Reservatório | UTILITY | 3 | +mana máx e regen (só com AuraSkills) |
| `second_wind` | Fôlego Extra | TANK, SUSTAIN | 1 | Ao quase morrer, ganha Absorção breve (cd longo) |
| `xp_boost_plus` | Estudioso | UTILITY | 3 | +25% ganho de XP por stack |
| `swift_plus` | Vento nos Pés | MOBILITY | 3 | +8% velocidade por stack |
| `bulwark` | Baluarte | TANK, RISK | 2 | +redução de dano %, −velocidade leve |
| `frost_touch` | Toque Gélido | DPS, UTILITY | 2 | Ataques aplicam Slowness breve |
| `vampiric_night` | Sede Noturna | SUSTAIN, RISK | 1 | +cura à noite, −cura de dia |
| `forager_plus` | Fazendeiro | BUILDER, ECONOMY | 1 | Animais/cultivos rendem o dobro |
| `adrenaline` | Adrenalina | MOBILITY, DPS | 3 | Ao matar mob, +velocidade breve (por stack) |
| `shield_battery` | Escudo Recarregável | TANK | 2 | Absorção regenera fora de combate |

### 🥇 Ouro — épicas/prismáticas, definem a build (19)

| id | Nome | Tags | Stacks | Efeito |
|----|------|------|--------|--------|
| `max_health_max` | Colosso | TANK | 3 | +4 corações máx por stack |
| `glass_cannon` | Canhão de Vidro | RISK, DPS | 1 | +40% dano causado, +30% dano recebido |
| `vampire_lord` | Lorde Vampiro | SUSTAIN, RISK | 1 | Cura 15% do dano; queima sob luz solar |
| `executioner` | Carrasco | DPS | 1 | Mata instantaneamente mobs abaixo de 15% de vida |
| `overcharge` | Sobrecarga | DPS | 1 | Todo 5º acerto causa dano em área |
| `midas` | Toque de Midas | LOOT, ECONOMY | 1 | Mobs têm chance de dropar ouro/esmeralda |
| `phoenix` | Fênix | SUSTAIN | 1 | Revive 1x na run (consome o charge; cd até reuso). Não sobrevive ao fim da run |
| `soul_harvest` | Colheita de Almas | SUSTAIN, DPS | 1 | Kills restauram mana + cura pequena |
| `chain_lightning` | Corrente Elétrica | DPS | 1 | Ataques saltam para inimigos próximos |
| `giant` | Gigante | TANK, RISK | 1 | +tamanho, +vida, +dano, −velocidade |
| `time_dilation` | Distorção Temporal | UTILITY, TANK | 1 | Ao levar dano, mobs próximos ficam lentos |
| `greedy_draft` | Ganância | UTILITY | 1 | Próximos drafts oferecem 4 cartas (escolhe 1) |
| `echo` | Eco | UTILITY, DPS | 1 | 10% de chance de a carta não entrar em cooldown |
| `blood_pact` | Pacto de Sangue | RISK, DPS | 1 | +60% dano, mas −4 corações de vida máx |
| `last_stand` | Último Suspiro | TANK, RISK | 1 | Quanto menos vida, maior a redução de dano |
| `momentum` | Ímpeto | MOBILITY | 1 | Velocidade cresce enquanto corre em linha reta |
| `explorer_ascendant` | Ascensão do Explorador | EXPLORER | 1 | Bônus forte por carta EXPLORER possuída (sinergia, EPIC-10) |
| `miner_ascendant` | Ascensão do Minerador | MINER | 1 | Bônus forte por carta MINER possuída |
| `builder_ascendant` | Ascensão do Construtor | BUILDER | 1 | Bônus forte por carta BUILDER possuída |

---

## 3. Cartas que se conectam a outros sistemas

| Carta | Conecta com | Observação |
|-------|-------------|------------|
| `recall` | EPIC-5 (distância + custo exponencial) | Não é por item; libera ao andar X blocos, X cresce a cada uso |
| `mana_pool`, ativas via mana | EPIC-6 (AuraSkills) | Só entram no pool se AuraSkills presente; fallback standalone |
| `*_ascendant`, tags de tipo | EPIC-10 (sinergias por tag) | Bônus de arquétipo cruzando cartas do mesmo tipo |
| `glass_cannon`, `blood_pact`, `giant` | EPIC-7 (dificuldade) | Somam ao multiplicador de dano recebido; risco/recompensa |
| `greedy_draft`, `echo`, reroll | EPIC-2 (draft) | Metacartas que alteram o próprio draft |
| Todas | EPIC-3 (reset total) | Toda carta é apagada na morte; `onRemove` recalcula stats |

---

## 4. Diretrizes de balanceamento (para a LLM ajustar valores)

1. **Bronze** deve ser "tempero" incremental e empilhável; nunca define a build sozinho.
2. **Prata** abre uma direção (sustain, dps, mobility...). Stacks médios (2–3).
3. **Ouro** define a build; quase sempre **única** (`maxStacks: 1`) e com trade-off.
4. **Trade-offs explícitos** em cartas de risco (glass_cannon, blood_pact, vampire_lord) para casar com a dificuldade do AuraMobs/Mayhem.
5. **Nada permanente entre runs** — todo número é per-run (EPIC-3).
6. **Pool mínimo viável para shipping:** as 35 abilities + ~20 augments (resto em ondas).
7. **Anti-redundância:** cartas de mesmo efeito empilham via `maxStacks`, não viram entradas separadas.

---

## 5. Como esta tabela vira config (exemplo de tradução)

A linha `max_health | Vigor | TANK,SUSTAIN | 10 | +1 coração máx por stack` →

```yaml
# augments.yml
max_health:
  name_key: card.max_health.name
  desc_key: card.max_health.desc
  tier: bronze
  kind: augment
  tags: [tank, sustain]
  icon: APPLE
  max_stacks: 10
  effect: { type: ATTRIBUTE, attribute: MAX_HEALTH, add_per_stack: 2 }
```

E a ability `dash` →

```yaml
# cards.yml (referencia o Skill do EPIC-1)
dash:
  name_key: card.dash.name
  desc_key: card.dash.desc
  tier: bronze
  kind: ability
  tags: [explorer, mobility]
  icon: NETHER_WART
  max_stacks: 1
  skill_ref: dash         # liga ao SkillRegistry (EPIC-1)
  activation: { mana: true, ritual_item: SUNFLOWER }   # EPIC-6
```
