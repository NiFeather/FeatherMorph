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

    public final SingleProperty<GameProfile> SKIN = SingleProperty.of(PropertyNames.PLAYER_SKIN, new GameProfile(Uuids.NIL_UUID, "unknown"), InputHandles::readGameProfile, OutputHandles::writeGameProfile, true);

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

    @Override
    public void validateInput(Map<SingleProperty<?>, Object> result, Player player, boolean ignorePermissions) throws PropertyValidationException
    {
        super.validateInput(result, player, ignorePermissions);

        if (result.containsKey(SKIN))
        {
            var skin = (GameProfile) result.get(SKIN);
            boolean skinMatchesCache = Objects.equals(PlayerSkinProvider.getInstance().getCachedProfile(skin.name()), skin);

            if (!ignorePermissions && !skinMatchesCache && !player.hasPermission(CommonPermissions.DISGUISE_CUSTOM_SKIN))
            {
                throw PropertyValidationException.forProperty(PropertyNames.PLAYER_SKIN)
                        .byMethod("PlayerProperties#validateInput")
                        .withLocalizableMessage(CommandStrings.noPermissionMessage())
                        .withMessage("Player don't have permission for setting custom skin")
                        .create();
            }

            if (skin.id().equals(Uuids.NIL_UUID))
            {
                throw PropertyValidationException.forProperty(PropertyNames.PLAYER_SKIN)
                        .byMethod("PlayerProperties#validateInput")
                        .withLocalizableMessage(ExceptionStrings.inputNotAllowed())
                        .withMessage("UUID of the skin may not be ZERO")
                        .create();
            }
        }
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
