package xyz.nifeather.morph.providers.animation.bundled;

import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.FoxPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

public class FoxAnimationSet extends AnimationSet
{
    private static FoxPropertyCollection properties()
    {
        return DisguiseProperties.INSTANCE.getCollectionOrThrow(FoxPropertyCollection.class);
    }

    public final PlayableAction SLEEP = PlayableAction.builder()
            .addStage(b ->
                    b.duration(5)
                            .legacyName(AnimationNames.SLEEP)
                            .onPlay(state -> state.disguisePropertyHandler().setTemp(properties().STATUS, FoxPropertyCollection.FoxStatus.SLEEPING)))
            .build();
    
    public final PlayableAction SIT = PlayableAction.builder()
            .addStage(b ->
                    b.duration(5)
                            .legacyName(AnimationNames.SIT)
                            .onPlay(state -> state.disguisePropertyHandler().setTemp(properties().STATUS, FoxPropertyCollection.FoxStatus.SITTING)))
            .build();
    
    public final PlayableAction STAND = PlayableAction.builder()
            .addStage(b ->
                    b.duration(5)
                            .legacyName(AnimationNames.STANDUP)
                            .onPlay(state -> state.disguisePropertyHandler().discardTemporaryProperty(properties().STATUS)))
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.RESET))
            .build();

    public FoxAnimationSet()
    {
        register(AnimationNames.SLEEP, SLEEP);
        register(AnimationNames.SIT, SIT);
        register(AnimationNames.STANDUP, STAND);
    }
}
