package xiamomc.morph.misc.integrations.modelengine;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.modelengine.MEBackend;
import xiamomc.morph.events.api.lifecycle.ManagerFinishedInitializeEvent;
import xiamomc.morph.providers.ModelEngineProvider;

import java.util.concurrent.atomic.AtomicBoolean;

public class ModelEngineHelper extends MorphPluginObject implements Listener
{
    @EventHandler
    public void onInitDone(ManagerFinishedInitializeEvent e)
    {
        var manager = e.manager;

        try
        {
            manager.registerBackend(new MEBackend());
        }
        catch (Throwable t)
        {
            logger.error("Can't register Model Engine as one of the backends: " + t.getMessage());
            t.printStackTrace();

            return;
        }

        try
        {
            manager.registerProvider(new ModelEngineProvider());
        }
        catch (Throwable t)
        {
            logger.error("Can't register Model Engine Provider as one of the providers: " + t.getMessage());
            t.printStackTrace();

            return;
        }

        logger.info("Successfully registered ModelEngine as a disguise provider and backend!");
    }
}
