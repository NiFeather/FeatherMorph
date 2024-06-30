package xiamomc.morph.backends.server.renderer.network;

import com.comphenix.protocol.ProtocolLibrary;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.listeners.*;
import xiamomc.pluginbase.Annotations.Initializer;

import java.util.List;

public class ProtocolHandler extends MorphPluginObject
{
    public ProtocolHandler()
    {
        registerRange(
                new SpawnPacketHandler(),
                new MetaPacketListener(),
                new EquipmentPacketListener(),
                new PlayerLookPacketListener(),
                new SoundListener(),
                new AnimationPacketListener()
        );
    }

    private final List<ProtocolListener> listeners = new ObjectArrayList<>();

    private void throwIfDisposed()
    {
        if (disposed)
            throw new IllegalStateException(
                    "This instance of ProtocolHandler(%s) is disposed and cannot be used."
                            .formatted(this)
            );
    }

    public boolean contains(ProtocolListener listener)
    {
        throwIfDisposed();

        return contains(listener.getIdentifier());
    }

    public boolean contains(String id)
    {
        throwIfDisposed();

        return listeners.stream().anyMatch(l -> l.getIdentifier().equalsIgnoreCase(id));
    }

    public boolean register(ProtocolListener listener)
    {
        throwIfDisposed();

        if (this.contains(listener))
            return false;

        listeners.add(listener);

        if (loadReady)
        {
            try
            {
                ProtocolLibrary.getProtocolManager().addPacketListener(listener);
            }
            catch (Throwable t)
            {
                logger.error("Unable to register listener '%s': %s".formatted(
                        listener.getIdentifier(), t.getMessage()
                ));
            }
        }

        return true;
    }

    public boolean registerRange(ProtocolListener... listeners)
    {
        throwIfDisposed();

        boolean allSuccess = true;

        for (ProtocolListener listener : listeners)
        {
            allSuccess = register(listener) && allSuccess;
        }

        return allSuccess;
    }

    public boolean unregister(ProtocolListener listener)
    {
        throwIfDisposed();

        listeners.remove(listener);

        try
        {
            ProtocolLibrary.getProtocolManager().removePacketListener(listener);
        }
        catch (Throwable t)
        {
            logger.error("Error removing packet listener '%s': %s".formatted(
                    listener.getIdentifier(), t.getMessage()
            ));
        }

        return true;
    }

    private boolean loadReady;

    @Initializer
    private void load()
    {
        if (disposed) return;

        var protocolMgr = ProtocolLibrary.getProtocolManager();

        for (var listener : listeners)
        {
            try
            {
                protocolMgr.addPacketListener(listener);
            }
            catch (Throwable t)
            {
                logger.error("Unable to register listener '%s': %s".formatted(
                        listener.getIdentifier(), t.getMessage()
                ));
            }
        }

        loadReady = true;
    }

    private boolean disposed;

    public boolean disposed()
    {
        return disposed;
    }

    public void dispose()
    {
        var protocolMgr = ProtocolLibrary.getProtocolManager();

        for (ProtocolListener listener : listeners)
        {
            try
            {
                protocolMgr.removePacketListener(listener);
            }
            catch (Throwable t)
            {
                logger.error("Error removing packet listener %s: %s".formatted(
                        listener.getIdentifier(),
                        t.getMessage()
                ));
            }
        }

        disposed = true;
    }
}
