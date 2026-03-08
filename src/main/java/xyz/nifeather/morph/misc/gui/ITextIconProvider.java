package xyz.nifeather.morph.misc.gui;

import net.kyori.adventure.text.Component;

import java.util.Optional;

@FunctionalInterface
public interface ITextIconProvider
{
    Optional<Component> resolve(String disguiseIdentifier);
}
