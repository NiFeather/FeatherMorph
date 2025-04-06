package xyz.nifeather.morph.backends.server.renderer.network;

import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;

public class DisplayParameters
{
    public SingleWatcher getWatcher()
    {
        return singleWatcher;
    }

    private final SingleWatcher singleWatcher;

    public DisplayParameters(SingleWatcher watcher)
    {
        this.singleWatcher = watcher;
    }

    public static DisplayParameters fromWatcher(SingleWatcher watcher)
    {
        return new DisplayParameters(watcher);
    }
}
