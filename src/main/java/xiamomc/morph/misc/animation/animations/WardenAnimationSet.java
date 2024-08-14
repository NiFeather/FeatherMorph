package xiamomc.morph.misc.animation.animations;

import net.minecraft.world.entity.ai.behavior.warden.Sniffing;
import net.minecraft.world.entity.monster.warden.WardenAi;
import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;

public class WardenAnimationSet extends AnimationSet
{
    public final SingleAnimation SNIFF = new SingleAnimation(AnimationNames.SNIFF, Sniffing.DEFAULT_DURATION, true);

    // For the 25 ticks of delay, see net.minecraft.world.entity.ai.behavior.warden.Roar#start()
    public final SingleAnimation ROAR = new SingleAnimation(AnimationNames.ROAR, 25, true);
    public final SingleAnimation ROAR_SOUND = new SingleAnimation(AnimationNames.ROAR_SOUND, WardenAi.ROAR_DURATION - 25, false);

    public final SingleAnimation DISAPPEAR = new SingleAnimation(AnimationNames.DISAPPEAR, 80, true);
    public final SingleAnimation APPEAR = new SingleAnimation(AnimationNames.APPEAR, 80, true);

    public final SingleAnimation HANG_200 = new SingleAnimation("hang200", 100, false);

    public WardenAnimationSet()
    {
        register(AnimationNames.ROAR, List.of(ROAR, ROAR_SOUND, RESET));
        register(AnimationNames.SNIFF, List.of(SNIFF, RESET));

        // Disabled because digging animation doesn't stop in vanilla client, causing invisible-like entity
        //register(AnimationNames.RE_APPEAR, List.of(DISAPPEAR, HANG_200, APPEAR, POSE_RESET));
    }

    @Override
    public List<String> getAvailableAnimationsForClient()
    {
        return List.of("roar", "sniff");
    }
}
