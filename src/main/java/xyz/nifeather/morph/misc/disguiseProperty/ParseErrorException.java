package xyz.nifeather.morph.misc.disguiseProperty;

public class ParseErrorException extends Exception
{
    public ParseErrorException(String msg)
    {
        super(msg);
    }

    public ParseErrorException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}
