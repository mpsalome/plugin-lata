package com.project.rpgplugin.core.skill;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public record SkillContext(
    Player player,
    SkillServices services,
    @Nullable ItemStack usedItem,
    @Nullable Block targetBlock,
    @Nullable Event sourceEvent
) {}
