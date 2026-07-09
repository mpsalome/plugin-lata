# Comandos e Permissões

## Comando principal: `/lata`

| Subcomando | Descrição | Exemplo |
|------------|-----------|---------|
| `/lata tp <player>` | Teleporta até um amigo (vida cheia = sem recarga; 3min de CD se não estiver com vida cheia; bloqueado em combate) | `/lata tp Steve` |
| `/lata boss spawn <id>` | Invoca um boss no bioma atual (ids: frostmaw, magma_tyrant, storm_wyvern, void_lich) | `/lata boss spawn frostmaw` |
| `/lata loja` | Abre a Loja Pao em Lata (compre upgrades com níveis de XP) | `/lata loja` |
| `/lata draft` | Abre o próximo draft pendente, ou reabre uma sessão ativa se o menu foi fechado | `/lata draft` |
| `/lata book` | Recebe uma Lata de Pão (RPG Book) caso tenha perdido | `/lata book` |

**Aliases:** `rogue`, `pao`, `roguelata`

## Comandos para jogadores (legado)

| Comando | Descrição | Exemplo |
|---------|-----------|---------|
| `/skills` | Abre o menu da sua build/coleção na run atual | `/skills` |
| `/run` | Exibe informações da run (nível, cartas, mayhem, multiplicadores, drafts pendentes) | `/run` |
| `/recall` | Ativa o Recall do Dragão (se disponível) | `/recall` |
| `/rpg` | Recebe o Livro de RPG (se não tiver) | `/rpg` |

## Menu Hub (interface gráfica)

Clique com o botão direito na **Lata de Pão** (RPG Book, item BREAD) para abrir o **HubMenu** com 3 botões:

| Botão | Descrição |
|-------|-----------|
| Coleção de Cartas | Abre o CollectionMenu (paginado, 36 cartas/página, filtros por categoria e tipo, ordenação alfabética) |
| Loja Pao em Lata | Abre o ShopMenu |
| Draft de Cartas | Abre o próximo draft pendente |

## Comandos administrativos

| Comando | Descrição | Permissão |
|---------|-----------|-----------|
| `/rpg reload` | Recarrega todos os YAML de configuração | `rpg.admin` |
| `/rpg reset` | Reseta os dados RPG do jogador | `rpg.admin` |
| `/rpg debug` | Mostra estado interno da run atual | `rpg.admin` |

## Permissões

| Permissão | Descrição | Default |
|-----------|-----------|---------|
| `rpg.admin` | Acesso a comandos de reload/reset/debug | `op` |

## Detalhes dos comandos

### `/lata loja`

Abre o **ShopMenu** com 5 itens compráveis com níveis de XP:

| Item | Custo | Efeito |
|------|-------|--------|
| Reroll de Sorte | 2 níveis | 1 reroll gratuito no próximo draft |
| Carta Avulsa | 5 níveis | +1 draft pendente |
| Absolvição do Caos | 10 níveis | Reduz o Mayhem em 1 nível |
| Sinalizador do Chefe | 15 níveis | Item consumível — clique direito para invocar um boss |
| Beque | 30 níveis | Salva itens do inventário na morte |

### `/lata draft`

- Se houver uma **sessão ativa** (menu fechado sem escolha), reabre o mesmo draft
- Se não, consome 1 draft pendente e abre 3 cartas para escolha
- Draft é **não-bloqueante**: level-ups acumulam drafts pendentes (exibidos na actionbar), o jogador abre manualmente

### `/skills`

Abre o **CollectionMenu** que mostra:
- Cartas draftadas na run atual
- Tier de cada carta (Bronze/Prata/Ouro)
- Efeitos ativos da sua build
- Paginação (36 cartas/página)
- Filtros por categoria (Explorador/Minerador/Construtor) e tipo (Habilidades/Aprimoramentos)
- Botão "Menu Principal" para voltar ao HubMenu

### `/run`

Mostra informações detalhadas da run:
- Nível atual
- Número de cartas na build
- Drafts pendentes
- Tempo decorrido
- Milestones atingidos
- Mayhem ativos
- Multiplicadores de dificuldade

### `/recall`

Ativa o **Recall do Dragão**, teleportando você ao spawn da run.
- Requer a carta "Recall do Dragão" (ouro, Explorador)
- Requer distância percorrida suficiente (cresce a cada uso)
- Mostra progresso no HUD

### `/rpg`

Sem argumentos, dá o **Livro de RPG** se você não tiver um no inventário.
