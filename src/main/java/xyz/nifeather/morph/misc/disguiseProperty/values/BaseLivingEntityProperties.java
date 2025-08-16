package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.MathUtils;

public abstract class BaseLivingEntityProperties<E extends Entity> extends AbstractProperties<E>
{
    public final SingleProperty<Component> CUSTOM_NAME = getSingle("entity/custom_name", Component.empty());

    public final SingleProperty<Boolean> CUSTOM_NAME_VISIBLE = getSingle("entity/custom_name_visible", false)
            .withValidInput("true", "false");

    public final SingleProperty<Integer> STUCKED_ARROWS = getSingle("entity/stucked_arrows", 0);

    public BaseLivingEntityProperties()
    {
        registerSingle(CUSTOM_NAME, CUSTOM_NAME_VISIBLE, STUCKED_ARROWS);
    }

    private static final Component minimessageFormatFail = Component.text("MiniMessage format error");
    private static final Component minimessageCastFail = Component.text("MiniMessage cast error");

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(CUSTOM_NAME.id()))
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
        else if (key.equals(CUSTOM_NAME_VISIBLE.id()))
        {
            return Pair.of(CUSTOM_NAME_VISIBLE, Boolean.parseBoolean(value));
        }
        else if (key.equals(STUCKED_ARROWS.id()))
        {
            int val = MathUtils.clamp(0, 100, MathUtils.parseIntOr(value, 0));
            return Pair.of(STUCKED_ARROWS, val);
        }

        return null;
    }
}
