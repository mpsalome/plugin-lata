# RogueLata — Plano de Evolução Detalhado (Master SDD)

**Documento mestre / fonte da verdade**
**Projeto:** RogueLata — addon roguelike para Minecraft
**Repositório:** https://github.com/mpsalome/plugin-lata
**Versão atual:** 1.2.0 · **Alvo:** Paper **26.2** · **Java 21** · **Maven**
**Última revisão de design:** 2026-06-29

> Este documento substitui o rascunho de alto nível em [`../ROGUELATA_SDD.md`](../ROGUELATA_SDD.md). Cada épico tem arquivo dedicado em [`epics/`](epics/) e o catálogo completo de cartas está em [`appendices/A-catalogo-cartas.md`](appendices/A-catalogo-cartas.md).

---

## 1. Visão do produto (pós-revisão de game design)

RogueLata é um **addon roguelike** sobre AuraSkills + AuraMobs. O loop: **cada morte = uma run nova e diferente**, no espírito de **ARAM Mayhem / Modo Arena do LoL** — builds emergem de **drafts aleatórios**, não de uma loja otimizável, e o caos cresce conforme a run avança.

### 1.1 As 3 camadas de progressão (decisão central)

```
┌─────────────────────────────────────────────────────────────────┐
│ CAMADA 1 — Loja/grind nativo do AuraSkills (recompensa por tempo) │
│   Minerar sobe Mining, andar sobe Agility... ganha recompensas    │
│   determinísticas. É o "chão". RESETA na morte (recompensa/run).  │
├─────────────────────────────────────────────────────────────────┤
│ CAMADA 2 — Draft de 3 cartas a cada X níveis (variância roguelike)│
│   A cada X níveis o jogo oferece 3 cartas ALEATÓRIAS; escolhe 1;  │
│   acumula pela run. Pool dividido por tier; chance de tier alto    │
│   cresce com o nível (mas tier alto pode cair cedo num rate baixo).│
├─────────────────────────────────────────────────────────────────┤
│ CAMADA 3 — Modificadores "Mayhem" cumulativos por milestone       │
│   Cada milestone atingido adiciona uma regra maluca que fica até  │
│   o fim da run. A run começa normal e vira caos crescente.        │
└─────────────────────────────────────────────────────────────────┘
        Dificuldade base escala via AURAMOBS (nível do mob pela
        média de nível dos players no raio) + spikes dos modificadores.
```

### 1.2 Pilares de design

| # | Pilar | Implicação |
|---|-------|------------|
| P1 | **Variância vem de aleatoriedade controlada** | Build montada por **draft 1-de-3**, não por compra livre de catálogo |
| P2 | **Reset total na morte** | Nada de poder persiste entre runs; cada run é do zero (sem meta-progressão) |
| P3 | **Caos crescente** | Modificadores acumulam por milestone (estilo Mayhem) |
| P4 | **Dificuldade pelo mundo, não pela punição** | AuraMobs escala mobs por nível; jogador não é punido por usar o sistema |
| P5 | **Toda run tem clímax** | Condição de **vitória** (boss/objetivo), não só de derrota |
| P6 | **Recompensa por investimento dentro da run** | Camada AuraSkills premia quem foca uma atividade |
| P7 | **Standalone-first** | Funciona sem AuraSkills/AuraMobs; integrações são soft |

### 1.3 O que muda vs. o design 1.2.0

| Antes (1.2.0) | Agora |
|---------------|-------|
| Comprar skill com XP num catálogo | **Draftar 1 de 3 cartas** a cada X níveis |
| Equipar 9 (3 por tier), gerenciar loadout | **Acumular** tudo que draftou na run (sem loadout) |
| Tiers = preço na loja | Tiers = **raridade da carta** (peso escala com nível) |
| Dificuldade +2%/skill desbloqueada (pune engajamento) | Dificuldade via **AuraMobs** (profundidade) + modificadores |
| Só condição de derrota (morte) | **Vitória (boss/objetivo)** + derrota |
| Sem caos por run | **Modificadores Mayhem** cumulativos |
| 35 skills hardcoded em if-else | 35 skills viram **cartas de habilidade** no pool, data-driven |

