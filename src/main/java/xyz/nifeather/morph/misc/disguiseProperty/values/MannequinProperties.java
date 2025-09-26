package xyz.nifeather.morph.misc.disguiseProperty.values;

import com.mojang.authlib.GameProfile;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.GameProfileUtils;
import xyz.nifeather.morph.utilities.Uuids;

public class MannequinProperties extends BaseLivingEntityProperties<Mannequin>
{
    public final SingleProperty<Component> NPC_DESCRIPTION = createProperty(PropertyNames.MANNEQUIN_NPC_DESCRIPTION, Component.empty(), InputHandles::readAdventureComponent, OutputHandles::writeAdventureComponentJSON);
    public final SingleProperty<Boolean> HIDE_DESCRIPTION = createProperty(PropertyNames.MANNEQUIN_HIDE_DESCRIPTION, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean);
    public final SingleProperty<Boolean> IMMOVABLE = createProperty(PropertyNames.MANNEQUIN_IMMOVABLE, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean);

    public final SingleProperty<ResolvableProfile> SKIN_INTERNAL = SingleProperty.of(PropertyNames.MANNEQUIN_SKIN_INTERNAL, GameProfileUtils.asResolvableProfile(new GameProfile(Uuids.NIL_UUID, "unknown")), InputHandles::immediateException, OutputHandles::writeResolvableProfileAny, true);
    public final SingleProperty<String> SKIN_NAME = createProperty(PropertyNames.MANNEQUIN_SKIN, "Notch", InputHandles::readSkinName, OutputHandles::writeString);

    public MannequinProperties()
    {
        registerSingle(NPC_DESCRIPTION, HIDE_DESCRIPTION, SKIN_INTERNAL, SKIN_NAME, IMMOVABLE);
    }

    @Override
    protected @Nullable Mannequin tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Mannequin mannequin ? mannequin : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Mannequin targetEntity)
    {
        propertyHandler.set(SKIN_INTERNAL, targetEntity.getProfile());

        var description = targetEntity.getDescription();
        if (description != null)
            propertyHandler.set(NPC_DESCRIPTION, description);

        propertyHandler.set(IMMOVABLE, targetEntity.isImmovable());

        super.setupPropertiesFromEntity(propertyHandler, targetEntity);
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        //propertyHandler.set(CUSTOM_NAME_VISIBLE, true);
    }
}
