package xiamomc.morph.config;

import xiamomc.pluginbase.Configuration.ConfigNode;

import java.util.ArrayList;

public enum ConfigOption
{
    ALLOW_HEAD_MORPH(ConfigNode.create().append("allowHeadMorph"), true),
    ALLOW_CHAT_OVERRIDE(ConfigNode.create().append("chatOverride").append("allowOverride"), false),
    CHAT_OVERRIDE_USE_CUSTOM_RENDERER(ConfigNode.create().append("chatOverride").append("UseCustomRenderer"), true),

    REVERSE_CONTROL_DISTANCE(ConfigNode.create().append("reverseControl").append("normalDistance"), -1),
    REVERSE_CONTROL_DISTANCE_IMMUNE(ConfigNode.create().append("reverseControl").append("immuneDistance"), 16),
    REVERSE_CONTROL_IMMUNE_ITEM(ConfigNode.create().append("reverseControl").append("immuneItem"), "minecraft:golden_helmet"),

    BANNED_DISGUISES(ConfigNode.create().append("bannedDisguises"), new ArrayList<String>()),

    //todo: 实现配置更新
    VERSION(ConfigNode.create().append("version"), 0);

    public final ConfigNode node;
    public final Object defaultValue;

    private ConfigOption(ConfigNode node, Object defaultValue)
    {
        this.node = node;
        this.defaultValue = defaultValue;
    }
}
