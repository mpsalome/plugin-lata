package com.project.rpgplugin.core.skill.impl;

import com.project.rpgplugin.core.run.RunState;
import com.project.rpgplugin.core.skill.AbstractSkill;
import com.project.rpgplugin.core.skill.SkillContext;
import com.project.rpgplugin.core.skill.SkillTier;
import com.project.rpgplugin.core.skill.SkillType;
import com.project.rpgplugin.core.skill.trigger.PassiveTrigger;
import com.project.rpgplugin.core.skill.trigger.SkillTrigger;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.Duration;

public class JumpBoostSkill extends AbstractSkill {

    public JumpBoostSkill(com.project.rpgplugin.core.skill.SkillServices services) {
        super(services);
    }

    @Override
    public String id() { return "jump_boost"; }

    @Override
    public SkillTier tier() { return SkillTier.SILVER; }

    @Override
    public SkillType type() { return SkillType.EXPLORER; }

    @Override
    public Material icon() { return Material.RABBIT_FOOT; }

    @Override
    public boolean passive() { return true; }

    @Override
    public Duration cooldown() { return Duration.ZERO; }

    @Override
    public SkillTrigger trigger() { return PassiveTrigger.instance(); }

    @Override
    public void activate(SkillContext ctx) {
        Player p = ctx.player();
        RunState run = services.plugin().getRunManager().getRun(p);
        if (run != null) {
            run.addPotionType("JUMP_BOOST");
        }
    }
}
