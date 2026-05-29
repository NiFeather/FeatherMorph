package xyz.nifeather.morph.providers.animation.bundled;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Sniffer;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.BaseLivingEntityPropertyCollection;
import xyz.nifeather.morph.misc.disguiseProperty.values.ShulkerPropertyCollection;
import xyz.nifeather.morph.misc.disguiseProperty.values.SnifferPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

import java.util.List;

public class SnifferAnimationSet extends AnimationSet
{
    private static SnifferPropertyCollection properties()
    {
        return DisguiseProperties.INSTANCE.getCollectionOrThrow(SnifferPropertyCollection.class);
    }

    public final PlayableAction SNIFF = PlayableAction.builder()
            .addStage(b ->
            {
                var properties = properties();

                b.duration(20)
                        .legacyName(AnimationNames.SNIFF)
                        .onPlay(state ->
                        {
                            var player = state.getPlayer();
                            state.disguisePropertyHandler().setTemp(properties.SNIFFER_STATE, Sniffer.State.SNIFFING);

                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SNIFFER_SNIFFING, SoundCategory.NEUTRAL, 1, 1);
                        })
                        .onFinish(state ->
                        {
                            state.disguisePropertyHandler().discardTemporaryProperty(properties.SNIFFER_STATE);
                        });
            })
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.RESET))
            .build();

    public SnifferAnimationSet()
    {
        register(AnimationNames.SNIFF, SNIFF);
    }
}
