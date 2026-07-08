package com.project.rpgplugin.core.skill;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public abstract class AbstractSkill implements Skill {

    private static final Map<String, List<String>> EFFECT_DESCRIPTIONS = Map.ofEntries(
        // Explorer
        Map.entry("dash", List.of("<white>Dash 6 blocos + Speed II 3s (Agache + Pulo no ar)")),
        Map.entry("grapple", List.of("<white>Lanca o jogador para frente")),
        Map.entry("hydration", List.of("<white>Ao tocar agua/chuva: +1 comida, +0.5 saturacao, Speed I")),
        Map.entry("water_breathing", List.of("<white>Respiracao subaquatica + Golfinho (passivo na agua)")),
        Map.entry("jump_boost", List.of("<white>Salto Elevado II (passivo enquanto se move)")),
        Map.entry("safe_fall", List.of("<white>Queda lenta + 50% reducao de dano de queda (passivo)")),
        Map.entry("thermal_resistance", List.of("<white>Ao sofrer dano de fogo/lava: Fire Resist 8s (CD 30s)")),
        Map.entry("recall", List.of("<white>Teleporta ao spawn do mundo")),
        Map.entry("sonar", List.of("<white>Revela entidades num raio de 20b com Glowing 10s (Agache + clic vazio)")),
        Map.entry("wind_burst", List.of("<white>Lanca para cima + knockback em area (Agache + clique esquerdo)")),
        Map.entry("dim_shift", List.of("<white>Teleporta 8b para frente (Ender Pearl / Mana 15)", "<white>Speed IV + Invisibilidade 10s", "<red>Blindness + Fome 5s")),
        Map.entry("harpoon_pull", List.of("<white>Fisgue e puxe um inimigo com a Vara de Pesca (dano + stun)")),
        Map.entry("blade_dance", List.of("<white>Apos 3 mobility skills: proximo hit causa AOE + lifesteal 5%")),
        // Miner
        Map.entry("diet", List.of("<white>+4 comida, +2 saturacao")),
        Map.entry("stone_smash", List.of("<white>Quebrar pedra da Haste (acumula ate III com momentum de 4s)")),
        Map.entry("torch_light", List.of("<white>Visao Noturna por 30s")),
        Map.entry("ore_sonar", List.of("<white>Revela minerios (Ferro/Ouro/Diamante) num raio de 8 blocos")),
        Map.entry("haste", List.of("<white>Ao quebrar minerios: Haste II + Speed I 10s (CD 20s)")),
        Map.entry("ore_repair", List.of("<white>Ao quebrar Fe/Au/Dia: repara 5% durabilidade dos equipados (passivo)")),
        Map.entry("transmutation", List.of("<white>5 Ferro -> 1 Ouro, ou 5 Ouro -> 1 Diamante")),
        Map.entry("gravity_shield", List.of("<white>Ao sofrer >4 coracoes: Resist III + anti-kb 6s (CD 30s)")),
        Map.entry("core_overdrive", List.of("<green>Haste III + Strength II + Speed II 15s", "<red>+25% dano recebido (tag RISK)")),
        Map.entry("sight", List.of("<white>Visao Noturna permanente (passivo enquanto se move)")),
        Map.entry("seismic_slam", List.of("<white>Onda de choque 8b: dano AOE + Slow II 3s (Picareta + Agache / 30 Mana)")),
        Map.entry("gold_rush_shield", List.of("<white>Ao sofrer dano fatal: absorve consumindo ouro do inventario")),
        Map.entry("unstable_core", List.of("<white>15% chance de explodir ao quebrar minerio: dano AOE + drop extra")),
        // Builder
        Map.entry("feast", List.of("<white>+2 comida, +0.8 saturacao")),
        Map.entry("woodcutter", List.of("<white>Treecapitator: quebra todos os troncos conectados")),
        Map.entry("flora_shield", List.of("<white>Cura 4 coracoes")),
        Map.entry("silk_touch", List.of("<white>Toque de Seda sem encantamento")),
        Map.entry("scaffold", List.of("<white>Lanca para cima + coloca feno temporario")),
        Map.entry("architect_focus", List.of("<green>Resistencia III 10s (colocar 8 blocos em <5s)")),
        Map.entry("unbreakable_block", List.of("<white>Agache + colocar bloco: barreira 3x3 de obsidiana 10s")),
        Map.entry("gravity_defiance", List.of("<white>Ao sofrer KB/dano de queda: anula e da voo temporario 4s")),
        Map.entry("canopy_step", List.of("<white>Speed II ao andar sobre blocos naturais (passivo)")),
        Map.entry("sentry_turret", List.of("<white>Invoca torreta de flechas por 15s (abobora / 50 Mana)")),
        Map.entry("entangling_roots", List.of("<white>Raizes prendem inimigos num raio de 4b (Slowness IV + dano)")),
        Map.entry("fortress_protocol", List.of("<white>Quando HP <30%: gaiola de ferro + regen + empurra inimigos"))
    );

    protected final SkillServices services;

    protected AbstractSkill(SkillServices services) {
        this.services = services;
    }

    @Override
    public List<String> effectDescription() {
        return EFFECT_DESCRIPTIONS.getOrDefault(id(), List.of());
    }

    protected boolean onCooldown(SkillContext ctx) {
        return services.isOnCooldown(ctx.player().getUniqueId(), id());
    }

    protected long cooldownRemaining(SkillContext ctx) {
        return services.cooldownRemaining(ctx.player().getUniqueId(), id());
    }

    protected void startCooldown(SkillContext ctx) {
        services.startCooldown(ctx.player().getUniqueId(), id(), cooldown());
    }

    protected void consume(SkillContext ctx, int amount) {
        if (ctx.usedItem() != null) {
            ctx.usedItem().subtract(amount);
        }
    }

    protected void feedback(SkillContext ctx, String message, Sound sound) {
        ctx.player().sendActionBar(net.kyori.adventure.text.Component.text(message));
        if (sound != null) {
            ctx.player().playSound(ctx.player().getLocation(), sound, 1.0f, 1.0f);
        }
    }

    protected ConfigurationSection cfg() {
        return services.skillConfig(id());
    }

    public SkillServices services() {
        return services;
    }
}
