# Gameplay — Regras do Jogo

## 1. Ciclo de uma run

Uma **run** é uma vida. Começa quando você nasce/renasce e termina quando você **morre** (derrota) ou **derrota o boss final** (vitória). Tudo que você conquista — cartas, níveis, poderes — vale **só para a run atual**.

## 2. Morte = reset total

Ao morrer, você **perde absolutamente todo o poder**:

- Todas as **cartas** draftadas
- Todos os **níveis e XP** do Minecraft
- Todos os **modificadores Mayhem** acumulados **(Mayhem é limpo: timers cancelados, entidades removidas)**
- Cooldowns, progresso de recall e bônus

**O que você mantém:** apenas o **Livro de RPG (Lata de Pão)**. Você renasce e começa uma run limpa.

> Com AuraSkills: os níveis das skills (Mining, Agility...) também são resetados.
> Sem AuraSkills: só o XP/níveis vanilla do Minecraft são resetados.
> **Respawn é totalmente vanilla** — o plugin não sobrescreve `setRespawnLocation`.

## 3. Três camadas de poder

| Camada | O que é | Como evolui |
|--------|---------|-------------|
| **Draft de cartas** | A alma roguelike | A cada X níveis, acumula um draft pendente; abra manualmente com `/lata draft` ou pelo HubMenu |
| **AuraSkills** *(se instalado)* | Recompensa por dedicação | Minerar sobe Mining, andar sobe Agility... libera cartas (gates) e influencia o draft |
| **Mayhem** | O caos | A cada marco, o mundo ganha uma regra maluca; **limpo na morte** ou pela Purificação |

## 4. O Draft

A cada **X níveis** (padrão: 3), um draft é **acumulado silenciosamente** (actionbar avisa). O jogador abre manualmente via `/lata draft` ou pelo botão Draft no HubMenu.

- O draft é **não-bloqueante**: você continua jogando normalmente, os drafts pendentes acumulam
- Cada draft mostra **3 cartas**. Você escolhe **uma**.
- As cartas se **acumulam**. Algumas empilham (ex: Vigor, +1 coração até 10x).
- **Reroll:** re-sorteia as 3 cartas (custa 1 nível, limitado por draft).
- **Pular:** ganhe um bônus de vida e feche o draft.
- O menu pode ser **fechado** sem escolha — a sessão ativa é preservada e reaberta com `/lata draft`

### Draft influenciado por AuraSkills

Se o AuraSkills estiver instalado, suas skills influenciam as cartas ofertadas:

| Sua skill AuraSkills | Cartas favorecidas |
|----------------------|--------------------|
| Agility + Archery + Defense + Fighting | Explorador (mobilidade, combate) |
| Mining + Excavation + Enchanting + Foraging | Minerador (recursos) |
| Farming + Fishing + Alchemy + Foraging | Construtor (natureza, construção) |

> Quanto maior seu nível na skill, maior a chance de cair carta da classe correspondente.
> Em modo standalone, todas as classes têm o mesmo peso.

## 5. Tiers = raridade

| Seu nível | Bronze | Prata | Ouro |
|-----------|--------|-------|------|
| 1–9 | 80% | 18% | 2% |
| 10–19 | 60% | 30% | 10% |
| 20–29 | 40% | 40% | 20% |
| 30+ | 25% | 40% | 35% |

- **Bronze** — bônus pequenos, empilháveis
- **Prata** — abrem uma direção (sustain, dano, mobilidade)
- **Ouro** — definem a build, únicas e com trade-off

## 6. Tags e sinergias

Cada carta tem **tags**. As três classes principais:

- **Explorador** — mobilidade e exploração
- **Minerador** — mineração e recursos
- **Construtor** — construção e natureza

Acumular cartas da **mesma tag** ativa **sinergias**:

- 4 cartas → Speed I (Explorador) / Haste I (Minerador) / Regeneração I (Construtor)
- 6 cartas → efeito amplificado
- 8 cartas → efeito máximo

Tags secundárias: `TANK`, `DPS`, `MOBILITY`, `LOOT`, `SUSTAIN`, `RISK`, `UTILITY`, `ECONOMY`.

## 7. Dificuldade dinâmica

O mundo escala com você (não é punição):

- **Profundidade da run** (tempo, distância, marcos) aumenta vida/dano dos mobs
- **Nível médio dos jogadores na região** também influencia
- **Mayhem** adiciona picos de caos
- **Cartas de risco** (ex: Canhão de Vidro) — você escolhe mais perigo por mais recompensa

> O AuraMobs, se instalado, reforça esse escalonamento. Sem ele, a dificuldade nativa continua ativa.

## 8. Mayhem

A cada **marco** (níveis 10, 20, 30...), o mundo ganha uma **regra maluca permanente** até o fim da run. Elas **acumulam**. Exemplos:

- **Noite Eterna** — tempo trava na noite
- **Mobs em Chamas** — mobs incendeiam ao atacar
- **Lua de Sangue** — mobs mais fortes e numerosos (recompensa alta)
- **Gravidade Baixa** — todos pulam mais alto e caem devagar
- **XP em Dobro**
- **Mobs Espelho**
- **Distorção do Tempo**

Ao morrer, **todo Mayhem é limpo** — timers cancelados e entidades mayhem removidas.

