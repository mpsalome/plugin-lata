package com.project.rpgplugin.core.skill;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public abstract class AbstractSkill implements Skill {

    private static final Map<String, List<String>> EFFECT_DESCRIPTIONS = Map.ofEntries(
        // Explorer
        Map.entry("dash", List.of("<white>Speed + Invisibilidade por 10s")),
        Map.entry("step_assist", List.of("<white>Speed II por 15s")),
        Map.entry("grapple", List.of("<white>Lanca o jogador para frente")),
        Map.entry("hydration", List.of("<white>+1 comida, +0.5 saturacao")),
        Map.entry("water_breathing", List.of("<white>Respiracao Aquatica III por 15s")),
        Map.entry("jump_boost", List.of("<white>Salto Elevado II (passivo enquanto se move)")),
        Map.entry("safe_fall", List.of("<white>Queda lenta + 50% reducao de dano de queda (passivo)")),
        Map.entry("thermal_resistance", List.of("<white>Resistencia ao Fogo por 15s")),
        Map.entry("recall", List.of("<white>Teleporta ao spawn do mundo")),
        Map.entry("sonar", List.of("<white>Revela entidades vivas num raio de 15 blocos")),
        Map.entry("wind_burst", List.of("<white>Lanca o jogador para o alto")),
        Map.entry("dim_shift", List.of("<white>Teleporta 8 blocos para frente", "<white>Speed IV + Invisibilidade 10s", "<red>Blindness + Fome 5s (penalidade)")),
        // Miner
        Map.entry("diet", List.of("<white>+4 comida, +2 saturacao")),
        Map.entry("stone_smash", List.of("<white>Pressa I por 2s ao quebrar pedra")),
        Map.entry("torch_light", List.of("<white>Visao Noturna por 30s")),
        Map.entry("ore_sonar", List.of("<white>Revela minerios (Ferro/Ouro/Diamante) num raio de 8 blocos")),
        Map.entry("haste", List.of("<white>Pressa II por 15s")),
        Map.entry("ore_repair", List.of("<white>Repara 30% da durabilidade da picareta")),
        Map.entry("molten_touch", List.of("<white>Auto-funde minerios de Ferro/Ouro/Cobre por 30s")),
        Map.entry("transmutation", List.of("<white>5 Ferro -> 1 Ouro, ou 5 Ouro -> 1 Diamante")),
        Map.entry("gravity_shield", List.of("<white>Resistencia III por 15s")),
        Map.entry("core_overdrive", List.of("<green>Pressa III + Forca II por 20s", "<red>Lentidao + Fome 15s (penalidade)")),
        Map.entry("sight", List.of("<white>Visao Noturna permanente (passivo enquanto se move)")),
        // Builder
        Map.entry("feast", List.of("<white>+2 comida, +0.8 saturacao")),
        Map.entry("woodcutter", List.of("<white>Pressa I por 10s")),
        Map.entry("fertilize", List.of("<white>Particulas decorativas (efeito visual)")),
        Map.entry("flora_shield", List.of("<white>Cura 4 coracoes")),
        Map.entry("lumberjack", List.of("<white>Pressa IV por 5s")),
        Map.entry("silk_touch", List.of("<white>Toque de Seda sem encantamento")),
        Map.entry("scaffold", List.of("<white>Lanca para cima + coloca feno temporario")),
        Map.entry("architect_focus", List.of("<green>Resistencia IV por 30s", "<red>Lentidao + Fraqueza por 30s (penalidade)")),
        Map.entry("unbreakable_block", List.of("<white>Torna o bloco visado inquebravel por 15s")),
        Map.entry("grace", List.of("<white>Salto Elevado III + Queda Lenta por 10s")),
        Map.entry("gravity_defiance", List.of("<white>Levitacao por 5s + Queda Lenta por 10s")),
        Map.entry("canopy_step", List.of("<white>Velocidade II ao andar sobre folhas ou grama (passivo)"))
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
