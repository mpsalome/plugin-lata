# EPIC-12 — Revisão de Documentação (Wiki, README & SDDs)

**Fase:** F5 (último) · **Prioridade:** P2 · **Dependências:** todos os épicos de feature
**Objetivo:** auditar e padronizar **toda a documentação** — wiki, README e SDDs — garantindo que **links funcionem**, **informações batam com o código**, **formatação seja consistente** e o status (implementado vs. design-alvo) seja **honesto**. É o passo final que torna a documentação confiável para players e contribuidores.

> Motivação real: já apareceram bugs de doc — links `[[Página|Rótulo]]` quebrando dentro de tabelas, âncoras `#secao` que o GitHub wiki ignora, README descrevendo features ainda não ligadas no código.

---

## 1. Escopo da auditoria

- **Wiki** (`roguelata-wiki/`): Home, _Sidebar, Regras-do-Jogo, Habilidades, Augments, Calculos-e-Formulas, Mayhem, Comandos-e-Configuracao.
- **README** (raiz do repo) + `docs/` (SMOKE_TEST, SERVER_SETUP).
- **SDDs** (`roguelata-plan/`): master, épicos, apêndices.

---

## 2. Tarefas

### 2.1 Links

| Tarefa | Descrição | CA |
|--------|-----------|-----|
| **T12.1** | **Nunca usar `[[Página\|Rótulo]]` dentro de tabela** (o `\|` quebra a coluna). Em tabelas, usar lista `[[ ]]` fora da tabela, ou link markdown sem pipe. Auditar todas as páginas. | Nenhum link quebrado em tabela |
| **T12.2** | Validar que **todo `[[Page]]` aponta para um arquivo existente** (nomes com hífen, sem acento: `Calculos-e-Formulas`, `Comandos-e-Configuracao`). | Zero link que leve a "criar página" |
| **T12.3** | **Âncoras `#secao`**: o GitHub wiki às vezes ignora. Validar cada `[[Page#anchor]]`; se não resolver, trocar por link que funcione ou remover a âncora. | Âncoras levam à seção certa (ou são removidas) |
| **T12.4** | `_Sidebar.md` consistente com as páginas reais; avaliar `_Footer.md` (links rápidos). | Sidebar reflete todas as páginas |

### 2.2 Acurácia (doc × código)

| Tarefa | Descrição | CA |
|--------|-----------|-----|
| **T12.5** | Marcar como *itálico*/🚧 **tudo que é design-alvo ainda não implementado** (cartas, vitória/boss, sinergias, persistência). Sincronizar com a tabela de status do [master SDD](../00-MASTER-SDD.md). | Player não é prometido o que não existe |
| **T12.6** | Conferir **valores numéricos** da wiki contra os YAML reais (`skills.yml`, `draft.yml`, `augments.yml`): cooldowns, pesos do draft, stacks, fórmula do recall. | Valores da wiki == config do código |
| **T12.7** | Conferir **contagem de cartas** e listas (35 habilidades; augments implementados vs. catálogo do Apêndice A). | Números batem com `CardRegistry.size()` |
| **T12.8** | Conferir **comandos/permissões** (`/skills`, `/run`, `/recall`, `/rpg ...`) contra `plugin.yml`. | Tabela de comandos == plugin.yml |
| **T12.9** | Nomes de cartas na doc == chaves de i18n (`messages_*.yml`, EPIC-10) — sem `id.replace("_"," ")`. | Doc e jogo usam os mesmos nomes |

### 2.3 Consistência & formatação

| Tarefa | Descrição | CA |
|--------|-----------|-----|
| **T12.10** | Consistência **README ↔ wiki ↔ SDD** (versão, plataforma, **lista de dependências/plugins**, comandos, design). | As três fontes não se contradizem |
| **T12.11** | Formatação: tabelas alinhadas, headings hierárquicos, blocos de código com linguagem, emojis padronizados por tier/tipo, sem linhas órfãs. | Render limpo no GitHub |
| **T12.12** | Revisão de idioma (PT-BR), ortografia e consistência de termos (usar o [Glossário](../appendices/C-glossario.md)). | Termos uniformes |
| **T12.13** | Garantir que **README e wiki citam as dependências** (Paper, AuraSkills, AuraMobs, MythicMobs, ModelEngine) — ver [EPIC-11](EPIC-11-dependencias-setup.md). | Seção de dependências presente e correta |

### 2.4 Processo

| Tarefa | Descrição | CA |
|--------|-----------|-----|
| **T12.14** | Checklist de publicação da wiki (subir via git, nomes de arquivo, ordem de criação para links resolverem). | `docs/WIKI_PUBLISH.md` |
| **T12.15** | Definir **gatilho de manutenção**: ao mudar comportamento/valores no código, atualizar wiki/README no mesmo PR (item no checklist de PR / CI). | Doc não diverge do código com o tempo |

---

## 3. Checklist final de qualidade da documentação

- [ ] Todos os links `[[ ]]` resolvem (nenhum "criar página").
- [ ] Nenhum `[[a|b]]` dentro de tabela.
- [ ] Âncoras funcionam ou foram removidas.
- [ ] Valores (cooldowns, pesos, stacks, recall) == YAML.
- [ ] Contagem de cartas/comandos/permissões == código.
- [ ] Features não implementadas marcadas como 🚧/itálico.
- [ ] Seção de dependências presente no README e na wiki.
- [ ] README, wiki e SDDs consistentes entre si.
- [ ] Formatação e idioma revisados.
- [ ] Processo de manutenção doc↔código definido.

## 4. Definition of Done

- [ ] T12.1–T12.15 com CA satisfeitos.
- [ ] Checklist §3 100% verde.
- [ ] Wiki navegável de ponta a ponta sem link quebrado.
- [ ] Documentação reflete fielmente o estado do código no momento da publicação.
