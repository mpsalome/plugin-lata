# EPIC-6 — Integração profunda com AuraSkills (loja nativa + mana)

**Fase:** F3 · **Prioridade:** P1 · **Dependências:** EPIC-1, EPIC-2, EPIC-3
**Objetivo:** consolidar a **Camada 1** (progressão/recompensa nativa do AuraSkills) e migrar as ativas para o sistema de **mana abilities** nativo. Eliminar a dependência da classe legada `PlayerManager` na ponte e fechar o reset-on-death (hoje placeholder).

---

## 1. Estado atual no código (auditado)

- `AuraSkillsIntegration` (no pacote raiz `com.project.rpgplugin`) ainda **registra CustomSkill usando metadados do `PlayerManager` legado** (`getAllSkillKeys`, `getSkillDisplayName`, `determineSkillMaterial`).
- `onSkillLevelUp` → `gateRegistry.check(player, skill.name(), level)` ✅ (gates já migrados, EPIC-5).
- Existe `resetPlayerSkills`, `resetAllAuraSkills`, `syncAuraSkillLevel` — porém **`ResetService.fullReset` tem só um placeholder** no passo 4: `// 4. AuraSkills: placeholder for EPIC-6`.
- **Não há mana abilities** — as ativas ainda dependem de consumir item (via `SkillServices`/triggers).
- A ponte é criada com `new AuraSkillsIntegration(this, playerManager, gateRegistry)` em `RPGPlugin.onEnable`.

---

## 2. Tarefas

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T6.1** | Renomear/mover `AuraSkillsIntegration` → `integration/AuraSkillsBridge`; **remover dependência de `PlayerManager`**, lendo metadados do `CardRegistry`/`SkillRegistry` | `integration/AuraSkillsBridge.java`, `RPGPlugin` | `registerCustomSkills()` itera `cardRegistry.all()`/`skillRegistry.all()`; zero referência a `PlayerManager` |
| **T6.2** | Implementar **`CustomManaAbility`** para cada ability card ativa (custo de mana + cooldown nativos), config em `mana_abilities.yml` | `AuraSkillsBridge`, `resources/mana_abilities.yml` | Com AuraSkills, ativas consomem mana e respeitam cooldown nativo |
| **T6.3** | **Fallback standalone**: sem AuraSkills, ativas usam clique-direito + cooldown via `SkillServices` (comportamento atual) | `AuraSkillsBridge`, triggers | Plugin funciona sem AuraSkills sem perder ativas |
| **T6.4** | Fechar o **reset-on-death**: chamar `auraSkillsBridge.resetAll(p)` no passo 4 de `ResetService.fullReset` (hoje placeholder) | `core/run/ResetService.java` | Morrer zera skills+XP nativos do AuraSkills (já existe `resetAllAuraSkills`) |
| **T6.5** | Alinhar `on_death` do AuraSkills (`reset_skills: true`, `reset_xp: true`, `reset_xp_ratio: 0.0`) e documentar no README/SMOKE_TEST | `docs/`, `README.md` | Camada 1 reseta junto com a run (decisão de design travada) |
| **T6.6** | Injeção de dependência: `DraftMenu`/serviços recebem a bridge, **sem** `Bukkit.getPluginManager().getPlugin(...)` + cast | `RPGPlugin`, UI | Nenhum lookup estático da bridge |
| **T6.7** | Expor cartas RogueLata no **menu nativo `/skills`** do AuraSkills (menus configuráveis) | `mana_abilities.yml`, docs | Cartas `roguelata/` aparecem no `/skills` com ícone/descrição |
| **T6.8** | Carta `mana_pool` (Apêndice A) só entra no pool **se AuraSkills presente** (`offerable` checa a bridge) | `core/card/augment/*`, `augments.yml` | Em standalone, `mana_pool` não é ofertada |
| **T6.9** | Testes: registro sem `PlayerManager`, reset chama a bridge, fallback standalone | `src/test/...` | `mvn test` verde |

---

## 3. `mana_abilities.yml` (novo)

Mapeia cada ability card ativa para uma mana ability nativa. Exemplo:

```yaml
# resources/mana_abilities.yml
dash:
  base_mana_cost: 15
  mana_cost_per_level: 0
  base_cooldown: 30
  cooldown_per_level: 0
  trigger: RIGHT_CLICK        # ou item ritual (EPIC-2 / Apêndice A)
sonar:
  base_mana_cost: 20
  base_cooldown: 20
recall:
  # gating é por distância (EPIC-5), não mana; declarar como cooldown 0
  base_mana_cost: 0
  base_cooldown: 0
```

> As **passivas** (`sight`, `jump_boost`, `canopy_step`, `safe_fall`, `hydration`) **não** viram mana ability — continuam ativas enquanto a carta estiver na run.

---

## 4. Como a Camada 1 (loja nativa) convive com o draft

- **Camada 1 (AuraSkills nativo):** minerar sobe Mining, andar sobe Agility, etc. Recompensa determinística **por run** (reseta na morte — T6.4/T6.5).
- **Gates (EPIC-5):** atingir nível X numa skill nativa injeta/concede cartas (`gates.yml`).
- **Camada 2 (draft):** independente, a cada X níveis (EPIC-2).
- **Resultado:** focar uma atividade acelera os gates daquela linha; o draft adiciona a variância roguelike por cima.

---

## 5. Edge cases

- **AuraSkills ausente no boot e instalado depois:** a bridge só ativa no `onEnable`; documentar que exige restart.
- **Falha de API (`AuraSkillsApi.get()` lança):** capturar e cair para standalone (já há try/catch).
- **Mana ability + carta não possuída:** a ability só deve estar ativa se a carta está em `RunState.ownedAbilities`.
- **Reset duplicado:** `resetAll` deve ser idempotente (chamar duas vezes não quebra).

## 6. Definition of Done

- [ ] T6.1–T6.9 com CA satisfeitos.
- [ ] `AuraSkillsBridge` sem dependência de `PlayerManager`.
- [ ] Ativas via mana (com AuraSkills) e via clique-direito (standalone).
- [ ] Reset-on-death zera skills nativas (passo 4 real, não placeholder).
- [ ] `mvn test` e smoke test (com e sem AuraSkills) verdes.
