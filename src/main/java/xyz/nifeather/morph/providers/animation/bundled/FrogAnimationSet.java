package xyz.nifeather.morph.providers.animation.bundled;

import org.bukkit.Sound;
import org.bukkit.entity.Pose;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.FrogPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

public class FrogAnimationSet extends AnimationSet
{
    private static FrogPropertyCollection properties()
    {
        return DisguiseProperties.INSTANCE.getCollectionOrThrow(FrogPropertyCollection.class);
    }

    public final PlayableAction EAT = PlayableAction.builder()
            .addStage(b ->
            {
                b.duration(10)
                        .legacyName(AnimationNames.EAT)
                        .onPlay(state -> 
                        {
                            state.disguisePropertyHandler().set(properties().STATIC_POSE, Pose.USING_TONGUE);

                            var player = state.getPlayer();
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FROG_EAT, 1, 1);
                        })
                        .onFinish(state -> state.disguisePropertyHandler().discardProperty(properties().STATIC_POSE));
            })
            .addStage(b -> b.duration(0).legacyName(AnimationNames.RESET))
            .build();

    //public final SingleAnimation EAT = new SingleAnimation(AnimationNames.EAT, 10, true);

    public FrogAnimationSet()
    {
        register(AnimationNames.EAT, EAT);
    }
}
