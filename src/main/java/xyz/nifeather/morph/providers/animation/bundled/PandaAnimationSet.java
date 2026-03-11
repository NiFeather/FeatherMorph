package xyz.nifeather.morph.providers.animation.bundled;

import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.PandaPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

public class PandaAnimationSet extends AnimationSet
{
    private static PandaPropertyCollection properties()
    {
        return DisguiseProperties.INSTANCE.getCollectionOrThrow(PandaPropertyCollection.class);
    }

    public final PlayableAction SIT = PlayableAction.builder()
            .addStage(b ->
                    b.duration(5)
                            .legacyName(AnimationNames.SIT)
                            .onPlay(state -> state.disguisePropertyHandler().set(properties().SITTING, true)))
            .build();

    public final PlayableAction STAND = PlayableAction.builder()
            .addStage(b ->
                    b.duration(5)
                            .legacyName(AnimationNames.STANDUP)
                            .onPlay(state -> state.disguisePropertyHandler().set(properties().SITTING, false)))
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.RESET))
            .build();

    public PandaAnimationSet()
    {
        register(AnimationNames.SIT, SIT);
        register(AnimationNames.STANDUP, STAND);
    }
}
