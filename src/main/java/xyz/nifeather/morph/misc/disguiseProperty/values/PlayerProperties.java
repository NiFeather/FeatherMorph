package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class PlayerProperties extends AbstractProperties
{
    public final SingleProperty<MainHand> MAIN_HAND = getSingle("main_hand", MainHand.RIGHT)
            .withValidInput("left", "right");

    public PlayerProperties()
    {
        registerSingle(MAIN_HAND);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(MAIN_HAND.id()))
        {
            var val = value.equals("left") ? MainHand.LEFT : MainHand.RIGHT;
            return Pair.of(MAIN_HAND, val);
        }

        return null;
    }
}
