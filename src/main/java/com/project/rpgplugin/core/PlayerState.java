package com.project.rpgplugin.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerState {

    private double currentMana;
    private double maxMana;
    private final Map<ActiveEffect, Long> activeEffects = new ConcurrentHashMap<>();
    private String skillBarTitle = "";

    public double getCurrentMana() {
        return currentMana;
    }

    public void setCurrentMana(double currentMana) {
        this.currentMana = currentMana;
    }

    public double getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(double maxMana) {
        this.maxMana = maxMana;
    }

    public Map<ActiveEffect, Long> getActiveEffects() {
        return activeEffects;
    }

    public String getSkillBarTitle() {
        return skillBarTitle;
    }

    public void setSkillBarTitle(String skillBarTitle) {
        this.skillBarTitle = skillBarTitle;
    }

    public void addActiveEffect(String name, long expiry) {
        activeEffects.put(new ActiveEffect(name), expiry);
    }

    public void removeActiveEffect(String name) {
        activeEffects.keySet().removeIf(e -> e.getName().equals(name));
    }

    public static class ActiveEffect {
        private final String name;

        public ActiveEffect(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
