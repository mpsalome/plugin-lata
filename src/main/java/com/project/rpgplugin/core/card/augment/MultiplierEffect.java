package com.project.rpgplugin.core.card.augment;

import com.project.rpgplugin.core.run.RunState;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public record MultiplierEffect(String key, double addPerStack) implements AugmentEffect {

    private static final Set<String> TOGGLE_KEYS = Set.of(
        "double_jump", "auto_smelt", "second_wind", "vampiric_night",
        "overcharge", "phoenix", "chain_lightning", "time_dilation",
        "greedy_draft", "echo", "last_stand", "momentum",
        "explorer_ascendant", "miner_ascendant", "builder_ascendant"
    );

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
        if (addPerStack == 1.0) {
            if (TOGGLE_KEYS.contains(key)) {
                return List.of("<green>" + effectDescription());
            }
            return List.of("<gray>" + displayKey() + ": <white>+1 por pilha");
        }
        if (addPerStack == (long) addPerStack && addPerStack > 1) {
            return List.of("<gray>" + displayKey() + ": <white>+" + (long) addPerStack + " por pilha");
        }
        int pct = (int) (addPerStack * 100);
        String sign = pct >= 0 ? "+" : "";
        return List.of("<gray>" + displayKey() + ": <white>" + sign + pct + "% por pilha");
    }

    private String effectDescription() {
        return switch (key) {
            case "double_jump" -> "Concede pulo duplo";
            case "dash_charge" -> "Concede carga extra de dash";
            case "auto_smelt" -> "Fundicao automatica ao minerar";
            case "second_wind" -> "Salva da morte uma vez (2min de CD)";
            case "frost_touch" -> "Lentidao ao acertar inimigos";
            case "vampiric_night" -> "Vida e forca aumentadas a noite";
            case "adrenaline" -> "Speed + Strength ao ficar com pouca vida";
            case "overcharge" -> "Dano extra ao custo de vida";
            case "phoenix" -> "Revive uma vez por run";
            case "chain_lightning" -> "Raio em cadeia ao acertar inimigos";
            case "time_dilation" -> "Lentidao em area ao redor";
            case "greedy_draft" -> "+1 opcao no draft";
            case "echo" -> "Chance de reativar habilidades";
            case "last_stand" -> "Resistencia aumentada ao morrer";
            case "momentum" -> "Velocidade progressiva ao correr";
            case "explorer_ascendant" -> "Todas as habilidades do Explorador";
            case "miner_ascendant" -> "Todas as habilidades do Minerador";
            case "builder_ascendant" -> "Todas as habilidades do Construtor";
            default -> displayKey() + ": Ativado";
        };
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
            case "damage_reduction" -> "Dano Recebido";
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
