package xyz.nifeather.morph;

import io.papermc.paper.datapack.DatapackRegistrar;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.registrar.RegistrarEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("UnstableApiUsage")
public class FeatherMorphBootstrap implements PluginBootstrap
{
    private final Logger logger = LoggerFactory.getLogger("FeatherMorph - Bootstrap");

    public boolean bootstrapLoaded = false;

    static final AtomicBoolean pluginDisabled = new AtomicBoolean(false);

    @Override
    public void bootstrap(@NotNull BootstrapContext context)
    {
        logger.info("Loading bootstrap...");

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY.newHandler(this::onDatapackDiscovery));

        bootstrapLoaded = true;
        logger.info("Done!");
    }

    private void onDatapackDiscovery(RegistrarEvent<DatapackRegistrar> event)
    {
        try
        {
            for (String datapack : BuiltinDatapackNames.values())
                registerBuiltinDatapack(event, datapack);
        }
        catch (URISyntaxException | IOException e)
        {
            throw new RuntimeException("Unable to register built-in datapack", e);
        }
    }

    private void registerBuiltinDatapack(RegistrarEvent<DatapackRegistrar> event, String id)
            throws IOException, URISyntaxException, NullDependencyException
    {
        URI datapackURI = Objects.requireNonNull(this.getClass().getResource("/datapacks/%s".formatted(id)))
                .toURI();

        var discoverResult = event.registrar().discoverPack(datapackURI, id);
        if (discoverResult == null)
            throw new NullDependencyException("Failed to discover recipe datapack!");

        logger.info("Successfully registered datapack {}!", id);
    }

    @Override
    public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context)
    {
        logger.info("Creating FeatherMorphMain...");
        return new FeatherMorphMain();
    }
}
