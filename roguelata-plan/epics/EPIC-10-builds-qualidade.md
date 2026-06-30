# EPIC-10 — Builds/Sinergias/Tags, Wiring de Augments não-combate, i18n, Limpeza de Legado & Qualidade

**Fase:** F4/F5 · **Prioridade:** P1–P2 · **Dependências:** EPIC-2, EPIC-7
**Objetivo:** dar identidade às builds (sinergias por tag), **ligar os efeitos de augment que ainda estão "mortos"** (não-combate), internacionalizar, **remover o código legado** que ainda roda em paralelo e fechar testes/CI.

---

## 1. Estado atual no código (auditado)

- **Sinergia antiga sumiu:** `PlayerManager.applySynergyEffects`/`ClassListeners` (4+ do mesmo tipo → Speed/Haste/Regen) **não** existe mais no caminho novo. `CardTag` tem `EXPLORER/MINER/BUILDER` + funcionais; cartas `*_ascendant` pressupõem sinergia, mas **não há `SynergyService`**.
- **Augments declarados-e-mortos (não-combate):** em `augments.yml`, chaves como `xp_gain`, `cooldown_reduction`, `fall_damage_reduction`, `hunger_decay_reduction`, `extra_mob_drops`, `mining_speed`, `crop_yield`, `double_ore_drop`, `item_magnet_range`, `double_jump`, `blind_on_hit_chance` são gravadas em `RunState.multipliers` mas **ninguém as consome** (as de combate são tratadas no EPIC-7).
- **Legado ainda ativo:** `RPGPlugin.onEnable` registra `ClassListeners`; `SkillDispatchListener` lê de `playerManager.getEquippedSkills` (não de `RunState.ownedAbilities`) e **não está registrado**; `/skills` abre `SkillGUI`; `PlayerManager`/`SkillGUI`/`ClassListeners` continuam no projeto.
- **i18n inexistente:** strings hardcoded e **mistura de Adventure com legado `§`** (ex: `RunManager` "§a§l✦ Nova Run Iniciada", `ResetService` "§c§l☠ RUN ENCERRADA"). `Text.mm` (MiniMessage) já existe e deveria ser o padrão. `Card.nameKey()/descKey()` existem mas o `DraftMenu` mostra `id.replace("_"," ")`.
- **Catálogo parcial:** `augments.yml` tem ~28 das ~53 cartas do [Apêndice A](../appendices/A-catalogo-cartas.md). Faltam várias (gold: `phoenix`, `overcharge`, `chain_lightning`, `time_dilation`, `greedy_draft`, `echo`, `blood_pact`, `last_stand`, `momentum`, `*_ascendant`, `max_health_max`; silver: `mana_pool`, `second_wind`, `bulwark`, `frost_touch`, `vampiric_night`, `forager_plus`, `adrenaline`, `shield_battery`, `dash_charge`, `auto_smelt`).

---

## 2. Tarefas

### 2.1 Sinergias por tag

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T10.1** | `SynergyService`: conta cartas por `CardTag` na run e aplica bônus escalonados (ex: EXPLORER 4/6/8) | `core/build/SynergyService.java`, `resources/synergies.yml` | Bônus em ≥3 patamares, vindos de config |
| **T10.2** | Cartas `explorer_ascendant`/`miner_ascendant`/`builder_ascendant` usam o `SynergyService` | `core/card/augment/*` | Ascendant amplifica o bônus do tipo |
| **T10.3** | `Archetype` (tank/mobility/loot/mining/dps): detecta o arquétipo dominante da build para HUD/CollectionMenu | `core/build/Archetype.java` | Build mostra arquétipo dominante |
| **T10.4** | `extra_skill_slot`/limites: reavaliar — no modelo de draft não há "slots"; remover conceito ou repropor como bônus | `core/build/*` | Decisão registrada; sem resíduo do loadout antigo |

### 2.2 Wiring dos augments não-combate (os de combate ficam no EPIC-7)

