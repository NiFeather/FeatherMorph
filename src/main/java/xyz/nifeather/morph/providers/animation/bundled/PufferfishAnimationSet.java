package xyz.nifeather.morph.providers.animation.bundled;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.PufferfishPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

public class PufferfishAnimationSet extends AnimationSet
{
    private static PufferfishPropertyCollection properties()
    {
        return DisguiseProperties.INSTANCE.getCollectionOrThrow(PufferfishPropertyCollection.class);
    }

    public final PlayableAction INFLATE = PlayableAction.builder()
            .addStage(b -> b.duration(0).legacyName(AnimationNames.INFLATE).onPlay(state ->
            {
                var properties = properties();
                var propertyHandler = state.disguisePropertyHandler();

                var existing = propertyHandler.get(properties.PUFF_STATE);

                var player = state.getPlayer();
                if (existing != PufferfishPropertyCollection.PufferfishState.LARGE)
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_UP, SoundCategory.HOSTILE, 1, 1);

                propertyHandler.setTemp(properties.PUFF_STATE, PufferfishPropertyCollection.PufferfishState.LARGE);
            }))
            .build();

    public final PlayableAction DEFLATE = PlayableAction.builder()
            .addStage(b -> b.duration(0).legacyName(AnimationNames.DEFLATE).onPlay(state ->
            {
                var properties = properties();
                var propertyHandler = state.disguisePropertyHandler();

                var existing = propertyHandler.get(properties.PUFF_STATE);
                var player = state.getPlayer();

                if (existing != PufferfishPropertyCollection.PufferfishState.SMALL)
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PUFFER_FISH_BLOW_OUT, SoundCategory.HOSTILE, 1, 1);

                propertyHandler.setTemp(properties.PUFF_STATE, PufferfishPropertyCollection.PufferfishState.SMALL);
            }))
            .addStage(b -> b.duration(0).legacyName(AnimationNames.RESET))
            .build();

    public PufferfishAnimationSet()
    {
        register(AnimationNames.INFLATE, INFLATE);
        register(AnimationNames.DEFLATE, DEFLATE);
    }
}
