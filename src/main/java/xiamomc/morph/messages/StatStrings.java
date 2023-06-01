package xiamomc.morph.messages;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class StatStrings extends AbstractMorphStrings
{
    public static FormattableMessage versionString()
    {
        return getFormattable(getKey("version"), "FeatherMorph <version> by <author> (Implementing protocol v<proto>)");
    }

    public static FormattableMessage backendString()
    {
        return getFormattable(getKey("backend"), "Current Backend: <backend>");
    }

    public static FormattableMessage providersString()
    {
        return getFormattable(getKey("providers"), "Registered Disguise Providers: <count>");
    }

    public static FormattableMessage bannedDisguisesString()
    {
        return getFormattable(getKey("banned_disguises"), "Banned Disguises: <count>");
    }

    public static FormattableMessage skillsString()
    {
        return getFormattable(getKey("skills"), "Registered Skills: <count>");
    }

    public static FormattableMessage abilitiesString()
    {
        return getFormattable(getKey("abilities"), "Registered Abilities: <count>");
    }

    public static FormattableMessage activeClientsString()
    {
        return getFormattable(getKey("active_clients"), "Active client connections: <count>");
    }

    public static FormattableMessage activeDisguisesString()
    {
        return getFormattable(getKey("active_disguises"), "Active Disguises: <count>/<max>");
    }

    private static String getKey(String key)
    {
        return "stat." + key;
    }
}
