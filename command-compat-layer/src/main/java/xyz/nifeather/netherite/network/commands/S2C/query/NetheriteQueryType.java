package xyz.nifeather.netherite.network.commands.S2C.query;

public enum NetheriteQueryType
{
    UNKNOWN,
    ADD,
    REMOVE,
    SET;

    public static NetheriteQueryType tryValueOf(String str)
    {
        try
        {
            return NetheriteQueryType.valueOf(str);
        }
        catch (Throwable t)
        {
            return UNKNOWN;
        }
    }
}