---

## 2. Índice de épicos

| Épico | Título | Fase | Arquivo |
|-------|--------|------|---------|
| EPIC-0 | Migração para 26.2 e saneamento | F1 | [epics/EPIC-0-migracao-26.2.md](epics/EPIC-0-migracao-26.2.md) |
| EPIC-1 | Arquitetura data-driven de skills/cartas | F1 | [epics/EPIC-1-arquitetura-skills.md](epics/EPIC-1-arquitetura-skills.md) |
| EPIC-2 | **Sistema de Draft (Choose-3) + Catálogo de Cartas** | F2 | [epics/EPIC-2-draft-e-cartas.md](epics/EPIC-2-draft-e-cartas.md) |
| EPIC-3 | Ciclo de run & reset total na morte | F2 | [epics/EPIC-3-ciclo-run-reset.md](epics/EPIC-3-ciclo-run-reset.md) |
| EPIC-4 | Modificadores "Mayhem" por milestone | F2 | [epics/EPIC-4-mayhem-modificadores.md](epics/EPIC-4-mayhem-modificadores.md) |
| EPIC-5 | Gatilhos por gameplay (distância, recall, gates) | F3 | [epics/EPIC-5-gameplay-triggers.md](epics/EPIC-5-gameplay-triggers.md) |
| EPIC-6 | Integração AuraSkills (loja nativa + mana) | F3 | [epics/EPIC-6-auraskills.md](epics/EPIC-6-auraskills.md) |
| EPIC-7 | AuraMobs, dificuldade, custom mobs & bosses (vitória) | F3 | [epics/EPIC-7-auramobs-mobs-bosses.md](epics/EPIC-7-auramobs-mobs-bosses.md) |
| EPIC-8 | Menus & GUI (draft menu premium) | F4 | [epics/EPIC-8-menus.md](epics/EPIC-8-menus.md) |
| EPIC-9 | Dados, cooldowns, performance, persistência | F1 | [epics/EPIC-9-dados-performance.md](epics/EPIC-9-dados-performance.md) |
| EPIC-10 | Builds/sinergias/tags + Qualidade (i18n, testes, CI) | F4/F5 | [epics/EPIC-10-builds-qualidade.md](epics/EPIC-10-builds-qualidade.md) |

**Apêndices:** [A — Catálogo de Cartas](appendices/A-catalogo-cartas.md) · [B — Referência de Config](appendices/B-config-reference.md) · [C — Glossário](appendices/C-glossario.md)

---

## 3. Roadmap e dependências

```
F1 (Fundação)     EPIC-0 ─► EPIC-1 ─► EPIC-9
                                │
F2 (Roguelike)      ┌───────────┼───────────┬───────────┐
                    ▼           ▼           ▼           
                 EPIC-2      EPIC-3      EPIC-4         
                 (draft)     (reset)     (mayhem)       
                    │           │           │           
F3 (Mundo)          ▼           ▼           ▼           
                 EPIC-5 ─► EPIC-6 ─► EPIC-7 (vitória/boss)
                                          │             
F4/F5 (Polish)        EPIC-8 ◄───────────┘   EPIC-10    
```

**Regra de ouro:** EPIC-0, EPIC-1 e EPIC-9 são pré-requisitos de tudo. EPIC-2/3/4 formam o coração roguelike e devem sair juntos para o loop fazer sentido.

---

## 4. Árvore de pacotes alvo

