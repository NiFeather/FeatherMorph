package xyz.nifeather.morph.providers.animation.bundled;

import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.CatPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

public class CatAnimationSet extends AnimationSet
{
    private static CatPropertyCollection properties()
    {
        return DisguiseProperties.INSTANCE.getCollectionOrThrow(CatPropertyCollection.class);
    }

    public final PlayableAction LAY = PlayableAction.builder()
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.RESET))
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.LAY_START)
                            .onPlay(state -> state.disguisePropertyHandler().set(properties().LYING, true)))
            .build();

    public final PlayableAction STANDUP = PlayableAction.builder()
            .addStage(b ->
            {
                b.duration(0)
                        .legacyName(AnimationNames.STANDUP)
                        .onPlay(state ->
                        {
                            var properties = properties();
                            state.disguisePropertyHandler().set(properties.LYING, false);
                            state.disguisePropertyHandler().set(properties.SITTING, false);
                        });
            })
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.RESET))
            .build();

    public final PlayableAction SIT = PlayableAction.builder()
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.RESET))
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.SIT)
                            .onPlay(state -> state.disguisePropertyHandler().set(properties().SITTING, true)))
            .build();

    public CatAnimationSet()
    {
        register(AnimationNames.LAY, LAY);
        register(AnimationNames.STANDUP, STANDUP);
        register(AnimationNames.SIT, SIT);
    }
}
