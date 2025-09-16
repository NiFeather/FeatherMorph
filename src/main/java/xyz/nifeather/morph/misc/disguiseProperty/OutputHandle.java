package xyz.nifeather.morph.misc.disguiseProperty;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@FunctionalInterface
public interface OutputHandle<T>
{
    @NotNull
    String handle(String propertyName, T input) throws ParseErrorException;
}
