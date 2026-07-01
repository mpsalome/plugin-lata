# 🎲 RogueLata — Wiki

**RogueLata** transforma o Minecraft numa experiência **roguelike**: você não monta uma build fixa de uma vez — você a **drafta** ao longo da run, escolhendo cartas aleatórias. Quanto mais fundo você vai, mais forte fica... e mais o mundo enlouquece. **Ao morrer, você perde tudo e recomeça do zero — com uma build completamente diferente.**

## Páginas

| Página | Descrição |
|--------|-----------|
| [Gameplay](gameplay.md) | Regras completas: ciclo da run, draft, tiers, tags, sinergias, mayhem, recall, fênix, dicas |
| [Comandos](commands.md) | Todos os comandos e permissões |
| [Cartas](cards.md) | Catálogo completo: 35 habilidades + 53 augments |
| [Integrações](integrations.md) | AuraSkills, AuraMobs, MythicMobs — como cada um afeta o jogo |
| [Configuração](config.md) | Arquivos de configuração e o que cada um controla |
| [Desenvolvedores](developers.md) | Build, arquitetura, como estender |

## Visão geral

RogueLata roda **standalone** (só Paper 1.21.4+) e opcionalmente se integra a AuraSkills, AuraMobs e MythicMobs para enriquecer a experiência.

### Em uma frase

> Toda morte é um recomeço. Toda run é uma build nova. Não existe "build ótima" — cada run é uma história curta e única.

### Dependências

| Plugin | Obrigatório? | Função |
|--------|:------------:|--------|
| Paper 1.21.4+ | ✅ Sim | Plataforma |
| AuraSkills | ➖ Opcional | Skills nativas + gates que liberam cartas + draft bias |
| AuraMobs | ➖ Opcional | Reforça escala de dificuldade dos mobs |
| MythicMobs | ➖ Opcional | AI avançada para bosses |

> Sem os opcionais, o plugin funciona **completo** — o draft, reset na morte, mayhem, recall, bosses e HUD são nativos (API Paper vanilla).
