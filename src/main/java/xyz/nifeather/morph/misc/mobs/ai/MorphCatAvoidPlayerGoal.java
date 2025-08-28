package xyz.nifeather.morph.misc.mobs.ai;

import net.minecraft.world.entity.animal.Cat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;

public class MorphCatAvoidPlayerGoal extends MorphCommonAvoidPlayerGoal
{
    private static final Logger log = LoggerFactory.getLogger(MorphCatAvoidPlayerGoal.class);
    private final Cat cat;

    public MorphCatAvoidPlayerGoal(MorphManager morphs, RevealingHandler revealingHandler, Cat bindingMob, float detectDistance, double walkSpeed, double sprintSpeed)
    {
        super(morphs, revealingHandler, bindingMob, detectDistance, walkSpeed, sprintSpeed);

        this.cat = bindingMob;
    }

    @Override
    public boolean canUse()
    {
        if (cat.isTame())
            return false;

        return super.canUse();
    }

}
