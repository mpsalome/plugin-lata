package com.project.rpgplugin.core.mana;

import org.bukkit.entity.Player;

public interface ManaProvider {

    boolean isAvailable();

    double getMana(Player player);

    double getMaxMana(Player player);

    boolean hasEnoughMana(Player player, double amount);

    boolean consumeMana(Player player, double amount);

    void addMana(Player player, double amount);

    void setMana(Player player, double amount);

    String name();
}
