package xyz.nifeather.morph.misc.recipe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import xiamomc.pluginbase.Configuration.ConfigNode;
import xiamomc.pluginbase.Configuration.ConfigOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeOptions
{
    private static final List<ConfigOption<?>> options = new ObjectArrayList<>();

    private static <X> ConfigOption<X> createOption(ConfigNode node, X val)
    {
        var option = new ConfigOption<X>(node, val);

        options.add(option);
        return option;
    }

    public static List<ConfigOption<?>> options()
    {
        return new ObjectArrayList<>(options);
    }

    public static final ConfigOption<Boolean> ALLOW_DISGUISE_TOOL_CRAFTING = createOption(skillItemNode().append("enabled"), true);
    public static final ConfigOption<Boolean> DISGUISE_TOOL_CRAFTING_UNSHAPED = createOption(skillItemNode().append("shapeless"), true);
    public static final ConfigOption<List<String>> DISGUISE_TOOL_CRAFTING_SHAPE = createOption(skillItemNode().append("crafting_shape"), new ArrayList<>());
    public static final ConfigOption<Map<String, String>> DISGUISE_TOOL_CRAFTING_MATERIALS = createOption(skillItemNode().append("crafting_materials"), new HashMap<>());
    public static final ConfigOption<String> DISGUISE_TOOL_RESULT_MATERIAL = createOption(skillItemNode().append("result_material"), "minecraft:feather");
    public static final ConfigOption<String> DISGUISE_TOOL_RESULT_NAME = createOption(skillItemNode().append("result_item_name"), "~UNSET");
    public static final ConfigOption<List<String>> DISGUISE_TOOL_RESULT_LORE = createOption(skillItemNode().append("result_item_lore"), new ArrayList<>());

    public static final ConfigOption<Boolean> ALLOW_MAGIC_BOTTLE_CRAFTING = createOption(mobBottleNode().append("enabled"), true);
    public static final ConfigOption<Boolean> MAGIC_BOTTLE_CRAFTING_UNSHAPED = createOption(mobBottleNode().append("shapeless"), true);
    public static final ConfigOption<List<String>> MAGIC_BOTTLE_CRAFTING_SHAPE = createOption(mobBottleNode().append("crafting_shape"), new ArrayList<>());
    public static final ConfigOption<Map<String, String>> MAGIC_BOTTLE_CRAFTING_MATERIALS = createOption(mobBottleNode().append("crafting_materials"), new HashMap<>());
    public static final ConfigOption<String> MAGIC_BOTTLE_RESULT_MATERIAL = createOption(mobBottleNode().append("result_material"), "minecraft:feather");
    public static final ConfigOption<String> MAGIC_BOTTLE_RESULT_NAME = createOption(mobBottleNode().append("result_item_name"), "~UNSET");
    public static final ConfigOption<List<String>> MAGIC_BOTTLE_RESULT_LORE = createOption(mobBottleNode().append("result_item_lore"), new ArrayList<>());

    private static ConfigNode craftingNode()
    {
        return ConfigNode.create().append("item_crafting");
    }
    private static ConfigNode skillItemNode()
    {
        return craftingNode().append("disguise_tool");
    }
    private static ConfigNode mobBottleNode()
    {
        return craftingNode().append("magic_bottle");
    }
}
