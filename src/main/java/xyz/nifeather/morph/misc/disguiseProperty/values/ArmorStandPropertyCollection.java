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
import xyz.nifeather.morph.config.ConfigOptions;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class ArmorStandPropertyCollection extends BaseLivingEntityPropertyCollection<ArmorStand>
{
    public final SingleProperty<Boolean> SHOW_ARMS = SingleProperty.builder(PropertyNames.ARMOR_STAND_SHOW_ARMS, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withSuggestions("true", "false")
            .build();

    public final SingleProperty<Boolean> HAS_BASE_PLATE = SingleProperty.builder(PropertyNames.ARMOR_STAND_HAS_BASE_PLATE, true)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withSuggestions("true", "false")
            .build();

    public final SingleProperty<Boolean> SMALL = SingleProperty.builder(PropertyNames.ARMOR_STAND_SMALL, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withSuggestions("true", "false")
            .build();

    public final SingleProperty<Rotations> HEAD_ROTATION = SingleProperty.builder(PropertyNames.ARMOR_STAND_HEAD_ROTATION, Rotations.ZERO)
            .withInputHandle(InputHandles::readRotations)
            .withOutputHandle(OutputHandles::writeRotations)
            .build();

    public final SingleProperty<Rotations> BODY_ROTATION = SingleProperty.builder(PropertyNames.ARMOR_STAND_BODY_ROTATION, Rotations.ZERO)
            .withInputHandle(InputHandles::readRotations)
            .withOutputHandle(OutputHandles::writeRotations)
            .build();

    public final SingleProperty<Rotations> RIGHT_ARM_ROTATION = SingleProperty.builder(PropertyNames.ARMOR_STAND_RIGHT_ARM_ROTATION, Rotations.ZERO)
            .withInputHandle(InputHandles::readRotations)
            .withOutputHandle(OutputHandles::writeRotations)
            .build();

    public final SingleProperty<Rotations> LEFT_ARM_ROTATION = SingleProperty.builder(PropertyNames.ARMOR_STAND_LEFT_ARM_ROTATION, Rotations.ZERO)
            .withInputHandle(InputHandles::readRotations)
            .withOutputHandle(OutputHandles::writeRotations)
            .build();

    public final SingleProperty<Rotations> RIGHT_LEG_ROTATION = SingleProperty.builder(PropertyNames.ARMOR_STAND_RIGHT_LEG_ROTATION, Rotations.ZERO)
            .withInputHandle(InputHandles::readRotations)
            .withOutputHandle(OutputHandles::writeRotations)
            .build();

    public final SingleProperty<Rotations> LEFT_LEG_ROTATION = SingleProperty.builder(PropertyNames.ARMOR_STAND_LEFT_LEG_ROTATION, Rotations.ZERO)
            .withInputHandle(InputHandles::readRotations)
            .withOutputHandle(OutputHandles::writeRotations)
            .build();

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

        configManager.bind(showArms, ConfigOptions.ARMORSTAND_SHOW_ARMS);
        this.config = configManager;
    }

    public ArmorStandPropertyCollection()
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
        super.setupPropertiesFromEntity(propertyHandler, armorStand);

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
