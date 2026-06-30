# EPIC-4 — Modificadores "Mayhem" por Milestone

**Fase:** F2 · **Prioridade:** P1 · **Dependências:** EPIC-3
**Objetivo:** a cada **milestone** atingido na run, adicionar uma **regra maluca global** (modificador) que fica até o fim da run. O caos é **cumulativo**: a run começa normal e vira insana conforme progride. Inspiração direta no **ARAM Mayhem**.

> Decisão travada: modificadores **acumulam por milestone**, não 1 por run.

---

## 1. Conceitos

```java
public interface Modifier {
    String id();
    ModifierSeverity severity();     // MILD | WILD | INSANE (influencia quando pode aparecer)
    Set<ModifierTag> tags();         // OFFENSE, DEFENSE, ENVIRONMENT, ECONOMY, CHAOS
    void onActivate(World scope, MayhemContext ctx);   // liga hooks/listeners/tasks
    void onDeactivate(World scope, MayhemContext ctx); // desliga no reset
    boolean compatibleWith(Set<String> activeModifiers); // evita combos quebrados
}

public enum ModifierSeverity { MILD, WILD, INSANE }
```

**Escopo:** modificadores valem para o **grupo/run** (servidor co-op pequeno, como o grupo do WhatsApp). Decidir em config se o escopo é por-player ou global do servidor (`mayhem.scope: server | player`). Para a vibe ARAM co-op, **`server`** faz mais sentido (todos sofrem o mesmo caos).

---

## 2. Milestones — o que dispara um modificador

`MilestoneService` define os marcos (config). Cada milestone atingido → `MayhemService.rollAndApply()`.

```yaml
# mayhem.yml
mayhem:
  scope: server
  milestones:
    type: level        # level | time | depth | boss
    thresholds: [10, 20, 30, 40, 50]   # cada um adiciona 1 modificador
  severity_by_index:   # quanto mais fundo, mais pesado
    - { index: 0, allow: [MILD] }
    - { index: 1, allow: [MILD, WILD] }
    - { index: 2, allow: [WILD] }
    - { index: 3, allow: [WILD, INSANE] }
    - { index: 4, allow: [INSANE] }
  announce: true       # broadcast quando um modifier liga
  max_active: 6
```

Tipos de milestone suportados: por **nível** (default), por **tempo de run**, por **profundidade** (distância/anos), por **boss derrotado** (casa com EPIC-7).

---

## 3. `MayhemService`

```java
public final class MayhemService {
    public void rollAndApply(MayhemContext ctx) {
        int idx = ctx.run().milestonesReached();
        Set<ModifierSeverity> allowed = cfg.severityFor(idx);
        Modifier m = registry.rollOne(allowed, ctx.run().activeModifiers());  // ponderado + compatível
        if (m == null) return;
        ctx.run().activeModifiers().add(m.id());
        m.onActivate(ctx.world(), ctx);
        if (cfg.announce()) broadcast("mayhem.activated", m);
    }
    public void clear(Player p, RunState run) {   // chamado no reset (EPIC-3)
        run.activeModifiers().forEach(id -> registry.byId(id).ifPresent(m -> m.onDeactivate(world, ctx)));
        run.activeModifiers().clear();
    }
    public void reapplyOnJoin(Player p, RunState run) { /* religa hooks após logout */ }
}
```

---

## 4. Catálogo de Modificadores

### MILD (cedo — colorem sem destruir)
| id | Nome | Tags | Efeito |
|----|------|------|--------|
| `eternal_night` | Noite Eterna | ENVIRONMENT | O tempo trava na noite; mais mobs hostis |
| `glass_world` | Mundo de Vidro | ENVIRONMENT | Quedas doem +25%, mas você anda mais rápido |
| `double_xp` | XP em Dobro | ECONOMY | +100% XP (drafts vêm mais rápido) |
| `iron_rain` | Chuva de Sucata | ECONOMY | Mobs dropam mais ferro/cobre |
| `caffeinated` | Cafeinado | OFFENSE | Todos com Haste leve permanente |
| `fragile_blocks` | Blocos Frágeis | ENVIRONMENT | Alguns blocos quebram num golpe, outros explodem |

