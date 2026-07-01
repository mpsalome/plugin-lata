# Gameplay — Regras do Jogo

## 1. Ciclo de uma run

Uma **run** é uma vida. Começa quando você nasce/renasce e termina quando você **morre** (derrota) ou **derrota o boss final** (vitória). Tudo que você conquista — cartas, níveis, poderes — vale **só para a run atual**.

## 2. Morte = reset total

Ao morrer, você **perde absolutamente todo o poder**:

- 🃏 Todas as **cartas** draftadas
- 📊 Todos os **níveis e XP** do Minecraft
- 🎲 Todos os **modificadores Mayhem** acumulados
- 🔁 Cooldowns, progresso de recall e bônus

**O que você mantém:** apenas o **Livro de RPG**. Você renasce e começa uma run limpa.

> Com AuraSkills: os níveis das skills (Mining, Agility...) também são resetados.
> Sem AuraSkills: só o XP/níveis vanilla do Minecraft são resetados.

## 3. Três camadas de poder

| Camada | O que é | Como evolui |
|--------|---------|-------------|
| **Draft de cartas** | A alma roguelike | A cada X níveis, escolha 1 de 3 cartas |
| **AuraSkills** *(se instalado)* | Recompensa por dedicação | Minerar sobe Mining, andar sobe Agility... libera cartas (gates) e influencia o draft |
| **Mayhem** | O caos | A cada marco, o mundo ganha uma regra maluca |

## 4. O Draft

A cada **X níveis** (padrão: 3), abre um menu com **3 cartas**. Você escolhe **uma**.

- As cartas se **acumulam**. Algumas empilham (ex: Vigor, +1 coração até 10x).
- 🔄 **Reroll:** re-sorteia as 3 cartas (custa 1 nível, limitado por draft).
- ⏭️ **Pular:** ganhe um bônus de vida e feche o draft.

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

| Seu nível | 🥉 Bronze | 🥈 Prata | 🥇 Ouro |
|-----------|-----------|----------|---------|
| 1–9 | 80% | 18% | 2% |
| 10–19 | 60% | 30% | 10% |
| 20–29 | 40% | 40% | 20% |
| 30+ | 25% | 40% | 35% |

- 🥉 **Bronze** — bônus pequenos, empilháveis
- 🥈 **Prata** — abrem uma direção (sustain, dano, mobilidade)
- 🥇 **Ouro** — definem a build, únicas e com trade-off

## 6. Tags e sinergias

Cada carta tem **tags**. As três classes principais:

- 🔵 **Explorador** — mobilidade e exploração
- 🟡 **Minerador** — mineração e recursos
- 🟢 **Construtor** — construção e natureza

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

- 🌙 **Noite Eterna** — tempo trava na noite
- 🔥 **Mobs em Chamas** — mobs incendeiam ao atacar
- 🩸 **Lua de Sangue** — mobs mais fortes e numerosos (recompensa alta)
- 🪶 **Gravidade Baixa** — todos pulam mais alto e caem devagar
- 💎 **XP em Dobro**
- 🪞 **Mobs Espelho**
- ⏳ **Distorção do Tempo**

## 9. Recall

A carta **Recall do Dragão** (ouro, Explorador) te leva de volta ao spawn — mas só depois de **andar uma certa distância**, e o requisito **cresce a cada uso** (ex: 2000 → 3000 → 4500 blocos).

## 10. Fênix

Carta ouro que te **revive uma vez** na run. Morreu com ela? Você volta com 1 de vida e a carta é consumida.

## 11. Vitória

**Derrote o boss final** para vencer a run. A vitória encerra a run com glória — e uma nova começa.

## 12. Dicas de build

- Foque uma atividade (minerar/andar) para o draft favorecer cartas daquela classe (com AuraSkills)
- Cartas Bronze empilháveis (vida, velocidade, %XP) somam muito ao longo da run
- Combine tags iguais para destravar sinergias
- Cartas de risco compensam quando você já tem sustain para bancar
- Pegue Recall do Dragão cedo — quanto mais você anda, mais útil ele fica
