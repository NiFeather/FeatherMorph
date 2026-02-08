package xyz.nifeather.morph.backends.server.renderer.network;

import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.VirtualEntity;

public class DisplayParameters
{
    public VirtualEntity getWatcher()
    {
        return virtualEntity;
    }

    private final VirtualEntity virtualEntity;

    public DisplayParameters(VirtualEntity watcher)
    {
        this.virtualEntity = watcher;
    }

    public static DisplayParameters fromWatcher(VirtualEntity watcher)
    {
        return new DisplayParameters(watcher);
    }
}
