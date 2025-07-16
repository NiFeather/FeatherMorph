package xyz.nifeather.morph.misc.sentry;

import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;

public class SentryConfigHandler extends MorphPluginObject
{
    private final SentryLogger sentryLogger;
    private final Bindable<Boolean> enableSentry = new Bindable<>(false);

    public SentryConfigHandler(SentryLogger sentryLogger)
    {
        this.sentryLogger = sentryLogger;
    }

    @Initializer
    private void load(MorphConfigManager config)
    {
        enableSentry.onValueChanged((o, n) -> sentryLogger.enabled(n));
        config.bind(enableSentry, ConfigOption.ENABLE_SENTRY_LOGGER);
    }
}
