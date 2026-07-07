# Server Setup — RogueLata

## Requirements

| Software | Version | Required |
|----------|---------|----------|
| Paper | 1.21.4+ | Yes |
| Java | 21+ | Yes |

## Optional Plugins (soft-depend)

| Plugin | Version | Purpose |
|--------|---------|---------|
| AuraSkills | 2.3.x | Custom skills, mana system, draft bias |
| AuraMobs | 1.x | Enhanced mob scaling |
| MythicMobs | 5.x | Custom boss models & behaviors |
| ModelEngine | 4.x | 3D model rendering for bosses |

No plugin is required. RogueLata runs full-featured in standalone mode.
When an optional plugin is absent, features depending on it are gracefully disabled.

## Installation Order

1. Install Paper 1.21.4+ and start once to generate folders
2. Stop the server
3. Place `RogueLata-<version>.jar` in `plugins/`
4. (Optional) Install AuraSkills, AuraMobs, MythicMobs, ModelEngine
5. Start the server
6. Check console for `RogueLata loaded X cards, Y modifiers`
7. Join the game and use `/skills` to start your first run

## Matrix — Feature Availability

| Feature | Standalone | +AuraSkills | +AuraMobs | +MythicMobs |
|---------|-----------|-------------|-----------|-------------|
| Draft (1-de-3) | ✅ | ✅ | ✅ | ✅ |
| 35 abilities | ✅ | ✅ (also as AuraSkills custom skills) | ✅ | ✅ |
| 53 augments | ✅ | ✅ | ✅ | ✅ |
| Mayhem modifiers | ✅ | ✅ | ✅ | ✅ |
| Reset on death | ✅ | ✅ (also resets AuraSkills XP) | ✅ | ✅ |
| Recall | ✅ | ✅ | ✅ | ✅ |
| Vanilla elites/bosses | ✅ | ✅ | ✅ | ✅ |
| Difficulty scaling | ✅ (depth + players) | ✅ | ✅ (enhanced) | ✅ |
| Mana system | ❌ | ✅ | ✅ | ✅ |
| Draft class bias | ❌ | ✅ | ✅ | ✅ |
| MythicMobs boss models | — | — | — | ✅ |
| ModelEngine 3D skins | — | — | — | ✅ (if ModelEngine present) |

## Config Files

Generated on first run under `plugins/RogueLata/`:

- `config.yml` — General settings
- `draft.yml` — Draft weights, reroll costs
- `augments.yml` — Augment card definitions
- `gates.yml` — AuraSkills level gates
- `mayhem.yml` — Mayhem thresholds & config
- `bosses.yml` — Boss definitions
- `mobs.yml` — Elite mob definitions
- `mana_abilities.yml` — Mana costs per ability
- `runs/<uuid>.yml` — Per-player run persistence