| Tarefa | Chave `RunState` | Onde ligar | CA |
|--------|------------------|------------|-----|
| **T10.5** | `xp_gain` | listener de ganho de XP (`PlayerExpChangeEvent`) | XP ganho multiplicado |
| **T10.6** | `cooldown_reduction` | `AbstractSkill.startCooldown`/`SkillServices` aplica redução | cooldowns reduzidos pela carta |
| **T10.7** | `fall_damage_reduction` | `EntityDamageEvent` (FALL) | dano de queda reduzido |
| **T10.8** | `hunger_decay_reduction` | `FoodLevelChangeEvent` | fome cai mais devagar (substitui a lógica legada de `getDifficultyHungerMultiplier`) |
| **T10.9** | `extra_mob_drops` (scavenger) | `EntityDeathEvent` | chance de drop extra |
| **T10.10** | `double_ore_drop` (ore_greed) | `BlockBreakEvent` (minério) | chance de dobrar minério |
| **T10.11** | `mining_speed` (miner_grip) | Haste leve ao segurar picareta (PassiveTask) | mineração mais rápida |
| **T10.12** | `crop_yield` (green_thumb) | colheita | +rendimento |
| **T10.13** | `item_magnet_range` (magnet) | `PassiveTask` puxa itens próximos | itens atraídos |
| **T10.14** | `double_jump` | listener de pulo/toggle fly | pulo duplo |
| **T10.15** | `blind_on_hit_chance` (pocket_sand) | `EntityDamageByEntityEvent` (player apanha) | chance de cegar atacante |

### 2.3 Completar o catálogo

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T10.16** | Adicionar as cartas faltantes do [Apêndice A](../appendices/A-catalogo-cartas.md) ao `augments.yml` | `resources/augments.yml` | `CardRegistry.size()` cobre o catálogo-alvo |
| **T10.17** | Implementar handlers de efeito faltantes (`ON_KILL` variantes, `execute_threshold`, `double_jump`, etc.) | `core/card/augment/*` | Todo `effect.type` no YAML tem handler |
| **T10.18** | `giant` completo (tamanho/dano/velocidade, não só +vida) | `augment`, `StatService` | `giant` aplica os 4 efeitos |

### 2.4 i18n

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T10.19** | `MessagesConfig` + `messages_pt.yml`/`messages_en.yml`; **toda** string visível via chave | `config/MessagesConfig.java`, `resources/messages_*.yml` | Idioma trocável por config |
| **T10.20** | Eliminar `§` legado; padronizar **MiniMessage** (`Text.mm`) em todo o código (RunManager, ResetService, etc.) | vários | `grep '§'` no código = 0 |
| **T10.21** | `DraftMenu`/`CollectionMenu` usam `card.nameKey()/descKey()` em vez de `id.replace` | `ui/*` | Cartas exibem nome/descrição i18n |

### 2.5 Remover o legado (fim da migração)

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T10.22** | Trocar `SkillDispatchListener.dispatch` para iterar `RunState.ownedAbilities()` (não `playerManager.getEquippedSkills`) e **registrar** o listener | `listener/SkillDispatchListener.java`, `RPGPlugin` | Ativas disparam pelas cartas da run |
| **T10.23** | Remover `ClassListeners`, `PlayerManager`, `SkillGUI` e seu registro/uso | `RPGPlugin`, arquivos | Build sem essas classes; nada legado registrado |
| **T10.24** | Remover métodos de dificuldade legados de `SkillServices` (movidos p/ EPIC-7 `DifficultyService`) | `SkillServices` | `getDifficulty*Multiplier` não existem mais |

### 2.6 Qualidade

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T10.25** | Ampliar testes: `SynergyService`, wiring de multiplicadores, i18n loader, serialização (EPIC-9) | `src/test/...` | Cobertura significativa; `mvn test` verde |
| **T10.26** | **CI** (GitHub Actions): build + test em push/PR | `.github/workflows/ci.yml` | PR mostra status |
| **T10.27** | Atualizar `README` + `CHANGELOG.md`; alinhar versão e contagem de cartas | `README.md`, `CHANGELOG.md` | Docs batem com o código |

---

## 3. `synergies.yml` (esboço)

```yaml
synergies:
  EXPLORER: { thresholds: { 4: speed_1, 6: speed_1_plus_jump, 8: speed_2 } }
  MINER:    { thresholds: { 4: haste_1, 6: haste_1_plus_fortune, 8: haste_2 } }
  BUILDER:  { thresholds: { 4: regen_1, 6: regen_1_plus_absorb, 8: regen_2 } }
```

## 4. Definition of Done

- [ ] Sinergias por tag funcionando em patamares.
- [ ] **Todos** os augments do catálogo ligados (combate no EPIC-7, resto aqui).
- [ ] i18n completo; zero `§` no código.
- [ ] Legado (`PlayerManager`/`ClassListeners`/`SkillGUI`) **removido**; `SkillDispatchListener` lê de `RunState`.
- [ ] CI verde; README/CHANGELOG atualizados.
