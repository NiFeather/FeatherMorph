package xyz.nifeather.morph.misc.disguiseProperty;

import com.mojang.authlib.GameProfile;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.strings.ExceptionStrings;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.misc.skins.PlayerSkinProvider;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.EnumSet;
import java.util.Objects;

public class PropertyValidations
{
    public static void noOp(Object value, Entity player, EnumSet<ValidationFlag> validationFlags)
    {
    }

    public static void validateCustomTextPermission(Object value, Entity player, EnumSet<ValidationFlag> validationFlags)
            throws PropertyValidationException
    {
        if (validationFlags.contains(ValidationFlag.SKIP_PERMISSIONS)) return;

        if (!player.hasPermission(CommonPermissions.DISGUISE_CUSTOM_TEXT))
        {
            throw PropertyValidationException.forProperty(PropertyNames.ENTITY_CUSTOM_NAME)
                    .byMethod("validateCustomTextPermission")
                    .withLocalizableMessage(CommandStrings.noPermissionMessage())
                    .withMessage("Player don't have permission setting custom name for disguise")
                    .create();
        }
    }

    public static void validatePlayerSkin(GameProfile skin, Entity player, EnumSet<ValidationFlag> validationFlags)
            throws PropertyValidationException
    {
        if (!validationFlags.contains(ValidationFlag.SKIP_PERMISSIONS))
        {
            boolean skinMatchesCache = Objects.equals(PlayerSkinProvider.getInstance().getCachedProfile(skin.name()), skin);

            if (!skinMatchesCache && !player.hasPermission(CommonPermissions.DISGUISE_CUSTOM_SKIN))
            {
                throw PropertyValidationException.forProperty(PropertyNames.MANNEQUIN_SKIN)
                        .byMethod("validatePlayerSkin")
                        .withLocalizableMessage(CommandStrings.noPermissionMessage())
                        .withMessage("Player don't have permission for setting custom skin")
                        .create();
            }
        }

        if (skin.id().equals(Uuids.NIL_UUID))
        {
            throw PropertyValidationException.forProperty(PropertyNames.PLAYER_SKIN)
                    .byMethod("validatePlayerSkin")
                    .withLocalizableMessage(ExceptionStrings.inputNotAllowed())
                    .withMessage("UUID of the skin may not be ZERO")
                    .create();
        }
    }

    public static void validateMannequinCustomSkinPermission(Object any, Entity player, EnumSet<ValidationFlag> validationFlags)
            throws PropertyValidationException
    {
        if (validationFlags.contains(ValidationFlag.SKIP_PERMISSIONS)) return;

        if (!player.hasPermission(CommonPermissions.DISGUISE_CUSTOM_SKIN))
        {
            throw PropertyValidationException.forProperty(PropertyNames.MANNEQUIN_SKIN)
                    .byMethod("validateMannequinCustomSkinPermission")
                    .withLocalizableMessage(CommandStrings.noPermissionMessage())
                    .withMessage("Player don't have permission for setting custom skin")
                    .create();
        }
    }

    public static void validateEquipment(DisguiseEquipment equipment, Entity player,  EnumSet<ValidationFlag> validationFlags)
            throws PropertyValidationException
    {
        if (validationFlags.contains(ValidationFlag.SKIP_PERMISSIONS)) return;

        for (ItemStack stack : equipment.contents().values())
        {
            var data = stack.getData(DataComponentTypes.PROFILE);
            if (data != null && !player.hasPermission(CommonPermissions.CUSTOM_SKIN_ON_ITEMS))
            {
                throw PropertyValidationException.forProperty(PropertyNames.ENTITY_EQUIPMENT)
                        .byMethod("validateEquipment")
                        .withLocalizableMessage(CommandStrings.noPermissionMessage())
                        .withMessage("Player don't have permission setting custom skin profile for items")
                        .create();
            }
        }
    }
}
