package xyz.nifeather.morph.misc.disguiseProperty;

import org.bukkit.entity.Player;

import java.util.EnumSet;

@FunctionalInterface
public interface IPropertyValidator<X>
{
    void validate(X value, Player player, EnumSet<ValidationFlag> validationFlags) throws PropertyValidationException;
}
