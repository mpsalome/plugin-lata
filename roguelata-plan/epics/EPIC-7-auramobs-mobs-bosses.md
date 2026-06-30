# EPIC-7 — Dificuldade, Elites & Bosses (vanilla-first), Vitória

**Fase:** F3 · **Prioridade:** P1 · **Dependências:** EPIC-3, EPIC-4
**Objetivo:** o **mundo escala com você** e ganha **bosses/elites** — usando **somente a API vanilla do Paper** (atributos, nome flutuante, BossBar, scheduler), sem depender de plugin. AuraMobs/MythicMobs/ModelEngine entram só como **enriquecimento opcional**. Inclui a **condição de vitória** e o **wiring dos efeitos de combate** dos augments.

> **Mudança de rota (compatibilidade 26.2):** não dá para garantir que MythicMobs/ModelEngine/AuraMobs estejam atualizados para a versão-alvo — plugins de terceiros sempre atrasam em relação ao MC novo (a dor que o grupo já citou). Então **bosses e elites são criados na mão sobre mobs vanilla** (mob comum + propriedades especiais + nome por cima), que é o único caminho que acompanha a versão do servidor.

---

## 1. Estado atual no código (auditado)

- **Não existe** pacote `mob/` nem `difficulty/`.
- Dificuldade legada em `SkillServices`: `getDifficultyDamageMultiplier = 1 + n*0.02`, `getDifficultyHungerMultiplier = 1 + n*0.015` (só a fome ainda é aplicada).
- `RunManager.endRun(p, RunOutcome.VICTORY)` existe com **placeholder** de celebração — falta quem dispare `VICTORY`.
- Augments de combate (`lifesteal`, `crit_strike`, `glass_cannon`, `berserker`, `thorns`, `executioner`, `vampire_lord`, `soul_harvest`, `midas`) gravam multiplicadores/efeitos em `RunState` mas **nenhum listener os consome**.

---

## 2. Postura de compatibilidade (26.2)

| Camada | API usada | Risco em 26.2 |
|--------|-----------|---------------|
| **Elites/bosses (este épico)** | **Vanilla Bukkit/Paper** (Attribute, BossBar, scheduler, PDC, eventos) | ✅ baixo — vem com o servidor |
| Dificuldade base | **Vanilla** (`CreatureSpawnEvent` + buff de atributos) | ✅ baixo |
| AuraMobs (scaling extra) | plugin | ⚠️ depende de update do AuraMobs |
| MythicMobs/ModelEngine (visual/AI ricos) | plugin | ⚠️ depende de update deles |

> **Pré-requisito:** confirmar no [EPIC-0](EPIC-0-migracao-26.2.md) o artefato `paper-api` real da versão-alvo. Nomes de atributos mudaram em versões recentes (`GENERIC_MAX_HEALTH` → `MAX_HEALTH`); validar também `Attribute.SCALE`, `KNOCKBACK_RESISTANCE`, etc. na versão de destino.

---

## 3. Framework de Elite/Boss vanilla (caminho primário)

Em vez de criar entidades novas, **pegamos um mob comum e o transformamos** via API:

```java
public final class EliteFactory {
    public LivingEntity spawnBoss(Location loc, BossDef def) {
        LivingEntity e = (LivingEntity) loc.getWorld().spawnEntity(loc, def.baseType()); // ex: RAVAGER
        // nome flutuante (vira "boss")
        e.customName(Text.mm(def.displayName()));
        e.setCustomNameVisible(true);
        // atributos
        set(e, Attribute.MAX_HEALTH, def.health());        e.setHealth(def.health());
        set(e, Attribute.ATTACK_DAMAGE, def.damage());
        set(e, Attribute.MOVEMENT_SPEED, def.speed());
        set(e, Attribute.KNOCKBACK_RESISTANCE, def.knockbackResist());
        set(e, Attribute.SCALE, def.scale());              // deixa o mob MAIOR = cara de boss (1.20.5+)
        // equipamento, efeitos passivos
        if (def.equipment() != null) applyEquipment(e, def.equipment());
        // marca como nosso (PDC)
        e.getPersistentDataContainer().set(ItemKeys.eliteId(), PersistentDataType.STRING, def.id());
        e.getPersistentDataContainer().set(ItemKeys.isBoss(), PersistentDataType.BYTE, (byte) 1);
        // BossBar vanilla, atualizada por task
        BossBar bar = BossBar.bossBar(Text.mm(def.displayName()), 1f, def.barColor(), Overlay.NOTCHED_10);
        bossBars.register(e.getUniqueId(), bar, def);      // mostra a players próximos; phases por task
        return e;
    }
}
```

