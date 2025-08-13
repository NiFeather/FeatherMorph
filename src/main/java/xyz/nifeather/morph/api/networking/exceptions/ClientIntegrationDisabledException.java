package xyz.nifeather.morph.api.networking.exceptions;

/**
 * Client integration has been disabled
 */
public class ClientIntegrationDisabledException extends RuntimeException
{
    public ClientIntegrationDisabledException(String s)
    {
        super(s);
    }
}
