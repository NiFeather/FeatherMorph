package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;

public class PlayerProperties extends BaseLivingEntityProperties<Player>
{
    public final SingleProperty<MainHandStatus> MAIN_HAND = getSingle("player/main_hand", MainHandStatus.NOTSET)
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
            var val = value.equals("left") ? MainHandStatus.LEFT : MainHandStatus.RIGHT;
            return Pair.of(MAIN_HAND, val);
        }

        return super.parseSingleInput(key, value);
    }

    @Override
    protected @Nullable Player tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Player player ? player : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Player targetEntity)
    {
        propertyHandler.set(MAIN_HAND, MainHandStatus.fromBukkitHand(targetEntity.getMainHand()));
        propertyHandler.set(STUCKED_ARROWS, targetEntity.getArrowsInBody());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

    @Override
    public Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        return Map.of(
                MAIN_HAND.id(), propertyHandler.get(MAIN_HAND).name().toLowerCase(),
                STUCKED_ARROWS.id(), propertyHandler.get(STUCKED_ARROWS).toString()
        );
    }

    public enum MainHandStatus
    {
        LEFT(MainHand.LEFT),
        RIGHT(MainHand.RIGHT),
        NOTSET(null);

        @Nullable
        public final MainHand bindingHand;

        MainHandStatus(@Nullable MainHand hand)
        {
            this.bindingHand = hand;
        }

        public static MainHandStatus fromBukkitHand(MainHand hand)
        {
            return hand == MainHand.LEFT ? LEFT : RIGHT;
        }

        @Nullable
        public MainHand toBukkitHand()
        {
            return bindingHand;
        }
    }
}
