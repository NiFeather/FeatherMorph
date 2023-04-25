package xiamomc.morph.storage;

import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

public class DirectoryStorage extends MorphPluginObject
{
    private final String directoryBaseName;
    private final URI pluginStorageBaseUri;
    private final URI absoluteDirectoryPath;

    public boolean initializeFailed()
    {
        return initializeFailed;
    }

    private boolean initializeFailed;

    private URI getAbsoulteURI(String path)
    {
        return new File(path).getAbsoluteFile().toURI();
    }

    public DirectoryStorage(String directoryBaseName)
    {
        this.directoryBaseName = directoryBaseName;
        this.pluginStorageBaseUri = this.plugin.getDataFolder().getAbsoluteFile().toURI();
        this.absoluteDirectoryPath = this.getAbsoulteURI(pluginStorageBaseUri.getPath() + "/" + directoryBaseName);

        var dirFile = new File(absoluteDirectoryPath);
        if (!dirFile.exists())
        {
            try
            {
                var success = dirFile.mkdirs();
                if (!success)
                    logger.error("Unable to create directory '%s': Unknown error".formatted(directoryBaseName));

                this.initializeFailed = true;
            }
            catch (Throwable t)
            {
                logger.error("Unable to create directory '%s' for logging: %s".formatted(directoryBaseName, t.getLocalizedMessage()));
                t.printStackTrace();

                this.initializeFailed = true;
            }
        }
    }

    public File[] getFiles(String pattern)
    {
        var path = Path.of(this.absoluteDirectoryPath).toFile();
        return path.listFiles(f -> f.isFile() && f.getName().matches(pattern));
    }

    public File[] getFiles()
    {
        return Path.of(this.absoluteDirectoryPath).toFile().listFiles(File::isFile);
    }

    public File[] getDirectories(String pattern)
    {
        var path = Path.of(this.absoluteDirectoryPath).toFile();
        return path.listFiles(f -> f.isDirectory() && f.getName().matches(pattern));
    }

    public File[] getDirectories()
    {
        return Path.of(this.absoluteDirectoryPath).toFile().listFiles(File::isDirectory);
    }

    public File getDirectory(String relativePath, boolean createIfNotExist)
    {
        var file = new File(this.getAbsoulteURI(absoluteDirectoryPath.getPath() + "/" + relativePath));

        if (!file.getAbsolutePath().contains(absoluteDirectoryPath.getPath()))
            throw new RuntimeException("Trying to access a file that does not belongs to this plugin");

        if (!file.exists() && createIfNotExist)
        {
            try
            {
                var success = file.mkdirs();
                if (!success)
                {
                    logger.warn("Unable to create directory: Unknown error");
                    return null;
                }
            }
            catch (Throwable t)
            {
                logger.error("Unable to create directory '%s': %s".formatted(relativePath, t.getLocalizedMessage()));
                t.printStackTrace();
            }
        }

        if (!file.isDirectory()) return null;

        return file;
    }

    @Nullable
    public File getFile(String fileName, boolean createIfNotExist)
    {
        var file = new File(this.getAbsoulteURI(absoluteDirectoryPath.getPath() + "/" + fileName));

        if (!file.getAbsolutePath().contains(absoluteDirectoryPath.getPath()))
            throw new RuntimeException("Trying to access a file that does not belongs to this plugin");

        if (!file.exists() && createIfNotExist)
        {
            try
            {
                var success = file.createNewFile();
                if (!success)
                {
                    logger.warn("Unable to create file: Unknown error");
                    return null;
                }
            }
            catch (Throwable t)
            {
                logger.error("Unable to create file '%s': %s".formatted(fileName, t.getLocalizedMessage()));
                t.printStackTrace();
            }
        }

        if (!file.isFile()) return null;

        return file;
    }
}
