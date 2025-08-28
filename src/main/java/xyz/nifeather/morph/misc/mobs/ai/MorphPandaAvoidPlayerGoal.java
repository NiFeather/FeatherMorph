package xyz.nifeather.morph.misc.mobs.ai;

import net.minecraft.world.entity.animal.Panda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;

public class MorphPandaAvoidPlayerGoal extends MorphCommonAvoidPlayerGoal
{
    private static final Logger log = LoggerFactory.getLogger(MorphPandaAvoidPlayerGoal.class);
    private final Panda panda;

    public MorphPandaAvoidPlayerGoal(MorphManager morphs, RevealingHandler revealingHandler, Panda bindingMob, float detectDistance, double walkSpeed, double sprintSpeed)
    {
        super(morphs, revealingHandler, bindingMob, detectDistance, walkSpeed, sprintSpeed);

        this.panda = bindingMob;
    }

    @Override
    public boolean canUse()
    {
        if (!panda.isWorried() || !panda.canPerformAction())
            return false;

        return super.canUse();
    }

}
