# 🎲 RogueLata

**Plugin Rogue-Like com sistema de habilidades em 3 tiers para Minecraft Paper 1.21+**

## Conceito

RogueLata transforma o Minecraft em uma experiencia rogue-like onde voce escolhe habilidades individualmente (sem classes fixas) em 3 tiers. Quanto mais habilidades voce desbloqueia, maior o desafio. **Ao morrer, tudo e perdido.**

## Sistema de Habilidades

### 3 Tiers (35 habilidades no total)

| Tier | Custo XP | Max Equipadas | Habilidades |
|------|----------|---------------|-------------|
| **Bronze** | 1 XP | 3 | 11 |
| **Prata** | 3 XP | 3 | 11 |
| **Ouro** | 5 XP | 3 | 13 |

Voce pode equipar ate **9 habilidades simultaneamente** (3 de cada tier).

### Lista Completa de Habilidades

#### 🥉 Bronze (1 XP)

| Habilidade | Tipo | Descricao |
|------------|------|-----------|
| Dash das Flores | Explorador | Consuma flor para dashes com Speed II e Invisibilidade |
| Hidratacao | Explorador | Garrafas de agua enchem fome e saturacao |
| Passo Agil | Explorador | Consuma acucar para Speed II por 15s |
| Salto Escalador | Explorador | Consuma slimeball para salto frontal |
| Dieta de Carvao | Minerador | Alimente-se de carvao para recuperar fome |
| Quebra-Pedra | Minerador | Quebre pedra mais rapido segurando pedregulho |
| Luz de Tocha | Minerador | Consuma tocha para Visao Noturna 30s |
| Banquete de Folhas | Construtor | Alimente-se de folhas diretamente |
| Lenhador Rapido | Construtor | Trigo com machado da Haste I por 10s |
| Toque de Seda Manual | Construtor | Colete blocos frágeis de mao vazia |
| Salto do Andaime | Construtor | Pula alto gerando blocos temporarios |

#### 🥈 Prata (3 XP)

| Habilidade | Tipo | Descricao |
|------------|------|-----------|
| Escudo Anti-Queda | Explorador | Anula dano de queda com Slow Falling |
| Respiracao Aquatica | Explorador | Consuma Lapis Lazuli para respirar na agua |
| Super Salto | Explorador | Salto passivo permanente |
| Escudo de Lava | Explorador | Magma cream da Fire Resistance por 15s |
| Radar de Minerio | Minerador | Mapeia minerios proximos com particulas |
| Febre do Ouro | Minerador | Consuma ouro com picareta para Haste II |
| Passo da Canopia | Construtor | Speed II ao pisar em folhas ou grama |
| Adubo Verde | Construtor | Farinha de osso cresce plantas ao redor |
| Escudo Floral | Construtor | Consuma flor para regenerar 8 de vida |
| Foco do Arquiteto | Construtor | Resistencia IV por 30s com penalty |
| Desafio Gravitacional | Construtor | Flutue no ar temporariamente |

#### 🥇 Ouro (5 XP)

| Habilidade | Tipo | Descricao |
|------------|------|-----------|
| Recall do Dragao | Explorador | Teleporta ao spawn do mundo |
| Sonar de Eco | Explorador | Revela entidades proximas com particulas |
| Mudanca Dimensional | Explorador | Teletransporte dimensional avancado |
| Explosao de Vento | Explorador | Ejetado ao ceu com explosao de vento |
| Visao Noturna | Minerador | Visao noturna permanente |
| Reparo de Minerio | Minerador | Use ferro para reparar 30% da picareta |
| Toque de Fusao | Minerador | Funde minerios automaticamente por 30s |
| Transmutacao | Minerador | Transmute metais em itens nobres |
| Escudo Gravitacional | Minerador | Resistencia III ao consumir obsidiana |
| Sobrecarga do Nucleo | Minerador | Haste III e Forca II com penalty |
| Graca da Pena | Construtor | Pule alto com queda lenta |

### Tipos de Habilidade

Cada habilidade pertence a um dos 3 tipos:

- **Explorador** (azul) - Mobilidade e exploracao
- **Minerador** (dourado) - Mineracao e recursos
- **Construtor** (verde) - Construcao e natureza

## Sistema Rogue-Like

### Morte = Progresso Perdido

Ao morrer:
- Todas as habilidades sao perdidas
- Todo XP e perdido
- Voce retorna ao spawn do mundo
- Apenas o Livro de RPG e preservado

### Dificuldade Dinamica

Quanto mais habilidades voce desbloqueia, maior o desafio:

- **Dano recebido**: Aumenta +2% por habilidade desbloqueada
- **Fome**: Aumenta +1.5% por habilidade desbloqueada
- Isso cria um equilibrio risco-recompensa: mais poder = mais perigo

## Sistema de Sinergia

Quando voce equipa **4+ habilidades do mesmo tipo**, uma passiva global e ativada:

| Tipo | Bonus (4+) |
|------|------------|
| Explorador | Speed I Permanente |
| Minerador | Haste I Permanente |
| Construtor | Regeneracao I Permanente |

## Comandos

| Comando | Descricao | Permissao |
|---------|-----------|-----------|
| `/skills` | Abre o menu de selecao de habilidades | Todos |
| `/rpg` | Recebe o Livro de RPG | Todos |
| `/rpg reload` | Recarrega configuracao | rpg.admin |
| `/rpg reset` | Reseta todos os dados do jogador | rpg.admin |

## Instalacao

1. Baixe o JAR mais recente
2. Coloque em `plugins/` do seu servidor Paper 1.21.4+ (26.2)
3. Coloque `InvUI.jar` em `plugins/` (necessario para os menus)
4. **(Opcional)** Coloque `AuraSkills.jar` e `AuraMobs.jar` em `plugins/` para integracao completa
5. Reinicie o servidor
6. Use `/skills` para comecar

> **Nota**: RogueLata funciona standalone sem AuraSkills. Com AuraSkills, as 35 habilidades custom sao registradas sob o namespace `roguelata/` e desbloqueadas automaticamente ao atingir certos niveis nas skills padrao (Agility, Mining, Foraging, etc.).

## Configuracao

O plugin possui configuracao padrao via `config.yml` (gerado na primeira execucao). Use `/rpg reload` para recarregar.

### AuraSkills (Recomendado)

Para reset completo na morte, configure em `plugins/AuraSkills/config.yml`:
```yaml
on_death:
  reset_skills: true
  reset_xp: true
  reset_xp_ratio: 0.0
```

### AuraMobs

Adicione `AuraMobs.jar` em `plugins/` para mobs com nivel baseado nas skills dos jogadores. Baixe em: https://wiki.aurelium.dev/auramobs/

## Permissoes

```
rpg.admin:
  description: Permite recarregar/resetar o plugin
  default: op
```

## Marcador de Skills Custom

Todas as habilidades do RogueLata sao registradas no AuraSkills sob o namespace `roguelata/` (ex: `roguelata/dash`, `roguelata/stone_smash`).
Isso serve como marcador visual para identificar quais skills sao nossas vs. as padrao do AuraSkills.

**Progression Gates** - Skills sao desbloqueadas ao atingir certos niveis:
- Explorador: Agility level 2-16
- Minerador: Mining level 2-20 + Enchanting 15
- Construtor: Foraging level 2-26

## Desenvolvimento

### Build

```bash
mvn clean package
```

O JAR sera gerado em `target/RogueLata-${project.version}.jar`.

### Dependencias

- Paper API 1.21.4 (Paper 26.2)
- AuraSkills API 2.3.12 (optional, compile-only)
- Java 21
- Maven 3.9+

### Estrutura do Projeto

```
src/main/java/com/project/rpgplugin/
├── RPGPlugin.java              # Entry point e comandos
├── PlayerManager.java          # Dados persistentes e metadados das skills
├── SkillGUI.java               # GUI InvUI de selecao de habilidades
├── ClassListeners.java         # Eventos, ativacao de skills e HUD
└── AuraSkillsIntegration.java  # Ponte com AuraSkills (namespace roguelata/)
```

## Licenca

MIT
