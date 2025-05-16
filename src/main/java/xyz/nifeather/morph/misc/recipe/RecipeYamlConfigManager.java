package xyz.nifeather.morph.misc.recipe;

import com.google.common.base.Charsets;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Configuration.ConfigOption;
import xyz.nifeather.morph.utilities.PluginAssetUtils;

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
        return 3;
    }

    @Override
    protected List<ConfigOption<?>> getAllOptions()
    {
        return RecipeOptions.options();
    }
}
