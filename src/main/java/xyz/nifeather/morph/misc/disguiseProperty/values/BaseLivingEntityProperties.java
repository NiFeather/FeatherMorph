package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.MathUtils;

import java.util.Map;

public abstract class BaseLivingEntityProperties<E extends Entity> extends AbstractProperties<E>
{
    public final SingleProperty<Component> CUSTOM_NAME = getSingle(PropertyNames.ENTITY_CUSTOM_NAME, Component.empty());

    public final SingleProperty<Boolean> CUSTOM_NAME_VISIBLE = getSingle(PropertyNames.ENTITY_CUSTOM_NAME_VISIBLE, false)
            .withValidInput("true", "false");

    public final SingleProperty<Integer> STUCKED_ARROWS = getSingle(PropertyNames.ENTITY_ARROW_COUNT, 0);

    public BaseLivingEntityProperties()
    {
        registerSingle(CUSTOM_NAME, CUSTOM_NAME_VISIBLE, STUCKED_ARROWS);
    }

    private static final Component minimessageFormatFail = Component.text("MiniMessage format error");
    private static final Component minimessageCastFail = Component.text("MiniMessage cast error");

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(PropertyNames.ENTITY_CUSTOM_NAME))
        {
            try
            {
                var component = (TextComponent) MiniMessage.miniMessage().deserialize(value);
                return Pair.of(CUSTOM_NAME, component);
            }
            catch (ClassCastException e)
            {
                return Pair.of(CUSTOM_NAME, minimessageCastFail);
            }
            catch (Throwable t)
            {
                return Pair.of(CUSTOM_NAME, minimessageFormatFail);
            }
        }
        else if (key.equals(PropertyNames.ENTITY_CUSTOM_NAME_VISIBLE))
        {
            return Pair.of(CUSTOM_NAME_VISIBLE, Boolean.parseBoolean(value));
        }
        else if (key.equals(PropertyNames.ENTITY_ARROW_COUNT))
        {
            int val = MathUtils.clamp(0, 100, MathUtils.parseIntOr(value, 0));
            return Pair.of(STUCKED_ARROWS, val);
        }

        return null;
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        propertyHandler.getOptional(CUSTOM_NAME).ifPresent(component ->
                map.put(CUSTOM_NAME.id(), JSONComponentSerializer.json().serialize(component)));

        propertyHandler.getOptional(CUSTOM_NAME_VISIBLE).ifPresent(v -> map.put(CUSTOM_NAME_VISIBLE.id(), v.toString().toLowerCase()));

        propertyHandler.getOptional(STUCKED_ARROWS).ifPresent(v -> map.put(STUCKED_ARROWS.id(), v.toString()));
    }
}
