package com.project.rpgplugin.integration;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class ModelEngineBridge {

    private final boolean enabled;

    public ModelEngineBridge() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("ModelEngine");
        this.enabled = plugin != null && plugin.isEnabled();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void applyModel(Entity entity, String modelId) {
        if (!enabled || modelId == null || modelId.isEmpty()) return;
        try {
            Class<?> apiClass = Class.forName("com.ticxo.modelengine.api.ModelEngineAPI");
            Object api = apiClass.getMethod("getAPI").invoke(null);
            Object modeledEntity = api.getClass()
                .getMethod("getModeledEntity", Entity.class).invoke(api, entity);
            if (modeledEntity == null) {
                modeledEntity = api.getClass()
                    .getMethod("createModeledEntity", Entity.class).invoke(api, entity);
            }
            modeledEntity.getClass()
                .getMethod("setModel", String.class).invoke(modeledEntity, modelId);
        } catch (Exception ignored) {}
    }
}
