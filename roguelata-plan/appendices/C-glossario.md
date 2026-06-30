# Apêndice C — Glossário

Termos do RogueLata e como aparecem no código.

| Termo | Definição | No código |
|-------|-----------|-----------|
| **Run** | Uma vida do jogador, do (re)spawn até morte/vitória. Unidade central do roguelike. | `core/run/RunState`, `RunManager` |
| **Reset total** | Limpeza completa do estado de poder na morte. Nada persiste entre runs (decisão travada). | `core/run/ResetService.fullReset` |
| **Outcome** | Desfecho da run: em andamento, morte ou vitória. | `RunOutcome` (ONGOING/DIED/VICTORY) |
| **Carta (Card)** | Unidade draftável de upgrade. Guarda-chuva de habilidade + augment. | `core/card/Card` |
| **Ability card** | Carta que concede um poder ativo/passivo (envolve um `Skill`). | `core/card/ability/AbilityCard` |
| **Augment card** | Carta que altera stats/regras (atributo, multiplicador, on-kill...). | `core/card/augment/*` |
| **Tier / Raridade** | Bronze / Prata / Ouro. Peso no draft escala com o nível. | `CardTier`, `DraftWeighting` |
| **Tag** | Sinergia (EXPLORER/MINER/BUILDER) + função (TANK/DPS/LOOT/...). | `CardTag` |
| **Draft** | Evento que oferece N cartas (3) para escolher 1. A cada X níveis. | `core/draft/DraftService`, `DraftSession`, `ui/DraftMenu` |
| **Stack** | Quantas vezes uma carta empilhável foi adquirida. | `RunState.cardCount(id)`, `Card.maxStacks()` |
| **StatService** | Recalcula atributos do player a partir das cartas (idempotente). | `core/card/StatService` |
| **Multiplier** | Valor acumulado por chave (xp_gain, crit_chance...). Augments gravam; listeners consomem. | `RunState.multipliers`, ver [Apêndice B](B-config-reference.md) |
| **Milestone** | Marco da run (nível/tempo/profundidade/boss) que dispara um modificador Mayhem. | `core/mayhem/MilestoneService` |
| **Modificador (Mayhem)** | Regra global cumulativa que aumenta o caos por milestone. | `core/mayhem/Modifier`, `MayhemService` |
| **Gate** | Atingir nível X numa skill nativa do AuraSkills concede/libera uma carta. | `core/progression/GateRegistry`, `gates.yml` |
| **Camada 1** | Progressão/recompensa nativa do AuraSkills (por investimento de tempo). Reseta na morte. | `AuraSkillsBridge` (EPIC-6) |
| **Recall por distância** | Carta que libera teleporte ao spawn ao andar X blocos; X cresce exponencialmente por uso. | `core/progression/RecallProgression`, `DistanceTracker` |
| **Mundo fresco** | Teleporte aleatório distante a cada run (vs. mesmo spawn). | `core/run/SpawnResolver`, `run.yml` |
| **Kit inicial** | Carta Bronze aleatória concedida no início da run (sabor ARAM). | `RunManager.rollInitialKit` |
| **Phoenix** | Carta que nega a primeira morte da run (revive 1x). | `RunState.phoenixCharge`, `PlayerLifecycleListener` |
| **Dificuldade dinâmica** | Mundo escala via AuraMobs (nível por raio) + profundidade + Mayhem (substitui o +2%/skill legado). | `DifficultyService`, `AuraMobsBridge` (EPIC-7) |
| **Sinergia** | Bônus por acumular cartas de uma mesma tag (4/6/8). | `SynergyService` (EPIC-10) |
| **Arquétipo** | Identidade dominante da build (tank/mobility/loot...). | `core/build/Archetype` (EPIC-10) |
| **CollectionMenu** | Tela que mostra a build atual da run (substitui o catálogo de compra). | `ui/CollectionMenu` (EPIC-8) |
| **Legado** | `PlayerManager`, `ClassListeners`, `SkillGUI` — código antigo a ser removido após a migração. | EPIC-10 §2.5 |
