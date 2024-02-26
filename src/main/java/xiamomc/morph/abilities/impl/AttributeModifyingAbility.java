package xiamomc.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.abilities.options.AttributeModifyOption;
import xiamomc.morph.misc.DisguiseState;

import java.util.Arrays;
import java.util.UUID;

public class AttributeModifyingAbility extends MorphAbility<AttributeModifyOption>
{
    /**
     * 获取此被动技能的ID
     *
     * @return {@link NamespacedKey}
     */
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.ATTRIBUTE;
    }

    @Override
    protected @NotNull AttributeModifyOption createOption()
    {
        return new AttributeModifyOption();
    }

    @Override
    public boolean applyToPlayer(Player player, DisguiseState state)
    {
        var option = this.getOptionFor(state);

        if (option == null)
        {
            logger.error("No attribute modifier option found for %s!".formatted(state.getDisguiseIdentifier()));
            return false;
        }

        for (var modifierOption : option.modifiers)
        {
            var attributeInstance = this.getInstanceFor(player, modifierOption);

            if (attributeInstance == null)
                continue;

            var modifier = new AttributeModifier(UUID.randomUUID(), attributeModifierName,
                    modifierOption.value, modifierOption.operationType.toBukkitOperation());

            attributeInstance.addModifier(modifier);
        }

        return super.applyToPlayer(player, state);
    }

    public static final String attributeModifierName = "FMAbilityModifier";

    @Override
    public boolean revokeFromPlayer(Player player, DisguiseState state)
    {
        if (!super.revokeFromPlayer(player, state))
            return false;

        var option = this.getOptionFor(state);
        if (option == null) return true;

        for (var attributeInfo : option.modifiers)
        {
            var attributeInstance = this.getInstanceFor(player, attributeInfo);

            if (attributeInstance == null) continue;

            var modifiers = attributeInstance.getModifiers().stream()
                    .filter(a -> a.getName().equals(attributeModifierName))
                    .toList();

            modifiers.forEach(attributeInstance::removeModifier);
        }

        return true;
    }

    @Nullable
    private AttributeInstance getInstanceFor(Player player, AttributeModifyOption.AttributeInfo option)
    {
        if (!option.isValid())
        {
            logger.error("Invalid attribute modifier option: " + option);
            return null;
        }

        var attribute = Arrays.stream(Attribute.values())
                .filter(a -> a.getKey().equals(NamespacedKey.fromString(option.attributeName)))
                .findFirst().orElse(null); //(option.attributeName);

        if (attribute == null)
        {
            logger.error("No such modifier: " + option.attributeName);
            return null;
        }

        var attributeInstance = player.getAttribute(attribute);

        if (attributeInstance == null)
        {
            logger.error("No such instance for attribute: " + option.attributeName);
            return null;
        }

        return attributeInstance;
    }
}
