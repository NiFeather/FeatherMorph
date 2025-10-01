package xyz.nifeather.morph.misc.disguiseProperty.values;

import com.mojang.authlib.GameProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.messages.ExceptionStrings;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.GameProfileUtils;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.Optional;
import java.util.UUID;

public class PlayerProperties extends BaseLivingEntityProperties<Player>
{
    public final SingleProperty<MainHandStatus> MAIN_HAND = createProperty(PropertyNames.PLAYER_MAIN_HAND, MainHandStatus.NOTSET, this::readHand, OutputHandles::writeEnum)
            .withValidInput("left", "right");

    public final SingleProperty<GameProfile> SKIN = SingleProperty.of(PropertyNames.PLAYER_SKIN, new GameProfile(Uuids.NIL_UUID, "unknown"), InputHandles::reservedException, OutputHandles::writeGameProfile, true);

    @Override
    protected SingleProperty<Component> createCustomNameProperty()
    {
        return createProperty(PropertyNames.ENTITY_CUSTOM_NAME, Component.empty(), InputHandles::unsupported, OutputHandles::writeAdventureComponentJSON);
    }

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
        super();
        registerSingle(MAIN_HAND, SKIN);
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
        propertyHandler.set(SKIN, GameProfileUtils.convertPlayerProfile(targetEntity.getPlayerProfile()));
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
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
