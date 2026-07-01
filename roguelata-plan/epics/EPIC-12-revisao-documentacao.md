# EPIC-12 — Revisão de Documentação (Wiki, README & SDDs)

**Fase:** F5 (último) · **Prioridade:** P2 · **Dependências:** todos os épicos de feature
**Objetivo:** reestruturar toda a documentação — wiki em `docs/wiki/`, README conciso e SDDs sincronizados — garantindo que links funcionem, informações batam com o código, formatação seja consistente e o status (implementado vs. design-alvo) seja honesto.

---

## 1. Escopo da auditoria

- **Wiki** (`docs/wiki/`): index, gameplay, commands, cards, integrations, config, developers
- **README** (raiz do repo) + `docs/` (SMOKE_TEST)
- **SDDs** (`roguelata-plan/`): master, épicos, apêndices

---

## 2. Tarefas

### 2.1 Estrutura

| Tarefa | Descrição | CA |
|--------|-----------|-----|
| **T12.1** | Criar `docs/wiki/` com páginas markdown padronizadas (index, gameplay, commands, cards, integrations, config, developers). | Todas as páginas existem e são navegáveis |
| **T12.2** | Garantir que **todo link relativo** entre páginas wiki resolva (nomes com hífen, sem acento). | Zero link quebrado |
| **T12.3** | `README.md` conciso (~100 linhas): visão geral, instalação, comandos essenciais, link para `docs/wiki/`. | README <= 120 linhas |
| **T12.4** | Remover catálogo de cartas do README (agora em `docs/wiki/cards.md`). | README não duplica wiki |

### 2.2 Acurácia (doc × código)

| Tarefa | Descrição | CA |
|--------|-----------|-----|
| **T12.5** | Marcar como 🚧/itálico features **não implementadas**. Sincronizar com a tabela de status do Master SDD. | Player não é prometido o que não existe |
| **T12.6** | Conferir **valores numéricos** da wiki contra os YAML reais (`skills.yml`, `draft.yml`, `augments.yml`): cooldowns, pesos do draft, stacks, fórmula do recall. | Valores da wiki == config do código |
| **T12.7** | Conferir **contagem de cartas** (35 habilidades + 53 augments = 88) contra `CardRegistry.size()`. | Números batem |
| **T12.8** | Conferir **comandos/permissões** (`/skills`, `/run`, `/recall`, `/rpg ...`) contra `plugin.yml`. | Tabela de comandos == plugin.yml |
| **T12.9** | Nomes de cartas na doc == chaves de i18n (`messages_*.yml`, EPIC-10) — sem `id.replace("_"," ")`. | Doc e jogo usam os mesmos nomes |

### 2.3 Consistência & formatação

| Tarefa | Descrição | CA |
|--------|-----------|-----|
| **T12.10** | Consistência **README ↔ wiki ↔ SDD** (versão, plataforma, lista de dependências, comandos, design). | As três fontes não se contradizem |
| **T12.11** | Formatação: tabelas alinhadas, headings hierárquicos, blocos de código com linguagem, emojis padronizados, sem linhas órfãs. | Render limpo no GitHub |
| **T12.12** | Revisão de idioma (PT-BR), ortografia e consistência de termos (usar o Glossário). | Termos uniformes |
| **T12.13** | Seção de dependências presente e correta no README e na wiki. | Dependências documentadas |

### 2.4 Processo

| Tarefa | Descrição | CA |
|--------|-----------|-----|
| **T12.14** | Atualizar `00-MASTER-SDD.md` com status de implementação real (pós EPIC-0 a EPIC-11). | SDD reflete o código |
| **T12.15** | Definir gatilho de manutenção: ao mudar comportamento/valores no código, atualizar wiki/README no mesmo PR. | Doc não diverge do código |

---

## 3. Checklist final de qualidade da documentação

- [ ] README <= 120 linhas, conciso, aponta para wiki
- [ ] `docs/wiki/` com todas as páginas criadas
- [ ] Nenhum link relativo quebrado entre páginas wiki
- [ ] Valores (cooldowns, pesos, stacks, recall) == YAML
- [ ] Contagem de cartas/comandos/permissões == código
- [ ] Features não implementadas marcadas como 🚧/itálico
- [ ] Seção de dependências presente no README e na wiki
- [ ] README, wiki e SDDs consistentes entre si
- [ ] Master SDD com status de implementação atualizado
- [ ] Formatação e idioma revisados

## 4. Definition of Done

- [ ] T12.1–T12.15 com CA satisfeitos
- [ ] Checklist §3 100% verde
- [ ] Documentação reflete fielmente o estado do código
- [ ] `mvn clean package` verde, testes verdes
- [ ] Commit e push para main
