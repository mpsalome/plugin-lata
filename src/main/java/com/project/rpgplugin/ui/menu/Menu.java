package com.project.rpgplugin.ui.menu;

import com.project.rpgplugin.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class Menu implements InventoryHolder {

    protected final Inventory inventory;
    protected Consumer<org.bukkit.event.inventory.InventoryClickEvent> clickHandler;

    public Menu(int size, String title) {
        this.inventory = Bukkit.createInventory(this, size, Text.mm(title));
    }

    public void setClickHandler(Consumer<org.bukkit.event.inventory.InventoryClickEvent> handler) {
        this.clickHandler = handler;
    }

    public void handleClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (clickHandler != null) {
            clickHandler.accept(event);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    public void fillBorder(ItemStack filler) {
        int size = inventory.getSize();
        int rows = size / 9;
        for (int i = 0; i < size; i++) {
            int row = i / 9;
            int col = i % 9;
            if (row == 0 || row == rows - 1 || col == 0 || col == 8) {
                if (inventory.getItem(i) == null) {
                    inventory.setItem(i, filler);
                }
            }
        }
    }
}
