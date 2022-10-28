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
    REVERSE_BEHAVIOR_SWING_HANDS(reverseControlBehaviorNode().append("swing_hands"), true),
    REVERSE_BEHAVIOR_SNEAK(reverseControlBehaviorNode().append("sneak"), false),
    REVERSE_BEHAVIOR_SWAP_HAND(reverseControlBehaviorNode().append("swap_hands"), false),
    REVERSE_BEHAVIOR_DROP(reverseControlBehaviorNode().append("allow_drop"), false),
    REVERSE_BEHAVIOR_HOTBAR(reverseControlBehaviorNode().append("hotbar"), false),

    BANNED_DISGUISES(ConfigNode.create().append("bannedDisguises"), new ArrayList<String>()),

    DISPLAY_BOSSBAR(bossbarNode().append("enabled"), true),
    BOSSBAR_RANGE(bossbarNode().append("range"), -1),

    ALLOW_LD_DISGUISES(ConfigNode.create().append("enable_ld_custom_disguises"), false),

    LANGUAGE_CODE(languageNode().append("code"), "zh_cn"),
    LANGUAGE_ALLOW_FALLBACK(languageNode().append("cast_translatable"), true),

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

    private static ConfigNode reverseControlNode()
    {
        return ConfigNode.create().append("reverseControl");
    }

    private static ConfigNode reverseControlBehaviorNode()
    {
        return reverseControlNode().append("behaviors");
    }

    private static ConfigNode bossbarNode()
    {
        return ConfigNode.create().append("bossbar");
    }

    private static ConfigNode languageNode()
    {
        return ConfigNode.create().append("language");
    }
}
