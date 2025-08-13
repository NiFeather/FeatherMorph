package xyz.nifeather.morph.api.networking.exceptions;

/**
 * Player has been rejected because of bad client behavior
 */
public class PlayerRejectedException extends RuntimeException
{
    public PlayerRejectedException(String s)
    {
        super(s);
    }
}
