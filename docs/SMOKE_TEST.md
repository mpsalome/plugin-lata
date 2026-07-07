# Smoke Test — RogueLata (Paper 1.21.4+)

## A. Standalone (no plugins)

- [ ] Servidor sobe sem erro no console
- [ ] Ao entrar, jogador recebe a "Lata de Pão" (`BREAD`) no inventário
- [ ] `/skills` abre o CollectionMenu (run atual)
- [ ] `/run` mostra informações da run
- [ ] `/rpg debug` mostra estado interno da run
- [ ] Draft abre a cada 3 níveis com 3 cartas distintas
- [ ] Escolher uma carta no draft a adiciona à run
- [ ] Escolher reroll re-sorteia as 3 cartas (consome 1 nível)
- [ ] Escolher skip concede cura (6 corações) e fecha o draft
- [ ] Ativar uma skill (ex: Dash com flor) aplica efeito e cooldown
- [ ] Morrer reseta cartas/XP e teleporta ao spawn
- [ ] Fênix revive 1x na run (se possuir a carta)
- [ ] HUD mostra progresso do recall e mayhem ativos
- [ ] Mobs spawnam com dificuldade escalada (profundidade + players)
- [ ] Mayhem ativa a cada milestone com broadcast MiniMessage
- [ ] Boss (Frostmaw) spawna no milestone 5 com BossBar
- [ ] Boss kill conta como vitória e fecha a run
- [ ] `mana_pool` NÃO aparece no draft (sem AuraSkills)
- [ ] `mvn clean package` verde
- [ ] `mvn test` verde

## B. +AuraSkills

- [ ] Todos os testes do modo A passam
- [ ] Skills `roguelata/*` aparecem no `/skills` nativo do AuraSkills
- [ ] Gates funcionam: nível de skill AuraSkills concede cartas
- [ ] Draft favorece cartas da classe com maior nível AuraSkills
- [ ] Mana system ativo: habilidades com custo consomem mana
- [ ] `mana_pool` aparece no draft
- [ ] Morte reseta XP/skills do AuraSkills também
- [ ] Log: `RogueLata + AuraSkills integrado com sucesso!`

## C. +AuraMobs

- [ ] Todos os testes do modo B passam
- [ ] Mobs recebem scaling adicional do AuraMobs
- [ ] Log: `RogueLata + AuraMobs detectado`
- [ ] Desabilitar AuraMobs: servidor sobe sem erro (standalone)

## D. +MythicMobs (+ModelEngine opcional)

- [ ] Todos os testes do modo C passam
- [ ] Boss Frostmaw spawna como MythicMob (se configurado)
- [ ] ModelEngine aplica modelo 3D (se configurado)
- [ ] Log: `RogueLata + MythicMobs detectado`
- [ ] Log: `RogueLata + ModelEngine detectado`
- [ ] Desabilitar MythicMobs: servidor sobe sem erro (standalone)
