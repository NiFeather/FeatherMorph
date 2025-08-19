package xyz.nifeather.morph.api.networking.exceptions;

/**
 * Player logged in using an incompatible API
 */
public class ClientAPIMismatchException extends Exception
{
    public ClientAPIMismatchException(String msg)
    {
        super(msg);
    }
}
