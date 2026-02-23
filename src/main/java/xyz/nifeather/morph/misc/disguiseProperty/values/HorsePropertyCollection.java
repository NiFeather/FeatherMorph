package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class HorsePropertyCollection extends BaseLivingEntityPropertyCollection<Horse>
{
    public final SingleProperty<Horse.Color> COLOR;
    public final SingleProperty<Horse.Style> STYLE;
    public final SingleProperty<Boolean> SADDLED = SingleProperty.builder(PropertyNames.HORSE_SADDLED, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withPostProcess(this::putSaddleIfPossible)
            .withSuggestions("true", "false")
            .build();

    public final SingleProperty<HorseArmorTier> HORSE_ARMOR = SingleProperty.builder(PropertyNames.HORSE_ARMOR, HorseArmorTier.LEATHER)
            .withInputHandle(this::readArmorTier)
            .withOutputHandle(OutputHandles::writeEnum)
            .withPostProcess(this::handleHorseArmor)
            .withSuggestions(Arrays.stream(HorseArmorTier.values()).map(t -> t.name().toLowerCase()).toList())
            .build();

    private void handleHorseArmor(HorseArmorTier armorTier, PropertyHandler propertyHandler)
    {
        var equipment = propertyHandler.get(EQUIPMENT)
                .cloneForEdit()
                .forSlot(EquipmentSlot.BODY, armorTier.armorSupplier.get())
                .build();

        propertyHandler.set(EQUIPMENT, equipment);
        propertyHandler.set(DISPLAY_DISGUISE_EQUIPMENT, true);
    }

    private Optional<HorseArmorTier> readArmorTier(String name, String value)
            throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(HorseArmorTier.values(), name, value);
    }

    private void putSaddleIfPossible(Boolean saddled, PropertyHandler propertyHandler)
    {
        if (!saddled) return;

        var equipment = propertyHandler.get(EQUIPMENT)
                .cloneForEdit()
                .forSlot(EquipmentSlot.SADDLE, ItemStack.of(Material.SADDLE))
                .build();

        propertyHandler.set(EQUIPMENT, equipment);
        propertyHandler.set(DISPLAY_DISGUISE_EQUIPMENT, true);
    }

    private final Map<String, Horse.Color> colorMap = new ConcurrentHashMap<>();
    private final Map<String, Horse.Style> styleMap = new ConcurrentHashMap<>();

    private void initMaps()
    {
        for (var color : Horse.Color.values())
            colorMap.put(color.name().toLowerCase(), color);

        for (var style : Horse.Style.values())
            styleMap.put(style.name().toLowerCase(), style);
    }

    private Optional<Horse.Color> readHorseColor(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Horse.Color.values(), propertyName, string);
    }

    private Optional<Horse.Style> readHorseStyle(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Horse.Style.values(), propertyName, string);
    }

    public HorsePropertyCollection()
    {
        initMaps();

        COLOR = SingleProperty.builder(PropertyNames.HORSE_COLOR, Horse.Color.WHITE)
                .withInputHandle(this::readHorseColor)
                .withOutputHandle(OutputHandles::writeEnum)
                .withRandom(Horse.Color.values())
                .withSuggestions(colorMap.keySet())
                .build();

        STYLE = SingleProperty.builder(PropertyNames.HORSE_STYLE, Horse.Style.NONE)
                .withInputHandle(this::readHorseStyle)
                .withOutputHandle(OutputHandles::writeEnum)
                .withRandom(Horse.Style.values())
                .withSuggestions(styleMap.keySet())
                .build();

        registerSingle(COLOR, STYLE, SADDLED, HORSE_ARMOR);
    }

    @Override
    protected @Nullable Horse tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Horse horse ? horse : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Horse horse)
    {
        super.setupPropertiesFromEntity(propertyHandler, horse);

        propertyHandler.set(COLOR, horse.getColor());
        propertyHandler.set(STYLE, horse.getStyle());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(COLOR, DisguiseUtils.pick(COLOR.randomValues()));
        propertyHandler.set(STYLE, DisguiseUtils.pick(STYLE.randomValues()));
    }

    public enum HorseArmorTier
    {
        LEATHER(() -> ItemStack.of(Material.LEATHER_HORSE_ARMOR)),
        COPPER(() -> ItemStack.of(Material.COPPER_HORSE_ARMOR)),
        IRON(() -> ItemStack.of(Material.IRON_HORSE_ARMOR)),
        GOLD(() -> ItemStack.of(Material.GOLDEN_HORSE_ARMOR)),
        DIAMOND(() -> ItemStack.of(Material.DIAMOND_HORSE_ARMOR)),
        NETHERITE(() -> ItemStack.of(Material.NETHERITE_HORSE_ARMOR));

        public final Supplier<ItemStack> armorSupplier;

        HorseArmorTier(Supplier<ItemStack> supplier)
        {
            this.armorSupplier = supplier;
        }
    }
}
