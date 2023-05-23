package xiamomc.morph.config;

import xiamomc.morph.events.InteractionMirrorProcessor;
import xiamomc.morph.utilities.NbtUtils;
import xiamomc.pluginbase.Configuration.ConfigNode;

import java.util.ArrayList;

public enum ConfigOption
{
    PLUGIN_PREFIX(ConfigNode.create().append("message_pattern"), "<color:#dddddd>≡ FM » <message>"),

    ALLOW_HEAD_MORPH(ConfigNode.create().append("allowHeadMorph"), true),
    ALLOW_CHAT_OVERRIDE(ConfigNode.create().append("chatOverride").append("allowOverride"), true),
    CHAT_OVERRIDE_USE_CUSTOM_RENDERER(ConfigNode.create().append("chatOverride").append("UseCustomRenderer"), true),
    CHAT_OVERRIDE_DEFAULT_PATTERN(ConfigNode.create().append("chatOverride").append("default_pattern"), "<<who>> <message>"),

    SKILL_COOLDOWN_ON_DAMAGE(ConfigNode.create().append("cooldown_on_damage"), 15),

    @Deprecated
    ACTION_ITEM(ConfigNode.create().append("action_item"), "", true),
    SKILL_ITEM(ConfigNode.create().append("skill_item"), "minecraft:feather"),

    ARMORSTAND_SHOW_ARMS(ConfigNode.create().append("armorstand_show_arms"), true),

    MODIFY_BOUNDING_BOX(ConfigNode.create().append("modify_bounding_boxes"), false),

    UNMORPH_ON_DEATH(ConfigNode.create().append("unmorph_on_death"), true),

    ALLOW_CLIENT(clientNode().append("allow_client_mod"), true),
    //FORCE_CLIENT(clientNode().append("force_client"), false),
    FORCE_TARGET_VERSION(clientNode().append("force_version"), false),

    LOG_INCOMING_PACKETS(clientNode().append("log_incoming_packets"), false),
    LOG_OUTGOING_PACKETS(clientNode().append("log_outgoing_packets"), false),

    MIRROR_CONTROL_DISTANCE(interactionMirrorNode().append("normalDistance"), -1),
    MIRROR_IGNORE_DISGUISED(interactionMirrorNode().append("ignore_disguised"), true),
    MIRROR_DESTROY_TIMEOUT(interactionMirrorNode().append("destroy_timeout"), 40),

    MIRROR_BEHAVIOR_DO_SIMULATION(interactionMirrorBehaviorNode().append("simulate_interactions"), true),
    MIRROR_BEHAVIOR_SNEAK(interactionMirrorBehaviorNode().append("sneak"), true),
    MIRROR_BEHAVIOR_SWAP_HAND(interactionMirrorBehaviorNode().append("swap_hands"), false),
    MIRROR_BEHAVIOR_DROP(interactionMirrorBehaviorNode().append("allow_drop"), false),
    MIRROR_BEHAVIOR_HOTBAR(interactionMirrorBehaviorNode().append("hotbar"), false),

    MIRROR_LOG_OPERATION(interactionMirrorNode().append("log_operations"), false),
    MIRROR_LOG_CLEANUP_DATE(interactionMirrorNode().append("log_cleanup"), 7),

    MIRROR_SELECTION_MODE(interactionMirrorNode().append("selection_mode"), InteractionMirrorProcessor.InteractionMirrorSelectionMode.BY_NAME),

    BANNED_DISGUISES(ConfigNode.create().append("bannedDisguises"), new ArrayList<String>()),

    PIGLIN_BRUTE_IGNORE_DISGUISES(ConfigNode.create().append("piglin_brute_ignore_disguises"), true),
    HEALTH_SCALE(healthScaleNode().append("enabled"), true),
    HEALTH_SCALE_MAX_HEALTH(healthScaleNode().append("max_health"), 60),

    DISPLAY_BOSSBAR(bossbarNode().append("enabled"), true),

    FLYABILITY_EXHAUSTION_BASE(flyAbilityNode().append("exhaustion_base"), 200d),
    FLYABILITY_IDLE_CONSUME(flyAbilityNode().append("idle_consumption"), true),

    @Deprecated
    ALLOW_LD_DISGUISES(ConfigNode.create().append("enable_ld_custom_disguises"), false, true),

    LANGUAGE_CODE(languageNode().append("code"), "en_us"),
    //LANGUAGE_ALLOW_FALLBACK(languageNode().append("cast_translatable"), true),
    SINGLE_LANGUAGE(languageNode().append("single_language"), true),

    BLACKLIST_PATTERNS(nbtBlacklistNode().append("patterns"), NbtUtils.defaultBlacklistedPatterns),

    BLACKLIST_TAGS(nbtBlacklistNode().append("names"), NbtUtils.defaultBlacklistedTags),

    AMBIENT_FREQUENCY(ambientSoundNode().append("frequency"), 1D),

    VERSION(ConfigNode.create().append("version"), 0);

    public final ConfigNode node;
    public final Object defaultValue;
    public final boolean excludeFromInit;

    private ConfigOption(ConfigNode node, Object defaultValue, boolean excludeFromInit)
    {
        this.node = node;
        this.defaultValue = defaultValue;
        this.excludeFromInit = excludeFromInit;
    }

    private ConfigOption(ConfigNode node, Object defaultValue)
    {
        this(node, defaultValue, false);
    }

    @Override
    public String toString()
    {
        return node.toString();
    }

    public static ConfigNode interactionMirrorNode()
    {
        return ConfigNode.create().append("interactionMirror");
    }

    private static ConfigNode nbtBlacklistNode()
    {
        return ConfigNode.create().append("nbt_blacklist");
    }

    private static ConfigNode interactionMirrorBehaviorNode()
    {
        return interactionMirrorNode().append("behaviors");
    }

    private static ConfigNode bossbarNode()
    {
        return ConfigNode.create().append("bossbar");
    }

    private static ConfigNode languageNode()
    {
        return ConfigNode.create().append("language");
    }

    private static ConfigNode clientNode()
    {
        return ConfigNode.create().append("client");
    }
    private static ConfigNode healthScaleNode()
    {
        return ConfigNode.create().append("health_scale");
    }
    private static ConfigNode ambientSoundNode()
    {
        return ConfigNode.create().append("ambient_sounds");
    }
    private static ConfigNode flyAbilityNode()
    {
        return ConfigNode.create().append("flying");
    }
}
