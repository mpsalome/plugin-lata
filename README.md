# 🎲 RogueLata

**Addon roguelike para Minecraft Paper 1.21.4+** — cada morte é um recomeço, toda run é uma build nova.

Inspirado no **ARAM Mayhem** e no **Modo Arena** do LoL. Roda **standalone** (só Paper) ou integrado a [AuraSkills](https://www.spigotmc.org/resources/auraskills.81069/), [AuraMobs](https://wiki.aurelium.dev/auramobs/) e [MythicMobs](https://www.spigotmc.org/resources/5702/).

```yaml
# plugin.yml (soft-depend — tudo funciona sem)
softdepend: [AuraSkills, AuraMobs, MythicMobs, ModelEngine]
```

---

## Quick start

1. Solte `RogueLata.jar` em `plugins/` do seu servidor Paper (Java 21).
2. (Opcional) Instale `AuraSkills` e `AuraMobs` antes.
3. Reinicie. Entre no jogo, use `/skills` para começar sua primeira run.

---

## Comandos

| Comando | Descrição |
|---------|-----------|
| `/skills` | Abre o menu da sua build/coleção na run atual |
| `/run` | Mostra informações da run |
| `/recall` | Usa o Recall (se disponível por distância) |
| `/rpg` | Recebe o Livro de RPG |
| `/rpg reload` | Recarrega config (`rpg.admin`) |
| `/rpg debug` | Estado interno da run (`rpg.admin`) |

---

## Documentação completa

👉 [**Wiki → `docs/wiki/index.md`**](docs/wiki/index.md)

| Página | Conteúdo |
|--------|----------|
| [Gameplay](docs/wiki/gameplay.md) | Regras: ciclo da run, draft, tiers, tags, sinergias, mayhem, recall, fênix, vitória |
| [Comandos](docs/wiki/commands.md) | Referência completa de comandos e permissões |
| [Cartas](docs/wiki/cards.md) | Catálogo: 35 habilidades + 53 augments = 88 cartas |
| [Integrações](docs/wiki/integrations.md) | AuraSkills (gates, draft bias, reset), AuraMobs, MythicMobs |
| [Configuração](docs/wiki/config.md) | `config.yml`, `draft.yml`, `augments.yml`, `gates.yml`, `mayhem.yml`, `messages/` |
| [Desenvolvedores](docs/wiki/developers.md) | Build, arquitetura, serviços, como adicionar cartas |

---

## Build

```bash
mvn clean package   # Java 21, Maven 3.9+
```

Gera `target/RogueLata-<versão>.jar`. Requer Paper API 1.21.4 (via [papermc.io](https://papermc.io)).

---

MIT License.