O que a API vanilla já entrega — **sem nenhum plugin**:
- **Nome por cima** (`customName` + `customNameVisible`) → vira "boss/elite" visualmente.
- **Atributos** (vida, dano, velocidade, resistência a knockback) e **`SCALE`** → mob comum vira gigante intimidante.
- **`BossBar`** (`net.kyori.adventure.bossbar.BossBar` ou `Bukkit.createBossBar`) → barra de vida no topo, vanilla.
- **"Fases" e habilidades** via `BukkitScheduler` repetindo: em limiares de vida, dispara comportamentos (invocar adds com `spawnEntity`, AoE com `PotionEffect`/`spawnParticle`, raio com `strikeLightning`, knockback com `setVelocity`).
- **Drops** via `EntityDeathEvent` (cancela drop padrão, concede carta garantida).
- **Identificação** via PDC (saber que é nosso elite/boss, qual definição, imunidade a `executioner`).

**Elites** = a versão "leve": mob comum + atributos turbinados + nome + um buff. **Boss** = elite com BossBar + fases + recompensa de vitória. É o mesmo framework, só muda a definição.

---

## 4. Dificuldade vanilla (substitui o +2%/skill)

```java
// CreatureSpawnEvent: turbina mobs conforme profundidade da run + média dos players no raio
public void onSpawn(CreatureSpawnEvent e) {
    double factor = difficulty.factorAround(e.getLocation()); // depth + nível médio
    buffAttributes(e.getEntity(), factor);
    maybePromoteToElite(e.getEntity(), factor);               // chance de virar elite nomeado
}
```
- **`DifficultyService.factorAround(loc)`** combina profundidade da run (tempo/distância/milestones) e os players próximos — **tudo via API vanilla**.
- **Aposentar** `getDifficultyDamageMultiplier`/`getDifficultyHungerMultiplier` de `SkillServices`.
- **AuraMobs**, se presente, pode complementar/substituir o scaling (detecção soft-dep).

---

## 5. Tarefas

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T7.1** | `DifficultyService` (depth + players no raio) e **aposentar** multiplicadores legados de `SkillServices` | `core/difficulty/DifficultyService.java`, `SkillServices` | `getDifficulty*Multiplier` removidos; dificuldade vem de depth/vizinhança |
| **T7.2** | Scaling de mobs vanilla via `CreatureSpawnEvent` (buff de atributos por fator) | `listener/MobScalingListener.java` | Mobs ficam mais fortes conforme a run avança, sem plugin |
| **T7.3** | `EliteFactory` + `BossManager` (atributos, `SCALE`, nome, BossBar, fases por scheduler, PDC) | `core/mob/EliteFactory.java`, `core/mob/boss/BossManager.java` | Boss = mob vanilla com nome + BossBar + ≥2 fases; **zero plugin** |
| **T7.4** | `mobs.yml`/`bosses.yml` data-driven (base, atributos, scale, equipamento, fases, drops) | `core/mob/MobDef.java`, `BossDef.java`, `resources/mobs.yml`, `bosses.yml` | Criar elite/boss = editar YAML |
| **T7.5** | `MobSpawnService`: regras de spawn (bioma/dimensão/horário/chance/profundidade) e invocação do boss num milestone | `core/mob/MobSpawnService.java` | Frostmaw aparece num marco da run |
| **T7.6** | **Vitória**: `EntityDeathEvent` do boss (PDC `isBoss`) → `runManager.endRun(p, RunOutcome.VICTORY)` + carta Ouro garantida | `listener/MobDeathListener.java`, `RunManager` | Matar o boss encerra a run como VICTORY + drop garantido |
| **T7.7** | `CombatListener`: aplicar **efeitos de combate dos augments** (ver §6) | `listener/CombatListener.java` | Augments de combate funcionam de fato |
| **T7.8** | Integrar boss-kill como **milestone** do Mayhem (`milestones.type: boss`) | `core/mayhem/MilestoneService.java` | Derrotar boss pode disparar modificador |
| **T7.9** | **(Opcional) AuraMobsBridge** — usa AuraMobs para o scaling se presente | `integration/AuraMobsBridge.java` | Com AuraMobs, scaling melhora; sem ele, vanilla cobre |
| **T7.10** | **(Opcional) MythicMobsBridge** — se o servidor tiver MythicMobs **e** for compatível, permite definir bosses lá e ouvir `MythicMobDeathEvent` | `integration/MythicMobsBridge.java` | Quando disponível, bosses MythicMobs também disparam vitória |
| **T7.11** | **(Opcional) ModelEngine** — modelo 3D do boss quando disponível | docs/config | Sem ele, usa a entidade base com `SCALE` |
| **T7.12** | Testes: depth factor, seleção de efeitos de combate, fases do boss, hook de vitória (mock death) | `src/test/...` | `mvn test` verde |

