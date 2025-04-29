package xyz.nifeather.netherite.network;

public class Constants
{
    public static final int PROTOCOL_VERSION = ApiLevel.ANIMATION.protocolVersion;

    private static Boolean IsServer = null;

    public static boolean isServer()
    {
        return IsServer != null && IsServer;
    }

    public static void initialize(boolean isServer)
    {
        if (IsServer != null)
            throw new RuntimeException("Already initialized once!");

        IsServer = isServer;
    }

    public static void unInitialize()
    {
        IsServer = null;
    }

    public static void dispose()
    {
        IsServer = null;
    }

    public enum ApiLevel
    {
        LEGACY(1),

        /**
         * Deny commands before initialization is done (Both server and client side)
         */
        DENY_EARLY_COMMANDS(2),

        /**
         * S2CSwapCommand and C2SSKillCommand
         */
        SWAP_AND_SKILL(3),

        @Deprecated(forRemoval = true)
        PREINDEPENDENT_PROTOCOL(4),

        /**
         * Protocol becomes a independent libarary
         */
        INDEPENDENT_PROTOCOL(5),

        /**
         * S2CSetBoundingBox and S2CSetReach command
         */
        BOUNDINGBOX_AND_REACH(6),

        /**
         * Server <-> Client Exchange request handling
         */
        REQUEST_HANDLING(7),

        /**
         * S2CSetSpiderCommand
         *
         * @deprecated Triggers anti-cheat and doesn't consider latency
         */
        @Deprecated
        SPIDER(8),

        /**
         * Revealing State (S2CSetRevealingCommand)
         */
        REVEALING(9),

        /**
         * Admin revealing (Display player name above their disguise)
         */
        ADMIN_REVEALING(10),

        /**
         * Client renderer protocol V1
         */
        CLIENT_RENDERER_V1(11),

        /**
         * Animation command
         */
        ANIMATION(12)
        ;

        public final int protocolVersion;

        ApiLevel(int protocolVersion)
        {
            this.protocolVersion = protocolVersion;
        }
    }
}