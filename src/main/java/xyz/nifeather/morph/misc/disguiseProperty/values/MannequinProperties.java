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
    public final SingleProperty<Component> NPC_DESCRIPTION = SingleProperty.builder(PropertyNames.MANNEQUIN_NPC_DESCRIPTION, Component.class, Component.empty())
            .withInputHandle(InputHandles::readComponentAny)
            .withOutputHandle(OutputHandles::writeAdventureComponentJSON)
            .withValidator(PropertyValidations::validateCustomTextPermission)
            .build();

    public final SingleProperty<Boolean> HIDE_DESCRIPTION = SingleProperty.builder(PropertyNames.MANNEQUIN_HIDE_DESCRIPTION, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withValidator(PropertyValidations::validateCustomTextPermission)
            .withValidInput("true", "false")
            .build();

    /**
     * We don't suggest using this property, as it doesn't work for both Server and Mod renderer
     */
    public final SingleProperty<Boolean> IMMOVABLE = SingleProperty.builder(PropertyNames.MANNEQUIN_IMMOVABLE, false)
            .withInputHandle(InputHandles::immediateException)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withValidator(PropertyValidations::validateCustomTextPermission)
            .withValidInput("true", "false")
            .build();

    public final SingleProperty<ResolvableProfile> SKIN = SingleProperty.builder(PropertyNames.MANNEQUIN_SKIN, ResolvableProfile.class, GameProfileUtils.asResolvableProfile(new GameProfile(Uuids.NIL_UUID, "notset")))
            .withInputHandle(InputHandles::readResolvableSkinInput)
            .withOutputHandle(OutputHandles::writeResolvableProfileAny)
            .withValidator(PropertyValidations::validateMannequinCustomSkinPermission)
            .build();

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
}
