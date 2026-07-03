package com.project.rpgplugin.ui.menu;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof MenuHolder mh) {
            e.setCancelled(true);
            mh.menu().handleClick(e);
        } else if (holder instanceof Menu menu) {
            e.setCancelled(true);
            menu.handleClick(e);
        }
    }
}
