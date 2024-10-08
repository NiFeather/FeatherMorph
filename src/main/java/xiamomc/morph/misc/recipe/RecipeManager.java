package xiamomc.morph.misc.recipe;

import com.google.common.base.Charsets;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.utilities.ItemUtils;
import xiamomc.morph.utilities.PluginAssetUtils;
import xiamomc.pluginbase.Annotations.Initializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RecipeManager extends MorphPluginObject
{
    @Nullable
    private YamlConfiguration yamlConfiguration;

    private final File configFile = new File(plugin.getDataFolder(), "recipes.yml");

    private boolean allowCrafting = false;
    private boolean unShaped = false;
    private List<String> shape = new ObjectArrayList<>();
    private Map<String, String> materials = new Object2ObjectOpenHashMap<>();
    private String resultMaterialId = "~UNSET";
    private String resultName = "~UNSET";
    private List<String> resultLore = new ObjectArrayList<>();

    public void reload()
    {
        var newConfig = new YamlConfiguration();

        if (!configFile.exists())
        {
            if (!copyInternalRecipeResource())
            {
                logger.error("Can't create file to save configuration! Not reloading recipes...");
                return;
            }
        }

        try
        {
            newConfig.load(configFile);
        }
        catch (Throwable e)
        {
            logger.error("Unable to load recipe configuration: " + e.getMessage());
            return;
        }

        this.yamlConfiguration = newConfig;
        readValuesFromConfig(newConfig);
        prepareRecipe();
    }

    private void readValuesFromConfig(YamlConfiguration config)
    {
        allowCrafting = config.getBoolean(RecipeOptions.ALLOW_SKILL_ITEM_CRAFTING.toString(), false);
        unShaped = config.getBoolean(RecipeOptions.SKILL_ITEM_CRAFTING_UNSHAPED.toString(), false);
        shape = config.getStringList(RecipeOptions.SKILL_ITEM_CRAFTING_SHAPE.toString());
        resultMaterialId = config.getString(RecipeOptions.SKILL_ITEM_RESULT_MATERIAL.toString(), "~UNSET");
        resultName = config.getString(RecipeOptions.SKILL_ITEM_RESULT_NAME.toString(), "~UNSET");
        resultLore = config.getStringList(RecipeOptions.SKILL_ITEM_RESULT_LORE.toString());

        var materialSection = config.getConfigurationSection(RecipeOptions.SKILL_ITEM_CRAFTING_MATERIALS.toString());

        if (materialSection != null)
        {
            materialSection.getKeys(false).forEach(key ->
            {
                var value = materialSection.getString(key, "~UNSET");
                materials.put(key, value);
            });
        }
    }

    private boolean copyInternalRecipeResource()
    {
        try
        {
            if (!configFile.createNewFile())
                return false;

            try (var writer = new OutputStreamWriter(new FileOutputStream(configFile), Charsets.UTF_8))
            {
                writer.write(PluginAssetUtils.getFileStrings("recipes.yml"));
            }
            catch (Throwable t)
            {
                logger.error("Can't write content: " + t.getMessage());
                return false;
            }

            return true;
        }
        catch (Throwable t)
        {
            logger.error("Can't create config file: " + t.getMessage());
        }

        return true;
    }

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        if (this.yamlConfiguration == null)
            reload();
    }

    @Nullable
    private Material getMaterialFrom(String str)
    {
        var key = NamespacedKey.fromString(str);

        return Arrays.stream(Material.values()).parallel().filter(m -> m.key().equals(key))
                .findFirst().orElse(null);
    }

    @NotNull
    public static final NamespacedKey SKILLITEM_CRAFTING_KEY = NamespacedKey.fromString("feathermorph:skill_item_crafting");

    private void prepareRecipe()
    {
        if (!allowCrafting)
        {
            Bukkit.removeRecipe(SKILLITEM_CRAFTING_KEY);
            return;
        }

        var minimessage = MiniMessage.miniMessage();

        Component name = this.resultName.equals("~UNSET") ? null : minimessage.deserialize(this.resultName);
        List<Component> loreComponents = this.resultLore.isEmpty() ? null : this.resultLore.parallelStream().map(minimessage::deserialize).toList();

        var resultMaterial = this.getMaterialFrom(this.resultMaterialId);
        if (resultMaterial == null)
        {
            logger.error("Invalid result material ID: '%s', skipping...".formatted(resultMaterialId));
            return;
        }

        Map<String, Material> materialsReal = new Object2ObjectOpenHashMap<>();
        this.materials.forEach((str, id) ->
        {
            var material = Arrays.stream(Material.values())
                    .filter(m -> m.key().equals(NamespacedKey.fromString(id)))
                    .findFirst()
                    .orElse(null);

            if (material == null)
            {
                logger.warn("Invalid material '%s', skipping...".formatted(id));
                return;
            }

            materialsReal.put(str, material);
        });

        var recipeProperty = new RecipeProperty(SKILLITEM_CRAFTING_KEY,
                !this.unShaped, this.shape,
                materialsReal,
                resultMaterial,
                name,
                loreComponents);

        buildAndAddRecipe(recipeProperty);
    }

    private void buildAndAddRecipe(RecipeProperty recipeProperty)
    {
        var resultItem = ItemUtils.buildSkillItemFrom(ItemStack.of(recipeProperty.resultMaterial()));
        resultItem.editMeta(meta ->
        {
            meta.setRarity(ItemRarity.UNCOMMON);
            meta.setEnchantmentGlintOverride(true);

            var name = recipeProperty.resultName();
            if (name != null)
                meta.itemName(name);

            var lore = recipeProperty.lore();
            if (lore != null && !lore.isEmpty())
                meta.lore(lore);
        });

        var key = recipeProperty.key();

        CraftingRecipe recipe;

        if (recipeProperty.shaped())
        {
            var shaped = new ShapedRecipe(key, resultItem);
            shaped.shape(recipeProperty.shape().toArray(new String[]{}));
            recipeProperty.materials().forEach((ch, material) -> shaped.setIngredient(ch.charAt(0), material));

            recipe = shaped;
        }
        else
        {
            var shapeless = new ShapelessRecipe(key, resultItem);
            recipeProperty.materials().forEach((ignored, material) -> shapeless.addIngredient(material));

            recipe = shapeless;
        }

        Bukkit.removeRecipe(key);
        Bukkit.addRecipe(recipe);
    }

    private void test_dumpExsampleConfig()
    {
        allowCrafting = true;
        unShaped = true;
        shape = List.of(
                "ABC",
                "DEF",
                "GHI"
        );
        resultMaterialId = Material.BEDROCK.key().asString();
        resultName = "<reset>技能物品";
        resultLore = List.of(
                "技能测试1", "技能测试2"
        );
        materials = new Object2ObjectOpenHashMap<>();
        materials.put("A", Material.BEDROCK.key().asString());
        materials.put("B", Material.ACACIA_BOAT.key().asString());
    }

    private void test_saveConfig()
    {
        if (yamlConfiguration == null)
        {
            logger.error("Null config!");
            return;
        }

        yamlConfiguration.set(RecipeOptions.ALLOW_SKILL_ITEM_CRAFTING.toString(), allowCrafting);
        yamlConfiguration.set(RecipeOptions.SKILL_ITEM_CRAFTING_UNSHAPED.toString(), this.unShaped);
        yamlConfiguration.set(RecipeOptions.SKILL_ITEM_CRAFTING_SHAPE.toString(), this.shape);
        yamlConfiguration.set(RecipeOptions.SKILL_ITEM_RESULT_MATERIAL.toString(), this.resultMaterialId);
        yamlConfiguration.set(RecipeOptions.SKILL_ITEM_RESULT_NAME.toString(), this.resultName);
        yamlConfiguration.set(RecipeOptions.SKILL_ITEM_RESULT_LORE.toString(), this.resultLore);

        this.materials.forEach((str, id) ->
        {
            var node = RecipeOptions.SKILL_ITEM_CRAFTING_MATERIALS.toString() + "." + str;
            yamlConfiguration.set(node, id);
        });

        try
        {
            yamlConfiguration.save(configFile);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
