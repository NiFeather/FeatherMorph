package xyz.nifeather.morph.providers.animation.bundled;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.ShulkerPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

import java.util.List;

public class ShulkerAnimationSet extends AnimationSet
{
    private static ShulkerPropertyCollection properties()
    {
        return DisguiseProperties.INSTANCE.getCollectionOrThrow(ShulkerPropertyCollection.class);
    }

    public final PlayableAction PEEK = PlayableAction.builder()
            .addStage(b ->
            {
                var properties = properties();

                b.duration(40)
                        .legacyName(AnimationNames.PEEK_START)
                        .onPlay(state ->
                        {
                            state.disguisePropertyHandler().setTemp(properties.SHELL_HEIGHT, (byte)30);

                            var player = state.getPlayer();
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHULKER_OPEN, SoundCategory.HOSTILE, 1, 1);
                        })
                        .onFinish(state ->
                        {
                            state.disguisePropertyHandler().discardTemporaryProperty(properties.SHELL_HEIGHT);

                            var player = state.getPlayer();
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHULKER_CLOSE, SoundCategory.HOSTILE, 1, 1);
                        });
            })
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.PEEK_STOP))
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.RESET))
            .build();

    public final PlayableAction OPEN = PlayableAction.builder()
            .addStage(b ->
            {
                var properties = properties();

                b.duration(100)
                        .legacyName(AnimationNames.OPEN_START)
                        .onPlay(state ->
                        {
                            state.disguisePropertyHandler().setTemp(properties().SHELL_HEIGHT, (byte)100);

                            var player = state.getPlayer();
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHULKER_OPEN, SoundCategory.HOSTILE, 1, 1);
                        })
                        .onFinish(state ->
                        {
                            state.disguisePropertyHandler().discardTemporaryProperty(properties.SHELL_HEIGHT);

                            var player = state.getPlayer();
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHULKER_CLOSE, SoundCategory.HOSTILE, 1, 1);
                        });
            })
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.OPEN_STOP))
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.RESET))
            .build();

    public ShulkerAnimationSet()
    {
        register(AnimationNames.PEEK, PEEK);
        register(AnimationNames.OPEN, OPEN);
    }
}
