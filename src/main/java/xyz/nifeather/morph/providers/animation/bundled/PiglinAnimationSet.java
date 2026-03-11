package xyz.nifeather.morph.providers.animation.bundled;

import org.bukkit.Sound;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.PiglinPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

public class PiglinAnimationSet extends AnimationSet
{
    private static PiglinPropertyCollection properties()
    {
        return DisguiseProperties.INSTANCE.getCollectionOrThrow(PiglinPropertyCollection.class);
    }

    public final PlayableAction START_DANCE = PlayableAction.builder()
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.DANCE_START)
                            .onPlay(state ->
                            {
                                state.disguisePropertyHandler().set(properties().DANCING, true);

                                var player = state.getPlayer();
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIGLIN_CELEBRATE, 1, 1);
                            }))
            .build();

    public final PlayableAction STOP_DANCE = PlayableAction.builder()
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.STOP)
                            .onPlay(state -> state.disguisePropertyHandler().set(properties().DANCING, false)))
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.RESET))
            .build();

    public PiglinAnimationSet()
    {
        register(AnimationNames.DANCE, START_DANCE);
        register(AnimationNames.STOP, STOP_DANCE);
    }
}
