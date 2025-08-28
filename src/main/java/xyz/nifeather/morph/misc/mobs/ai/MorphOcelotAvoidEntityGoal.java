package xyz.nifeather.morph.misc.mobs.ai;

import net.minecraft.world.entity.animal.Ocelot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;

public class MorphOcelotAvoidEntityGoal extends MorphCommonAvoidPlayerGoal
{
    private static final Logger log = LoggerFactory.getLogger(MorphOcelotAvoidEntityGoal.class);
    private final Ocelot bindingOcelot;

    public MorphOcelotAvoidEntityGoal(MorphManager morphs, RevealingHandler revealingHandler, Ocelot bindingMob, float detectDistance, double walkSpeed, double sprintSpeed)
    {
        super(morphs, revealingHandler, bindingMob, detectDistance, walkSpeed, sprintSpeed);

        this.bindingOcelot = bindingMob;
    }

    @Override
    public boolean canUse()
    {
        if (!this.bindingOcelot.isTrusting())
            return false;

        return super.canUse();
    }

}
