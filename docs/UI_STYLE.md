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

- Draft menu: 27 slots, cards at slots 11/14/17, reroll at 22, skip at 26
- Collection menu: 54 slots, cards from slot 10, summary at 4, mayhem at 49
- Fill empty slots with `GRAY_STAINED_GLASS_PANE` (no display name)
