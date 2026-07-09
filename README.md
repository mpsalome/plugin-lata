# RogueLata

**Addon Action RPG para Minecraft Paper 1.21.4+** — cada morte é um recomeço, toda run é uma build nova.

Inspirado no **ARAM Mayhem** e no **Modo Arena** do LoL. Roda **standalone** (só Paper) ou integrado a [AuraSkills](https://www.spigotmc.org/resources/auraskills.81069/), [AuraMobs](https://wiki.aurelium.dev/auramobs/), [MythicMobs](https://www.spigotmc.org/resources/5702/), [BetterHud](https://www.spigotmc.org/resources/betterhud.28330/) e [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/).

```yaml
softdepend: [AuraSkills, AuraMobs, MythicMobs, ModelEngine, BetterHud, PlaceholderAPI]
```

---

## Quick start

1. Solte `RogueLata-<versão>.jar` em `plugins/` do seu servidor **Paper 1.21.4+** (Java 21).
2. (Opcional) Instale `AuraSkills`, `AuraMobs`, `MythicMobs` ou `ModelEngine` antes.
3. Reinicie. Entre no jogo. Você ganha o **RPG Book** (`BREAD`) automaticamente.
4. **Clique direito** com o livro para abrir o **HubMenu** (Coleção, Loja, Draft).
5. Use `/lata` para todos os comandos.

---

## Comandos

| Comando | Descrição |
|---------|-----------|
| `/lata` | Comando principal (aliases: `rogue`, `pao`, `roguelata`) |
| `/lata tp <player>` | Teleporta ao jogador |
| `/lata boss spawn <id>` | Spawna um boss (ids: frostmaw, magma_tyrant, storm_wyvern, void_lich, sir_creeper_alot, slime_shady, the_beheader, ancient_guardian, piglin_warlord, phantom_king) |
| `/lata loja` | Abre a Loja (ShopMenu) |
| `/lata draft` | Abre o Draft pendente (não-bloqueante) |
| `/lata book` | Dá o RPG Book (`BREAD`); cai no chão se inventário cheio |
| `/lata info` | Exibe informações da jornada (nível, cartas, mayhem) |
| **Clique direito** no RPG Book | Abre o HubMenu (Coleção, Loja, Draft) |

> **v3.3.0:** Integração BetterHud — se presente, a BossBar nativa (cooldowns/efeitos) é desativada e delegada ao BetterHud via PlaceholderAPI. Placeholders: `%roguelata_mana%`, `%roguelata_level%`, `%roguelata_health%`, etc. Draft automático (próximo draft abre ao escolher/skip). Item **Treinamento Acelerado** na loja (slot 22, 3 níveis, +100 XP AuraSkills). 10 bosses no `/lata boss spawn`. Mensagens renomeadas: "Jornada/Personagem", "Mundo Difícil".

---

## Documentação completa

👉 [**Wiki → `docs/wiki/index.md`**](docs/wiki/index.md)

| Página | Conteúdo |
|--------|----------|
| [Gameplay](docs/wiki/gameplay.md) | Regras: ciclo da run, draft, tiers, tags, sinergias, mayhem, recall, fênix, vitória |
| [Comandos](docs/wiki/commands.md) | Referência completa de comandos e permissões |
| [Cartas](docs/wiki/cards.md) | Catálogo: 37 habilidades + 53 augments = 90 cartas |
| [Integrações](docs/wiki/integrations.md) | AuraSkills (gates, draft bias, reset, veteran migration), AuraMobs, MythicMobs, BetterHud, PlaceholderAPI |
| [Configuração](docs/wiki/config.md) | `config.yml`, `draft.yml`, `augments.yml`, `gates.yml`, `mayhem.yml`, `bosses.yml`, `messages/` |
| [Desenvolvedores](docs/wiki/developers.md) | Build, arquitetura, serviços, como adicionar cartas |

---

## Build

```bash
mvn clean package   # Java 21, Maven 3.9+
```

Gera `target/RogueLata-<versão>.jar`. Requer Paper API 1.21.4 (via [papermc.io](https://papermc.io)).

---

MIT License.
