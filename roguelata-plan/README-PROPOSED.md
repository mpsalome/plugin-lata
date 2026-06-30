# 🎲 RogueLata

**Addon roguelike para Minecraft — toda morte é um recomeço, toda run é uma build nova.**
Inspirado no caos do **ARAM Mayhem** e no draft do **Modo Arena** do LoL. Construído sobre o [AuraSkills](https://www.spigotmc.org/resources/auraskills.81069/) + [AuraMobs](https://wiki.aurelium.dev/auramobs/).

> Plataforma: Paper 1.21.4+ · Java 21 · funciona em modo standalone ou integrado ao AuraSkills/AuraMobs.

---

## 🎯 O que é

RogueLata transforma o Minecraft numa experiência **roguelike**: você não monta uma build fixa de uma vez — você a **drafta** ao longo da run, escolhendo cartas aleatórias. Quanto mais fundo você vai, mais forte fica... e mais o mundo enlouquece. **Ao morrer, você perde tudo e recomeça do zero — com uma build completamente diferente.**

Não existe "build ótima" para decorar. Cada run é uma história curta e única.

---

## 🔌 Dependências (plugins necessários)

O RogueLata roda **standalone** (só com Paper) — o draft, o reset na morte, o Mayhem e o recall já funcionam sozinhos. Os demais plugins são **opcionais (soft-depend)** e só **enriquecem** a experiência; sem eles, o plugin degrada com elegância (não quebra).

| Plugin | Obrigatório? | Para quê | O que acontece sem ele |
|--------|:------------:|----------|------------------------|
| **[Paper](https://papermc.io/)** 1.21.4+ (Java 21) | ✅ **Sim** | Servidor e API base | O plugin não roda |
| **[AuraSkills](https://www.spigotmc.org/resources/auraskills.81069/)** 2.3.12+ | ➖ Opcional | Skills nativas (Mining, Agility...), gates que liberam cartas, mana e reset na morte | Modo standalone: sem skills nativas/gates/mana (draft segue normal) |
| **[AuraMobs](https://www.spigotmc.org/resources/auramobs-a-mob-levels-add-on-for-auraskills.94168/)** | ➖ Opcional | Escala a dificuldade: nível dos mobs pela média de nível dos players na região | Dificuldade escala só pela profundidade da run |
| **[MythicMobs](https://www.spigotmc.org/resources/5702/)** | ➖ Opcional | AI/skills mais ricos para bosses e mobs nomeados | Bosses e elites **já funcionam só com Paper** (nome + barra de boss + fases); MythicMobs só os deixa mais elaborados |
| **[ModelEngine](https://mythiccraft.io/index.php?resources/1213/)** | ➖ Opcional | Modelos 3D dos bosses (imersão, sem mod no cliente) | Boss usa a entidade base do Minecraft (maior, via atributo de escala) |

> 🧱 **Bosses são vanilla:** o **Frostmaw** e os mobs-elite são mobs comuns turbinados (vida/dano/tamanho + nome flutuante + barra de boss), feitos só com a API do Paper. Isso garante que funcionem na versão do servidor mesmo que os plugins opcionais ainda não tenham atualizado. MythicMobs/ModelEngine são só "tempero" visual e de IA.

> 💡 **Combo recomendado (quando compatíveis com sua versão):** Paper + AuraSkills + AuraMobs + MythicMobs (+ ModelEngine). Mas o jogo é completo só com o Paper.

**Ordem de carregamento:** instale os plugins opcionais que quiser **antes** de subir o RogueLata (ele detecta cada um no boot e informa no console quais integrações foram ligadas).

---

## ⚡ Como funciona (resumo)

1. Você joga normalmente e **ganha níveis**.
2. A cada poucos níveis, abre um **Draft**: escolha **1 de 3 cartas** aleatórias.
3. As cartas se **acumulam** e formam sua build (habilidades + bônus).
4. Conforme você progride, o mundo fica mais difícil e surgem **regras malucas (Mayhem)**.
5. **Derrote o boss** para vencer a run — ou **morra** e perca tudo.
6. Renasceu? **Nova run, build nova.** Repete.

---

# 📜 Regras do Jogo (para Players)

Leia isto antes de jogar. É simples, mas muda tudo em relação ao Minecraft normal.

## 1. O ciclo de uma run

Uma **run** é uma vida. Ela começa quando você nasce/renasce e termina quando você **morre** (derrota) ou **derrota o boss final** (vitória). Tudo que você conquista — cartas, níveis, poderes — vale **só para a run atual**.

## 2. 💀 Morte = reset total

Ao morrer, você **perde absolutamente todo o seu poder**:

- 🃏 Todas as **cartas** draftadas
- 📊 Todos os **níveis e XP** (inclusive das skills do AuraSkills: Mining, Agility, etc.)
- 🎲 Todos os **modificadores Mayhem** acumulados
- 🎒 Todos os **itens de habilidade**
- 🔁 Cooldowns, progresso de recall e bônus

**O que você mantém:** apenas o **Livro de RPG** (sua ferramenta para abrir o menu). Você renasce e começa uma run limpa.

> Não tem meta-progressão: nada de poder passa de uma run para a outra. É a graça do roguelike.

## 3. 🧱 As 3 camadas de poder

Sua força numa run vem de três fontes que se somam:

| Camada | O que é | Como evolui |
|--------|---------|-------------|
| **1. Skills do AuraSkills** | Recompensa por dedicação | Minerar sobe Mining, andar sobe Agility... e isso **libera cartas** (gates) |
| **2. Draft de cartas** | A alma roguelike | A cada X níveis, escolha 1 de 3 cartas aleatórias |
| **3. Mayhem** | O caos | A cada marco, o mundo ganha uma regra maluca nova |

## 4. 🃏 O Draft (escolha 1 de 3)

A cada **X níveis** (padrão: 3), o jogo abre um menu com **3 cartas aleatórias**. Você escolhe **uma**. Não dá para comprar do catálogo inteiro nem trocar depois — a variedade vem justamente do que o jogo te oferece.

- As cartas se **acumulam**. Algumas podem ser pegas **várias vezes** (empilham).
- 🔄 **Reroll:** re-sortear as 3 cartas pagando um custo (limitado por draft).
- ⏭️ **Pular:** se nenhuma agradar, pule e ganhe um bônus (ex: vida/mana).

## 5. 🥉🥈🥇 Tiers = raridade

Cada carta tem um tier, que é a sua **raridade**. Quanto **mais alto seu nível**, maior a chance de aparecer carta de tier alto — mas tier alto pode pintar cedo (raro!):

| Seu nível | 🥉 Bronze | 🥈 Prata | 🥇 Ouro |
|-----------|-----------|----------|---------|
| 1–9 | 80% | 18% | 2% |
| 10–19 | 60% | 30% | 10% |
| 20–29 | 40% | 40% | 20% |
| 30+ | 25% | 40% | 35% |

- 🥉 **Bronze** — bônus pequenos, empilháveis (o "tempero" da build)
- 🥈 **Prata** — abrem uma direção (sustain, dano, mobilidade...)
- 🥇 **Ouro** — definem a build, quase sempre únicas e com trade-off

## 6. 🎨 Tipos, tags e sinergias

Cada carta tem **tags**. As três principais são os tipos clássicos:

- 🔵 **Explorador** — mobilidade e exploração
- 🟡 **Minerador** — mineração e recursos
- 🟢 **Construtor** — construção e natureza

Acumular várias cartas da **mesma tag** ativa **sinergias** (bônus crescentes em 4/6/8 cartas). Quer ser tanque? Junte cartas `TANK`. Quer farmar? Junte `LOOT`. A build emerge das suas escolhas no draft.

## 7. 🔥 Dificuldade dinâmica

O mundo **escala com você** — não é punição, é o mundo subindo de nível junto:

- 🧟 **Mobs mais fortes** conforme a profundidade da run e o nível médio dos jogadores na região (nativo; o AuraMobs, se instalado, reforça isso)
- 📈 **Profundidade da run** (tempo, distância, marcos) aumenta o desafio
- 🎲 **Mayhem** adiciona picos de caos
- ⚠️ **Cartas de risco** (ex: Canhão de Vidro) — *você* escolhe mais perigo por mais recompensa

## 8. 🌪️ Modificadores Mayhem

A cada **marco** que o grupo atinge (ex: níveis 10, 20, 30...), o mundo ganha uma **regra maluca permanente** para o resto da run — e elas **acumulam**. A run começa tranquila e vira caos total. Exemplos:

- 🌙 **Noite Eterna** — o tempo trava na noite
- 🔥 **Mobs em Chamas** — mobs pegam fogo e te incendeiam
- 🩸 **Lua de Sangue** — mobs muito mais fortes e numerosos (recompensa alta)
- 🪶 **Gravidade Baixa** — todos pulam mais alto e caem devagar
- 💎 **XP em Dobro**, 🪞 **Mobs Espelho**, ⏳ **Distorção do Tempo**... e mais.

Quanto mais fundo, mais pesados os modificadores que podem sair.

## 9. 🏆 Vitória

Diferente do Minecraft normal, aqui dá para **vencer** uma run: **derrote o boss final** (ex: o **Frostmaw**) ou cumpra o objetivo da run. A vitória encerra a run com glória — e aí começa uma nova.

## 10. 🌀 Recall por distância

A carta **Recall do Dragão** te leva de volta ao spawn — mas só depois de **andar uma certa distância**, e o requisito **cresce a cada uso** (ex: 2000 → 3000 → 4500 blocos). Use com sabedoria; não é teleporte grátis.

## 11. 🐦‍🔥 Fênix e Kit Inicial

- **Fênix** (carta Ouro): te **revive uma vez** na run. Morreu com ela? Você volta com 1 de vida e a carta é consumida.
- **Kit inicial:** ao começar uma run, você pode receber **uma carta Bronze aleatória** para já começar com um sabor diferente (estilo ARAM).

## 12. 💡 Dicas de build

- Foque uma atividade (minerar/andar) para acelerar os **gates** daquela linha de cartas.
- Cartas Bronze empilháveis (vida, velocidade, %XP) somam muito ao longo da run.
- Combine tags iguais para destravar **sinergias**.
- Cartas de risco compensam quando você já tem **sustain** (cura/vida) para bancar.

---

# 🃏 Catálogo de Cartas

## ⚡ Habilidades

### 🥉 Bronze
| Carta | Tipo | Efeito |
|-------|------|--------|
| Dash das Flores | Explorador | Speed II + Invisibilidade temporária |
| Hidratação | Explorador | Garrafa d'água enche fome e saturação |
| Passo Ágil | Explorador | Speed II por 15s |
| Salto Escalador | Explorador | Impulso pra frente e pra cima |
| Dieta de Carvão | Minerador | Coma carvão para recuperar fome |
| Quebra-Pedra | Minerador | Quebra pedra mais rápido |
| Luz de Tocha | Minerador | Visão Noturna por 30s |
| Banquete de Folhas | Construtor | Coma folhas para recuperar fome |
| Lenhador Rápido | Construtor | Haste I com machado |
| Toque de Seda Manual | Construtor | Coleta blocos frágeis com a mão |
| Salto do Andaime | Construtor | Salto + bloco temporário |

### 🥈 Prata
| Carta | Tipo | Efeito |
|-------|------|--------|
| Escudo Anti-Queda | Explorador | Anula/reduz dano de queda |
| Respiração Aquática | Explorador | Respira embaixo d'água |
| Super Salto | Explorador | Pulo aumentado permanente |
| Escudo de Lava | Explorador | Resistência a fogo por 15s |
| Radar de Minério | Minerador | Marca minérios próximos |
| Febre do Ouro | Minerador | Haste II ao minerar |
| Passo da Canópia | Construtor | Speed II em folhas/grama |
| Adubo Verde | Construtor | Faz plantas crescerem ao redor |
| Escudo Floral | Construtor | Cura ao consumir flor |
| Foco do Arquiteto | Construtor | Resistência IV (com penalidade) |
| Desafio Gravitacional | Construtor | Flutua brevemente |

### 🥇 Ouro
| Carta | Tipo | Efeito |
|-------|------|--------|
| Recall do Dragão | Explorador | Teleporta ao spawn após andar X blocos (custo crescente) |
| Sonar de Eco | Explorador | Revela entidades próximas |
| Mudança Dimensional | Explorador | Teleporte + Speed IV (com penalidades) |
| Explosão de Vento | Explorador | Lançado para o alto |
| Visão Noturna | Minerador | Visão noturna permanente |
| Reparo de Minério | Minerador | Repara 30% da picareta |
| Toque de Fusão | Minerador | Funde minérios automaticamente |
| Transmutação | Minerador | Converte metais (5 ferro→ouro, 5 ouro→diamante) |
| Escudo Gravitacional | Minerador | Resistência III |
| Sobrecarga do Núcleo | Minerador | Haste III + Força II (com penalidade) |
| Graça da Pena | Construtor | Super salto + queda lenta |
| Golpe do Lenhador | Construtor | Haste IV por 5s |
| Bloco Reforçado | Construtor | Torna um bloco indestrutível por 15s |

## ✦ Augments

### 🥉 Bronze
| Carta | Efeito |
|-------|--------|
| Vigor | +1 coração máximo (empilha até 10) |
| Aprendiz | +10% de XP (empilha) |
| Passada Leve | +velocidade (empilha) |
| Pele Dura | +armadura (empilha) |
| Mãos Rápidas | −5% de cooldown das cartas (empilha) |
| Aterrissagem Suave | −dano de queda (empilha) |
| Bom de Garfo | Fome cai mais devagar |
| Catador | Chance de drop extra de mobs |
| Recuperação | Regeneração lenta fora de combate |
| Punho do Minerador | Mineração mais rápida |
| Dedo Verde | Colheitas rendem mais |
| Olho Atento | Minérios próximos brilham |
| Aquecimento | +dano no início do combate |
| Areia nos Olhos | Chance de cegar quem te ataca |

### 🥈 Prata
| Carta | Efeito |
|-------|--------|
| Constituição | +2 corações máximos (empilha) |
| Sanguessuga | Cura parte do dano causado |
| Fúria | +dano com pouca vida |
| Espinhos | Reflete dano corpo a corpo |
| Pulo Duplo | Um segundo salto no ar |
| Bateria de Dash | +cargas/−cooldown de dash |
| Golpe Crítico | Chance de dano crítico |
| Cobiça | Chance de dobrar drop de minério |
| Forja Interna | Funde minério ao quebrar |
| Ímã | Atrai itens próximos |
| Reservatório | +mana (com AuraSkills) |
| Fôlego Extra | Absorção ao quase morrer |
| Estudioso | +25% de XP |
| Vento nos Pés | +velocidade |

### 🥇 Ouro
| Carta | Efeito |
|-------|--------|
| Colosso | +4 corações máximos |
| Canhão de Vidro | +40% dano causado, +30% recebido |
| Lorde Vampiro | Cura 15% do dano; queima ao sol |
| Carrasco | Executa mobs com pouca vida |
| Sobrecarga | A cada 5º acerto, dano em área |
| Toque de Midas | Mobs dropam ouro/esmeralda |
| Fênix | Revive 1x na run |
| Colheita de Almas | Kills curam e dão mana |
| Corrente Elétrica | Ataques saltam entre inimigos |
| Gigante | Maior, mais vida e dano, mais lento |
| Distorção Temporal | Lentidão nos mobs ao levar dano |
| Ganância | Próximos drafts oferecem 4 cartas |
| Pacto de Sangue | +dano, −vida máxima |

> O catálogo é configurável e está em expansão. Cartas com trade-off pesado são propositais — são as que mudam a run.

---

## 🎮 Comandos

| Comando | Descrição | Permissão |
|---------|-----------|-----------|
| `/skills` | Abre o menu da sua build/coleção da run | Todos |
| `/run` | Mostra informações da run atual | Todos |
| `/recall` | Usa o Recall (se disponível por distância) | Todos |
| `/rpg` | Recebe o Livro de RPG se não tiver | Todos |
| `/rpg reload` | Recarrega a configuração | `rpg.admin` |
| `/rpg reset` | Reseta os dados RPG do jogador | `rpg.admin` |
| `/rpg debug` | Mostra o estado interno da run | `rpg.admin` |

---

## 🧩 Integrações

Resumo (detalhes e links na seção [🔌 Dependências](#-dependências-plugins-necessários)):

- **AuraSkills**: skills nativas (Mining, Agility, Foraging...), **gates** que liberam cartas, mana e reset de skills na morte.
- **AuraMobs**: escala a dificuldade dos mobs pelo nível médio dos jogadores na região.
- **MythicMobs**: bosses e mobs nomeados (ex: **Frostmaw**) com fases, skills e boss bar.
- **ModelEngine**: modelos 3D dos bosses, sem mod no cliente.

> Sem esses plugins, o RogueLata roda em **modo standalone** — o draft, o reset na morte e o Mayhem continuam funcionando.

### Configuração recomendada do AuraSkills (`plugins/AuraSkills/config.yml`)
```yaml
on_death:
  reset_skills: true
  reset_xp: true
  reset_xp_ratio: 0.0
```

---

## 📦 Instalação

1. Baixe o `RogueLata.jar` mais recente.
2. (Opcional, recomendado) instale os plugins da seção [🔌 Dependências](#-dependências-plugins-necessários) em `plugins/` — `AuraSkills`, `AuraMobs`, `MythicMobs` e `ModelEngine`.
3. Coloque o `RogueLata.jar` em `plugins/` do seu servidor Paper (Java 21).
4. Reinicie o servidor.
5. Entre no jogo, use `/skills` e comece sua primeira run.

---

## ⚙️ Configuração

Arquivos em `plugins/RogueLata/`:

| Arquivo | Para quê |
|---------|----------|
| `config.yml` / `skills.yml` | Parâmetros das habilidades (durações, cooldowns) |
| `draft.yml` | Frequência do draft, pesos por tier, reroll, skip |
| `augments.yml` | Catálogo de cartas de augment |
| `gates.yml` | Quais skills/níveis liberam quais cartas |
| `mayhem.yml` | Modificadores, marcos e severidade |
| `run.yml` | Onde o jogador renasce (spawn / ponto aleatório) |

Use `/rpg reload` após editar.

---

## 🛠️ Build (desenvolvedores)

```bash
mvn clean package
```
O JAR é gerado em `target/`. Requisitos: Java 21, Maven 3.9+, Paper API.

---

## 📄 Licença

MIT.
