package xyz.nifeather.morph.abilities.options;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.misc.disguiseProperty.InputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class AttributeModifyOption implements ISkillAbilityOption
{
    public static class AttributeModifyOptionHandler implements ISkillAbilityOptionHandler<AttributeModifyOption>
    {
        @Override
        public Class<AttributeModifyOption> getOptionClass()
        {
            return AttributeModifyOption.class;
        }

        @Override
        public void writeOption(AttributeModifyOption option, @NotNull Map<String, Object> gsonMap)
        {
            List<Map<String, Object>> attributeInfoOptionList = new ObjectArrayList<>();
            for (AttributeInfo modifier : option.modifiers)
            {
                var map = new HashMap<String, Object>();
                ATTRIBUTE_INFO_OPTION_HANDLER.writeOption(modifier, map);

                attributeInfoOptionList.add(map);
            }

            gsonMap.put("modifiers", attributeInfoOptionList);
        }

        @Override
        public @NotNull AttributeModifyOption readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            var clazzSimpleName = this.getClass().getSimpleName();
            List<?> rawModifierList = utilGetTypedOrThrow("modifiers", gsonMap, List.class);

            List<AttributeInfo> attributeInfos = new ObjectArrayList<>();
            for (Object o : rawModifierList)
            {
                if (!(o instanceof Map<?,?> rawMap))
                    throw new ParseErrorException(clazzSimpleName, "Attribute modifiers contain at least one non-map value");

                Map<String, Object> converted = new HashMap<>();
                rawMap.forEach((o1, o2) -> converted.put(o1.toString(), o2));

                var instance = ATTRIBUTE_INFO_OPTION_HANDLER.readOption(converted);
                attributeInfos.add(instance);
            }

            return new AttributeModifyOption(attributeInfos);
        }
    }

    public static final AttributeModifyOptionHandler OPTION_HANDLER = new AttributeModifyOptionHandler();

    public AttributeModifyOption(List<AttributeInfo> list)
    {
        this.modifiers.addAll(list);
    }

    public final List<AttributeInfo> modifiers = new CopyOnWriteArrayList<>();

    public boolean isValid()
    {
        return modifiers.stream().allMatch(AttributeInfo::isValid);
    }

    public static AttributeModifyOption from(Attribute attribute, OperationType operationType, double value)
    {
        var info = new AttributeInfo(attribute.key().asString(), operationType, value);
        return new AttributeModifyOption(List.of(info));
    }

    public AttributeModifyOption with(Attribute attribute, OperationType operationType, double value)
    {
        var info = new AttributeInfo(attribute.key().asString(), operationType, value);
        this.modifiers.add(info);
        return this;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;

        if (!(obj instanceof AttributeModifyOption option)) return false;

        return this.modifiers.equals(option.modifiers);
    }

    public enum OperationType
    {
        invalid,
        add,
        multiply,
        multiply_base;

        public AttributeModifier.Operation toBukkitOperation()
        {
            return switch (this)
            {
                case add -> AttributeModifier.Operation.ADD_NUMBER;
                case multiply -> AttributeModifier.Operation.ADD_SCALAR;
                case multiply_base -> AttributeModifier.Operation.MULTIPLY_SCALAR_1;
                default -> null;
            };
        }
    }

    public static class AttributeInfoOptionHandler implements ISkillAbilityOptionHandler<AttributeInfo>
    {
        @Override
        public Class<AttributeInfo> getOptionClass()
        {
            return AttributeInfo.class;
        }

        @Override
        public void writeOption(AttributeInfo option, @NotNull Map<String, Object> gsonMap)
        {
            gsonMap.put("name", option.attributeName);
            gsonMap.put("type", option.operationType.name().toLowerCase());
            gsonMap.put("value", option.value);
        }

        @Override
        public @NotNull AttributeInfo readOption(@NotNull Map<String, Object> gsonMap) throws ParseErrorException
        {
            var clazzSimpleName = this.getClass().getSimpleName();

            String name = utilGetTypedOrThrow("name", gsonMap, String.class);
            String typeName = utilGetTypedOrThrow("type", gsonMap, String.class);
            double value = utilGetTypedOrThrow("value", gsonMap, Number.class).doubleValue();

            if (!Double.isFinite(value))
                throw new ParseErrorException(clazzSimpleName, "Non-Finite attribute value");

            OperationType operationType = InputHandles.readEnumNonNull(OperationType.values(), clazzSimpleName, typeName)
                    .orElseThrow(() -> new ParseErrorException(clazzSimpleName, "No value match for operationType '%s'".formatted(typeName)));

            return new AttributeInfo(name, operationType, value);
        }
    }

    public static final AttributeInfoOptionHandler ATTRIBUTE_INFO_OPTION_HANDLER = new AttributeInfoOptionHandler();

    public static class AttributeInfo implements ISkillAbilityOption
    {
        public String attributeName;
        public final OperationType operationType;
        public final double value;

        public AttributeInfo(String name, OperationType type, double value)
        {
            this.attributeName = name;
            this.operationType = type;
            this.value = value;
        }

        public boolean isValid()
        {
            return !Double.isNaN(value)
                    && operationType != OperationType.invalid
                    && attributeName != null && !attributeName.isEmpty() && !attributeName.isBlank();
        }

        @Override
        public String toString()
        {
            return "AttributeInfo{name=%s, type=%s, value=%s}".formatted(attributeName, operationType, value);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null) return false;

            if (!(obj instanceof AttributeInfo info)) return false;

            return this.value == info.value
                    && Objects.equals(this.attributeName, info.attributeName)
                    && this.operationType == info.operationType;
        }
    }
}
