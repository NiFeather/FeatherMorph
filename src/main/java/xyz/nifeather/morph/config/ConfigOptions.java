package xyz.nifeather.morph.config;

import org.apache.commons.lang3.RandomStringUtils;
import xiamomc.pluginbase.Configuration.ConfigNode;
import xiamomc.pluginbase.Configuration.ConfigOption;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.events.InteractionMirrorProcessor;
import xyz.nifeather.morph.utilities.NbtUtils;

import java.util.ArrayList;
import java.util.List;

public class ConfigOptions
{
    public static final ConfigOption<String> PLUGIN_PREFIX = ConfigOption.builder(String.class)
            .node(ConfigNode.create().append("message_pattern"))
            .defaultValue("<color:#dddddd>≡ FM » <message>")
            .build();

    public static final ConfigOption<Boolean> ALLOW_HEAD_MORPH = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("allowHeadMorph"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<Boolean> ALLOW_CHAT_OVERRIDE = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("chatOverride").append("allowOverride"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<Boolean> CHAT_OVERRIDE_USE_CUSTOM_RENDERER = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("chatOverride").append("UseCustomRenderer"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<String> CHAT_OVERRIDE_DEFAULT_PATTERN = ConfigOption.builder(String.class)
            .node(ConfigNode.create().append("chatOverride").append("default_pattern"))
            .defaultValue("<<who>> <message>")
            .build();

    public static final ConfigOption<Integer> SKILL_COOLDOWN_ON_DAMAGE = ConfigOption.builder(Integer.class)
            .node(ConfigNode.create().append("cooldown_on_damage"))
            .defaultValue(15)
            .build();

    @Deprecated(forRemoval = true)
    public static final ConfigOption<Boolean> ENABLE_SENTRY_LOGGER = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("enable_sentry"))
            .defaultValue(false)
            .build();

    @Deprecated(forRemoval = true)
    public static final ConfigOption<String> ACTION_ITEM = ConfigOption.builder(String.class)
            .node(ConfigNode.create().append("action_item"))
            .defaultValue("")
            .excludeFromInit(true)
            .build();

    @Deprecated(forRemoval = true, since = "1.3.0")
    public static final ConfigOption<String> SKILL_ITEM = ConfigOption.builder(String.class)
            .node(ConfigNode.create().append("skill_item"))
            .defaultValue("")
            .excludeFromInit(true)
            .build();

    //@Deprecated(forRemoval = true)
    //SKILL_ITEM_USE_COMPONENT(ConfigNode.create().append("skill_item_use_component_detection"), true, true),

    public static final ConfigOption<Boolean> ARMORSTAND_SHOW_ARMS = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("armorstand_show_arms"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<Boolean> MODIFY_BOUNDING_BOX = ConfigOption.builder(Boolean.class)
            .node(boundingBoxNode().append("modify_boxes"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<Boolean> CHECK_AVAILABLE_SPACE = ConfigOption.builder(Boolean.class)
            .node(boundingBoxNode().append("check_space"))
            .defaultValue(true)
            .build();

    @Deprecated(forRemoval = true)
    public static final ConfigOption<Boolean> MODIFY_BOUNDING_BOX_LEGACY = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("modify_bounding_boxes"))
            .defaultValue(false)
            .excludeFromInit(true)
            .build();

    public static final ConfigOption<Boolean> UNMORPH_ON_DEATH = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("unmorph_on_death"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<Boolean> ALLOW_CLIENT = ConfigOption.builder(Boolean.class)
            .node(clientNode().append("allow_client_mod"))
            .defaultValue(true)
            .build();

    //FORCE_CLIENT(clientNode().append("force_client"), false),
    public static final ConfigOption<Boolean> FORCE_TARGET_VERSION = ConfigOption.builder(Boolean.class)
            .node(clientNode().append("force_version"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<Boolean> LOG_INCOMING_PACKETS = ConfigOption.builder(Boolean.class)
            .node(clientNode().append("log_incoming_packets"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<Boolean> LOG_OUTGOING_PACKETS = ConfigOption.builder(Boolean.class)
            .node(clientNode().append("log_outgoing_packets"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<Boolean> USE_CLIENT_RENDERER = ConfigOption.builder(Boolean.class)
            .node(clientNode().append("client_renderer"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<Integer> MIRROR_CONTROL_DISTANCE = ConfigOption.builder(Integer.class)
            .node(interactionMirrorNode().append("normalDistance"))
            .defaultValue(-1)
            .build();

    public static final ConfigOption<Boolean> MIRROR_IGNORE_DISGUISED = ConfigOption.builder(Boolean.class)
            .node(interactionMirrorNode().append("ignore_disguised"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<Integer> MIRROR_DESTROY_TIMEOUT = ConfigOption.builder(Integer.class)
            .node(interactionMirrorNode().append("destroy_timeout"))
            .defaultValue(40)
            .build();

    public static final ConfigOption<Boolean> MIRROR_BEHAVIOR_DO_SIMULATION = ConfigOption.builder(Boolean.class)
            .node(interactionMirrorBehaviorNode().append("simulate_interactions"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<Boolean> MIRROR_BEHAVIOR_SNEAK = ConfigOption.builder(Boolean.class)
            .node(interactionMirrorBehaviorNode().append("sneak"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<Boolean> MIRROR_BEHAVIOR_SWAP_HAND = ConfigOption.builder(Boolean.class)
            .node(interactionMirrorBehaviorNode().append("swap_hands"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<Boolean> MIRROR_BEHAVIOR_DROP = ConfigOption.builder(Boolean.class)
            .node(interactionMirrorBehaviorNode().append("allow_drop"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<Boolean> MIRROR_BEHAVIOR_HOTBAR = ConfigOption.builder(Boolean.class)
            .node(interactionMirrorBehaviorNode().append("hotbar"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<Boolean> MIRROR_LOG_OPERATION = ConfigOption.builder(Boolean.class)
            .node(interactionMirrorNode().append("log_operations"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<Integer> MIRROR_LOG_CLEANUP_DATE = ConfigOption.builder(Integer.class)
            .node(interactionMirrorNode().append("log_cleanup"))
            .defaultValue(7)
            .build();

    public static final ConfigOption<String> MIRROR_SELECTION_MODE = ConfigOption.builder(String.class)
            .node(interactionMirrorNode().append("selection_mode"))
            .defaultValue(InteractionMirrorProcessor.InteractionMirrorSelectionMode.BY_NAME)
            .build();

    public static final ConfigOption<List<String>> BANNED_DISGUISES = ConfigOption.<List<String>>builder()
            .node(ConfigNode.create().append("bannedDisguises"))
            .defaultValue(new ArrayList<>())
            .build();

    public static final ConfigOption<Boolean> PIGLIN_BRUTE_IGNORE_DISGUISES = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("piglin_brute_ignore_disguises"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<Boolean> HEALTH_SCALE = ConfigOption.builder(Boolean.class)
            .node(healthScaleNode().append("enabled"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<Integer> HEALTH_SCALE_MAX_HEALTH = ConfigOption.builder(Integer.class)
            .node(healthScaleNode().append("max_health"))
            .defaultValue(60)
            .build();

    public static final ConfigOption<Boolean> DISPLAY_BOSSBAR = ConfigOption.builder(Boolean.class)
            .node(bossbarNode().append("enabled"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<Double> FLYABILITY_EXHAUSTION_BASE = ConfigOption.builder(Double.class)
            .node(flyAbilityNode().append("exhaustion_base"))
            .defaultValue(200d)
            .build();

    public static final ConfigOption<Boolean> FLYABILITY_IDLE_CONSUME = ConfigOption.builder(Boolean.class)
            .node(flyAbilityNode().append("idle_consumption"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<List<String>> FLYABILITY_DISALLOW_FLY_IN_WATER = ConfigOption.<List<String>>builder()
            .node(flyAbilityNode().append("disallow_in_water"))
            .defaultValue(new ArrayList<>())
            .build();

    public static final ConfigOption<List<String>> FLYABILITY_DISALLOW_FLY_IN_LAVA = ConfigOption.<List<String>>builder()
            .node(flyAbilityNode().append("disallow_in_lava"))
            .defaultValue(new ArrayList<>())
            .build();

    @Deprecated(since = "1.2.2", forRemoval = true)
    public static final ConfigOption<Boolean> FLYABILITY_NO_LIQUID = ConfigOption.builder(Boolean.class)
            .node(flyAbilityNode().append("no_fly_in_liquid"))
            .defaultValue(true)
            .excludeFromInit(true)
            .build();

    public static final ConfigOption<String> LANGUAGE_CODE = ConfigOption.builder(String.class)
            .node(languageNode().append("code"))
            .defaultValue("en_us")
            .build();

    public static final ConfigOption<Boolean> SINGLE_LANGUAGE = ConfigOption.builder(Boolean.class)
            .node(languageNode().append("single_language"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<Double> AMBIENT_FREQUENCY = ConfigOption.builder(Double.class)
            .node(ambientSoundNode().append("frequency"))
            .defaultValue(1D)
            .build();

    public static final ConfigOption<Boolean> DEBUG_OUTPUT = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("debug_output"))
            .defaultValue(false)
            .excludeFromInit(true)
            .build();

    public static final ConfigOption<Boolean> REVEALING = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("revealing"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<Boolean> CHECK_UPDATE = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("check_update"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<Boolean> ALLOW_ACQUIRE_MORPHS = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("allow_acquire_morphs"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<String> FORCED_DISGUISE = ConfigOption.builder(String.class)
            .node(ConfigNode.create().append("forced_disguise"))
            .defaultValue(MorphManager.forcedDisguiseNoneId)
            .build();

    public static final ConfigOption<Boolean> ALLOW_FLIGHT = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("allow_flight"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<List<String>> NOFLY_WORLDS = ConfigOption.<List<String>>builder()
            .node(ConfigNode.create().append("nofly_worlds"))
            .defaultValue(new ArrayList<>())
            .build();

    public static final ConfigOption<String> UUID_RANDOM_BASE = ConfigOption.builder(String.class)
            .node(ConfigNode.create().append("uuid_random_base"))
            .defaultValue(RandomStringUtils.secure().randomAlphabetic(8))
            .build();

    public static final ConfigOption<Boolean> ENABLE_MULTIINSTANCE = ConfigOption.builder(Boolean.class)
            .node(multiInstanceNode().append("enabled"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<String> MASTER_ADDRESS = ConfigOption.builder(String.class)
            .node(multiInstanceNode().append("master_address"))
            .defaultValue("0.0.0.0:39210")
            .build();

    public static final ConfigOption<Boolean> IS_MASTER = ConfigOption.builder(Boolean.class)
            .node(multiInstanceNode().append("is_master_service"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<String> MASTER_SECRET = ConfigOption.builder(String.class)
            .node(multiInstanceNode().append("secret"))
            .defaultValue(RandomStringUtils.randomAlphabetic(12))
            .build();

    public static final ConfigOption<Boolean> DO_CHECK_ABILITY_PERMISSIONS = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("check_ability_permissions"))
            .defaultValue(true)
            .build();

    public static final ConfigOption<Boolean> DO_MODIFY_AI = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("modify_ai"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<List<String>> GUI_PATTERN = ConfigOption.<List<String>>builder()
            .node(ConfigNode.create().append("gui_pattern"))
            .defaultValue(new ArrayList<>())
            .build();

    //ANIM_SELECT_PATTERN(ConfigNode.create().append("anim_select_pattern"), new ArrayList<String>()),

    @Deprecated(forRemoval = true)
    public static final ConfigOption<Boolean> HIDE_DISGUISED_PLAYERS_IN_TAB = ConfigOption.builder(Boolean.class)
            .node(ConfigNode.create().append("hide_disguised_players_in_tab"))
            .defaultValue(false)
            .build();

    // SRR -> ServerRenderer
    @Deprecated(forRemoval = true)
    public static final ConfigOption<Boolean> SR_SHOW_PLAYER_DISGUISES_IN_TAB = ConfigOption.builder(Boolean.class)
            .node(serverRendererNode().append("show_player_disguises_in_tab"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<Boolean> TOWNY_ALLOW_FLY_IN_WILDERNESS = ConfigOption.builder(Boolean.class)
            .node(townyNode().append("allow_fly_in_wilderness"))
            .defaultValue(false)
            .build();

    public static final ConfigOption<List<String>> DISGUISE_DISABLED_WORLDS = ConfigOption.<List<String>>builder()
            .node(worldOptionNode().append("disabled_worlds"))
            .defaultValue(new ArrayList<>())
            .build();

    @Deprecated(forRemoval = true, since = "2.5.0")
    public static final ConfigOption<List<String>> BLACKLIST_PATTERNS = ConfigOption.<List<String>>builder()
            .node(nbtBlacklistNode().append("patterns"))
            .defaultValue(new ArrayList<>(NbtUtils.defaultBlacklistedPatterns))
            .build();

    @Deprecated(forRemoval = true, since = "2.5.0")
    public static final ConfigOption<List<String>> BLACKLIST_TAGS = ConfigOption.<List<String>>builder()
            .node(nbtBlacklistNode().append("names"))
            .defaultValue(new ArrayList<>(NbtUtils.defaultBlacklistedTags))
            .build();

    public static final ConfigOption<Integer> VERSION = ConfigOption.builder(Integer.class)
            .node(ConfigNode.create().append("version"))
            .defaultValue(0)
            .build();

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
    private static ConfigNode boundingBoxNode()
    {
        return ConfigNode.create().append("bounding_boxes");
    }
    private static ConfigNode multiInstanceNode()
    {
        return ConfigNode.create().append("multi_instance");
    }
    private static ConfigNode serverRendererNode()
    {
        return ConfigNode.create().append("server_renderer");
    }
    private static ConfigNode integrationNode()
    {
        return ConfigNode.create().append("integrations");
    }
    public static ConfigNode townyNode()
    {
        return integrationNode().append("towny");
    }
    private static ConfigNode worldOptionNode()
    {
        return ConfigNode.create().append("world_option");
    }
}