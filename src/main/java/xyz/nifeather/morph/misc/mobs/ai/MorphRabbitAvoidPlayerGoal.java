package xyz.nifeather.morph.misc.mobs.ai;

import net.minecraft.world.entity.animal.Rabbit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.RevealingHandler;

public class MorphRabbitAvoidPlayerGoal extends MorphCommonAvoidPlayerGoal
{
    private static final Logger log = LoggerFactory.getLogger(MorphRabbitAvoidPlayerGoal.class);
    private final Rabbit rabbit;

    public MorphRabbitAvoidPlayerGoal(MorphManager morphs, RevealingHandler revealingHandler, Rabbit bindingMob, float detectDistance, double walkSpeed, double sprintSpeed)
    {
        super(morphs, revealingHandler, bindingMob, detectDistance, walkSpeed, sprintSpeed);

        this.rabbit = bindingMob;
    }

    @Override
    public boolean canUse()
    {
        if (rabbit.getVariant() == Rabbit.Variant.EVIL)
            return false;

        return super.canUse();
    }

}
