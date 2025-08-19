package xyz.nifeather.morph.api.networking.exceptions;

/**
 * FeatherMorph has been disabled
 */
public class PluginDisabledException extends RuntimeException
{
    public PluginDisabledException(String m)
    {
        super(m);
    }
}