### WILD (meio — mudam o jeito de jogar)
| id | Nome | Tags | Efeito |
|----|------|------|--------|
| `mobs_on_fire` | Mobs em Chamas | OFFENSE | Mobs hostis pegam fogo e incendeiam ao tocar |
| `vampiric_mobs` | Mobs Vampíricos | OFFENSE | Mobs curam ao te acertar |
| `low_gravity` | Gravidade Baixa | ENVIRONMENT | Pulos altos, queda lenta para todos |
| `glass_cannon_world` | Mundo Canhão | OFFENSE | Todos (você e mobs) causam +50% e recebem +50% |
| `swarm` | Enxame | CHAOS | Spawns hostis em maior quantidade, mais fracos |
| `healing_inverted` | Cura Invertida | DEFENSE | Comida cura menos; matar mobs cura você |
| `explosive_death` | Morte Explosiva | CHAOS | Mobs explodem (pequeno) ao morrer |
| `magnetic_storm` | Tempestade Magnética | ENVIRONMENT | Bússola/relógio falham; bruma reduz visão |

### INSANE (fundo — caos total, casa com o boss do EPIC-7)
| id | Nome | Tags | Efeito |
|----|------|------|--------|
| `blood_moon` | Lua de Sangue | CHAOS, OFFENSE | Mobs muito mais fortes e numerosos; recompensa alta |
| `glass_body` | Corpo de Vidro | DEFENSE, CHAOS | Você causa muito dano, mas tem metade da vida |
| `time_warp` | Distorção do Tempo | CHAOS | Dia/noite aceleram; eventos mais rápidos |
| `mirror_mobs` | Mobs Espelho | OFFENSE | Mobs ganham uma das suas cartas de habilidade |
| `mana_drought` | Seca de Mana | DEFENSE | Mana regenera muito devagar (penaliza ativas) |
| `gravity_chaos` | Gravidade Caótica | CHAOS | Gravidade muda periodicamente |
| `apex_predator` | Predador Alfa | CHAOS, OFFENSE | Surge um mini-boss errante que caça o grupo |

> **Combos perigosos:** `compatibleWith` deve bloquear pares que travam o jogo (ex: `mana_drought` + um boss que exige ativas; `glass_body` + `glass_cannon_world` = morte instantânea). Listar incompatibilidades em `mayhem.yml`.

---

## 5. Tarefas

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T4.1** | `Modifier`, `ModifierRegistry`, severidade/tags | `core/mayhem/*` | Modificadores registráveis e consultáveis |
| **T4.2** | `MilestoneService` (level/time/depth/boss) | `core/mayhem/MilestoneService.java` | Atingir threshold dispara 1 modificador |
| **T4.3** | `MayhemService` (roll ponderado por severidade + compatibilidade + reapply no join) | `core/mayhem/MayhemService.java` | Caos cresce com a profundidade; sem combos travados |
| **T4.4** | Implementar **todos** os modificadores do catálogo (§4) | `core/mayhem/impl/*`, `mayhem.yml` | Cada modificador liga/desliga limpo; sem leak de listener/task |
| **T4.5** | Limpeza no reset (EPIC-3) e religação no join | `MayhemService`, `PlayerLifecycleListener` | Reset remove todos; relog reaplica os ativos |
| **T4.6** | Broadcast/HUD do modificador ativo (anúncio + ícone) | `ui/HudService.java` | Jogadores veem qual caos está ativo |
| **T4.7** | Escopo `server` vs `player` configurável | `mayhem.yml` | Em co-op, todos compartilham o caos (default) |
| **T4.8** | Testes: roll por severidade, incompatibilidades, ativa/desativa sem leak | `src/test/...` | `mvn test` verde |

---

## 6. Edge cases

- **Leaks de listener/task:** cada `onActivate` que registra listener/`BukkitTask` **deve** removê-lo em `onDeactivate`. Cobrir com teste de ciclo.
- **Modificadores que alteram mundo (tempo/gravidade):** restaurar estado original no `onDeactivate` (ex: voltar `doDaylightCycle`).
- **Player entra no meio da run (co-op):** aplicar os modificadores ativos a ele no join.
- **Performance:** modificadores que rodam por tick devem ser leves e cancelar quando 0 players online.

## 7. Definition of Done

- [ ] T4.1–T4.8 com CA satisfeitos.
- [ ] Caos cumulativo por milestone, escalando severidade com profundidade.
- [ ] Todos os modificadores do catálogo implementados e limpos no reset.
- [ ] Sem leaks (teste de ativa/desativa em ciclo).
- [ ] `mvn test` e smoke test verdes.
