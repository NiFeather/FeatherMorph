package xyz.nifeather.morph.misc.disguiseProperty;

import java.util.Optional;

public interface InputHandle<T>
{
    public Optional<T> handle(String propertyName, String input) throws ParseErrorException;
}
