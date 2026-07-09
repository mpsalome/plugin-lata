# UI Style Guide — RogueLata Menus

## Color Palette

| Element | MiniMessage | Usage |
|---------|------------|-------|
| Title | `<gold><bold>` | Menu titles |
| Tier Bronze | `<gradient:#CD7F32:#B8860B>` | Bronze card display |
| Tier Silver | `<gradient:#C0C0C0:#E8E8E8>` | Silver card display |
| Tier Gold | `<gradient:#FFD700:#FFA500>` | Gold card display |
| Info | `<gray>` | Secondary text |
| Active | `<green>` | Active/toggle on |
| Disabled | `<red>` | Inactive/danger |
| Mayhem | `<red><bold>` | Mayhem modifiers |
| Recall | `<light_purple>` | Recall status |

## Icons

| Element | Material |
|---------|----------|
| RPG Book | `BREAD` |
| Draft card slot | per card `icon` field |
| Reroll | `ENDER_EYE` |
| Skip | `BARRIER` |
| Border/filler | `GRAY_STAINED_GLASS_PANE` |
| Card (active) | `ENCHANTED_BOOK` |
| Card (disabled) | `BOOK` |
| Run summary | `KNOWLEDGE_BOOK` |
| Mayhem indicator | `TOTEM_OF_UNDYING` |

## Sound

| Event | Sound |
|-------|-------|
| Choose Bronze card | `BLOCK_NOTE_BLOCK_HAT` |
| Choose Silver card | `BLOCK_NOTE_BLOCK_PLING` |
| Choose Gold card | `UI_TOAST_CHALLENGE_COMPLETE` |

## Menu Patterns

- All menus fill borders with `BLACK_STAINED_GLASS_PANE` (no display name)

### DraftMenu
- Size: 54 slots
- Cards at slots 20, 23, 26, 29
- Reroll at slot 40
- Skip at slot 44

### CollectionMenu
- Size: 54 slots
- Cards start at slot 10 (with pagination, 36 cards/page)
- Category filter toggles at slots 3-6
- "Menu Principal" button at slot 49
- Alphabetical sort

### ShopMenu
- Size: 27 slots
- Items at slots 10, 12, 14, 16, 18
- Items:
  - Carta Avulsa = `MAP`
  - Reroll = `ENDER_EYE`
  - Absolvição = `TOTEM_OF_UNDYING`
  - Sinalizador = `BEACON`
  - Beque = `HEART_OF_THE_SEA`

### HubMenu
- Size: 27 slots
- Navigation items at slots 11 (Coleção), 13 (Loja), 15 (Draft)
- "Fechar" button at slot 22
- Fill borders with `BLACK_STAINED_GLASS_PANE`
