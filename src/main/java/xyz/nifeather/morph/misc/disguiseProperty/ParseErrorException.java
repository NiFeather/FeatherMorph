package xyz.nifeather.morph.misc.disguiseProperty;

public class ParseErrorException extends Exception
{
    public final String propertyName;

    public ParseErrorException(String propertyName, String msg)
    {
        super(msg);
        this.propertyName = propertyName;
    }

    public ParseErrorException(String propertyName, String msg, Throwable cause)
    {
        super(msg, cause);
        this.propertyName = propertyName;
    }
}
