package xyz.nifeather.morph.providers.animation.bundled;

import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.BaseLivingEntityPropertyCollection;
import xyz.nifeather.morph.misc.disguiseProperty.values.WolfPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

import java.util.List;

public class WolfAnimationSet extends AnimationSet
{
    private WolfPropertyCollection properties()
    {
        return DisguiseProperties.INSTANCE.getCollectionOrThrow(WolfPropertyCollection.class);
    }

    public final PlayableAction SIT = PlayableAction.builder()
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.SIT)
                            .onPlay(state -> state.disguisePropertyHandler().set(properties().SITTING, true)))
            .build();

    public final PlayableAction STANDUP = PlayableAction.builder()
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.STANDUP)
                            .onPlay(state -> state.disguisePropertyHandler().set(properties().SITTING, false)))
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.RESET))
            .build();

    public WolfAnimationSet()
    {
        register(AnimationNames.SIT, SIT);
        register(AnimationNames.STANDUP, STANDUP);
    }
}
