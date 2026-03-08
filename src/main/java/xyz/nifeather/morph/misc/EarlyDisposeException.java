package xyz.nifeather.morph.misc;

/**
 * @deprecated Since 2.8.0, {@link DisguiseState} no longer throws this error, and the {@link java.util.concurrent.CompletableFuture} now always get finished on dispose
 */
@Deprecated(forRemoval = true)
public class EarlyDisposeException extends RuntimeException
{
    public EarlyDisposeException(String msg)
    {
        super(msg);
    }
}
