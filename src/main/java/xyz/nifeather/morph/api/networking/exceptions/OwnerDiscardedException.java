package xyz.nifeather.morph.api.networking.exceptions;

/**
 * Owner discarded from the listening map for unknown reason
 * For more details, please check internal method {@link xyz.nifeather.morph.misc.PlayerWaitingHandler#discard(Object, Exception)}
 */
public class OwnerDiscardedException extends Exception
{
    public OwnerDiscardedException(String msg)
    {
        super(msg);
    }
}
