package xiamomc.morph.misc.recipe;

import xiamomc.pluginbase.Configuration.ConfigNode;
import xiamomc.pluginbase.Configuration.ConfigOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeOptions
{
    public static final ConfigOption<Boolean> ALLOW_DISGUISE_TOOL_CRAFTING = new ConfigOption<>(skillItemNode().append("enabled"), true);
    public static final ConfigOption<Boolean> DISGUISE_TOOL_CRAFTING_UNSHAPED = new ConfigOption<>(skillItemNode().append("shapeless"), true);
    public static final ConfigOption<List<String>> DISGUISE_TOOL_CRAFTING_SHAPE = new ConfigOption<>(skillItemNode().append("crafting_shape"), new ArrayList<>());
    public static final ConfigOption<Map<String, String>> DISGUISE_TOOL_CRAFTING_MATERIALS = new ConfigOption<>(skillItemNode().append("crafting_materials"), new HashMap<>());
    public static final ConfigOption<String> DISGUISE_TOOL_RESULT_MATERIAL = new ConfigOption<>(skillItemNode().append("result_material"), "minecraft:feather");
    public static final ConfigOption<String> DISGUISE_TOOL_RESULT_NAME = new ConfigOption<>(skillItemNode().append("result_item_name"), "~UNSET");
    public static final ConfigOption<List<String>> DISGUISE_TOOL_RESULT_LORE = new ConfigOption<>(skillItemNode().append("result_item_lore"), new ArrayList<>());
    public static final ConfigOption<Integer> CONFIG_VERSION = new ConfigOption<>(ConfigNode.create().append("version"), 0);

    private static ConfigNode craftingNode()
    {
        return ConfigNode.create().append("item_crafting");
    }
    private static ConfigNode skillItemNode()
    {
        return craftingNode().append("disguise_tool");
    }
}
