package xyz.nifeather.morph.misc.recipe;

import com.google.common.base.Charsets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.utilities.PluginAssetUtils;
import xiamomc.pluginbase.Configuration.ConfigOption;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class RecipeYamlConfigManager extends StandaloneYamlConfigManager
{
    public RecipeYamlConfigManager(File file, @Nullable String internalResourceName)
    {
        super(file, internalResourceName);
    }

    /**
     * Copy internal resource to the location
     *
     * @return Whether this operation was successful
     */
    @Override
    protected boolean copyInternalResource()
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

    @Override
    protected int getExpectedConfigVersion()
    {
        return 1;
    }

    private final List<ConfigOption<?>> options = new ObjectArrayList<>(List.of(
            RecipeOptions.DISGUISE_TOOL_CRAFTING_SHAPE,
            RecipeOptions.DISGUISE_TOOL_RESULT_LORE,
            RecipeOptions.DISGUISE_TOOL_RESULT_NAME,
            RecipeOptions.ALLOW_DISGUISE_TOOL_CRAFTING,
            RecipeOptions.DISGUISE_TOOL_CRAFTING_MATERIALS,
            RecipeOptions.DISGUISE_TOOL_CRAFTING_UNSHAPED,
            RecipeOptions.DISGUISE_TOOL_RESULT_MATERIAL
    ));

    @Override
    protected List<ConfigOption<?>> getAllOptions()
    {
        return options;
    }
}