---

## 6. Wiring dos efeitos de combate (augments hoje "mortos")

O `CombatListener` consome as chaves já gravadas em `RunState`:

| Augment | Chave/efeito | Onde aplicar |
|---------|--------------|--------------|
| `lifesteal`, `vampire_lord` | `heal_pct` | player ataca → cura % do dano |
| `crit_strike` | `crit_chance` | rolar chance → ×1.5 |
| `glass_cannon` | `damage_dealt` / `damage_taken` | dano causado e recebido |
| `berserker` | `low_hp_damage` | HP < 50% → +dano |
| `thorns` | `thorns_reflect` | levou melee → reflete % |
| `executioner` | `execute_threshold` | alvo < threshold e **não for boss (PDC `isBoss`)** → executa |
| `warmup` | `early_combat_damage` | início do combate |
| `midas`, `soul_harvest` | `onKillEffects` | `EntityDeathEvent` por kill |

> Ordem do dano causado: `base → crit → berserker/warmup → glass_cannon(dealt) → alvo → executioner`. Recebido: `incoming → glass_cannon(taken) → last_stand/bulwark → thorns`.

---

## 7. Schemas

```yaml
# resources/bosses.yml — boss 100% vanilla (sem plugin)
frostmaw:
  base: RAVAGER
  display: "<aqua><bold>Frostmaw"
  health: 300
  damage: 14
  speed: 0.28
  scale: 1.8                     # Attribute.SCALE → boss bem maior
  knockback_resist: 0.8
  bossbar: { color: BLUE, overlay: NOTCHED_10 }
  phases:
    - { at_health_pct: 100, abilities: [ice_slam, summon_stray] }
    - { at_health_pct: 50,  abilities: [blizzard, frost_nova], speed_mult: 1.3 }
  reward: { guaranteed_card_tier: gold, xp: 200 }
  victory: true                  # morte => RunOutcome.VICTORY
```

```yaml
# resources/mobs.yml — elites (mob comum nomeado e turbinado)
frost_zombie:
  base: ZOMBIE
  display: "<aqua>Zumbi Congelante"
  health: 40
  damage: 7
  scale: 1.2
  equipment: { helmet: DIAMOND_HELMET }
  drops:
    - { item: PACKED_ICE, chance: 0.5, amount: [1,3] }
    - { card: random, tier: silver, chance: 0.05 }
  spawn: { biomes: [SNOWY_PLAINS, FROZEN_PEAKS], night_only: true, chance: 0.1 }
```

> As "abilities" (ice_slam, blizzard...) são pequenas rotinas em código que combinam `PotionEffect`, `spawnParticle`, `spawnEntity` (adds), `strikeLightning` e `setVelocity` — tudo vanilla.

---

## 8. Edge cases

- **`executioner` vs boss:** checar PDC `isBoss` → bosses são imunes à execução.
- **BossBar e logout/morte do boss:** remover a barra e cancelar a task de fases (sem leak).
- **Boss em co-op:** BossBar visível a todos no raio; vitória encerra a run de quem participou (decidir critério).
- **Despawn/chunk unload do boss:** marcar `setRemoveWhenFarAway(false)` e/ou persistir; tratar `EntityRemoveEvent`.
- **AuraMobs/MythicMobs ausentes ou incompatíveis com a versão:** o caminho vanilla cobre tudo — nenhuma feature de boss/dificuldade depende deles.
- **Atributos renomeados na versão-alvo:** validar `MAX_HEALTH`, `SCALE`, `KNOCKBACK_RESISTANCE` (ver EPIC-0).

## 9. Definition of Done

- [ ] T7.1–T7.12 com CA satisfeitos.
- [ ] Dificuldade e bosses/elites **funcionam só com Paper** (sem nenhum plugin externo).
- [ ] Frostmaw = mob vanilla com nome + BossBar + fases + vitória + drop garantido.
- [ ] Augments de combate funcionando; `executioner` não afeta bosses.
- [ ] AuraMobs/MythicMobs/ModelEngine são extras opcionais, nunca requisitos.
- [ ] `mvn test` e smoke test verdes (com e sem os plugins opcionais).
