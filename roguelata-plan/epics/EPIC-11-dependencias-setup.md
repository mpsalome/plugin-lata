# EPIC-11 — Dependências Externas & Setup do Servidor (pendências)

**Dependências:** T11.6 deve ser confirmado antes de fechar EPIC-8 T8.5 (remoção do InvUI)

| Tarefa | Status | Descrição | Arquivos | CA |
|---|---|---|---|---|
| **T11.1** | 🔴 incompleto | `plugin.yml` tem `softdepend: [AuraSkills, AuraMobs, MythicMobs]` — **falta `ModelEngine`**. | `plugin.yml` | `ModelEngine` presente no `softdepend` |
| **T11.3** | 🟡 não validado | Matriz de degradação está documentada, mas não há confirmação de que todas as combinações (nenhum plugin / só AuraSkills / só AuraMobs / todos) foram testadas de fato. | QA manual | Cada combinação testada e documentada como ok |
| **T11.4** | ❌ não existe | `docs/SERVER_SETUP.md` (passo a passo, versões, ordem de instalação) não existe — só o quick-start curto do README. | novo `docs/SERVER_SETUP.md` | Documento existe com tabela de versões/links/ordem |
| **T11.5** | ❌ não existe | `docs/packs/` com exemplos mínimos de config (MythicMobs para o Frostmaw, ModelEngine) não existe. | novo `docs/packs/` | Exemplos existem e funcionam plugados |
| **T11.6** | 🟡 confirmar | InvUI está `provided` sem relocation no shade — parece correto (não é bundled), mas precisa confirmação explícita antes de remover de vez (EPIC-8 T8.5). | `pom.xml` | Decisão documentada: InvUI nunca é shaded, é responsabilidade do servidor instalar (ou é removida de vez) |
| **T11.7** | 🔴 incompleto | `SMOKE_TEST.md` não tem seções por combinação de plugin presente/ausente. | `docs/SMOKE_TEST.md` | 4 seções (Standalone, +AuraSkills, +AuraMobs, +MythicMobs) com itens próprios |
| **T11.8** | ❌ não existe | Nenhum aviso amigável quando uma carta exige um plugin ausente (ex: `mana_pool` sem AuraSkills) — deveria avisar/bloquear no draft. | `core/draft/` | Mensagem clara ao jogador quando uma carta depende de plugin ausente |

## Definition of Done
- [ ] Todas as tarefas acima resolvidas.
