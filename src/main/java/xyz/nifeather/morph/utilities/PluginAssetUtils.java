package xyz.nifeather.morph.utilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.messages.DumpResult;
import xyz.nifeather.morph.misc.ExecutionErrorException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

public class PluginAssetUtils
{
    /**
     * 通过给定的路径获取资源内容
     * @param path 目标路径
     * @return 原始数据，返回null则未找到或出现异常
     */
    public static byte @Nullable [] getFileBytes(String path)
    {
        var plugin = FeatherMorphMain.getPlugin(FeatherMorphMain.class);

        var stream = plugin.getResource(path);

        if (stream == null) return null;

        try
        {
            return stream.readAllBytes();
        }
        catch (Throwable ignored)
        {
        }

        return null;
    }

    /**
     * 以字符串的形式获取资源内容
     * @param path 资源路径
     * @return 文件资源内容，返回空则未找到或出现异常
     */
    public static Optional<String> getFileStringsOptional(String path)
    {
        var bytes = getFileBytes(path);

        if (bytes == null) return Optional.empty();

        return Optional.of(new String(bytes, StandardCharsets.UTF_8));
    }

    /**
     * 以字符串的形式获取资源内容
     * @param path 资源路径
     * @return 文件资源内容，返回空则未找到或出现异常
     */
    public static String getFileStrings(String path)
    {
        var bytes = getFileBytes(path);

        if (bytes == null) return "";

        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static final String assetPath = "assets/feathermorph";

    public static String langPath(@NotNull String languageCode)
    {
        return assetPath + "/lang/" + languageCode + ".json";
    }

    public static List<String> allSupportedLanguages()
    {
        return List.of(
                "zh_cn",
                "en_us",
                "ru_ru"
        );
    }

    //utils
    public static File createBackupFile(File parent, String name, String ext)
    {
        String random = Long.toHexString(System.currentTimeMillis());
        return new File(parent, "%s.old.%s.%s".formatted(name, random, ext));
    }

    public static DumpResult extractLocaleFile(String targetLocale, File parentDirectory, boolean overwriteExistingIfExist)
            throws ExecutionErrorException
    {
        var targetFile = new File(parentDirectory, "%s.json".formatted(targetLocale));
        File backupFile = null;

        if (targetFile.exists())
        {
            if (!overwriteExistingIfExist)
                return new DumpResult(targetFile, null);

            // Let's try backup the original file
            try
            {
                int retry = 0;
                var backup = createBackupFile(parentDirectory, targetLocale, "json");
                while (backup.exists())
                {
                    retry++;
                    backup = createBackupFile(parentDirectory, targetLocale, "json");

                    if (retry > 10)
                    {
                        throw ExecutionErrorException.forMethod("dumpFromAssets")
                                .withMessage("Too many retries for creating backup for an existing file!")
                                .create();
                    }
                }

                if (!targetFile.renameTo(backup))
                {
                    throw ExecutionErrorException.forMethod("dumpFromAssets")
                            .withMessage("Failed to rename original file to the backup!")
                            .create();
                }

                backupFile = backup;
            }
            catch (SecurityException e)
            {
                throw ExecutionErrorException.forMethod("dumpFromAssets")
                        .causedBy(e)
                        .withMessage("Unknown error occurred while attempting to make backup")
                        .create();
            }
        }

        var path = PluginAssetUtils.langPath(targetLocale);
        var asset = PluginAssetUtils.getFileStringsOptional(path);
        if (asset.isEmpty())
        {
            throw ExecutionErrorException.forMethod("dumpFromAssets")
                    .withMessage("Language file for locale %s doesn't exist in plugin assets".formatted(targetLocale))
                    .create();
        }

        try
        {
            if (!parentDirectory.exists())
                Files.createDirectory(parentDirectory.toPath());

            Files.writeString(targetFile.toPath(), asset.get(), StandardCharsets.UTF_8);
            return new DumpResult(targetFile, backupFile);
        }
        catch (IOException e)
        {
            throw ExecutionErrorException.forMethod("dumpFromAssets")
                    .causedBy(e)
                    .withMessage("Unable to write file!")
                    .create();
        }
    }
}
