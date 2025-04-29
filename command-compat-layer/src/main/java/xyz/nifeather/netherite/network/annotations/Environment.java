package xyz.nifeather.netherite.network.annotations;

import xyz.nifeather.netherite.network.annotations.EnvironmentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
public @interface Environment
{
    EnvironmentType value();
}

