package xyz.nifeather.morph.misc;

import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.Optional;

public interface IMaybeUserFriendlyException
{
    Optional<FormattableMessage> localizableMessage();

    String underlyingMessage();
}
