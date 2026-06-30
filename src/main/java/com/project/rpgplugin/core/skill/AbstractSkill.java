package com.project.rpgplugin.core.skill;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class AbstractSkill implements Skill {

    protected final SkillServices services;

    protected AbstractSkill(SkillServices services) {
        this.services = services;
    }

    protected boolean onCooldown(SkillContext ctx) {
        return services.isOnCooldown(ctx.player().getUniqueId(), id());
    }

    protected long cooldownRemaining(SkillContext ctx) {
        return services.cooldownRemaining(ctx.player().getUniqueId(), id());
    }

    protected void startCooldown(SkillContext ctx) {
        services.startCooldown(ctx.player().getUniqueId(), id(), cooldown());
    }

    protected void consume(SkillContext ctx, int amount) {
        if (ctx.usedItem() != null) {
            ctx.usedItem().subtract(amount);
        }
    }

    protected void feedback(SkillContext ctx, String message, Sound sound) {
        ctx.player().sendActionBar(net.kyori.adventure.text.Component.text(message));
        if (sound != null) {
            ctx.player().playSound(ctx.player().getLocation(), sound, 1.0f, 1.0f);
        }
    }

    protected ConfigurationSection cfg() {
        return services.skillConfig(id());
    }

    public SkillServices services() {
        return services;
    }
}
