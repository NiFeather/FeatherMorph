package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.entity.Display;
import org.joml.Vector3f;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Arrays;
import java.util.Optional;

public abstract class DisplayEntityPropertyCollection<E extends Display> extends PropertyCollection<E>
{
    public final SingleProperty<Float> WIDTH = SingleProperty.builder(PropertyNames.DISPLAY_WIDTH, 1f)
            .withInputHandle(InputHandles::readFloatStrict)
            .withOutputHandle(OutputHandles::writeFloat)
            .hideFromUserInput(true)
            .build();

    public final SingleProperty<Float> HEIGHT = SingleProperty.builder(PropertyNames.DISPLAY_HEIGHT, 1f)
            .withInputHandle(InputHandles::readFloatStrict)
            .withOutputHandle(OutputHandles::writeFloat)
            .hideFromUserInput(true)
            .build();

    public final SingleProperty<Vector3f> SCALE = SingleProperty.builder(PropertyNames.DISPLAY_SCALE, new Vector3f(1, 1, 1))
            .withInputHandle(InputHandles::readVector3fRelaxed)
            .withOutputHandle(OutputHandles::writeVector3f)
            .build();

    public final SingleProperty<Color> GLOW_COLOR = SingleProperty.builder(PropertyNames.DISPLAY_GLOW_COLOR, DyeColor.WHITE.getColor())
            .withInputHandle(InputHandles::readHexColorRGB)
            .withOutputHandle(OutputHandles::writeBukkitColor)
            .withSuggestions(Arrays.stream(DyeColor.values()).map(c -> c.name().toLowerCase()).toList())
            .build();

    public final SingleProperty<Float> SHADOW_RADIUS = SingleProperty.builder(PropertyNames.DISPLAY_SHADOW_RADIUS, 0f)
            .withInputHandle(InputHandles::readFloatStrict)
            .withOutputHandle(OutputHandles::writeFloat)
            .build();

    public final SingleProperty<Float> SHADOW_STRENGTH = SingleProperty.builder(PropertyNames.DISPLAY_SHADOW_STRENGTH, 1f)
            .withInputHandle(InputHandles::readFloatStrict)
            .withOutputHandle(OutputHandles::writeFloat)
            .build();

    // See client LightTexture#FULL_BRIGHT
    public final SingleProperty<Integer> LIGHT_OVERRIDE = SingleProperty.builder(PropertyNames.DISPLAY_LIGHT_OVERRIDE, 15728880)
            .withInputHandle(InputHandles::readLight)
            .withOutputHandle(OutputHandles::writeLight)
            .build();

    public final SingleProperty<Vector3f> TRANSLATION = SingleProperty.builder(PropertyNames.DISPLAY_TRANSLATION, new Vector3f(0))
            .withInputHandle(InputHandles::readVector3fRelaxed)
            .withOutputHandle(OutputHandles::writeVector3f)
            .build();

    public final SingleProperty<Display.Billboard> BILLBOARD = SingleProperty.builder(PropertyNames.DISPLAY_BILLBOARD, Display.Billboard.FIXED)
            .withInputHandle(this::readBillboard)
            .withOutputHandle(OutputHandles::writeEnum)
            .withSuggestions(Arrays.stream(Display.Billboard.values()).map(c -> c.name().toLowerCase()).toList())
            .build();

    private Optional<Display.Billboard> readBillboard(String propertyName, String input)
            throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Display.Billboard.values(), propertyName, input);
    }

    public DisplayEntityPropertyCollection()
    {
        registerSingle(WIDTH, HEIGHT, SCALE, GLOW_COLOR, SHADOW_RADIUS, SHADOW_STRENGTH, LIGHT_OVERRIDE, TRANSLATION, BILLBOARD);
    }
}
