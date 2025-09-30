package xyz.nifeather.morph.misc.disguiseProperty;

import xyz.nifeather.morph.misc.disguiseProperty.values.AbstractProperties;

import java.util.Map;

@FunctionalInterface
public interface PropertyValidator
{
    void validate(AbstractProperties<?> properties, Map<SingleProperty<?>, Object> map) throws ParseErrorException;
}
