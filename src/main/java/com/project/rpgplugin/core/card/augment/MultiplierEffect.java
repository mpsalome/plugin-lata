package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;

import java.util.List;

public record MultiplierEffect(String key, double addPerStack) implements AugmentEffect {
    @Override
    public void apply(Player p, RunState run, int stacks) {
        run.addMultiplier(key, addPerStack);
    }

    @Override
    public void unapply(Player p, RunState run, int stacks) {
        run.removeMultiplier(key, addPerStack);
    }

    @Override
    public List<String> description() {
        int pct = (int) (addPerStack * 100);
        String sign = pct >= 0 ? "+" : "";
        return List.of("<gray>" + displayKey() + ": <white>" + sign + pct + "% por pilha");
    }

    private String displayKey() {
        return switch (key) {
            case "xp_gain" -> "Ganho de XP";
            case "cooldown_reduction" -> "Reducao de Cooldown";
            case "fall_damage_reduction" -> "Reducao de Dano Queda";
            case "hunger_decay_reduction" -> "Reducao de Fome";
            case "extra_mob_drops" -> "Drops Extras de Mobs";
            case "mining_speed" -> "Velocidade de Mineracao";
            case "crop_yield" -> "Colheita";
            case "early_combat_damage" -> "Dano no Inicio do Combate";
            case "blind_on_hit_chance" -> "Chance de Cegueira";
            case "low_hp_damage" -> "Dano com Vida Baixa";
            case "thorns_reflect" -> "Reflexao de Dano";
            case "double_jump" -> "Pulo Duplo";
            case "crit_chance" -> "Chance Critica";
            case "double_ore_drop" -> "Drop Duplo de Minerio";
            case "item_magnet_range" -> "Alcance do Imã";
            case "dash_charge" -> "Carga de Dash";
            case "auto_smelt" -> "Auto-Fundicao";
            case "mana_max" -> "Mana Maxima";
            case "second_wind" -> "Segundo Vento";
            case "damage_reduction" -> "Reducao de Dano";
            case "frost_touch" -> "Toque Congelante";
            case "vampiric_night" -> "Noite Vampirica";
            case "adrenaline" -> "Adrenalina";
            case "execute_threshold" -> "Limiar de Execucao";
            case "overcharge" -> "Sobrecarga";
            case "phoenix" -> "Fenix";
            case "chain_lightning" -> "Raio em Cadeia";
            case "time_dilation" -> "Dilatacao Temporal";
            case "greedy_draft" -> "Compulsao";
            case "echo" -> "Eco";
            case "last_stand" -> "Ultimo Suspiro";
            case "momentum" -> "Momentum";
            case "explorer_ascendant" -> "Explorador Ascendente";
            case "miner_ascendant" -> "Minerador Ascendente";
            case "builder_ascendant" -> "Construtor Ascendente";
            default -> key;
        };
    }
}
