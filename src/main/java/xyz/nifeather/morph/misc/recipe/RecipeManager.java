package xyz.nifeather.morph.misc.recipe;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.utilities.ItemUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RecipeManager extends MorphPluginObject
{
    private final StandaloneYamlConfigManager configManager = new RecipeYamlConfigManager(new File(plugin.getDataFolder(), "recipes.yml"), "recipes.yml");

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        reload();
    }

    public void reload()
    {
        this.configManager.reload();

        // too bad
        prepareRecipe(
                SKILLITEM_CRAFTING_KEY,
                configManager.getOrDefault(RecipeOptions.ALLOW_DISGUISE_TOOL_CRAFTING),
                configManager.getOrDefault(RecipeOptions.DISGUISE_TOOL_RESULT_NAME),
                configManager.getList(RecipeOptions.DISGUISE_TOOL_RESULT_LORE),
                configManager.getList(RecipeOptions.DISGUISE_TOOL_CRAFTING_SHAPE),
                configManager.getMap(RecipeOptions.DISGUISE_TOOL_CRAFTING_MATERIALS),
                configManager.getOrDefault(RecipeOptions.DISGUISE_TOOL_RESULT_MATERIAL),
                configManager.getOrDefault(RecipeOptions.DISGUISE_TOOL_CRAFTING_UNSHAPED)
        );

        //todo: Result material is hardcoded to GLASS_BOTTLE, see `CustomItemRelatedEvents#onConsume`
        //      ...
        //      First, when an **Empty** Magic Bottle is used, we ALWAYS replace it with a potion as the **Collected** Magic Bottle
        //      Then, when the Collected Magic Bottle is consumed, we ALWAYS return a **normal** glass bottle
        //      ...
        //      If we want to make it fully customizable, we need to...
        //          I.  Let server choose the material of the **Collected** Magic Bottle
        //          II. When the Collected is consumed, we need to return the *Result Material* of the bottle recipe.
        //      But! If we choose to do this...
        //          I. The server might have custom recipe plugin installed and take over our recipe management.
        //             If this happens, how should we choose the material of Collected and Empty bottle?
        //             ...
        //          II. Related options will definitely not in the recipe configuration,
        //              ss I don't want to "pollute" the recipe configuration with unrelated options...
        //      ...
        //      So bruh, this is so complicated, so I decided to make it hardcoded to glass bottle...
        prepareRecipe(
                MAGIC_BOTTLE_CRAFTING_KEY,
                configManager.getOrDefault(RecipeOptions.ALLOW_MAGIC_BOTTLE_CRAFTING),
                configManager.getOrDefault(RecipeOptions.MAGIC_BOTTLE_RESULT_NAME),
                configManager.getList(RecipeOptions.MAGIC_BOTTLE_RESULT_LORE),
                configManager.getList(RecipeOptions.MAGIC_BOTTLE_CRAFTING_SHAPE),
                configManager.getMap(RecipeOptions.MAGIC_BOTTLE_CRAFTING_MATERIALS),
                Material.GLASS_BOTTLE.key().asString(),
                configManager.getOrDefault(RecipeOptions.MAGIC_BOTTLE_CRAFTING_UNSHAPED)
        );
    }

    @Nullable
    private Material getMaterialFrom(String str)
    {
        var key = NamespacedKey.fromString(str);

        return Arrays.stream(Material.values())
                .parallel()
                .filter(m -> !m.isLegacy() && m.key().equals(key))
                .findFirst()
                .orElse(null);
    }

    @NotNull
    public static final NamespacedKey SKILLITEM_CRAFTING_KEY = Objects.requireNonNull(NamespacedKey.fromString("feathermorph:disguise_tool_crafting"));

    @NotNull
    public static final NamespacedKey MAGIC_BOTTLE_CRAFTING_KEY = Objects.requireNonNull(NamespacedKey.fromString("feathermorph:magic_bottle_crafting"));

    private void prepareRecipe(NamespacedKey recipeKey,
                               boolean enabled,
                               String resultName,
                               List<String> resultLore,
                               List<String> shape,
                               Map<String, String> inputMaterials,
                               String resultMaterialId,
                               boolean unShaped)
    {
        if (!enabled)
        {
            Bukkit.removeRecipe(recipeKey);
            return;
        }

        var minimessage = MiniMessage.miniMessage();

        Component name = resultName.equals("~UNSET") ? null : minimessage.deserialize(resultName);
        List<Component> loreComponents = resultLore.isEmpty() ? null : resultLore.parallelStream().map(minimessage::deserialize).toList();

        var resultMaterial = this.getMaterialFrom(resultMaterialId);
        if (resultMaterial == null)
        {
            logger.error("Invalid result material ID: '%s', skipping...".formatted(resultMaterialId));
            return;
        }

        Map<String, Material> materialsReal = new Object2ObjectOpenHashMap<>();
        inputMaterials.forEach((str, id) ->
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

        var recipeProperty = new RecipeProperty(recipeKey,
                !unShaped, shape,
                materialsReal,
                resultMaterial,
                name,
                loreComponents);

        buildAndAddRecipe(recipeProperty);
    }

    private ItemStack getResultItemFromRecipe(NamespacedKey recipeKey, Material resultMaterial)
    {
        if (recipeKey.equals(SKILLITEM_CRAFTING_KEY))
            return ItemUtils.buildDisguiseToolFrom(ItemStack.of(resultMaterial));
        else if (recipeKey.equals(MAGIC_BOTTLE_CRAFTING_KEY))
            return ItemUtils.buildMagicItemFrom(ItemStack.of(resultMaterial));
        else return ItemStack.of(resultMaterial);
    }

    private void buildAndAddRecipe(RecipeProperty recipeProperty)
    {
        ItemStack resultItem = getResultItemFromRecipe(recipeProperty.key(), recipeProperty.resultMaterial());

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
        Bukkit.addRecipe(recipe, true);
    }
}
