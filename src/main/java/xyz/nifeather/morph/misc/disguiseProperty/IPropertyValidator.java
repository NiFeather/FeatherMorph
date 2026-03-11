package xyz.nifeather.morph.misc.disguiseProperty;

import org.bukkit.entity.Entity;

import java.util.EnumSet;

@FunctionalInterface
public interface IPropertyValidator<X>
{
    /**
     * Validate the given value input from the player.
     * @param value The value that's parsed by an instance of {@link SingleProperty}
     * @param player The player that's giving this input
     * @param validationSkipFlags Validation flags, see {@link ValidationSkipFlag}
     * @throws PropertyValidationException If the given input is invalid, or not legal, or malformed
     */
    void validate(X value, Entity player, EnumSet<ValidationSkipFlag> validationSkipFlags) throws PropertyValidationException;
}
