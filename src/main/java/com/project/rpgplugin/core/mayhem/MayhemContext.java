package com.project.rpgplugin.core.mayhem;

import com.project.rpgplugin.RPGPlugin;
import com.project.rpgplugin.core.run.RunState;
import org.bukkit.World;

public record MayhemContext(RPGPlugin plugin, RunState run, World world) {
}
