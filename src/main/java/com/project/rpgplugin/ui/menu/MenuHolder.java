package com.project.rpgplugin.ui.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public record MenuHolder(Menu menu) implements InventoryHolder {

    @Override
    public @NotNull Inventory getInventory() {
        return menu.getInventory();
    }
}
