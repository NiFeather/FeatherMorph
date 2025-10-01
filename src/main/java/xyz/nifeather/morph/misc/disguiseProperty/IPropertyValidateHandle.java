package xyz.nifeather.morph.misc.disguiseProperty;

import java.util.Map;

@FunctionalInterface
public interface IPropertyValidateHandle
{
    void validate(Map<SingleProperty<?>, Object> parsedProperties) throws PropertyValidationException;
}
