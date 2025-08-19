package xyz.nifeather.morph.api.networking.exceptions;

/**
 * Player disconnected before completing the future
 */
public class PlayerDisconnectedException extends RuntimeException
{
    public PlayerDisconnectedException(String s)
    {
        super(s);
    }
}
