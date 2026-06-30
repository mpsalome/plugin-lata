# EPIC-0 — Migração para Paper 26.2 e saneamento mínimo

**Fase:** F1 (Fundação) · **Prioridade:** P0 (bloqueia tudo) · **Dependências:** nenhuma
**Objetivo:** o plugin compila e roda em **Paper 26.2 / Java 21** com **comportamento idêntico** ao atual. Nada de feature nova aqui — só destravar a plataforma e trocar APIs removidas.

---

## Contexto

- `pom.xml` fixa `paper-api 1.21-R0.1-SNAPSHOT` e `plugin.yml` declara `api-version: '1.21'`.
- A conversa estabelece que o servidor-alvo é **26.2** e que "plugins bons de menu ainda não atualizaram pra 26.2".
- APIs usadas hoje que mudaram/saíram nas versões recentes:
  - `org.bukkit.ChatColor` → legado; usar **Adventure** (`NamedTextColor`/MiniMessage).
  - `ItemMeta#getDisplayName()`/`setDisplayName(String)` → usar `Component` (`displayName(Component)`).
  - `Attribute.GENERIC_MAX_HEALTH` → renomeado para `Attribute.MAX_HEALTH`.
  - `PotionEffectType.SLOWNESS`/`HASTE`/etc → conferir nomes vigentes em 26.2 (alguns viraram registry keys).
  - `Particle.EXPLOSION`/`Particle.GLOW`/`Particle.HEART` → conferir nomes no enum/registry 26.2.

## Escopo

**Inclui:** bump de dependências, troca de APIs removidas, alinhamento de versão InvUI, padronização de versão/contagem de skills, smoke test documentado.
**Não inclui:** refatoração de arquitetura (EPIC-1), qualquer feature.

---

## Tarefas

### T0.1 — Bump de plataforma (Paper 26.2 + Java 21)

**Arquivos:** `pom.xml`, `src/main/resources/plugin.yml`

1. Descobrir o coordinate correto do `paper-api` para 26.2 (verificar `https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/`). Atualizar a versão.
2. Em `plugin.yml`, `api-version: '1.21'` → versão correspondente a 26.2 (Paper expõe `api-version` por release; usar o valor publicado para 26.2).
3. Confirmar `maven.compiler.release` = 21.

```xml
<!-- pom.xml (trecho) -->
<properties>
    <java.version>21</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <paper.version>26.2-R0.1-SNAPSHOT</paper.version> <!-- VERIFICAR coordinate real -->
</properties>
...
<dependency>
    <groupId>io.papermc.paper</groupId>
    <artifactId>paper-api</artifactId>
    <version>${paper.version}</version>
    <scope>provided</scope>
</dependency>
```

**CA:** `mvn dependency:resolve` baixa `paper-api` de 26.2; `mvn clean package` compila.
**Risco:** se o coordinate de 26.2 não existir no repo, **parar e reportar** — não inventar versão.

---

### T0.2 — Alinhar InvUI à versão compatível com 26.2

**Arquivos:** `pom.xml`

InvUI v2 não é mais multi-versão (cada release casa com versões específicas do MC). Passos:

1. Consultar a tabela de compatibilidade do InvUI (README do repositório / docs) e escolher a versão que suporta 26.2.
2. Atualizar `<version>` do artefato `xyz.xenondevs.invui:invui`.
3. Conferir se o **classifier**/módulo correto está sendo usado (InvUI v2 separa `invui` e módulos de inventário; alguns setups exigem `invui-core` + `inventoryaccess`).
4. Garantir que o `maven-shade-plugin` relocaliza o pacote da InvUI para evitar conflito com outros plugins:

```xml
<configuration>
  <relocations>
    <relocation>
      <pattern>xyz.xenondevs.invui</pattern>
      <shadedPattern>com.project.rpgplugin.libs.invui</shadedPattern>
    </relocation>
  </relocations>
  <filters> ... (manter filtros de MANIFEST/SF/DSA/RSA atuais) ... </filters>
</configuration>
```

**CA:** servidor 26.2 sobe; `/skills` abre a GUI sem `NoClassDefFoundError`/erro de NMS. Se não houver build de InvUI para 26.2, **acionar o plano B do [EPIC-6](EPIC-6-menus.md) (abstração `GuiProvider`)** e registrar a decisão.

---

### T0.3 — Trocar APIs deprecadas/removidas por Adventure + nomes vigentes

**Arquivos:** `RPGPlugin.java`, `ClassListeners.java`, `PlayerManager.java`, `SkillGUI.java`

Mapa de substituições obrigatórias:

| De (atual) | Para (26.2) |
|------------|-------------|
| `import org.bukkit.ChatColor;` + `ChatColor.RED + "..."` | `Component.text("...").color(NamedTextColor.RED)` ou MiniMessage |
| `meta.setDisplayName("§6...")` | `meta.displayName(mm("<gold>..."))` (MiniMessage helper) |
| `meta.getDisplayName().contains("Livro de RPG")` | comparar via **PersistentDataContainer tag** no item (ver nota abaixo) |
| `meta.setLore(Arrays.asList(...))` (String) | `meta.lore(List<Component>)` |
| `Attribute.GENERIC_MAX_HEALTH` | `Attribute.MAX_HEALTH` |
| `player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()` | `player.getAttribute(Attribute.MAX_HEALTH).getValue()` |
| `player.sendMessage(ChatColor...)` | `player.sendMessage(Component)` |

**Nota crítica — identificação do Livro de RPG:** hoje o livro é reconhecido por `getDisplayName().contains("Livro de RPG")`. Isso é frágil (i18n, renomeio). Trocar por uma **tag no PDC do item**:

```java
// util/ItemKeys.java
public final class ItemKeys {
    public static final NamespacedKey RPG_BOOK = new NamespacedKey("roguelata", "rpg_book");
    public static boolean isRpgBook(ItemStack is) {
        if (is == null || is.getType() != Material.BOOK || !is.hasItemMeta()) return false;
        return is.getItemMeta().getPersistentDataContainer()
                 .has(RPG_BOOK, PersistentDataType.BYTE);
    }
}
```
Marcar o livro com essa tag na criação e usar `ItemKeys.isRpgBook(...)` em todos os checks (interact, drop, join, respawn).

**Helper MiniMessage** (centralizar para reuso):
```java
// util/Text.java
public final class Text {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    public static Component mm(String s) { return MM.deserialize(s).decoration(TextDecoration.ITALIC, false); }
}
```

**CA:**
- Build sem warnings de remoção (apenas deprecações benignas, se houver).
- Servidor 26.2 sobe sem `NoSuchFieldError`/`NoSuchMethodError`.
- Livro reconhecido pela tag PDC (renomear o item no inventário não quebra o reconhecimento).

---

### T0.4 — Padronizar versão e contagem de skills

**Arquivos:** `pom.xml`, `README.md`

Inconsistências atuais: pom = `1.2.0`, README fala "1.1" e "35 habilidades"; o código tem 35 chaves em `getAllSkillKeys()` mas o README tabela diz 33.

1. Definir a contagem **canônica** = `SkillRegistry.size()` (após EPIC-1) ou, por ora, `playerManager.getAllSkillKeys().size()`.
2. Ajustar README para refletir o número real (contar: 11 bronze + 11 prata + 13 ouro = **35**, validar).
3. Escolher versão semântica de saída do EPIC-0 (sugerido `1.3.0` por mudança de plataforma).

**CA:** README, `pom.xml` e código concordam na contagem e na versão.

---

### T0.5 — Smoke test documentado

**Arquivos:** `docs/SMOKE_TEST.md` (novo)

Criar checklist reproduzível num servidor Paper 26.2 local:

```markdown
# Smoke Test — RogueLata (Paper 26.2)
- [ ] Servidor sobe sem erro no console (modo standalone, sem AuraSkills)
- [ ] Ao entrar, jogador recebe o "Livro de RPG" no slot 8
- [ ] /skills abre a GUI; abas Bronze/Prata/Ouro trocam o conteúdo
- [ ] Desbloquear uma skill consome XP corretamente
- [ ] Equipar/desequipar respeita limite de 3 por tier e 9 total
- [ ] Ativar uma skill (ex: dash com flor) aplica efeito e cooldown
- [ ] Morrer reseta skills/XP e teleporta ao spawn; livro permanece
- [ ] Tentar dropar o livro é bloqueado
- [ ] /rpg reload recarrega config sem erro
- [ ] (com AuraSkills) skills roguelata/ aparecem e gates funcionam
```

**CA:** checklist executado e todos os itens passam contra o JAR migrado.

---

## Edge cases / gotchas

- **Folia:** se o alvo for Folia (forks regionais do Paper), `Bukkit.getScheduler().runTaskLater` precisa virar scheduler regional. Verificar e, se for o caso, abrir tarefa no EPIC-9.
- **Shade + relocation:** sem relocation da InvUI, dois plugins com InvUI diferente quebram. Sempre relocar.
- **Registry de Particle/Sound/PotionEffectType:** em versões recentes esses enums podem ter virado `Registry`. Se algum nome usado não existir, mapear para o equivalente vigente (não remover o efeito).

## Definition of Done

- [ ] T0.1–T0.5 com CA satisfeitos.
- [ ] `mvn clean package` verde contra paper-api 26.2.
- [ ] Smoke test 100% verde.
- [ ] Comportamento idêntico ao 1.2.0 (nenhuma feature alterada).
- [ ] Versão de saída e contagem de skills padronizadas.
