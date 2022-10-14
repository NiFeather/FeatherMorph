package xiamomc.morph.config;

import xiamomc.pluginbase.Configuration.ConfigNode;

public enum ConfigOption
{
    ALLOW_HEAD_MORPH(ConfigNode.create().Append("allowHeadMorph"), true),
    ALLOW_CHAT_OVERRIDE(ConfigNode.create().Append("chatOverride").Append("allowOverride"), false),
    CHAT_OVERRIDE_USE_CUSTOM_RENDERER(ConfigNode.create().Append("chatOverride").Append("UseCustomRenderer"), true);

    public final ConfigNode node;
    public final Object defaultValue;

    private ConfigOption(ConfigNode node, Object defaultValue)
    {
        this.node = node;
        this.defaultValue = defaultValue;
    }
}
