package xiamomc.morph.network;

import java.util.List;
import java.util.stream.Stream;

public class ClientCommands
{
    private static final String setBase = "set";

    public static String setToggleSelfCommand(boolean val)
    {
        return setBase + " toggleself " + val;
    }

    public static String setSelfViewCommand(String identifier)
    {
        return setBase + " selfview " + identifier;
    }

    public static String setNbtCommand(String nbtString)
    {
        return setBase + " nbt " + nbtString;
    }

    public static String setProfileCommand(String nbtString)
    {
        return setBase + " profile " + nbtString;
    }

    private static final String denyBase = "deny";

    public static String denyOperationCommand(String operationName)
    {
        return denyBase + " " + operationName;
    }

    private static final String queryBase = "query";

    public static String queryAddCommand(String identifier)
    {
        return queryBase + " add " + identifier;
    }

    public static String queryRemoveCommand(String identifier)
    {
        return queryBase + " remove " + identifier;
    }

    public static String querySetCommand(List<String> identifiers)
    {
        var additBuilder = new StringBuilder(queryBase + " set ");

        for (var s : identifiers)
            additBuilder.append(s).append(" ");

        return additBuilder.toString();
    }
}
