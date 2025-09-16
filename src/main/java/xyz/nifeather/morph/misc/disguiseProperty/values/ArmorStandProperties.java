package xyz.nifeather.morph.misc.disguiseProperty.values;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.papermc.paper.math.Rotations;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Bindables.Bindable;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class ArmorStandProperties extends BaseLivingEntityProperties<ArmorStand>
{
    public final SingleProperty<Boolean> SHOW_ARMS = createProperty(PropertyNames.ARMOR_STAND_SHOW_ARMS, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean)
            .withValidInput("true", "false");
    public final SingleProperty<Boolean> HAS_BASE_PLATE = createProperty(PropertyNames.ARMOR_STAND_HAS_BASE_PLATE, true, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean)
            .withValidInput("true", "false");
    public final SingleProperty<Boolean> SMALL = createProperty(PropertyNames.ARMOR_STAND_SMALL, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean)
            .withValidInput("true", "false");

    public final SingleProperty<Rotations> HEAD_ROTATION = createProperty(PropertyNames.ARMOR_STAND_HEAD_ROTATION, Rotations.ZERO, InputHandles::readRotations, OutputHandles::writeRotations);
    public final SingleProperty<Rotations> BODY_ROTATION = createProperty(PropertyNames.ARMOR_STAND_BODY_ROTATION, Rotations.ZERO, InputHandles::readRotations, OutputHandles::writeRotations);
    public final SingleProperty<Rotations> RIGHT_ARM_ROTATION = createProperty(PropertyNames.ARMOR_STAND_RIGHT_ARM_ROTATION, Rotations.ZERO, InputHandles::readRotations, OutputHandles::writeRotations);
    public final SingleProperty<Rotations> LEFT_ARM_ROTATION = createProperty(PropertyNames.ARMOR_STAND_LEFT_ARM_ROTATION, Rotations.ZERO, InputHandles::readRotations, OutputHandles::writeRotations);
    public final SingleProperty<Rotations> RIGHT_LEG_ROTATION = createProperty(PropertyNames.ARMOR_STAND_RIGHT_LEG_ROTATION, Rotations.ZERO, InputHandles::readRotations, OutputHandles::writeRotations);
    public final SingleProperty<Rotations> LEFT_LEG_ROTATION = createProperty(PropertyNames.ARMOR_STAND_LEFT_LEG_ROTATION, Rotations.ZERO, InputHandles::readRotations, OutputHandles::writeRotations);

    @Nullable
    private volatile MorphConfigManager config;
    private final Bindable<Boolean> showArms = new Bindable<>(false);

    private void lateInitConfig()
    {
        if (config != null)
            return;

        var api = FeatherMorphAPI.instance();
        if (api == null)
            return;

        var configManager = api.directAccess().getGlobalDependency(MorphConfigManager.class, false);
        if (configManager == null)
            return;

        configManager.bind(showArms, ConfigOption.ARMORSTAND_SHOW_ARMS);
        this.config = configManager;
    }

    public ArmorStandProperties()
    {
        registerSingle(SHOW_ARMS, HAS_BASE_PLATE, SMALL);

        registerSingle(HEAD_ROTATION, BODY_ROTATION, RIGHT_ARM_ROTATION, LEFT_ARM_ROTATION, RIGHT_LEG_ROTATION, LEFT_LEG_ROTATION);
    }

    @Override
    protected @Nullable ArmorStand tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof ArmorStand armorStand ? armorStand : null;
    }

    @Override
    public void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull ArmorStand armorStand)
    {
        propertyHandler.set(SHOW_ARMS, armorStand.hasArms());
        propertyHandler.set(HAS_BASE_PLATE, armorStand.hasBasePlate());
        propertyHandler.set(SMALL, armorStand.isSmall());

        propertyHandler.set(HEAD_ROTATION, armorStand.getHeadRotations());
        propertyHandler.set(BODY_ROTATION, armorStand.getBodyRotations());
        propertyHandler.set(LEFT_ARM_ROTATION, armorStand.getLeftArmRotations());
        propertyHandler.set(RIGHT_ARM_ROTATION, armorStand.getRightArmRotations());
        propertyHandler.set(LEFT_LEG_ROTATION, armorStand.getLeftLegRotations());
        propertyHandler.set(RIGHT_LEG_ROTATION, armorStand.getRightLegRotations());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        lateInitConfig();
        propertyHandler.set(SHOW_ARMS, showArms.get());
    }

    private final Gson gson = new GsonBuilder().create();

    private String rotationToStringArray(Rotations vec)
    {
        float[] array = new float[] {(float)vec.x(), (float)vec.y(), (float)vec.z()};
        return gson.toJson(array);
    }

}
