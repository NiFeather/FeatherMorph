package xyz.nifeather.morph.misc.disguiseProperty.values;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class TextDisplayPropertyCollection extends DisplayEntityPropertyCollection<TextDisplay>
{
    public final SingleProperty<Component> TEXT = SingleProperty.builder(PropertyNames.TEXT_DISPLAY_TEXT, Component.class, Component.empty())
            .withInputHandle(InputHandles::readAdventureComponentLimited)
            .withOutputHandle(OutputHandles::writeAdventureComponentJSON)
            .withValidator(PropertyValidations::validateCustomTextPermission)
            .build();

    public final SingleProperty<Integer> LINE_WIDTH = SingleProperty.builder(PropertyNames.TEXT_DISPLAY_LINE_WIDTH, 200)
            .withInputHandle(InputHandles::readInteger)
            .withOutputHandle(OutputHandles::writeInteger)
            .build();

    public final SingleProperty<Color> BACKGROUND_COLOR = SingleProperty.builder(PropertyNames.TEXT_DISPLAY_BACKGROUND_COLOR, DyeColor.BLACK.getColor())
            .withInputHandle(InputHandles::readHexColorRGB)
            .withOutputHandle(OutputHandles::writeBukkitColor)
            .build();

    public final SingleProperty<Byte> TEXT_OPACITY = SingleProperty.builder(PropertyNames.TEXT_DISPLAY_TEXT_OPACITY, (byte) -1)
            .withInputHandle(InputHandles::readByte)
            .withOutputHandle(OutputHandles::writeByte)
            .build();

    public TextDisplayPropertyCollection()
    {
        registerSingle(TEXT, LINE_WIDTH, BACKGROUND_COLOR, TEXT_OPACITY);
    }

    @Override
    protected @Nullable TextDisplay tryCastEntity(@org.jetbrains.annotations.Nullable Entity targetEntity)
    {
        return targetEntity instanceof TextDisplay display ? display : null;
    }

    /**
     * Setup property for the given disguise from the given entity
     *
     * @param propertyHandler The {@link PropertyHandler} for the given disguise
     * @param targetEntity    The targeted entity
     */
    @Override
    public void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NonNull TextDisplay targetEntity)
    {
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }
}
