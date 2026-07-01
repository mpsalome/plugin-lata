# Comandos e Permissões

## Comandos para jogadores

| Comando | Descrição | Exemplo |
|---------|-----------|---------|
| `/skills` | Abre o menu da sua build/coleção na run atual | `/skills` |
| `/run` | Exibe informações da run (nível, cartas, mayhem, multiplicadores) | `/run` |
| `/recall` | Ativa o Recall do Dragão (se disponível) | `/recall` |
| `/rpg` | Recebe o Livro de RPG (se não tiver) | `/rpg` |

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

### `/skills`

Abre o **CollectionMenu** que mostra:
- Cartas draftadas na run atual
- Tier de cada carta (Bronze/Prata/Ouro)
- Efeitos ativos da sua build

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
