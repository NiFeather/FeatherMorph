package xiamomc.morph.config;

import xiamomc.pluginbase.Configuration.ConfigNode;

import java.util.ArrayList;

public enum ConfigOption
{
    ALLOW_HEAD_MORPH(ConfigNode.create().append("allowHeadMorph"), true),
    ALLOW_CHAT_OVERRIDE(ConfigNode.create().append("chatOverride").append("allowOverride"), false),
    CHAT_OVERRIDE_USE_CUSTOM_RENDERER(ConfigNode.create().append("chatOverride").append("UseCustomRenderer"), true),

    SKILL_COOLDOWN_ON_DAMAGE(ConfigNode.create().append("cooldown_on_damage"), 15),
    ACTION_ITEM(ConfigNode.create().append("action_item"), "minecraft:carrot_on_a_stick"),

    REVERSE_CONTROL_DISTANCE(reverseControlNode().append("normalDistance"), -1),
    REVERSE_CONTROL_DISTANCE_IMMUNE(reverseControlNode().append("immuneDistance"), 16),
    REVERSE_CONTROL_IMMUNE_ITEM(reverseControlNode().append("immuneItem"), "minecraft:golden_helmet"),

    REVERSE_BEHAVIOR_DO_SIMULATION(reverseControlBehaviorNode().append("simulate_interactions"), false),

    REVRSE_BEHAVIOR_SWING_HANDS(reverseControlBehaviorNode().append("swing_hands"), true),

    BANNED_DISGUISES(ConfigNode.create().append("bannedDisguises"), new ArrayList<String>()),

    VERSION(ConfigNode.create().append("version"), 0);

    public final ConfigNode node;
    public final Object defaultValue;

    private ConfigOption(ConfigNode node, Object defaultValue)
    {
        this.node = node;
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString()
    {
        return node.toString();
    }

    public static ConfigNode reverseControlNode()
    {
        return ConfigNode.create().append("reverseControl");
    }

    public static ConfigNode reverseControlBehaviorNode()
    {
        return reverseControlNode().append("behaviors");
    }
}
