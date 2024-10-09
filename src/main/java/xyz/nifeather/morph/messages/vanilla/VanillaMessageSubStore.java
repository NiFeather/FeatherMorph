package xyz.nifeather.morph.messages.vanilla;

import org.jetbrains.annotations.NotNull;

public class VanillaMessageSubStore extends BasicVanillaMessageStore
{
    public VanillaMessageSubStore(String localeCode)
    {
        this.localeCode = localeCode;
    }

    private final String localeCode;

    @Override
    protected @NotNull String getLocaleCode()
    {
        return localeCode;
    }
}
