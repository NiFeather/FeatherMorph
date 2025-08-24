package xyz.nifeather.morph.misc;

public class UpdateFailedException extends RuntimeException
{
    public UpdateFailedException(String msg)
    {
        super(msg);
    }

    public UpdateFailedException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
