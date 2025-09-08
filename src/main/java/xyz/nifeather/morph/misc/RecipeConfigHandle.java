package xyz.nifeather.morph.misc;

import org.slf4j.LoggerFactory;

import java.io.File;

public class RecipeConfigHandle
{
    public static void handle(File dataFolder)
    {
        if (!dataFolder.exists()) return;
        if (!dataFolder.isDirectory()) return;

        var oldRecipeConfig = new File(dataFolder, "recipes.yml");
        if (!oldRecipeConfig.exists()) return;

        var logger = LoggerFactory.getLogger("FeatherMorph - RecipeConfigHandle");
        var moveTarget = new File(dataFolder, "recipes.we_now_use_datapack.yml");
        if (moveTarget.exists())
        {
            logger.warn("We already have a '%s' file?! not processing...".formatted(moveTarget.getName()));
            return;
        }

        if (oldRecipeConfig.renameTo(moveTarget))
        {
            logger.warn("Unable to rename old recipe config due to an unknown error");
            return;
        }
    }
}
