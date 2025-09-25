package xyz.nifeather.morph.misc.disguiseProperty.values;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class MannequinProperties extends BaseLivingEntityProperties<Mannequin>
{
    public final SingleProperty<Component> NPC_DESCRIPTION = createProperty(PropertyNames.MANNEQUIN_NPC_DESCRIPTION, Component.empty(), InputHandles::readAdventureComponent, OutputHandles::writeAdventureComponentJSON);
    public final SingleProperty<String> SKIN = createProperty(PropertyNames.MANNEQUIN_SKIN, "Dinnerbone", InputHandles::readSkinName, OutputHandles::writeString);
    public final SingleProperty<Boolean> IMMOVABLE = createProperty(PropertyNames.MANNEQUIN_IMMOVABLE, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean);

    public MannequinProperties()
    {
        registerSingle(NPC_DESCRIPTION, SKIN, IMMOVABLE);
    }

    @Override
    protected @Nullable Mannequin tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Mannequin mannequin ? mannequin : null;
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(CUSTOM_NAME_VISIBLE, true);
    }
}
