package xyz.nifeather.morph.network.server;

import xyz.nifeather.morph.FeatherMorphMain;

public class MessageChannel
{
    private static final String nameSpace = FeatherMorphMain.getMorphNameSpace();

    //region Currently using

    public static final String initializeChannelV3 = nameSpace + ":init_v3";
    public static final String commandChannelV3 = nameSpace + ":commands_v3";

    //endregion

    //region Deprecated channels

    @Deprecated
    public static final String versionChannelV2 = nameSpace + ":version_v2";

    @Deprecated
    public static final String commandChannelV2 = nameSpace + ":commands_v2";

    @Deprecated
    public static final String initializeChannelV1 = nameSpace + ":init";

    @Deprecated
    public static final String commandChannelV1 = nameSpace + ":commands";

    @Deprecated
    public static final String versionChannelV1 = nameSpace + ":version";

    @Deprecated(forRemoval = true)
    public static final String versionChannelLegacy = versionChannelV1;

    @Deprecated(forRemoval = true)
    public static final String initializeChannelLegacy = initializeChannelV1;

    @Deprecated(forRemoval = true)
    public static final String commandChannelLegacy = commandChannelV1;

    /**
     * Currently we prefer to use these channels to communicate with the client mod
     */
    public static String[] preferredChannels()
    {
        return new String[]
                {
                        initializeChannelV3,
                        commandChannelV3
                };
    }

    /**
     * All valid plugin channels, include deprecated channel
     */
    public static String[] allValidChannels()
    {
        return new String[]
                {
                        initializeChannelV3,
                        commandChannelV3,

                        versionChannelV2,
                        commandChannelV2,

                        versionChannelV1,
                        commandChannelV1,
                        initializeChannelV1,
                };
    }

    //endregion Deprecated channels
}
