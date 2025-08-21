package xyz.nifeather.morph.misc.disguiseProperty;

import java.util.Optional;

@FunctionalInterface
public interface InputHandle<T>
{
    Optional<T> handle(String propertyName, String input) throws ParseErrorException;
}
