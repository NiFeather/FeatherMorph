package xyz.nifeather.morph.misc.disguiseProperty.values;

import com.mojang.authlib.GameProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.strings.ExceptionStrings;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.misc.skins.PlayerSkinProvider;
import xyz.nifeather.morph.utilities.GameProfileUtils;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PlayerProperties extends BaseLivingEntityProperties<Player>
{
    public final SingleProperty<MainHandStatus> MAIN_HAND = createProperty(PropertyNames.PLAYER_MAIN_HAND, MainHandStatus.NOTSET, this::readHand, OutputHandles::writeEnum)
            .withValidInput("left", "right");

    public final SingleProperty<GameProfile> SKIN = SingleProperty.builder(PropertyNames.PLAYER_SKIN, GameProfile.class, new GameProfile(Uuids.NIL_UUID, "unknown"))
            .withInputHandle(InputHandles::readGameProfile)
            .withOutputHandle(OutputHandles::writeGameProfile)
            .withHideFromUserInput(true)
            .withValidator(PropertyValidations::validatePlayerSkin)
            .build();

    @Override
    protected SingleProperty<Component> createCustomNameProperty()
    {
        return SingleProperty.builder(PropertyNames.ENTITY_CUSTOM_NAME, Component.class, Component.empty())
                .withInputHandle(InputHandles::unsupported)
                .withOutputHandle(OutputHandles::writeAdventureComponentJSON)
                .withValidator(PropertyValidations::validateCustomTextPermission)
                .build();
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
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Player targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

        propertyHandler.set(MAIN_HAND, MainHandStatus.fromBukkitHand(targetEntity.getMainHand()));
        propertyHandler.set(STUCKED_ARROWS, targetEntity.getArrowsInBody());

        if (meta.playerDisguiseTargetName != null && meta.playerDisguiseTargetName.equalsIgnoreCase(targetEntity.getName()))
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
