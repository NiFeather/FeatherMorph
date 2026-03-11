package xyz.nifeather.morph.providers.animation.bundled;

import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.AllayPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

public class AllayAnimationSet extends AnimationSet
{
    public final PlayableAction ROLL_START = PlayableAction.builder()
            .addStage(b ->
            {
                var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(AllayPropertyCollection.class);

                b.legacyName(AnimationNames.DANCE_START)
                        .onPlay(s -> s.disguisePropertyHandler().set(properties.DANCING, true));
            })
            .build();

    public final PlayableAction ROLL_STOP = PlayableAction.builder()
            .addStage(b ->
            {
                var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(AllayPropertyCollection.class);

                b.legacyName(AnimationNames.STOP)
                        .onPlay(s -> s.disguisePropertyHandler().set(properties.DANCING, false));
            })
            .addStage(b ->
                    b.duration(0).legacyName(AnimationNames.RESET))
            .build();

    public AllayAnimationSet()
    {
        register(AnimationNames.DANCE, ROLL_START);
        register(AnimationNames.STOP, ROLL_STOP);
    }
}