**Purificação:** compre o item "Purificação do Mundo" na Loja por 30 níveis para remover TODO o mayhem do mundo instantaneamente.

### Milestone Boss (novo na 3.2.0)

A cada 10 níveis, há 50% de chance de invocar um **boss aleatório** ao invés de aplicar mayhem. O boss escala com o nível do invocador.

## 9. Recall

A carta **Recall do Dragão** (ouro, Explorador) te leva de volta ao spawn — mas só depois de **andar uma certa distância**, e o requisito **cresce a cada uso** (ex: 2000 → 3000 → 4500 blocos).

## 10. Fênix

Carta ouro que te **revive uma vez** na run. Morreu com ela? Você volta com 1 de vida e a carta é consumida.

## 11. Loja

Acessível via `/lata loja` ou botão "Loja Pao em Lata" no HubMenu. Compre upgrades com seus níveis de XP:

| Slot | Item | Custo | Efeito |
|------|------|-------|--------|
| 11 | Reroll de Sorte | 2 níveis | 1 reroll gratuito no próximo draft |
| 12 | Carta Avulsa | 5 níveis | +1 draft pendente |
| 13 | Absolvição do Caos | 10 níveis | Reduz o Mayhem em 1 nível |
| 14 | Sinalizador do Chefe | 15 níveis | Item consumível — clique direito para invocar um boss no bioma atual |
| 15 | Beque | 30 níveis | Na morte, preserva itens do inventário |
| 18 | Purificação do Mundo | 30 níveis | Remove TODO o mayhem do mundo |

## 12. HubMenu

Acessado clicando com o **botão direito na Lata de Pão** (RPG Book, item BREAD). Contém 3 botões:

1. **Coleção de Cartas** — CollectionMenu paginado com filtros
2. **Loja Pao em Lata** — ShopMenu
3. **Draft de Cartas** — abre o próximo draft pendente

## 13. Bosses

**10 bosses** disponíveis (4 originais + 6 novos), invocados via `/lata boss spawn <id>`, pelo item **Sinalizador do Chefe**, ou como **Milestone Boss** (50% de chance a cada 10 níveis).

**Novos bosses da v3.2.0:** Sir Creeper-A-Lot (CREEPER), Slime Shady (SLIME), O Decapitador (WITHER_SKELETON), Guardião Ancestral (IRON_GOLEM), Senhor da Guerra Piglin (PIGLIN_BRUTE), Rei Fantasma (SKELETON).

| ID | Tipo | Display (MiniMessage) |
|----|------|-----------------------|
| `frostmaw` | POLAR_BEAR | Frostmaw, Senhor do Gelo |
| `magma_tyrant` | MAGMA_CUBE | Tirano Magmático, Coração do Inferno |
| `storm_wyvern` | RAVAGER | Fúria Tempestuosa, Asa do Céu |
| `void_lich` | WITHER_SKELETON | Lich do Vazio, A Noite Eterna |
| `sir_creeper` | CREEPER | Sir Creeper-A-Lot |
| `slime_shady` | SLIME | Slime Shady |
| `o_decapitador` | WITHER_SKELETON | O Decapitador |
| `guardiao_ancestral` | IRON_GOLEM | Guardião Ancestral |
| `senhor_da_guerra_piglin` | PIGLIN_BRUTE | Senhor da Guerra Piglin |
| `rei_fantasma` | SKELETON | Rei Fantasma |

- Ao invocar, o **nível do boss** é exibido + uma dica do loot (sneak peek)
- Bosses usam **MiniMessage** para nomes com formatação (BossBar usa texto plano sem tags)
- Invocação tem delay de 5 segundos com broadcast de aviso
- Configuráveis via `bosses.yml`
- **Loot:** cada boss dropa peças do seu **set temático** (armadura/arma customizada com nome, encantamentos e lore) + loot aleatório. A quantidade de peças aumenta com o nível do boss.
- **Escala:** vida do boss = base * (1 + 0.15 × nível_invocador), dano = base * (1 + 0.10 × nível_invocador), com ±20% de variação RNG.
- **Equipamento:** o BossLootService gera as peças do set + loot aleatório escalado pelo nível do boss.

## 14. HUD e BossBar

- **Actionbar:** exibe apenas mana e vida atual
- **BossBar:** exibe status dos cooldowns, habilidades ativas e efeitos ativos
- **SonarSkill:** agache + clique direito para ativar um **glow contínuo** em entidades próximas; clique novamente para desativar

## 15. Vitória

**Derrote o boss final** para vencer a run. A vitória encerra a run com glória — e uma nova começa.

## 16. Dicas de build

- Foque uma atividade (minerar/andar) para o draft favorecer cartas daquela classe (com AuraSkills)
- Cartas Bronze empilháveis (vida, velocidade, %XP) somam muito ao longo da run
- Combine tags iguais para destravar sinergias
- Cartas de risco compensam quando você já tem sustain para bancar
- Pegue Recall do Dragão cedo — quanto mais você anda, mais útil ele fica
- Use a Loja para comprar drafts extras e rerolls quando estiver com níveis sobrando
- Compre **Purificação do Mundo** se o mayhem estiver acumulando demais
- Fique atento ao **Milestone Boss** ao subir de nível — prepare-se para um combate