package xyz.nifeather.morph.misc;

public class BuildFailedException extends Exception
{
    public BuildFailedException(String msg)
    {
        super(msg);
    }

    public BuildFailedException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
