package xyz.nifeather.morph.misc.disguiseProperty.values;

import com.mojang.authlib.GameProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.messages.strings.ExceptionStrings;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.GameProfileUtils;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.Optional;

public class PlayerPropertyCollection extends BaseLivingEntityPropertyCollection<Player>
{
    public final SingleProperty<MainHandStatus> MAIN_HAND = SingleProperty.builder(PropertyNames.PLAYER_MAIN_HAND, MainHandStatus.NOTSET)
            .withInputHandle(this::readHand)
            .withOutputHandle(OutputHandles::writeEnum)
            .withSuggestions("left", "right")
            .build();

    public final SingleProperty<GameProfile> SKIN = SingleProperty.builder(PropertyNames.PLAYER_SKIN, GameProfile.class, new GameProfile(Uuids.NIL_UUID, "unknown"))
            .withInputHandle(InputHandles::readGameProfile)
            .withOutputHandle(OutputHandles::writeGameProfile)
            .hideFromUserInput(true)
            .withValidator(PropertyValidations::validatePlayerSkin)
            .build();

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

    public PlayerPropertyCollection()
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
    protected boolean validateEntity(DisguiseMeta meta, Player entityToSetup)
    {
        return meta.playerDisguiseTargetName != null
                && entityToSetup.getName().equalsIgnoreCase(meta.playerDisguiseTargetName);
    }

    @Override
    public void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Player targetEntity)
    {
        super.setupPropertiesFromEntity(propertyHandler, targetEntity);

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
