package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Llama.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class LlamaPropertyCollection extends BaseLivingEntityPropertyCollection<Llama>
{
    private final Map<String, Color> colorMap = new ConcurrentHashMap<>();

    private void initMaps()
    {
        for (var value : Color.values())
            colorMap.put(value.name().toLowerCase(), value);
    }

    public final SingleProperty<Llama.Color> COLOR;

    private Optional<Color> readLlamaColor(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Color.values(), propertyName, string);
    }

    public LlamaPropertyCollection()
    {
        initMaps();
        COLOR = SingleProperty.builder(PropertyNames.LLAMA_COLOR, Color.CREAMY)
                .withInputHandle(this::readLlamaColor)
                .withOutputHandle(OutputHandles::writeEnum)
                .withRandom(Color.values())
                .withSuggestions(colorMap.keySet())
                .build();

        registerSingle(COLOR);
    }

    @Override
    protected @Nullable Llama tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Llama llama ? llama : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Llama llama)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, llama);

        propertyHandler.set(COLOR, llama.getColor());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(COLOR, DisguiseUtils.pick(COLOR.randomValues()));
    }

}
