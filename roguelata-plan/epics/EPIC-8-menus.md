# EPIC-8 — Menus & GUI (draft menu como estrela)

**Fase:** F4 · **Prioridade:** P1 · **Dependências:** EPIC-2
**Objetivo:** menus **usáveis, bonitos e à prova de versão** (a dor explícita do grupo: *"plugins de menu não atualizaram pra 26.2"*). Padronizar a estratégia de GUI, criar a tela de coleção/build da run e aposentar o menu legado.

---

## 1. Estado atual no código (auditado) — e a inconsistência

- **`DraftMenu`** (EPIC-2) usa **inventário vanilla** (`Bukkit.createInventory(null, 27, ...)` + `player.openInventory`), 3 cartas nos slots 11/14/17, reroll no 22, skip no 26, borda de vidro cinza. Cliques tratados por `DraftMenuListener` (`InventoryClickEvent`). **Não usa InvUI.**
- **`SkillGUI` legado** usa **InvUI** (TabGui/PagedGui) e é aberto por `/skills` (via `classListeners.openSelectionGUI`).
- No `pom.xml`, **InvUI virou `provided`** e o **shade não a empacota mais** → se o `SkillGUI` rodar sem InvUI instalado no servidor, **quebra**.

**Conclusão:** o caminho do draft já provou que **inventário vanilla resolve** — e é exatamente o que sobrevive a qualquer versão (inclusive 26.2), sem depender de lib de menu. O caminho legado é o problemático.

---

## 2. Decisão de estratégia de GUI

**Padronizar em inventário vanilla** com um mini-framework próprio (à prova de versão, **zero dependência externa de menu**), e **aposentar InvUI + `SkillGUI`**. Camadas:

1. **Framework vanilla próprio** (`ui/menu/`): `Menu` (abstrato) + `MenuHolder` (InventoryHolder marcador) + **um** `MenuListener` que roteia cliques por holder. Fim do acoplamento a InvUI.
2. **Menus nativos do AuraSkills** (EPIC-6) para skills/mana — já bonitos e mantidos.
3. **(Opcional, premium)** texturas custom via **fonte de espaço negativo** + resource pack (o "vidrinho preto"/showcase que o Hugo citou) — depende do pack do EPIC-7.

> Se no futuro quiserem InvUI de novo, manter atrás de uma interface `GuiProvider` para troca fácil. Mas o default passa a ser vanilla.

---

## 3. Tarefas

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T8.1** | Mini-framework `ui/menu/` (`Menu`, `MenuHolder`, `MenuListener` único) baseado em inventário vanilla | `ui/menu/*` | Abrir/rotear cliques sem InvUI; um listener para todos os menus |
| **T8.2** | Migrar `DraftMenu` para o framework (`MenuHolder` em vez de inventário "solto") | `ui/DraftMenu.java`, `ui/DraftMenuListener.java` | Draft continua idêntico, mas roteado pelo `MenuListener` (sem comparar título de inventário) |
| **T8.3** | **`CollectionMenu`**: visão da run (cartas possuídas + stacks, multiplicadores, sinergias, mayhem ativos, dificuldade) — substitui o catálogo do `SkillGUI` | `ui/CollectionMenu.java` | `/skills` e o Livro de RPG abrem o CollectionMenu |
| **T8.4** | Repointar `/skills` e o Livro de RPG para `CollectionMenu`; **remover** `SkillGUI` e `classListeners.openSelectionGUI` | `RPGPlugin`, `SkillDispatchListener` | `SkillGUI.java` deletado; nada abre InvUI |
| **T8.5** | Remover dependência **InvUI** do `pom.xml` (após T8.4) | `pom.xml` | Build sem InvUI; servidor não precisa instalá-la |
| **T8.6** | Polir `DraftMenu`: cor/gradiente por tier (já existe), descrição i18n real da carta (hoje usa `id.replace("_"," ")`), som por raridade ao abrir/escolher | `ui/DraftMenu.java`, `MessagesConfig` (EPIC-10) | Cartas mostram nome/descrição i18n; som por tier |
| **T8.7** | `HudService`: action bar + boss bar com estado da run (cartas, recall `1340/2000`, mayhem ativo) | `ui/HudService.java`, `task/` | HUD mostra progresso do recall (EPIC-5) e modificadores (EPIC-4) |
| **T8.8** | (Opcional) protótipo de 1 tela com resource pack negative-space | `resourcepack/`, `docs/UI_RESOURCEPACK.md` | Demo com textura custom + fallback |
| **T8.9** | Guia de estilo (`docs/UI_STYLE.md`): cores por tier/tag/raridade, ícones, padrões de navegação, slot de filler | `docs/UI_STYLE.md` | Demais telas seguem o guia |

---

## 4. `CollectionMenu` — conteúdo

Substitui o antigo catálogo "comprar/equipar". Mostra o que a run **é** (não há mais compra):

- **Cartas possuídas** agrupadas por tier/tag, com stacks (`run.cardCount(id)`).
- **Multiplicadores ativos** (`run.multipliers()` — ex: xp_gain, cooldown_reduction, crit_chance).
- **Sinergias** (EPIC-10) por tag (ex: "Minerador 4/6").
- **Modificadores Mayhem ativos** (`run.activeModifiers()`).
- **Recall** (progresso/uso) e **dificuldade** atual.
- Item informativo central (ex: `KNOWLEDGE_BOOK`) com resumo da run.

---

## 5. Edge cases

- **Roteamento de clique:** usar `InventoryHolder` (MenuHolder) em vez de comparar **título** do inventário (frágil, quebra com i18n). Refatorar `DraftMenuListener` para isso.
- **Inventário aberto durante reset/morte:** fechar menus abertos no `fullReset`.
- **Slots de borda:** padronizar filler (vidro) para "blocked area" (estética que o grupo gostou).
- **Stack/itens fantasma:** `setCancelled(true)` em todo clique de menu (não deixar pegar item).

## 6. Definition of Done

- [ ] T8.1–T8.9 (T8.8 opcional) com CA satisfeitos.
- [ ] Zero dependência de InvUI; `SkillGUI` removido.
- [ ] `DraftMenu` e `CollectionMenu` no framework vanilla, roteados por holder.
- [ ] HUD mostra recall e mayhem.
- [ ] Build e smoke test verdes num servidor **sem** InvUI instalada.
