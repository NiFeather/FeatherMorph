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

    // Uhhh this might not be a good idea, as this exception should always trigger a panic or something?
    private boolean critical;

    public boolean critical()
    {
        return critical;
    }

    public BuildFailedException critical(boolean isCritical)
    {
        this.critical = isCritical;
        return this;
    }
}
