# EPIC-11 — Dependências Externas & Setup do Servidor

**Fase:** F1/F3 (transversal) · **Prioridade:** P1 · **Dependências:** EPIC-0
**Objetivo:** consolidar **todos os plugins externos** que o RogueLata usa, definir o que é **obrigatório vs. opcional (soft-dep)**, como o plugin **degrada com elegância** quando um deles falta, e padronizar `plugin.yml` + documentação de instalação.

> Princípio: **standalone-first e à prova de versão.** O RogueLata sobe e joga **só com o Paper** — draft, reset, mayhem, recall, **dificuldade e bosses/elites (vanilla, ver [EPIC-7](EPIC-7-auramobs-mobs-bosses.md))** funcionam sem nenhum plugin. Toda integração é **opcional** e **risco de versão**: só funciona em 26.2 se o autor do plugin tiver atualizado. Por isso nenhuma feature core depende de plugin de terceiro.

---

## 1. Matriz de dependências

| Plugin | Papel no RogueLata | Tipo | Versão-alvo | Sem ele... |
|--------|--------------------|------|-------------|------------|
| **Paper** | Servidor / API | **Obrigatório** | 1.21.4+ (alvo 26.2 no EPIC-0) | não roda |
| **Java** | Runtime | **Obrigatório** | 21 | não compila/roda |
| **AuraSkills** | Camada 1 (skills nativas), gates, mana abilities, reset-on-death | **Soft-dep** | 2.3.12+ | standalone: sem skills nativas/gates/mana; draft segue normal |
| **AuraMobs** | Escala de dificuldade (nível do mob por raio) | **Soft-dep** | atual p/ AuraSkills | dificuldade cai para depth-only (`DifficultyService`) |
| **MythicMobs** | AI/skills mais ricos para bosses/mobs nomeados | **Soft-dep (extra)** | versão compatível com a do servidor | bosses/elites já funcionam via **API vanilla** (nome + BossBar + fases); MythicMobs só enriquece |
| **ModelEngine** | Modelos 3D dos bosses (imersão sem mod) | **Soft-dep (extra)** | compatível c/ MythicMobs | boss usa a entidade base vanilla (maior via atributo `SCALE`) |
| **InvUI** | GUI legada (`SkillGUI`) | **Em remoção** | 2.2.0 (`provided`) | após [EPIC-8](EPIC-8-menus.md), removida (menus passam a ser vanilla) |

> **InvUI:** hoje está como `provided` no `pom.xml` e **precisa** estar instalada no servidor para o `SkillGUI` legado não quebrar. O [EPIC-8](EPIC-8-menus.md) remove essa dependência (menus vanilla). Até lá: ou instale InvUI no servidor, ou priorize o EPIC-8.

---

## 2. Tarefas

| Tarefa | Descrição | Arquivos | CA |
|--------|-----------|----------|-----|
| **T11.1** | Atualizar `plugin.yml`: `softdepend: [AuraSkills, AuraMobs, MythicMobs, ModelEngine]` | `src/main/resources/plugin.yml` | Load order correto; nenhum hard-depend além do Paper |
| **T11.2** | Detecção robusta de cada soft-dep no `onEnable` (presente + `isEnabled()`), com log claro do modo ativo | `RPGPlugin`, `integration/*Bridge` | Console informa quais integrações ligaram |
| **T11.3** | **Matriz de degradação**: garantir que cada combinação de presença/ausência funciona sem erro | `integration/*`, `core/*` | Tabela §3 validada por smoke test |
| **T11.4** | `docs/SERVER_SETUP.md`: como instalar cada plugin, versões, ordem, e o que cada um habilita | `docs/SERVER_SETUP.md` (novo) | Documento passo a passo com links de download |
| **T11.5** | Packs de config de exemplo para MythicMobs (Frostmaw) e ModelEngine (modelo) | `docs/packs/` (exemplos) | Admin consegue subir o Frostmaw seguindo o doc |
| **T11.6** | Decisão de empacotamento (shade) por dependência: **nada de API de servidor é shaded** (Paper/AuraSkills/AuraMobs/MythicMobs = `provided`); libs próprias, se houver, são relocadas | `pom.xml` | JAR não embute APIs de outros plugins; sem conflito de classes |
| **T11.7** | Ampliar `docs/SMOKE_TEST.md` com cenários por combinação de plugins | `docs/SMOKE_TEST.md` | Checklists para standalone, +AuraSkills, +AuraMobs, +MythicMobs |
| **T11.8** | Mensagem amigável quando uma carta/feature exige um plugin ausente (ex: `mana_pool` sem AuraSkills não entra no pool — já previsto no EPIC-6) | `core/card/*`, `integration/*` | Features dependentes ficam ocultas/avisadas, sem erro |

---

## 3. Matriz de degradação (o que funciona sem cada plugin)

| Recurso | Standalone | +AuraSkills | +AuraMobs | +MythicMobs | +ModelEngine |
|---------|:---------:|:-----------:|:---------:|:-----------:|:------------:|
| Draft / cartas / reset / mayhem | ✅ | ✅ | ✅ | ✅ | ✅ |
| Skills nativas / gates / mana | ❌ | ✅ | — | — | — |
| Dificuldade (scaling de mobs) | ✅ vanilla | ✅ | ✅ melhor | — | — |
| Boss/elites + vitória | ✅ vanilla | ✅ | ✅ | ✅ + AI rica | ✅ + visual |
| Mobs nomeados / mini-boss errante | ✅ vanilla | — | — | ✅ + AI rica | ✅ + visual |
| Modelo 3D do boss | base (`SCALE`) | — | — | base | ✅ |

> "—" = não afeta esse recurso. O importante: **a coluna Standalone já é jogável e completa** — os plugins só melhoram. Nenhuma célula é "crash".

---

## 4. `plugin.yml` (alvo)

```yaml
name: RogueLata
version: '${project.version}'
main: com.project.rpgplugin.RPGPlugin
api-version: '1.21'          # EPIC-0: alvo 26.2
author: Developer
softdepend:
  - AuraSkills
  - AuraMobs
  - MythicMobs
  - ModelEngine
commands: { skills: {...}, rpg: {...}, run: {...}, recall: {...} }
permissions: { rpg.admin: { default: op } }
```

## 5. Definition of Done

- [ ] T11.1–T11.8 com CA satisfeitos.
- [ ] `plugin.yml` com todos os soft-deps; nenhum hard-depend além do Paper.
- [ ] Matriz de degradação (§3) validada — zero crash em qualquer combinação.
- [ ] `docs/SERVER_SETUP.md` e `SMOKE_TEST.md` cobrindo as combinações.
- [ ] JAR não embute APIs de plugins externos.
