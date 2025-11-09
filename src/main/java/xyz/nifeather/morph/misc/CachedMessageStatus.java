package xyz.nifeather.morph.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public record CachedMessageStatus(
        short statusBit,
        Component display
)
{
    public static final CachedMessageStatus DEFAULT = new CachedMessageStatus
            (
                    (short) -1,
                    MiniMessage.miniMessage().deserialize("<yellow>missingno")
            );
}
