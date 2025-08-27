package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.messages.ExceptionStrings;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Map;
import java.util.Optional;

public class PlayerProperties extends BaseLivingEntityProperties<Player>
{
    public final SingleProperty<MainHandStatus> MAIN_HAND = getSingle(PropertyNames.PLAYER_MAIN_HAND, MainHandStatus.NOTSET, this::readHand)
            .withValidInput("left", "right");

    private Optional<MainHandStatus> readHand(String propertyName, String string) throws ParseErrorException
    {
        if (string.equalsIgnoreCase("notset"))
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readHand")
                    .withLocalizableMessage(ExceptionStrings.inputNotAllowed())
                    .withMessage("This value is not allowed here!")
                    .create();
        }

        return InputHandles.readEnumNonNull(MainHandStatus.values(), propertyName, string);
    }

    public PlayerProperties()
    {
        registerSingle(MAIN_HAND);
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
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);

        propertyHandler.getOptional(MAIN_HAND).ifPresent(hand -> map.put(MAIN_HAND.id(), hand.name().toLowerCase()));
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
