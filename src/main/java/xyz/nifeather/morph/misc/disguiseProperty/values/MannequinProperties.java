package xyz.nifeather.morph.misc.disguiseProperty.values;

import com.mojang.authlib.GameProfile;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.utilities.GameProfileUtils;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.Map;

public class MannequinProperties extends BaseLivingEntityProperties<Mannequin>
{
    public final SingleProperty<Component> NPC_DESCRIPTION = createProperty(PropertyNames.MANNEQUIN_NPC_DESCRIPTION, Component.empty(), InputHandles::readComponentAny, OutputHandles::writeAdventureComponentJSON);
    public final SingleProperty<Boolean> HIDE_DESCRIPTION = createProperty(PropertyNames.MANNEQUIN_HIDE_DESCRIPTION, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean)
            .withValidInput("true", "false");

    /**
     * We don't suggest using this property, as it doesn't work for both Server and Mod renderer
     */
    public final SingleProperty<Boolean> IMMOVABLE = SingleProperty.of(PropertyNames.MANNEQUIN_IMMOVABLE, false, InputHandles::immediateException, OutputHandles::writeBoolean, true)
            .withValidInput("true", "false");

    public final SingleProperty<ResolvableProfile> SKIN = createProperty(PropertyNames.MANNEQUIN_SKIN, GameProfileUtils.asResolvableProfile(new GameProfile(Uuids.NIL_UUID, "notset")), InputHandles::readResolvableSkinInput, OutputHandles::writeResolvableProfileAny);

    public MannequinProperties()
    {
        registerSingle(NPC_DESCRIPTION, HIDE_DESCRIPTION, SKIN, IMMOVABLE);
    }

    @Override
    protected @Nullable Mannequin tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Mannequin mannequin ? mannequin : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Mannequin targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

        propertyHandler.set(SKIN, targetEntity.getProfile());

        var description = targetEntity.getDescription();
        if (description != null)
            propertyHandler.set(NPC_DESCRIPTION, description);
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(CUSTOM_NAME_VISIBLE, true);
    }

    @Override
    public void validateInput(Map<SingleProperty<?>, Object> result, Player player, boolean ignorePermissions) throws PropertyValidationException
    {
        super.validateInput(result, player, ignorePermissions);

        if (ignorePermissions) return;

        if (result.containsKey(SKIN) && !player.hasPermission(CommonPermissions.DISGUISE_CUSTOM_SKIN))
        {
            throw PropertyValidationException.forProperty(PropertyNames.MANNEQUIN_SKIN)
                    .byMethod("MannequinProperties#validateInput")
                    .withLocalizableMessage(CommandStrings.noPermissionMessage())
                    .withMessage("Player don't have permission for setting custom skin")
                    .create();
        }

        if (result.containsKey(NPC_DESCRIPTION) && !player.hasPermission(CommonPermissions.DISGUISE_CUSTOM_TEXT))
        {
            throw PropertyValidationException.forProperty(PropertyNames.MANNEQUIN_NPC_DESCRIPTION)
                    .byMethod("MannequinProperties#validateInput")
                    .withLocalizableMessage(CommandStrings.noPermissionMessage())
                    .withMessage("Player don't have permission for setting custom description")
                    .create();
        }

        //todo: maybe merge this with NPC_DESCRIPTION, but how should we deal with property name?
        if (result.containsKey(HIDE_DESCRIPTION) && !player.hasPermission(CommonPermissions.DISGUISE_CUSTOM_TEXT))
        {
            throw PropertyValidationException.forProperty(PropertyNames.MANNEQUIN_HIDE_DESCRIPTION)
                    .byMethod("MannequinProperties#validateInput")
                    .withLocalizableMessage(CommandStrings.noPermissionMessage())
                    .withMessage("Player don't have permission for setting custom description")
                    .create();
        }
    }
}
