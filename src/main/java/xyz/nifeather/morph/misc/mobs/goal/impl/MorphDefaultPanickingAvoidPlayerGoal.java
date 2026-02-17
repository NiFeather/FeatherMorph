package xyz.nifeather.morph.misc.mobs.goal.impl;

import org.bukkit.entity.Creature;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;

public class MorphDefaultPanickingAvoidPlayerGoal extends MorphBasicAvoidPlayerGoal<Creature>
{
    public MorphDefaultPanickingAvoidPlayerGoal(Creature bindingMob, RevealingHandler revealingHandler, MorphManager morphManager, double detectDistance, double walkSpeed, double sprintSpeed)
    {
        super(bindingMob, revealingHandler, morphManager, detectDistance, walkSpeed, sprintSpeed);
    }

    @Override
    protected boolean mobPanicFromPlayerByDefault()
    {
        return true;
    }
}