```
com.project.rpgplugin
├── RPGPlugin.java                  # bootstrap + wiring (DI manual)
├── command/                        # /skills (legado), /run, /rpg, /rpg debug
├── core/
│   ├── card/                       # EPIC-2 — unifica skills + augments como "cartas"
│   │   ├── Card.java               # interface (id, tier, tags, apply/onEvent)
│   │   ├── CardTier.java (enum)     # BRONZE, SILVER, GOLD (= raridade)
│   │   ├── CardTag.java (enum)      # EXPLORER, MINER, BUILDER, TANK, DPS, LOOT, RISK, MOBILITY, SUSTAIN, UTILITY
│   │   ├── CardRegistry.java        # registra/consulta todas as cartas
│   │   ├── ability/                # cartas de habilidade (as 35 skills migradas)
│   │   └── augment/                # cartas de stat/passiva/meta
│   ├── draft/                      # EPIC-2 — motor do draft
│   │   ├── DraftService.java
│   │   ├── DraftWeighting.java     # peso por tier escalado por nível
│   │   └── DraftSession.java
│   ├── run/                        # EPIC-3 — ciclo da run
│   │   ├── RunState.java           # estado da run atual do player
│   │   ├── RunManager.java         # start/end/reset
│   │   └── ResetService.java       # reset total na morte
│   ├── mayhem/                     # EPIC-4 — modificadores por milestone
│   │   ├── Modifier.java
│   │   ├── ModifierRegistry.java
│   │   ├── MilestoneService.java
│   │   └── MayhemService.java
│   ├── progression/                # EPIC-5 — distância, recall, gates
│   ├── difficulty/                 # escalonamento (delegado a AuraMobs quando presente)
│   ├── build/                      # EPIC-10 — arquétipos/sinergias por tags
│   └── mob/                        # EPIC-7 — custom mobs + bosses (vitória)
├── data/                           # EPIC-9 — PlayerProfile, DataStore, CooldownService
├── ui/                             # EPIC-8 — DraftMenu (estrela), CollectionMenu, HUD
├── integration/                    # AuraSkillsBridge, AuraMobsBridge
├── listener/                       # CardDispatchListener, PlayerLifecycleListener
├── task/                           # PassiveTask, DistanceTask
└── config/                         # ConfigManager + *Config + MessagesConfig (i18n)
```

> **Nota de nomenclatura:** "skill" e "augment" convergem para o conceito único **Card**. Uma carta de habilidade (ability) concede um poder ativo/passivo; uma carta de augment altera stats/regras. Ambas vivem no `CardRegistry` e entram no mesmo draft.

---

## 5. Convenções globais (valem para todos os épicos)

1. **Adventure/MiniMessage** para todo texto; nada de `ChatColor`/`§`.
2. **Sem `switch`/if-else por carta** — tudo via `CardRegistry` + handlers.
3. **Toda constante de gameplay** (durações, cooldowns, custos, chances, pesos) vem de YAML.
4. **Standalone-first**; AuraSkills/AuraMobs são soft-deps.
5. **Reset total na morte** é sagrado (EPIC-3): nada de poder sobrevive entre runs.
6. **Limpeza de estado**: todo `Map<UUID,...>` é limpo em quit/death.
7. **i18n** em `messages_<lang>.yml`.
8. **Async para I/O**, main-thread para mundo/entidades (Folia-aware se aplicável).
9. **DoD por tarefa**: `mvn clean package` verde + testes verdes + CA satisfeito + smoke test sem regressão.

---

## 6. Glossário rápido

- **Run:** uma vida do jogador, do (re)spawn até a morte/vitória.
- **Carta (Card):** unidade de upgrade draftável (habilidade ou augment).
- **Draft:** evento que oferece 3 cartas para escolher 1.
- **Tier/Raridade:** Bronze/Prata/Ouro — peso no draft escala com o nível.
- **Milestone:** marco da run (nível/boss/profundidade) que dispara um modificador Mayhem.
- **Modificador (Mayhem):** regra global da run, cumulativa, que aumenta o caos.
- **Reset total:** limpeza completa do estado de poder na morte.

Detalhes em [appendices/C-glossario.md](appendices/C-glossario.md).
